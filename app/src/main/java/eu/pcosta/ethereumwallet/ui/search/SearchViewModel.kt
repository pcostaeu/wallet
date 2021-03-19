package eu.pcosta.ethereumwallet.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import eu.pcosta.ethereumwallet.domain.TokenBalance
import eu.pcosta.ethereumwallet.network.ConnectivityService
import eu.pcosta.ethereumwallet.repository.BalanceRepository
import eu.pcosta.ethereumwallet.ui.base.BaseViewModel
import eu.pcosta.ethereumwallet.ui.base.Response
import eu.pcosta.ethereumwallet.ui.base.Status
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*

class SearchViewModel(
    connectivityService: ConnectivityService,
    balanceRepository: BalanceRepository
) : BaseViewModel() {

    private val balance = MutableLiveData<Response<List<TokenBalance>>>()
    private val queryProcessor = BehaviorProcessor.create<String>()

    init {
        queryProcessor
            .observeOn(Schedulers.io())
            .doOnNext {
                balance.postValue(Response(status = Status.LOADING))
            }
            .switchMapSingle { query ->
                balanceRepository.searchTokens(query)
                    .map {
                        Response(
                            timestamp = Date().time,
                            status = Status.OK,
                            data = it
                        )
                    }
                    .onErrorReturnItem(
                        Response(
                            status = if (connectivityService.isConnectedToInternet()) Status.ERROR_GENERIC else Status.ERROR_NO_INTERNET
                        )
                    )
            }
            .subscribe {
                balance.postValue(it)
            }
            .bind()
    }

    fun onSearchChanged(query: CharSequence) = queryProcessor.onNext(query.toString())
    fun observeTokens(): LiveData<Response<List<TokenBalance>>> = balance
}