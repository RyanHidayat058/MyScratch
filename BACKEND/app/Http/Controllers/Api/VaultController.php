<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\VaultItem;
use Illuminate\Http\Request;

class VaultController extends Controller
{
    /**
     * Get All Vault Items for User
     */
    public function index(Request $request)
    {
        $query = $request->user()->vaultItems();

        if ($request->filled('category')) {
            $query->where('category', $request->category);
        }

        $items = $query->orderBy('updated_at', 'desc')->get();

        // Optional in-memory search across decrypted fields if search query is provided
        if ($request->filled('search')) {
            $search = strtolower($request->search);
            $items = $items->filter(function ($item) use ($search) {
                return str_contains(strtolower($item->title ?? ''), $search) ||
                       str_contains(strtolower($item->username ?? ''), $search) ||
                       str_contains(strtolower($item->notes ?? ''), $search);
            })->values();
        }

        return response()->json([
            'success' => true,
            'items' => $items,
        ]);
    }

    /**
     * Store Vault Item
     */
    public function store(Request $request)
    {
        $request->validate([
            'category' => 'required|string|in:account,bank,identity,note',
            'title' => 'required|string|max:255',
            'username' => 'nullable|string',
            'password' => 'nullable|string',
            'extra_data' => 'nullable|string',
            'notes' => 'nullable|string',
        ]);

        $item = $request->user()->vaultItems()->create([
            'category' => $request->category,
            'title' => trim($request->title),
            'username' => $request->username ? trim($request->username) : null,
            'password' => $request->password,
            'extra_data' => $request->extra_data,
            'notes' => $request->notes ? trim($request->notes) : null,
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Data rahasia berhasil disimpan ke Brankas.',
            'item' => $item,
        ], 201);
    }

    /**
     * Update Vault Item
     */
    public function update(Request $request, $id)
    {
        $item = $request->user()->vaultItems()->findOrFail($id);

        $request->validate([
            'category' => 'nullable|string|in:account,bank,identity,note',
            'title' => 'required|string|max:255',
            'username' => 'nullable|string',
            'password' => 'nullable|string',
            'extra_data' => 'nullable|string',
            'notes' => 'nullable|string',
        ]);

        $item->update([
            'category' => $request->category ?: $item->category,
            'title' => trim($request->title),
            'username' => $request->username ? trim($request->username) : null,
            'password' => $request->password !== null ? $request->password : $item->password,
            'extra_data' => $request->extra_data !== null ? $request->extra_data : $item->extra_data,
            'notes' => $request->notes ? trim($request->notes) : null,
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Data rahasia berhasil diperbarui.',
            'item' => $item,
        ]);
    }

    /**
     * Delete Vault Item
     */
    public function destroy(Request $request, $id)
    {
        $item = $request->user()->vaultItems()->findOrFail($id);
        $item->delete();

        return response()->json([
            'success' => true,
            'message' => 'Data rahasia berhasil dihapus dari Brankas.',
        ]);
    }
}
