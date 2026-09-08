package com.myscratch.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.myscratch.app.data.local.dao.FolderDao
import com.myscratch.app.data.local.dao.NoteDao
import com.myscratch.app.data.local.dao.TransactionDao
import com.myscratch.app.data.local.dao.UserDao
import com.myscratch.app.data.local.dao.VaultDao
import com.myscratch.app.data.local.entity.FolderEntity
import com.myscratch.app.data.local.entity.NoteEntity
import com.myscratch.app.data.local.entity.TransactionEntity
import com.myscratch.app.data.local.entity.UserEntity
import com.myscratch.app.data.local.entity.VaultItemEntity

@Database(
    entities = [
        UserEntity::class,
        TransactionEntity::class,
        FolderEntity::class,
        NoteEntity::class,
        VaultItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun folderDao(): FolderDao
    abstract fun noteDao(): NoteDao
    abstract fun vaultDao(): VaultDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myscratch.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
