<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Mail\OtpVerificationMail;
use App\Models\OtpCode;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Mail;
use Illuminate\Validation\ValidationException;

class AuthController extends Controller
{
    /**
     * Step 1 Register: Validate data, generate OTP, send email.
     */
    public function registerRequest(Request $request)
    {
        $request->validate([
            'name' => 'required|string|max:100',
            'email' => 'required|email|max:150',
            'password' => 'required|string|min:6',
        ], [
            'name.required' => 'Nama lengkap wajib diisi.',
            'email.required' => 'Alamat email wajib diisi.',
            'email.email' => 'Format email tidak valid.',
            'password.required' => 'Kata sandi wajib diisi.',
            'password.min' => 'Kata sandi minimal 6 karakter.',
        ]);

        $email = strtolower(trim($request->email));

        // Check if verified user exists
        $existingUser = User::where('email', $email)->whereNotNull('email_verified_at')->first();
        if ($existingUser) {
            return response()->json([
                'success' => false,
                'message' => 'Alamat email sudah terdaftar. Silakan masuk menggunakan akun Anda.',
            ], 422);
        }

        // Generate 6-digit OTP
        $code = (string) random_int(100000, 999999);

        // Expire older unused registration OTPs for this email
        OtpCode::where('email', $email)
            ->where('type', 'REGISTER')
            ->where('is_used', false)
            ->update(['is_used' => true]);

        // Save OTP
        OtpCode::create([
            'email' => $email,
            'code' => $code,
            'type' => 'REGISTER',
            'payload' => [
                'name' => trim($request->name),
                'password' => Hash::make($request->password),
            ],
            'expires_at' => now()->addMinutes(5),
            'is_used' => false,
        ]);

        // Send Email
        try {
            Mail::to($email)->send(new OtpVerificationMail($code, 'REGISTER', trim($request->name)));
        } catch (\Exception $e) {
            Log::error("Gagal mengirim email OTP ke {$email}: " . $e->getMessage());
        }

        return response()->json([
            'success' => true,
            'message' => 'Kode verifikasi telah dikirimkan ke email Anda. Silakan periksa kotak masuk atau spam.',
            'email' => $email,
        ]);
    }

    /**
     * Step 2 Register: Verify OTP, create/activate user, return token.
     */
    public function verifyRegisterOtp(Request $request)
    {
        $request->validate([
            'email' => 'required|email',
            'otp' => 'required|string|size:6',
        ], [
            'email.required' => 'Email wajib diisi.',
            'otp.required' => 'Kode OTP wajib diisi.',
            'otp.size' => 'Kode OTP harus berjumlah 6 digit.',
        ]);

        $email = strtolower(trim($request->email));
        $otp = trim($request->otp);

        $otpRecord = OtpCode::where('email', $email)
            ->where('code', $otp)
            ->where('type', 'REGISTER')
            ->where('is_used', false)
            ->where('expires_at', '>', now())
            ->latest()
            ->first();

        if (!$otpRecord) {
            return response()->json([
                'success' => false,
                'message' => 'Kode verifikasi salah atau telah kedaluwarsa. Silakan minta kode baru.',
            ], 422);
        }

        // Mark OTP as used
        $otpRecord->update(['is_used' => true]);

        $payload = $otpRecord->payload;

        // Create or update user
        $user = User::updateOrCreate(
            ['email' => $email],
            [
                'name' => $payload['name'] ?? 'Pengguna MyScratch',
                'password' => $payload['password'],
                'email_verified_at' => now(),
            ]
        );

        // Generate Sanctum token
        $token = $user->createToken('myscratch-mobile')->plainTextToken;

        return response()->json([
            'success' => true,
            'message' => 'Akun berhasil diverifikasi dan didaftarkan.',
            'token' => $token,
            'user' => [
                'id' => $user->id,
                'name' => $user->name,
                'email' => $user->email,
                'email_verified_at' => $user->email_verified_at,
            ],
        ]);
    }

    /**
     * Resend OTP for Register
     */
    public function resendRegisterOtp(Request $request)
    {
        $request->validate(['email' => 'required|email']);
        $email = strtolower(trim($request->email));

        $lastOtp = OtpCode::where('email', $email)
            ->where('type', 'REGISTER')
            ->latest()
            ->first();

        if (!$lastOtp || empty($lastOtp->payload)) {
            return response()->json([
                'success' => false,
                'message' => 'Tidak ada pendaftaran yang sedang berlangsung untuk email ini.',
            ], 404);
        }

        $code = (string) random_int(100000, 999999);

        // Invalidate previous OTPs
        OtpCode::where('email', $email)
            ->where('type', 'REGISTER')
            ->update(['is_used' => true]);

        OtpCode::create([
            'email' => $email,
            'code' => $code,
            'type' => 'REGISTER',
            'payload' => $lastOtp->payload,
            'expires_at' => now()->addMinutes(5),
            'is_used' => false,
        ]);

        try {
            Mail::to($email)->send(new OtpVerificationMail($code, 'REGISTER', $lastOtp->payload['name'] ?? ''));
        } catch (\Exception $e) {
            Log::error("Gagal mengirim ulang email OTP: " . $e->getMessage());
        }

        return response()->json([
            'success' => true,
            'message' => 'Kode verifikasi baru telah dikirimkan ke email Anda.',
        ]);
    }

    /**
     * Login with Email and Password
     */
    public function login(Request $request)
    {
        $request->validate([
            'email' => 'required|email',
            'password' => 'required|string',
        ], [
            'email.required' => 'Email wajib diisi.',
            'password.required' => 'Kata sandi wajib diisi.',
        ]);

        $email = strtolower(trim($request->email));
        $user = User::where('email', $email)->first();

        if (!$user || !Hash::check($request->password, $user->password)) {
            return response()->json([
                'success' => false,
                'message' => 'Email atau kata sandi tidak sesuai.',
            ], 401);
        }

        if (!$user->email_verified_at) {
            return response()->json([
                'success' => false,
                'message' => 'Alamat email belum diverifikasi. Silakan selesaikan verifikasi OTP terlebih dahulu.',
                'email_not_verified' => true,
                'email' => $email,
            ], 403);
        }

        $token = $user->createToken('myscratch-mobile')->plainTextToken;

        return response()->json([
            'success' => true,
            'message' => 'Berhasil masuk ke akun.',
            'token' => $token,
            'user' => [
                'id' => $user->id,
                'name' => $user->name,
                'email' => $user->email,
                'email_verified_at' => $user->email_verified_at,
            ],
        ]);
    }

    /**
     * Logout
     */
    public function logout(Request $request)
    {
        $request->user()->currentAccessToken()->delete();

        return response()->json([
            'success' => true,
            'message' => 'Berhasil keluar akun.',
        ]);
    }
}
