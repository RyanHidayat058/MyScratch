<?php

namespace App\Mail;

use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Attachment;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;
use Illuminate\Queue\SerializesModels;

class OtpVerificationMail extends Mailable
{
    use Queueable, SerializesModels;

    public string $otpCode;
    public string $type;
    public string $userName;

    /**
     * Create a new message instance.
     */
    public function __construct(string $otpCode, string $type = 'REGISTER', string $userName = '')
    {
        $this->otpCode = $otpCode;
        $this->type = $type;
        $this->userName = $userName ?: 'Pengguna';
    }

    /**
     * Get the message envelope.
     */
    public function envelope(): Envelope
    {
        $subject = match ($this->type) {
            'REGISTER' => 'Verifikasi Akun MyScratch Anda',
            'CHANGE_PASSWORD' => 'Kode Keamanan Ganti Kata Sandi - MyScratch',
            'CHANGE_EMAIL' => 'Kode Verifikasi Pembaruan Email - MyScratch',
            'RESET_PASSWORD' => 'Kode Pemulihan Kata Sandi - MyScratch',
            default => 'Kode Verifikasi MyScratch'
        };

        return new Envelope(
            subject: $subject,
        );
    }

    /**
     * Get the message content definition.
     */
    public function content(): Content
    {
        return new Content(
            view: 'emails.otp_verification',
            text: 'emails.otp_verification_text',
        );
    }

    /**
     * Get the attachments for the message.
     *
     * @return array<int, Attachment>
     */
    public function attachments(): array
    {
        return [];
    }
}
