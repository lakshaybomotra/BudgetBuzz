package com.lbdev.budgetbuzz.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lbdev.budgetbuzz.data.model.Profile

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM profiles WHERE userID = :uid")
    fun getProfile(uid: String): LiveData<Profile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(userProfile: Profile)
}