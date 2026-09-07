package com.myscratch.app.domain.model

enum class TransactionType {
    INCOME,
    EXPENSE,
    DEBT_PAYABLE,    // Hutang Saya (Kewajiban bayar ke orang lain)
    DEBT_RECEIVABLE  // Hutang ke Saya (Piutang / Uang saya di orang lain)
}
