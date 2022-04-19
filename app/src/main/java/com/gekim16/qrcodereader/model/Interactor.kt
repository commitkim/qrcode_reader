package com.gekim16.qrcodereader.presenter

import android.content.Context
import androidx.room.Room
import com.gekim16.qrcodereader.model.QRCodeDatabase
import com.gekim16.qrcodereader.model.Result


/**
 *  Presenter가 context를 가지고 있지 않게 하기위해서 Room에 접근하기위한 context를 가진 클래스를 새로 생성하여
 *  Interactor를 통해서 값을 가져오도록 구현
 */
class Interactor(context: Context) {
    private val db =
        Room.databaseBuilder(context, QRCodeDatabase::class.java, "qr_code_database").build()

    fun getResults() = db.resultDao().selectResults()

    fun addResult(result: Result) {
        db.resultDao().insertResult(result)
    }

    fun deleteResult(result: Result) {
        db.resultDao().deleteResult(result)
    }
}