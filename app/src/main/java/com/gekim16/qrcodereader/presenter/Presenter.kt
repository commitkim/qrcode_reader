package com.gekim16.qrcodereader.presenter

import com.gekim16.qrcodereader.model.Result
import com.gekim16.qrcodereader.model.Type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class Presenter(private val interactor: Interactor) : Contract.Presenter,
    BasePresenter<Contract.View>() {

    override fun getResults(callbacks: (MutableList<Result>) -> Unit) {
        val results = mutableListOf<Result>()
        CoroutineScope(Default).launch {
            results.addAll(interactor.getResults())

            withContext(Main) {
                callbacks.invoke(results)
            }
        }
    }


    override fun addResult(url: String) {
        val result = Result(type = typeCheck(url), url = url)

        CoroutineScope(Default).launch {
            interactor.addResult(result)

            withContext(Main) {
                getView()?.addAdapterList(result)
                getView()?.showToast("추가되었습니다.")
            }
        }
    }

    override fun deleteResult(result: Result) {
        CoroutineScope(Default).launch {
            interactor.deleteResult(result)

            withContext(Main) {
                getView()?.deleteAdapterList(result)
                getView()?.showToast("삭제되었습니다.")
            }
        }
    }

    override fun filterResult(str: String) {
        getView()?.showFilteredList(str)
    }

    private fun typeCheck(url: String): String {
        val lowCase = url.toLowerCase(Locale.ROOT)
        return when {
            lowCase.startsWith("mailto") -> Type.EMAIL.name
            lowCase.startsWith("tel") -> Type.TELEPHONE.name
            lowCase.startsWith("sms") -> Type.SMS.name
            lowCase.startsWith("geo") -> Type.MAP.name
            else -> Type.URl.name
        }
    }
}