<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Transaction;
use Illuminate\Http\Request;

class FinanceController extends Controller
{
    /**
     * Get All Transactions
     */
    public function index(Request $request)
    {
        $user = $request->user();

        $query = $user->transactions();

        if ($request->filled('type')) {
            $query->where('type', $request->type);
        }

        if ($request->filled('category')) {
            $query->where('category', $request->category);
        }

        if ($request->filled('search')) {
            $search = $request->search;
            $query->where(function ($q) use ($search) {
                $q->where('title', 'like', "%{$search}%")
                  ->orWhere('category', 'like', "%{$search}%")
                  ->orWhere('note', 'like', "%{$search}%");
            });
        }

        $transactions = $query->orderBy('date', 'desc')->get();

        return response()->json([
            'success' => true,
            'transactions' => $transactions,
        ]);
    }

    /**
     * Get Finance Summary (Metrics & Chart)
     */
    public function summary(Request $request)
    {
        $user = $request->user();

        $transactions = $user->transactions()->get();

        $totalIncome = (float) $transactions->where('type', 'INCOME')->sum('amount');
        $totalExpense = (float) $transactions->where('type', 'EXPENSE')->sum('amount');
        $totalDebtPayable = (float) $transactions->where('type', 'DEBT_PAYABLE')->sum('amount');
        $totalDebtReceivable = (float) $transactions->where('type', 'DEBT_RECEIVABLE')->sum('amount');

        $totalBalance = $totalIncome - $totalExpense;
        $expectedNetWorth = $totalBalance + $totalDebtReceivable - $totalDebtPayable;

        // Calculate Category Breakdown (e.g. for Expenses)
        $expenseTransactions = $transactions->where('type', 'EXPENSE');
        $categoryBreakdown = [];

        if ($totalExpense > 0) {
            $grouped = $expenseTransactions->groupBy('category');
            foreach ($grouped as $cat => $items) {
                $catSum = (float) $items->sum('amount');
                $categoryBreakdown[] = [
                    'category' => $cat,
                    'total' => $catSum,
                    'percentage' => round($catSum / $totalExpense, 4),
                ];
            }
            usort($categoryBreakdown, fn($a, $b) => $b['total'] <=> $a['total']);
        }

        return response()->json([
            'success' => true,
            'summary' => [
                'totalBalance' => $totalBalance,
                'totalIncome' => $totalIncome,
                'totalExpense' => $totalExpense,
                'totalDebtPayable' => $totalDebtPayable,
                'totalDebtReceivable' => $totalDebtReceivable,
                'expectedNetWorth' => $expectedNetWorth,
                'categoryBreakdown' => $categoryBreakdown,
            ],
        ]);
    }

    /**
     * Store a New Transaction
     */
    public function store(Request $request)
    {
        $request->validate([
            'title' => 'required|string|max:200',
            'amount' => 'required|numeric|min:1',
            'type' => 'required|in:INCOME,EXPENSE,DEBT_PAYABLE,DEBT_RECEIVABLE',
            'category' => 'required|string|max:100',
            'date' => 'required|integer',
            'note' => 'nullable|string',
        ]);

        $transaction = $request->user()->transactions()->create([
            'title' => trim($request->title),
            'amount' => $request->amount,
            'type' => $request->type,
            'category' => trim($request->category),
            'date' => $request->date,
            'note' => $request->note ? trim($request->note) : null,
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Transaksi berhasil disimpan.',
            'transaction' => $transaction,
        ], 201);
    }

    /**
     * Update an Existing Transaction
     */
    public function update(Request $request, $id)
    {
        $transaction = $request->user()->transactions()->findOrFail($id);

        $request->validate([
            'title' => 'required|string|max:200',
            'amount' => 'required|numeric|min:1',
            'type' => 'required|in:INCOME,EXPENSE,DEBT_PAYABLE,DEBT_RECEIVABLE',
            'category' => 'required|string|max:100',
            'date' => 'required|integer',
            'note' => 'nullable|string',
        ]);

        $transaction->update([
            'title' => trim($request->title),
            'amount' => $request->amount,
            'type' => $request->type,
            'category' => trim($request->category),
            'date' => $request->date,
            'note' => $request->note ? trim($request->note) : null,
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Transaksi berhasil diperbarui.',
            'transaction' => $transaction,
        ]);
    }

    /**
     * Delete a Transaction
     */
    public function destroy(Request $request, $id)
    {
        $transaction = $request->user()->transactions()->findOrFail($id);
        $transaction->delete();

        return response()->json([
            'success' => true,
            'message' => 'Transaksi berhasil dihapus.',
        ]);
    }
}
