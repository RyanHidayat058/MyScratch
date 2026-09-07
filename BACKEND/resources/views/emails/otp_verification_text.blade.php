MyScratch - Catatan Keuangan & Dokumen Pribadi

Halo, {{ $userName }}!

@if($type === 'REGISTER')
Terima kasih telah bergabung dengan MyScratch. Untuk memastikan alamat email ini aktif dan milik Anda, silakan gunakan kode verifikasi (OTP) berikut untuk menyelesaikan proses pendaftaran:
@elseif($type === 'CHANGE_PASSWORD')
Kami menerima permintaan untuk mengganti kata sandi akun MyScratch Anda. Masukkan kode verifikasi berikut untuk melanjutkan:
@elseif($type === 'CHANGE_EMAIL')
Kami menerima permintaan untuk memperbarui alamat email akun MyScratch Anda. Masukkan kode verifikasi berikut untuk mengonfirmasi email baru ini:
@elseif($type === 'RESET_PASSWORD')
Kami menerima permintaan untuk mereset kata sandi akun MyScratch Anda. Masukkan kode verifikasi berikut pada aplikasi untuk membuat kata sandi baru:
@else
Berikut adalah kode verifikasi keamanan untuk akun MyScratch Anda:
@endif

Kode Verifikasi (OTP): {{ $otpCode }}
Berlaku selama 5 menit.

Pemberitahuan Keamanan: Demi keamanan akun Anda, jangan berikan kode ini kepada pihak lain. Tim MyScratch tidak pernah meminta kode OTP Anda. Jika Anda tidak merasa melakukan permintaan ini, silakan abaikan email ini.

Salam hangat,
Tim Pengembang MyScratch
