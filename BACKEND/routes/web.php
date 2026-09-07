<?php

use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    return response()->json([
        'app' => 'MyScratch API',
        'status' => 'online',
        'version' => '1.0.0',
    ]);
});
