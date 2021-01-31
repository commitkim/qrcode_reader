package com.gekim16.qrcodereader.presenter

abstract class BasePresenter<V> {
    private var view: V? = null

    fun setView(view: V){
        this.view = view
    }
    fun getView() = this.view
}