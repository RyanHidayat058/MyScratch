<?php

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\FinanceController;
use App\Http\Controllers\Api\NotesController;
use App\Http\Controllers\Api\ProfileController;
use App\Http\Controllers\Api\VaultController;
use Illuminate\Support\Facades\Route;

// Public Auth & OTP Routes
Route::prefix('auth')->group(function () {
    Route::post('/register-request', [AuthController::class, 'registerRequest']);
    Route::post('/verify-register-otp', [AuthController::class, 'verifyRegisterOtp']);
    Route::post('/resend-register-otp', [AuthController::class, 'resendRegisterOtp']);
    Route::post('/login', [AuthController::class, 'login']);
});

// Public App Version & Update Checker
Route::get('/app/check-update', function () {
    $rawDownloadUrl = env('APP_DOWNLOAD_URL', url('/download/app'));

    // Otomatis ubah link sharing Google Drive menjadi link Direct Download
    // Contoh: https://drive.google.com/file/d/XYZ/view?usp=sharing -> https://drive.google.com/uc?export=download&id=XYZ
    $downloadUrl = $rawDownloadUrl;
    if (preg_match('/drive\.google\.com\/file\/d\/([a-zA-Z0-9_-]+)/', $rawDownloadUrl, $matches)) {
        $downloadUrl = 'https://drive.google.com/uc?export=download&id=' . $matches[1];
    } elseif (preg_match('/drive\.google\.com\/open\?id=([a-zA-Z0-9_-]+)/', $rawDownloadUrl, $matches)) {
        $downloadUrl = 'https://drive.google.com/uc?export=download&id=' . $matches[1];
    }

    return response()->json([
        'success' => true,
        'latest_version_code' => (int) env('APP_LATEST_VERSION_CODE', 2),
        'latest_version_name' => env('APP_LATEST_VERSION_NAME', '1.1.0'),
        'download_url' => $downloadUrl,
        'changelog' => env('APP_CHANGELOG', "- Modul baru Brankas Rahasia (AES-256)\n- Kunci Biometrik sidik jari per catatan\n- Kotak Sampah (Recycle Bin / Soft Delete)\n- Checklist / To-Do list interaktif"),
        'is_force_update' => (bool) env('APP_FORCE_UPDATE', false),
    ]);
});

// Protected Routes (Sanctum)
Route::middleware('auth:sanctum')->group(function () {
    // Auth & Profile
    Route::post('/auth/logout', [AuthController::class, 'logout']);
    Route::get('/user/profile', [ProfileController::class, 'profile']);
    Route::post('/user/change-password-request', [ProfileController::class, 'changePasswordRequest']);
    Route::post('/user/change-password-verify', [ProfileController::class, 'changePasswordVerify']);
    Route::post('/user/change-email-request', [ProfileController::class, 'changeEmailRequest']);
    Route::post('/user/change-email-verify', [ProfileController::class, 'changeEmailVerify']);
    Route::delete('/user/delete-account', [ProfileController::class, 'deleteAccount']);

    // Finance
    Route::get('/finance/summary', [FinanceController::class, 'summary']);
    Route::get('/finance/transactions', [FinanceController::class, 'index']);
    Route::post('/finance/transactions', [FinanceController::class, 'store']);
    Route::put('/finance/transactions/{id}', [FinanceController::class, 'update']);
    Route::delete('/finance/transactions/{id}', [FinanceController::class, 'destroy']);

    // Notes & Folders
    Route::get('/notes/folders', [NotesController::class, 'folders']);
    Route::post('/notes/folders', [NotesController::class, 'storeFolder']);
    Route::delete('/notes/folders/{id}', [NotesController::class, 'deleteFolder']);

    Route::get('/notes', [NotesController::class, 'notes']);
    Route::get('/notes/trash', [NotesController::class, 'trash']);
    Route::delete('/notes/trash/empty', [NotesController::class, 'emptyTrash']);
    Route::post('/notes/{id}/restore', [NotesController::class, 'restoreNote']);
    Route::delete('/notes/{id}/force-delete', [NotesController::class, 'forceDelete']);
    Route::post('/notes', [NotesController::class, 'storeNote']);
    Route::put('/notes/{id}', [NotesController::class, 'updateNote']);
    Route::delete('/notes/{id}', [NotesController::class, 'deleteNote']);

    // Secure Vault (Brankas Rahasia)
    Route::get('/vault/items', [VaultController::class, 'index']);
    Route::post('/vault/items', [VaultController::class, 'store']);
    Route::put('/vault/items/{id}', [VaultController::class, 'update']);
    Route::delete('/vault/items/{id}', [VaultController::class, 'destroy']);
});
