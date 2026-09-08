<?php

use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    return response()->json([
        'app' => 'MyScratch API',
        'status' => 'online',
        'version' => '1.0.0',
    ]);
});

// Endpoint unduh APK langsung dari server/web hosting
Route::get('/download/app', function () {
    $apkPath = public_path('downloads/myscratch-latest.apk');
    if (file_exists($apkPath)) {
        return response()->download($apkPath, 'MyScratch.apk', [
            'Content-Type' => 'application/vnd.android.package-archive',
        ]);
    }
    return response()->json([
        'error' => 'File APK belum tersedia di server.',
    ], 404);
})->name('app.download');

