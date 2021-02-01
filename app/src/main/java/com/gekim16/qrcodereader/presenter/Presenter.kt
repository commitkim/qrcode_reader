package com.gekim16.qrcodereader.presenter

import com.gekim16.qrcodereader.model.Result
import com.gekim16.qrcodereader.model.Type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 *  Room으로의 접근은 Coroutine을 사용하여 비동기적으로 처리하고 화면을 변경하기 위해
 *  Main Thread에서 화면을 변경하는 메소드를 호출하는 방식으로 구현
 *
 *  View에서 생성된 Interactor 객체를 통해서 Room 접근
 */
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

    /**
     *  Result를 저장하기전 어떤 타입인지 확인하는 메소드
     */
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