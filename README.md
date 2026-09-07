# MyScratch - Personal Finance & Secure Notes Application

A modern, responsive, and privacy-focused Android application for personal finance management and encrypted personal notes, powered by a self-hosted Laravel 11 REST API with MySQL.

![MyScratch Logo](https://raw.githubusercontent.com/RyanHidayat058/MyScratch/main/FRONTEND/logo.png)

## Architecture Overview

This project is structured as a monorepo containing both the Android client and the backend API service:

```
MyScratch/
├── FRONTEND/   # Android Application (Kotlin, Jetpack Compose, Room, Retrofit)
└── BACKEND/    # REST API & Database (Laravel 11, Sanctum, MySQL, AES-256 Encryption)
```

### 1. FRONTEND (`FRONTEND/`)
* **Framework**: Android Jetpack Compose (Material3)
* **Architecture**: MVVM with Repository Pattern, Clean Architecture, Kotlin Coroutines & Flow
* **Networking**: Retrofit 2 with OkHttp3 logging and custom Auth Bearer Interceptor
* **Local Caching**: Room Database for seamless offline-first experience
* **Features**:
  * Grid Dashboard: Keuangan & Catatan Pribadi
  * Interactive Donut Charts with percentages and category legends
  * Transaction Management: Income, Expense, Debt Payable, Debt Receivable with custom date pickers
  * Popup Calculator with insert-to-amount integration
  * Rich Note Editor with Folders, Pinning, and search
  * Adaptive Dark & Light theme with Obsidian & Emerald brand colors
  * Full Profile Management: Name, Email, Change Password (with OTP), Change Email (with OTP), Delete Account, and Logout

### 2. BACKEND (`BACKEND/`)
* **Framework**: Laravel 11 with Laravel Sanctum API authentication
* **Database**: MySQL 8.0 with automated migrations
* **Data Security & Privacy**: AES-256-CBC database-level attribute encryption for all sensitive user contents (titles, notes, folder names)
* **Email System**: Custom branded transactional email notification with 6-digit OTP verification via SMTP
* **API Endpoints**:
  * `/api/register-request`, `/api/verify-register-otp`, `/api/resend-register-otp`
  * `/api/login`, `/api/logout`
  * `/api/profile`, `/api/profile/change-password/*`, `/api/profile/change-email/*`, `/api/profile/delete-account`
  * `/api/finance/transactions`, `/api/finance/summary`
  * `/api/notes/folders`, `/api/notes`

---

## Authors & License
Developed by Ryan Hidayat. All rights reserved.
