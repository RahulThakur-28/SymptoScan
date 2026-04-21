package com.example.rahul.symptoscan.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SymptomEntity::class], version = 1, exportSchema = false)
abstract class SymptomDatabase : RoomDatabase() {
    abstract fun symptomDao(): SymptomDao

    companion object {
        @Volatile
        private var INSTANCE: SymptomDatabase? = null

        fun getDatabase(context: Context): SymptomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SymptomDatabase::class.java,
                    "symptom_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}