package eu.pcosta.ethereumwallet.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import eu.pcosta.ethereumwallet.domain.BalanceService
import eu.pcosta.ethereumwallet.domain.ConnectivityService
import eu.pcosta.ethereumwallet.domain.FavoritesService
import eu.pcosta.ethereumwallet.domain.models.Favorite
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
    private val favoritesService: FavoritesService
) : BaseViewModel() {

    private val balance = MutableLiveData<Response<List<Pair<TokenBalance, Favorite?>>>>()
    private val queryProcessor = BehaviorProcessor.create<String>()

    init {
        combineLatest(
            favoritesService.observeFavorites(),
            queryProcessor.doOnNext {
                balance.postValue(Response(status = Status.LOADING))
            }
        ) { favorites, query ->
            favorites to query
        }
            .observeOn(Schedulers.io())
            .switchMapSingle { (favorites, query) ->
                balanceService.searchTokens(query)
                    .map {
                        Response(
                            timestamp = Date().time,
                            status = Status.OK,
                            data = it.sortedBy { it.name }.map { token ->
                                token to favorites.firstOrNull { it.id == token.id }
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

    fun observeTokens(): LiveData<Response<List<Pair<TokenBalance, Favorite?>>>> = balance

    fun addFavorite(token: TokenBalance) {
        favoritesService.addFavorite(
            Favorite(token.id, token.name)
        )
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun removeFavorite(favorite: Favorite) {
        favoritesService.removeFavorite(favorite)
            .subscribeOn(Schedulers.io())
            .subscribe()
    }
}