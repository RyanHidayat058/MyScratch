<?php

use Illuminate\Foundation\Application;
use Illuminate\Foundation\Configuration\Exceptions;
use Illuminate\Foundation\Configuration\Middleware;
use Illuminate\Http\Request;

return Application::configure(basePath: dirname(__DIR__))
    ->withRouting(
        web: __DIR__.'/../routes/web.php',
        api: __DIR__.'/../routes/api.php',
        commands: __DIR__.'/../routes/console.php',
        health: '/up',
        apiPrefix: '',
    )
    ->withProviders([
        \Illuminate\Filesystem\FilesystemServiceProvider::class,
        \Illuminate\View\ViewServiceProvider::class,
    ])
    ->withMiddleware(function (Middleware $middleware): void {
        //
    })
    ->withExceptions(function (Exceptions $exceptions): void {
        $exceptions->shouldRenderJsonWhen(
            fn () => true,
        );
        $exceptions->respond(function (\Symfony\Component\HttpFoundation\Response $response, \Throwable $e, Request $request) {
            return response()->json([
                'error' => $e->getMessage(),
                'class' => get_class($e),
                'file' => $e->getFile(),
                'line' => $e->getLine(),
            ], 500);
        });
    })->create()
    ->useStoragePath(isset($_ENV['VERCEL']) || isset($_SERVER['VERCEL']) ? '/tmp/storage' : dirname(__DIR__).'/storage');
