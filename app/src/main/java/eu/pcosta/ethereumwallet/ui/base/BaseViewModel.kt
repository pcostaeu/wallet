package eu.pcosta.ethereumwallet.ui.base

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

enum class Status {
    ERROR_NO_INTERNET, ERROR_GENERIC, LOADING, OK
}

data class Response<T>(
    val status: Status,
    val timestamp: Long? = null,
    val data: T? = null
)

abstract class BaseViewModel : ViewModel() {

    /**
     * Composite disposable to kill stream when ViewModel is removed
     */
    private val compositeDisposable = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun Disposable.bind() = compositeDisposable.add(this)
}