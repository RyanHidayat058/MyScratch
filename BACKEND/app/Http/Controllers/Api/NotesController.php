<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Folder;
use App\Models\Note;
use Illuminate\Http\Request;

class NotesController extends Controller
{
    /**
     * Get All Folders for User
     */
    public function folders(Request $request)
    {
        $folders = $request->user()->folders()->withCount('notes')->get();

        return response()->json([
            'success' => true,
            'folders' => $folders,
        ]);
    }

    /**
     * Create Folder
     */
    public function storeFolder(Request $request)
    {
        $request->validate([
            'name' => 'required|string|max:100',
            'color' => 'nullable|string|max:20',
        ]);

        $folder = $request->user()->folders()->create([
            'name' => trim($request->name),
            'color' => $request->color ?: '#38BDF8',
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Folder berhasil dibuat.',
            'folder' => $folder,
        ], 201);
    }

    /**
     * Delete Folder
     */
    public function deleteFolder(Request $request, $id)
    {
        $folder = $request->user()->folders()->findOrFail($id);
        $folder->delete();

        return response()->json([
            'success' => true,
            'message' => 'Folder berhasil dihapus.',
        ]);
    }

    /**
     * Get Notes
     */
    public function notes(Request $request)
    {
        $query = $request->user()->notes();

        if ($request->filled('folder_id')) {
            $query->where('folder_id', $request->folder_id);
        }

        if ($request->filled('search')) {
            $search = $request->search;
            $query->where(function ($q) use ($search) {
                $q->where('title', 'like', "%{$search}%")
                  ->orWhere('content', 'like', "%{$search}%");
            });
        }

        $notes = $query->orderBy('is_pinned', 'desc')
                       ->orderBy('updated_at', 'desc')
                       ->get();

        return response()->json([
            'success' => true,
            'notes' => $notes,
        ]);
    }

    /**
     * Create Note
     */
    public function storeNote(Request $request)
    {
        $request->validate([
            'title' => 'nullable|string|max:255',
            'content' => 'required|string',
            'folder_id' => 'nullable|exists:folders,id',
            'is_pinned' => 'nullable|boolean',
        ]);

        $note = $request->user()->notes()->create([
            'title' => $request->title ? trim($request->title) : 'Tanpa Judul',
            'content' => trim($request->content),
            'folder_id' => $request->folder_id,
            'is_pinned' => (bool) $request->is_pinned,
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Catatan berhasil disimpan.',
            'note' => $note,
        ], 201);
    }

    /**
     * Update Note
     */
    public function updateNote(Request $request, $id)
    {
        $note = $request->user()->notes()->findOrFail($id);

        $request->validate([
            'title' => 'nullable|string|max:255',
            'content' => 'required|string',
            'folder_id' => 'nullable|exists:folders,id',
            'is_pinned' => 'nullable|boolean',
        ]);

        $note->update([
            'title' => $request->title ? trim($request->title) : 'Tanpa Judul',
            'content' => trim($request->content),
            'folder_id' => $request->folder_id,
            'is_pinned' => $request->has('is_pinned') ? (bool) $request->is_pinned : $note->is_pinned,
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Catatan berhasil diperbarui.',
            'note' => $note,
        ]);
    }

    /**
     * Delete Note
     */
    public function deleteNote(Request $request, $id)
    {
        $note = $request->user()->notes()->findOrFail($id);
        $note->delete();

        return response()->json([
            'success' => true,
            'message' => 'Catatan berhasil dihapus.',
        ]);
    }
}
