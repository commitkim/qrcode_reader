package com.gekim16.qrcodereader.presenter

import android.content.Context
import androidx.room.Room
import com.gekim16.qrcodereader.model.QRCodeDatabase
import com.gekim16.qrcodereader.model.Result

class Interactor(context: Context) {
    private val db = Room.databaseBuilder(context, QRCodeDatabase::class.java, "qr_code_database").build()

    fun getResults() = db.resultDao().selectResults()

    fun addResult(result: Result) {
        db.resultDao().insertResult(result)
    }

    fun deleteResult(result: Result) {
        db.resultDao().deleteResult(result)
    }
}