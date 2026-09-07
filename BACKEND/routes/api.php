<?php

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\FinanceController;
use App\Http\Controllers\Api\NotesController;
use App\Http\Controllers\Api\ProfileController;
use Illuminate\Support\Facades\Route;

// Public Auth & OTP Routes
Route::prefix('auth')->group(function () {
    Route::post('/register-request', [AuthController::class, 'registerRequest']);
    Route::post('/verify-register-otp', [AuthController::class, 'verifyRegisterOtp']);
    Route::post('/resend-register-otp', [AuthController::class, 'resendRegisterOtp']);
    Route::post('/login', [AuthController::class, 'login']);
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
    Route::post('/notes', [NotesController::class, 'storeNote']);
    Route::put('/notes/{id}', [NotesController::class, 'updateNote']);
    Route::delete('/notes/{id}', [NotesController::class, 'deleteNote']);
});
