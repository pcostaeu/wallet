package eu.pcosta.ethereumwallet.ui.balance

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import eu.pcosta.ethereumwallet.domain.BalanceService
import eu.pcosta.ethereumwallet.domain.ConnectivityService
import eu.pcosta.ethereumwallet.domain.FavoritesService
import eu.pcosta.ethereumwallet.domain.models.EtherBalance
import eu.pcosta.ethereumwallet.domain.models.Favorite
import eu.pcosta.ethereumwallet.ui.base.BaseViewModel
import eu.pcosta.ethereumwallet.ui.base.Response
import eu.pcosta.ethereumwallet.ui.base.Status
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*


class BalanceViewModel(
    private val connectivityService: ConnectivityService,
    private val balanceService: BalanceService,
    private val favoritesService: FavoritesService
) : BaseViewModel() {

    fun observeBalance(): LiveData<Response<EtherBalance>> {
        val stream = connectivityService.observeIsConnectedToInternet()
            .switchMap { isConnected ->
                if (isConnected) {
                    balanceService.observeAccountBalance()
                        .map {
                            Response(
                                timestamp = Date().time,
                                status = Status.OK,
                                data = it
                            )
                        }
                } else {
                    Flowable.just(Response(status = Status.ERROR_NO_INTERNET))
                }
            }
            .startWithItem(Response(status = Status.LOADING))
            .onErrorReturnItem(Response(status = Status.ERROR_GENERIC))
            .subscribeOn(Schedulers.io())

        return LiveDataReactiveStreams.fromPublisher(stream)
    }

    fun observeFavorites(): LiveData<Response<List<Favorite>>> {
        val stream = favoritesService.observeFavorites()
            .map {
                Response(
                    status = Status.OK,
                    data = it
                )
            }
            .startWithItem(Response(status = Status.LOADING))
            .subscribeOn(Schedulers.io())

        return LiveDataReactiveStreams.fromPublisher(stream)
    }

    fun deleteFavorite(favorite: Favorite) {
        favoritesService.removeFavorite(favorite)
            .subscribeOn(Schedulers.io())
            .subscribe()
    }
}