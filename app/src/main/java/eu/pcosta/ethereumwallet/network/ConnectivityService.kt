package eu.pcosta.ethereumwallet.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.BehaviorProcessor

/**
 * Service to inform the current device internet connection state
 */
interface ConnectivityService {

    /**
     * Get current connection state
     *
     * @return Connection state
     */
    fun isConnectedToInternet(): Boolean

    /**
     * Observe the connection state
     *
     * @return Flowable observable with the connection state. It will start with value if available
     */
    fun observeIsConnectedToInternet(): Flowable<Boolean>
}

class ConnectivityServiceImpl(context: Context) : ConnectivityService {

    private val connectionProcessor = BehaviorProcessor.createDefault(false)

    init {
        val connectivityService = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityService.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    connectionProcessor.onNext(true)
                }

                override fun onLost(network: Network) {
                    connectionProcessor.onNext(false)
                }
            })
    }

    override fun isConnectedToInternet(): Boolean = connectionProcessor.value!!

    override fun observeIsConnectedToInternet(): Flowable<Boolean> = connectionProcessor
}