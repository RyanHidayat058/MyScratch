<?php
ini_set('display_errors', '1');
error_reporting(E_ALL);

try {
    $storageDirs = [
        '/tmp/cache',
        '/tmp/storage',
        '/tmp/storage/app',
        '/tmp/storage/app/public',
        '/tmp/storage/framework',
        '/tmp/storage/framework/cache',
        '/tmp/storage/framework/cache/data',
        '/tmp/storage/framework/sessions',
        '/tmp/storage/framework/views',
        '/tmp/storage/logs',
    ];

    foreach ($storageDirs as $dir) {
        if (!is_dir($dir)) {
            @mkdir($dir, 0755, true);
        }
    }

    putenv('APP_SERVICES_CACHE=/tmp/cache/services.php');
    putenv('APP_PACKAGES_CACHE=/tmp/cache/packages.php');
    putenv('APP_CONFIG_CACHE=/tmp/cache/config.php');
    putenv('APP_ROUTES_CACHE=/tmp/cache/routes.php');
    putenv('APP_EVENTS_CACHE=/tmp/cache/events.php');
    $_ENV['APP_SERVICES_CACHE'] = '/tmp/cache/services.php';
    $_ENV['APP_PACKAGES_CACHE'] = '/tmp/cache/packages.php';
    $_ENV['APP_CONFIG_CACHE'] = '/tmp/cache/config.php';
    $_ENV['APP_ROUTES_CACHE'] = '/tmp/cache/routes.php';
    $_ENV['APP_EVENTS_CACHE'] = '/tmp/cache/events.php';
    $_SERVER['APP_SERVICES_CACHE'] = '/tmp/cache/services.php';
    $_SERVER['APP_PACKAGES_CACHE'] = '/tmp/cache/packages.php';
    $_SERVER['APP_CONFIG_CACHE'] = '/tmp/cache/config.php';
    $_SERVER['APP_ROUTES_CACHE'] = '/tmp/cache/routes.php';
    $_SERVER['APP_EVENTS_CACHE'] = '/tmp/cache/events.php';

    $defaults = [
        'APP_NAME' => 'MyScratch',
        'APP_ENV' => 'production',
        'APP_KEY' => 'base64:6qXB0PBllzhVNjsIIzYel/7owyox4s1xPIDlmL0NS/E=',
        'APP_DEBUG' => 'false',
        'APP_URL' => 'https://myscratch-prod.vercel.app',
        'DB_CONNECTION' => 'mysql',
        'DB_HOST' => 'gateway01.ap-southeast-1.prod.aws.tidbcloud.com',
        'DB_PORT' => '4000',
        'DB_DATABASE' => 'myscratch',
        'DB_USERNAME' => '4YBkgPD3aKQbuV8.root',
        'DB_PASSWORD' => 'VhWMfBENLWDkazo9',
        'MYSQL_ATTR_SSL_CA' => '/etc/pki/tls/certs/ca-bundle.crt',
        'MAIL_MAILER' => 'smtp',
        'MAIL_HOST' => 'smtp.gmail.com',
        'MAIL_PORT' => '465',
        'MAIL_USERNAME' => 'myscratchid@gmail.com',
        'MAIL_PASSWORD' => 'gnhjflzxcfcvqztk',
        'MAIL_ENCRYPTION' => 'ssl',
        'MAIL_FROM_ADDRESS' => 'myscratchid@gmail.com',
        'MAIL_FROM_NAME' => 'MyScratch',
        'SESSION_DRIVER' => 'array',
        'CACHE_STORE' => 'array',
    ];

    foreach ($defaults as $key => $val) {
        if (!getenv($key) && !isset($_ENV[$key]) && !isset($_SERVER[$key])) {
            putenv("{$key}={$val}");
            $_ENV[$key] = $val;
            $_SERVER[$key] = $val;
        }
    }

    define('LARAVEL_START', microtime(true));
    require __DIR__.'/../vendor/autoload.php';
    $app = require_once __DIR__.'/../bootstrap/app.php';
    $app->register(\Illuminate\Filesystem\FilesystemServiceProvider::class);
    $app->register(\Illuminate\View\ViewServiceProvider::class);

    $request = \Illuminate\Http\Request::capture();
    $response = $app->handle($request);

    if ($response->getStatusCode() === 500 && isset($response->exception)) {
        header('Content-Type: application/json');
        echo json_encode([
            'status' => 500,
            'exception' => $response->exception->getMessage(),
            'class' => get_class($response->exception),
            'file' => $response->exception->getFile(),
            'line' => $response->exception->getLine(),
        ], JSON_PRETTY_PRINT);
        exit;
    }

    $response->send();
    $app->terminate();
} catch (\Throwable $e) {
    http_response_code(500);
    header('Content-Type: application/json');
    echo json_encode([
        'error' => $e->getMessage(),
        'file' => $e->getFile(),
        'line' => $e->getLine(),
        'trace' => explode("\n", $e->getTraceAsString())
    ], JSON_PRETTY_PRINT);
}