<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kode Verifikasi MyScratch</title>
    <style>
        body {
            margin: 0;
            padding: 0;
            background-color: #F8FAFC;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
            color: #0F172A;
        }
        .wrapper {
            width: 100%;
            table-layout: fixed;
            background-color: #F8FAFC;
            padding: 40px 0;
        }
        .container {
            max-width: 520px;
            margin: 0 auto;
            background-color: #FFFFFF;
            border-radius: 16px;
            border: 1px solid #E2E8F0;
            overflow: hidden;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
        }
        .header {
            background-color: #0B0D11;
            padding: 28px;
            text-align: center;
        }
        .logo-text {
            color: #10B981;
            font-size: 24px;
            font-weight: 800;
            letter-spacing: 1px;
            margin: 0;
            display: inline-block;
        }
        .tagline {
            color: #9CA3AF;
            font-size: 12px;
            margin-top: 4px;
            margin-bottom: 0;
        }
        .body-content {
            padding: 32px 28px;
        }
        h2 {
            font-size: 20px;
            font-weight: 700;
            color: #0F172A;
            margin-top: 0;
            margin-bottom: 12px;
        }
        p {
            font-size: 14px;
            line-height: 1.6;
            color: #334155;
            margin-top: 0;
            margin-bottom: 18px;
        }
        .otp-card {
            background-color: #F1FDF8;
            border: 1.5px dashed #10B981;
            border-radius: 12px;
            padding: 20px;
            text-align: center;
            margin: 26px 0;
        }
        .otp-label {
            font-size: 11px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 1px;
            color: #059669;
            margin-bottom: 8px;
        }
        .otp-number {
            font-size: 36px;
            font-weight: 800;
            letter-spacing: 8px;
            color: #047857;
            margin: 0;
            font-family: 'Courier New', Courier, monospace;
        }
        .otp-expiration {
            font-size: 12px;
            color: #64748B;
            margin-top: 8px;
            margin-bottom: 0;
        }
        .warning-box {
            background-color: #FFFBEB;
            border-left: 4px solid #F59E0B;
            padding: 12px 16px;
            border-radius: 6px;
            margin-bottom: 24px;
        }
        .warning-box p {
            color: #92400E;
            font-size: 12px;
            margin: 0;
            line-height: 1.5;
        }
        .footer {
            border-top: 1px solid #E2E8F0;
            padding: 20px 28px;
            text-align: center;
            background-color: #F8FAFC;
        }
        .footer p {
            font-size: 12px;
            color: #94A3B8;
            margin: 0;
        }
    </style>
</head>
<body>
    <div class="wrapper">
        <div class="container">
            <!-- Header -->
            <div class="header">
                <div style="margin-bottom: 12px;">
                    <img src="https://raw.githubusercontent.com/RyanHidayat058/MyScratch/main/logo.png" alt="MyScratch Logo" width="60" height="60" style="display: inline-block; vertical-align: middle; border: 0;" />
                </div>
                <div class="logo-text">MyScratch</div>
                <p class="tagline">Catatan Keuangan &amp; Dokumen Pribadi</p>
            </div>

            <!-- Body -->
            <div class="body-content">
                <h2>Halo, {{ $userName }}!</h2>

                @if($type === 'REGISTER')
                    <p>Terima kasih telah bergabung dengan <strong>MyScratch</strong>. Untuk memastikan alamat email ini aktif dan milik Anda, silakan gunakan kode verifikasi (OTP) berikut untuk menyelesaikan proses pendaftaran:</p>
                @elseif($type === 'CHANGE_PASSWORD')
                    <p>Kami menerima permintaan untuk mengganti kata sandi akun <strong>MyScratch</strong> Anda. Masukkan kode verifikasi berikut untuk melanjutkan:</p>
                @elseif($type === 'CHANGE_EMAIL')
                    <p>Kami menerima permintaan untuk memperbarui alamat email akun <strong>MyScratch</strong> Anda. Masukkan kode verifikasi berikut untuk mengonfirmasi email baru ini:</p>
                @elseif($type === 'RESET_PASSWORD')
                    <p>Kami menerima permintaan untuk mereset kata sandi akun <strong>MyScratch</strong> Anda. Masukkan kode verifikasi berikut pada aplikasi untuk membuat kata sandi baru:</p>
                @else
                    <p>Berikut adalah kode verifikasi keamanan untuk akun <strong>MyScratch</strong> Anda:</p>
                @endif

                <!-- OTP Code Display -->
                <div class="otp-card">
                    <div class="otp-label">Kode Verifikasi (OTP)</div>
                    <div class="otp-number">{{ $otpCode }}</div>
                    <p class="otp-expiration">Berlaku selama <strong>5 menit</strong></p>
                </div>

                <!-- Security Advisory -->
                <div class="warning-box">
                    <p><strong>Pemberitahuan Keamanan:</strong> Demi keamanan akun Anda, jangan berikan kode ini kepada pihak lain. Tim MyScratch tidak pernah meminta kode OTP Anda. Jika Anda tidak merasa melakukan permintaan ini, silakan abaikan email ini.</p>
                </div>

                <p style="font-size: 13px; color: #64748B; margin-bottom: 0;">
                    Salam hangat,<br>
                    <strong>Tim Pengembang MyScratch</strong>
                </p>
            </div>

            <!-- Footer -->
            <div class="footer">
                <p>&copy; {{ date('Y') }} MyScratch. Hak cipta dilindungi undang-undang.</p>
            </div>
        </div>
    </div>
</body>
</html>
