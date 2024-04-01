package com.lbdev.budgetbuzz.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lbdev.budgetbuzz.data.db.dao.UserProfileDao
import com.lbdev.budgetbuzz.data.model.Profile

@Database(entities = [Profile::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
}