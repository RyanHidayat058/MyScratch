package com.myscratch.app

import android.app.Application
import com.myscratch.app.data.local.AppDatabase
import com.myscratch.app.data.network.TokenManager
import com.myscratch.app.data.repository.AuthRepositoryImpl
import com.myscratch.app.data.repository.FinanceRepositoryImpl
import com.myscratch.app.data.repository.NotesRepositoryImpl
import com.myscratch.app.data.repository.VaultRepositoryImpl
import com.myscratch.app.domain.repository.AuthRepository
import com.myscratch.app.domain.repository.FinanceRepository
import com.myscratch.app.domain.repository.NotesRepository
import com.myscratch.app.domain.repository.VaultRepository

class MyScratchApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var tokenManager: TokenManager
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var financeRepository: FinanceRepository
        private set

    lateinit var notesRepository: NotesRepository
        private set

    lateinit var vaultRepository: VaultRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        tokenManager = TokenManager(this)
        authRepository = AuthRepositoryImpl(tokenManager)
        financeRepository = FinanceRepositoryImpl(database.transactionDao(), tokenManager)
        notesRepository = NotesRepositoryImpl(database.folderDao(), database.noteDao(), tokenManager)
        vaultRepository = VaultRepositoryImpl(database.vaultDao(), tokenManager)
    }

    companion object {
        lateinit var instance: MyScratchApp
            private set
    }
}
