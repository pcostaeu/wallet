package eu.pcosta.ethereumwallet

import android.app.Application
import android.util.Log
import androidx.room.Room
import eu.pcosta.ethereumwallet.database.TokensDatabase
import eu.pcosta.ethereumwallet.network.*
import eu.pcosta.ethereumwallet.repository.BalanceRepository
import eu.pcosta.ethereumwallet.repository.BalanceRepositoryImpl
import eu.pcosta.ethereumwallet.ui.balance.BalanceViewModel
import eu.pcosta.ethereumwallet.ui.search.SearchViewModel
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module


val serviceModule = module {
    single<ConnectivityService> { ConnectivityServiceImpl(get()) }
    single<CoinGeckoApiService> { CoinGeckoApiServiceImpl(get()) }
    single<EtherscanApiService> { EtherscanApiServiceImpl(get()) }
    single<EthplorerApiService> { EthplorerApiServiceImpl() }
    single<BalanceRepository> {
        val dao = Room.databaseBuilder(get(), TokensDatabase::class.java, "tokens.db").build().dao
        BalanceRepositoryImpl(get(), get(), dao, get(), get())
    }
}

val viewModelModule = module {
    viewModel { BalanceViewModel(get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
}

/**
 * Wallet Main Application. Start Koin and necessary modules to be injected
 */
class WalletApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@WalletApplication)
            modules(listOf(serviceModule, viewModelModule))
        }.koin.let {
            // Init balance repository so we can start fetching the top tokens
            it.get<BalanceRepository>()
        }

        // Logging errors for streams that already have been disposed, aka undeliverable exceptions
        RxJavaPlugins.setErrorHandler {
            Log.d("RxJavaError", it.message ?: "empty")
        }
    }
}