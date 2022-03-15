package eu.pcosta.ethereumwallet

import android.app.Application
import android.util.Log
import androidx.room.Room
import eu.pcosta.ethereumwallet.database.FavoritesDatabase
import eu.pcosta.ethereumwallet.database.TokensDatabase
import eu.pcosta.ethereumwallet.domain.*
import eu.pcosta.ethereumwallet.repository.*
import eu.pcosta.ethereumwallet.ui.balance.BalanceViewModel
import eu.pcosta.ethereumwallet.ui.search.SearchViewModel
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module


val repositoryModule = module {
    single<CoinGeckoRepository> { CoinGeckoRepositoryImpl(get()) }
    single<EtherscanRepository> { EtherscanRepositoryImpl(get()) }
    single<EthplorerRepository> { EthplorerRepositoryImpl() }
}

val serviceModule = module {
    single<ConnectivityService> { ConnectivityServiceImpl(get()) }

    factory { Room.databaseBuilder(get(), TokensDatabase::class.java, "tokens.db").build().dao }
    factory { Room.databaseBuilder(get(), FavoritesDatabase::class.java, "favorites.db").build().dao }

    single<BalanceService> { BalanceServiceImpl(get(), get(), get(), get(), get()) }
    single<FavoritesService> { FavoritesServiceImpl(get()) }
}

val viewModelModule = module {
    viewModel { BalanceViewModel(get(), get(), get()) }
    viewModel { SearchViewModel(get(), get(), get()) }
}

/**
 * Wallet Main Application. Start Koin and necessary modules to be injected
 */
class WalletApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@WalletApplication)
            modules(listOf(repositoryModule, serviceModule, viewModelModule))
        }.koin.let {
            // Init balance repository so we can start fetching the top tokens
            it.get<BalanceService>()
        }

        // Logging errors for streams that already have been disposed, aka undeliverable exceptions
        RxJavaPlugins.setErrorHandler {
            Log.e("RxJavaError", it.stackTraceToString())
        }
    }
}