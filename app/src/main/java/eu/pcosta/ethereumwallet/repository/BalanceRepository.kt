package eu.pcosta.ethereumwallet.repository

import android.util.Log
import eu.pcosta.ethereumwallet.database.TokenRoomEntry
import eu.pcosta.ethereumwallet.database.TokensDatabase
import eu.pcosta.ethereumwallet.domain.*
import eu.pcosta.ethereumwallet.network.*
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.Flowables.combineLatest
import io.reactivex.rxjava3.kotlin.toFlowable
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import java.math.BigDecimal
import java.util.concurrent.TimeUnit


/**
 * Balance repository. Allows to fetch the amount of tokens the hardcoded client address has
 */
interface BalanceRepository {

    /**
     * Merges the data from Coin Gecko with Etherscan to get Ether balance and respective value
     */
    fun observeAccountBalance(): Flowable<EtherBalance>

    /**
     * Search for tokens matching the query
     * The query can be a partial ID, i.e. "USD" will match with "USDT" and "USDC"
     *
     * @param query Token ID
     */
    fun searchTokens(query: String): Single<List<TokenBalance>>
}

class BalanceRepositoryImpl(
    connectivityService: ConnectivityService,
    ethplorerApiService: EthplorerApiService,
    private val tokensDatabaseDao: TokensDatabase.TokensDao,
    private val coinGeckoApiService: CoinGeckoApiService,
    private val etherscanApiService: EtherscanApiService
) : BalanceRepository {

    private val readyFlag = BehaviorProcessor.create<Boolean>()

    init {
        // If we already have something, we are ready to start searching, even if with cached values
        tokensDatabaseDao.count()
            .subscribeOn(Schedulers.io())
            .doOnSuccess { readyFlag.onNext(it > 0) }
            .subscribe()

        // Update database with latest data
        connectivityService.observeIsConnectedToInternet()
            .filter { it }
            .firstElement()
            .flatMapSingle { ethplorerApiService.getTopTokens() }
            .subscribeOn(Schedulers.io())
            .doOnSuccess { tokens ->
                tokensDatabaseDao.clear()
                tokensDatabaseDao.insert(tokens.map { it.toRoomEntry() })
                readyFlag.onNext(true)
            }
            .subscribe()
    }

    override fun observeAccountBalance(): Flowable<EtherBalance> {
        // Refresh Ether balance every minute and the price every 15 seconds
        return combineLatest(
            Flowable.interval(0, 60, TimeUnit.SECONDS)
                .switchMapSingle {
                    etherscanApiService.getBalance().toOptional()
                }
                .onErrorReturnItem(Optional()),
            Flowable.interval(0, 15, TimeUnit.SECONDS)
                .switchMapSingle {
                    coinGeckoApiService.getEtherPrice().toOptional()
                }
                .onErrorReturnItem(Optional())
        )
            .map { (balance, price) ->
                EtherBalance(
                    amount = balance.value?.balance?.fixUnit(18),
                    price = price.value?.let {
                        EtherPrice(
                            eur = it.eur,
                            usd = it.usd
                        )
                    }
                )
            }
    }

    override fun searchTokens(query: String): Single<List<TokenBalance>> {
        return readyFlag.filter { it }
            .firstOrError()
            .timeout(5, TimeUnit.SECONDS)
            .flatMap { tokensDatabaseDao.search("$query%") }
            .flatMapPublisher { tokenList ->
                tokenList.toFlowable()
                    .flatMapSingle { token ->
                        etherscanApiService.getTokenBalance(token.address)
                            .map { balance ->
                                TokenBalance(
                                    id = token.id,
                                    name = token.name,
                                    amount = balance.balance.fixUnit(token.decimals)
                                )
                            }
                    }
            }
            .toList()
    }

    /**
     * Extension function to map Token network model to DB model
     */
    private fun Token.toRoomEntry() = TokenRoomEntry(
        id = id, name = name, address = address, decimals = decimals.toInt()
    )

    /**
     * Fix the amount of a token with the correct decimal number
     */
    private fun String.fixUnit(decimals: Int): BigDecimal = BigDecimal(this).divide(BigDecimal.valueOf(10).pow(decimals))
}