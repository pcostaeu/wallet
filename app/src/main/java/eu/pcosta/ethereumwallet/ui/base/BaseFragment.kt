package eu.pcosta.ethereumwallet.ui.base

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable


abstract class BaseFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {

    private val compositeDisposable = CompositeDisposable()

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    fun Disposable.bind() = compositeDisposable.add(this)
}