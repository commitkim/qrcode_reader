package com.gekim16.qrcodereader.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Result::class], version = 1)
abstract class QRCodeDatabase : RoomDatabase() {
    abstract fun resultDao(): ResultDao
}