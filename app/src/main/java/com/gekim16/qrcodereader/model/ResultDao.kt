package com.gekim16.qrcodereader.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ResultDao {
    @Query("SELECT * FROM result")
    fun selectResults() : List<Result>

    @Insert
    fun insertResult(result: Result)

    @Delete
    fun deleteResult(result: Result)
}