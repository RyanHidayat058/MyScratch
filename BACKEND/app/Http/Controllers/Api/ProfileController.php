<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Mail\OtpVerificationMail;
use App\Models\OtpCode;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Mail;

class ProfileController extends Controller
{
    /**
     * Get Current Profile
     */
    public function profile(Request $request)
    {
        $user = $request->user();

        return response()->json([
            'success' => true,
            'user' => [
                'id' => $user->id,
                'name' => $user->name,
                'email' => $user->email,
                'email_verified_at' => $user->email_verified_at,
                'created_at' => $user->created_at,
            ],
        ]);
    }

    /**
     * Request OTP to Change Password
     */
    public function changePasswordRequest(Request $request)
    {
        $user = $request->user();

        $code = (string) random_int(100000, 999999);

        // Expire older change password OTPs
        OtpCode::where('email', $user->email)
            ->where('type', 'CHANGE_PASSWORD')
            ->where('is_used', false)
            ->update(['is_used' => true]);

        OtpCode::create([
            'email' => $user->email,
            'code' => $code,
            'type' => 'CHANGE_PASSWORD',
            'expires_at' => now()->addMinutes(5),
            'is_used' => false,
        ]);

        try {
            Mail::to($user->email)->send(new OtpVerificationMail($code, 'CHANGE_PASSWORD', $user->name));
        } catch (\Exception $e) {
            Log::error("Gagal kirim OTP ganti password ke {$user->email}: " . $e->getMessage());
        }

        return response()->json([
            'success' => true,
            'message' => 'Kode OTP verifikasi telah dikirim ke email Anda.',
            'email' => $user->email,
        ]);
    }

    /**
     * Verify OTP and Update Password
     */
    public function changePasswordVerify(Request $request)
    {
        $request->validate([
            'old_password' => 'required|string',
            'new_password' => 'required|string|min:6',
            'otp' => 'required|string|size:6',
        ], [
            'old_password.required' => 'Kata sandi saat ini wajib diisi.',
            'new_password.required' => 'Kata sandi baru wajib diisi.',
            'new_password.min' => 'Kata sandi baru minimal 6 karakter.',
            'otp.required' => 'Kode OTP wajib diisi.',
            'otp.size' => 'Kode OTP harus 6 digit.',
        ]);

        $user = $request->user();

        // Check old password
        if (!Hash::check($request->old_password, $user->password)) {
            return response()->json([
                'success' => false,
                'message' => 'Kata sandi saat ini tidak sesuai.',
            ], 422);
        }

        // Verify OTP
        $otp = trim($request->otp);
        $otpRecord = OtpCode::where('email', $user->email)
            ->where('code', $otp)
            ->where('type', 'CHANGE_PASSWORD')
            ->where('is_used', false)
            ->where('expires_at', '>', now())
            ->latest()
            ->first();

        if (!$otpRecord) {
            return response()->json([
                'success' => false,
                'message' => 'Kode OTP salah atau telah kedaluwarsa.',
            ], 422);
        }

        $otpRecord->update(['is_used' => true]);

        // Update password
        $user->password = Hash::make($request->new_password);
        $user->save();

        return response()->json([
            'success' => true,
            'message' => 'Kata sandi Anda berhasil diperbarui.',
        ]);
    }

    /**
     * Request OTP to Change Email
     */
    public function changeEmailRequest(Request $request)
    {
        $request->validate([
            'new_email' => 'required|email|unique:users,email',
        ], [
            'new_email.required' => 'Alamat email baru wajib diisi.',
            'new_email.email' => 'Format email baru tidak valid.',
            'new_email.unique' => 'Alamat email tersebut sudah digunakan akun lain.',
        ]);

        $user = $request->user();
        $newEmail = strtolower(trim($request->new_email));

        $code = (string) random_int(100000, 999999);

        // Expire older change email OTPs
        OtpCode::where('email', $newEmail)
            ->where('type', 'CHANGE_EMAIL')
            ->where('is_used', false)
            ->update(['is_used' => true]);

        OtpCode::create([
            'email' => $newEmail,
            'code' => $code,
            'type' => 'CHANGE_EMAIL',
            'payload' => [
                'user_id' => $user->id,
                'new_email' => $newEmail,
            ],
            'expires_at' => now()->addMinutes(5),
            'is_used' => false,
        ]);

        try {
            Mail::to($newEmail)->send(new OtpVerificationMail($code, 'CHANGE_EMAIL', $user->name));
        } catch (\Exception $e) {
            Log::error("Gagal kirim OTP ganti email ke {$newEmail}: " . $e->getMessage());
        }

        return response()->json([
            'success' => true,
            'message' => 'Kode verifikasi telah dikirimkan ke alamat email baru Anda.',
            'new_email' => $newEmail,
        ]);
    }

    /**
     * Verify OTP and Update Email
     */
    public function changeEmailVerify(Request $request)
    {
        $request->validate([
            'new_email' => 'required|email',
            'otp' => 'required|string|size:6',
        ], [
            'new_email.required' => 'Email baru wajib diisi.',
            'otp.required' => 'Kode OTP wajib diisi.',
            'otp.size' => 'Kode OTP harus 6 digit.',
        ]);

        $user = $request->user();
        $newEmail = strtolower(trim($request->new_email));
        $otp = trim($request->otp);

        $otpRecord = OtpCode::where('email', $newEmail)
            ->where('code', $otp)
            ->where('type', 'CHANGE_EMAIL')
            ->where('is_used', false)
            ->where('expires_at', '>', now())
            ->latest()
            ->first();

        if (!$otpRecord || ($otpRecord->payload['user_id'] ?? null) !== $user->id) {
            return response()->json([
                'success' => false,
                'message' => 'Kode OTP salah atau telah kedaluwarsa.',
            ], 422);
        }

        $otpRecord->update(['is_used' => true]);

        // Update email
        $user->email = $newEmail;
        $user->email_verified_at = now();
        $user->save();

        return response()->json([
            'success' => true,
            'message' => 'Alamat email Anda berhasil diperbarui.',
            'user' => [
                'id' => $user->id,
                'name' => $user->name,
                'email' => $user->email,
            ],
        ]);
    }

    /**
     * Permanently Delete Account and Data
     */
    public function deleteAccount(Request $request)
    {
        $user = $request->user();

        // Delete all transactions, folders, notes
        $user->transactions()->delete();
        $user->notes()->delete();
        $user->folders()->delete();

        // Delete all tokens
        $user->tokens()->delete();

        // Delete user
        $user->delete();

        return response()->json([
            'success' => true,
            'message' => 'Akun dan seluruh data Anda telah berhasil dihapus secara permanen.',
        ]);
    }
}
