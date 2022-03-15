package eu.pcosta.ethereumwallet.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import eu.pcosta.ethereumwallet.domain.BalanceService
import eu.pcosta.ethereumwallet.domain.ConnectivityService
import eu.pcosta.ethereumwallet.domain.FavoritesService
import eu.pcosta.ethereumwallet.domain.models.TokenBalance
import eu.pcosta.ethereumwallet.ui.base.BaseViewModel
import eu.pcosta.ethereumwallet.ui.base.Response
import eu.pcosta.ethereumwallet.ui.base.Status
import io.reactivex.rxjava3.core.Flowable.combineLatest
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*

class SearchViewModel(
    connectivityService: ConnectivityService,
    balanceService: BalanceService,
    favoritesService: FavoritesService
) : BaseViewModel() {

    private val balance = MutableLiveData<Response<List<Pair<TokenBalance, Boolean>>>>()
    private val queryProcessor = BehaviorProcessor.create<String>()

    init {
        combineLatest(
            favoritesService.observeFavorites(),
            queryProcessor
        ) { favorites, query ->
            favorites.map { it.id } to query
        }
            .observeOn(Schedulers.io())
            .doOnNext {
                balance.postValue(Response(status = Status.LOADING))
            }
            .switchMapSingle { (favorites, query) ->
                balanceService.searchTokens(query)
                    .map {
                        Response(
                            timestamp = Date().time,
                            status = Status.OK,
                            data = it.map { token ->
                                token to favorites.contains(token.id)
                            }
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
    fun observeTokens(): LiveData<Response<List<Pair<TokenBalance, Boolean>>>> = balance
}