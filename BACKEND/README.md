# MyScratch - Backend RESTful API Service

<p align="center">
  <img src="../logo.png" alt="MyScratch Logo" width="96" height="96" style="border-radius: 18px;" />
</p>

<p align="center">
  <strong>Secure, High-Performance Laravel 11 Microservice with Database-Level AES-256 Encryption</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Laravel-11.x-FF2D20.svg?style=flat-square&logo=laravel" alt="Laravel 11" />
  <img src="https://img.shields.io/badge/PHP-8.3+-777BB4.svg?style=flat-square&logo=php" alt="PHP 8.3" />
  <img src="https://img.shields.io/badge/Auth-Sanctum-red.svg?style=flat-square" alt="Sanctum" />
  <img src="https://img.shields.io/badge/Security-AES--256--CBC-059669.svg?style=flat-square" alt="AES-256" />
  <img src="https://img.shields.io/badge/Serverless-Vercel-black.svg?style=flat-square&logo=vercel" alt="Vercel" />
</p>

---

## Overview

The MyScratch Backend is a privacy-first RESTful API designed to serve the MyScratch Android client. It handles authentication, data synchronization, cryptographic hashing, and automated transactional OTP delivery.

### Core Architectural Pillars
1. **Zero-Trust Attribute Encryption**: Highly sensitive fields (`notes.title`, `notes.content`, `folders.name`, `transactions.title`, `transactions.note`) are encrypted at rest using Laravel's native AES-256-CBC encryption casts before storing to disk.
2. **Stateless Token Authentication**: Authentication is handled via Laravel Sanctum, issuing lightweight, revokable SHA-256 hashed bearer tokens.
3. **Automated OTP Engine**: 6-digit one-time passwords for registration, password recovery, and email transfers with strict 5-minute expiration windows.
4. **Cloud and Serverless Ready**: Configured for instant deployment via Docker container or Vercel serverless functions (`vercel-php`).

---

## Tech Stack & Requirements

- **Framework**: Laravel 11.x
- **Runtime**: PHP 8.3+
- **Extensions**: `pdo_mysql`, `openssl`, `mbstring`, `tokenizer`, `xml`, `ctype`, `json`, `bcmath`, `curl`
- **Authentication**: Laravel Sanctum
- **Database**: MySQL 8.0+ or SQLite 3.35+
- **Mailing**: SMTP (TLS/SSL) with custom responsive HTML mailable

---

## Local Development Setup

### 1. Install Dependencies
```bash
composer install --optimize-autoloader
```

### 2. Configure Environment
```bash
cp .env.example .env
php artisan key:generate
```

### 3. Database Configuration
Update `.env` with your database credentials:
```env
DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=myscratch
DB_USERNAME=root
DB_PASSWORD=your_password
```

### 4. SMTP Mail Configuration (For OTP)
Configure your mail gateway to receive real OTP verification emails:
```env
MAIL_MAILER=smtp
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
MAIL_ENCRYPTION=tls
MAIL_FROM_ADDRESS="no-reply@myscratch.app"
MAIL_FROM_NAME="MyScratch Security"
```
> Note: For local testing without an SMTP server, you can set `MAIL_MAILER=log` to inspect generated OTP codes in `storage/logs/laravel.log`.

### 5. Run Migrations
```bash
php artisan migrate
```

### 6. Serve Application
```bash
php artisan serve --port=8000
```
API endpoints are exposed under `http://localhost:8000/api/`.

---

## API Endpoint Directory

### Authentication & Account Security (`/api/auth`)
- `POST /api/auth/register-request` — Request email verification code for new accounts.
- `POST /api/auth/verify-register-otp` — Verify 6-digit OTP and complete account creation.
- `POST /api/auth/resend-register-otp` — Regenerate and resend expired or missing OTP.
- `POST /api/auth/login` — Authenticate and receive Sanctum bearer token.
- `POST /api/auth/logout` — Revoke caller's active bearer token (Auth Required).

### Profile & Identity Operations (`/api/user`)
- `GET /api/user/profile` — Fetch current user details (Auth Required).
- `POST /api/user/change-password-request` — Send password reset OTP (Auth Required).
- `POST /api/user/change-password-verify` — Verify OTP and commit new password (Auth Required).
- `POST /api/user/change-email-request` — Send migration OTP to new email (Auth Required).
- `POST /api/user/change-email-verify` — Verify OTP and commit new email (Auth Required).
- `DELETE /api/user/delete-account` — GDPR permanent erasure of user and associated data (Auth Required).

### Personal Finance Management (`/api/finance`)
- `GET /api/finance/summary` — Aggregate metrics: Total Balance, Net Worth, Income/Expense, Chart Data (Auth Required).
- `GET /api/finance/transactions` — Query transactions with optional filters (`type`, `category`, `search`) (Auth Required).
- `POST /api/finance/transactions` — Store transaction (title & note encrypted with AES-256) (Auth Required).
- `PUT /api/finance/transactions/{id}` — Update existing transaction (Auth Required).
- `DELETE /api/finance/transactions/{id}` — Delete transaction record (Auth Required).

### Encrypted Notes & Folders (`/api/notes`)
- `GET /api/notes/folders` — List folders with real-time note counts (Auth Required).
- `POST /api/notes/folders` — Create folder with custom color HEX (Auth Required).
- `DELETE /api/notes/folders/{id}` — Delete folder (Auth Required).
- `GET /api/notes` — List notes with search/folder filtering (Auth Required).
- `POST /api/notes` — Store note (title & content encrypted with AES-256) (Auth Required).
- `PUT /api/notes/{id}` — Update note (Auth Required).
- `DELETE /api/notes/{id}` — Delete note (Auth Required).

---

## Docker Deployment

A production-ready Dockerfile is included:

```bash
# Build the container
docker build -t myscratch-backend .

# Run container with SQLite fallback
docker run -d \
  -p 8000:8000 \
  --name myscratch-api \
  -e APP_KEY=base64:YOUR_GENERATED_APP_KEY \
  -e DB_CONNECTION=sqlite \
  myscratch-backend
```

---

## Vercel Serverless Deployment

This repository includes `vercel.json` and `/api/index.php` for seamless deployment to Vercel:

1. Install Vercel CLI: `npm i -g vercel`
2. Deploy:
   ```bash
   cd BACKEND
   vercel --prod
   ```
3. Add environment secrets via the Vercel Dashboard.

---

## License

Developed by Ryan Hidayat. Distributed under the **MIT License**.
