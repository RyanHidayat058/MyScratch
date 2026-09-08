<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\SoftDeletes;

class VaultItem extends Model
{
    use SoftDeletes;

    protected $fillable = [
        'user_id',
        'category',
        'title',
        'username',
        'password',
        'extra_data',
        'notes',
    ];

    protected $casts = [
        'title' => 'encrypted',
        'username' => 'encrypted',
        'password' => 'encrypted',
        'extra_data' => 'encrypted',
        'notes' => 'encrypted',
    ];

    public function user()
    {
        return $this->belongsTo(User::class);
    }
}
