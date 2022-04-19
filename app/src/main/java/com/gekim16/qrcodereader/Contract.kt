package com.gekim16.qrcodereader

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

abstract class BasePresenter<V>: Contract.Presenter {
    private var view: V? = null

    fun setView(view: V) {
        this.view = view
    }

    fun getView() = this.view
}