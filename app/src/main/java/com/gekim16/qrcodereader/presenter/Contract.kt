package com.gekim16.qrcodereader.presenter

import android.content.ComponentCallbacks
import com.gekim16.qrcodereader.model.Result


interface Contract {
    interface View{
        fun addAdapterList(result: Result)

        fun deleteAdapterList(result: Result)

        fun showFilteredList(str: String)

        fun showToast(message: String)
    }

    interface Presenter{
        fun getResults(callbacks: (MutableList<Result>)->Unit)

        fun addResult(url: String)

        fun deleteResult(result: Result)

        fun filterResult(str: String)


    }
}