package eu.pcosta.ethereumwallet

import com.nhaarman.mockitokotlin2.*
import eu.pcosta.ethereumwallet.database.TokenRoomEntry
import eu.pcosta.ethereumwallet.database.TokensDatabase
import eu.pcosta.ethereumwallet.network.*
import eu.pcosta.ethereumwallet.repository.BalanceRepositoryImpl
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import org.amshove.kluent.any
import org.amshove.kluent.itReturns
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.math.BigDecimal


class BalanceRepositoryTest {

    private val tokenRoomEntry = TokenRoomEntry(
        id = "USDT",
        name = "Tether USD",
        address = "0x00",
        decimals = 6
    )

    private val tokenNetwork = Token(
        id = "USDT",
        name = "Tether USD",
        address = "0x00",
        decimals = "6"
    )

    private val tokenBalance = Balance("1000000", "", "")

    private val etherBalance = Balance("100000000", "", "")

    private val timeout = timeout(1000)

    @Mock
    private lateinit var tokensDao: TokensDatabase.TokensDao

    @Mock
    private lateinit var connectivityService: ConnectivityService

    @Mock
    private lateinit var coinGeckoApiService: CoinGeckoApiService

    @Mock
    private lateinit var etherscanApiService: EtherscanApiService

    @Mock
    private lateinit var ethplorerApiService: EthplorerApiService

    private fun buildService() =
        BalanceRepositoryImpl(connectivityService, ethplorerApiService, tokensDao, coinGeckoApiService, etherscanApiService)

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        whenever(connectivityService.observeIsConnectedToInternet()) itReturns Flowable.just(true)
        whenever(coinGeckoApiService.getEtherPrice()) itReturns Single.just(Price(BigDecimal("1"), BigDecimal("1.15")))
        whenever(ethplorerApiService.getTopTokens()) itReturns Single.just(listOf(tokenNetwork))

        whenever(etherscanApiService.getBalance()) itReturns Single.just(etherBalance)
        whenever(etherscanApiService.getTokenBalance(any())) itReturns Single.just(tokenBalance)

        doNothing().whenever(tokensDao).clear()
        doNothing().whenever(tokensDao).insert(any())
        whenever(tokensDao.count()) itReturns Single.just(1)
        whenever(tokensDao.search(any())) itReturns Single.just(listOf(tokenRoomEntry))
    }

    @Test
    fun testInit() {
        buildService()

        verify(tokensDao, timeout).count()
        verify(connectivityService, timeout).observeIsConnectedToInternet()
        verify(tokensDao, timeout).clear()
        verify(tokensDao, timeout).insert(eq(listOf(tokenRoomEntry)))
    }

    @Test
    fun testObserveAccountBalance() {
        val service = buildService()

        val balance = service.observeAccountBalance().blockingFirst()
        balance.amount shouldBeEqualTo BigDecimal("0.0000000001")
        balance.price!!.eur shouldBeEqualTo BigDecimal("1")
        balance.price!!.usd shouldBeEqualTo BigDecimal("1.15")

        verify(etherscanApiService, timeout).getBalance()
        verify(coinGeckoApiService, timeout).getEtherPrice()
    }

    @Test
    fun testSearchTokens() {
        val service = buildService()

        val balance = service.searchTokens("USD").blockingGet()
        balance.size shouldBeEqualTo 1
        balance[0].id shouldBeEqualTo "USDT"
        balance[0].name shouldBeEqualTo "Tether USD"
        balance[0].amount shouldBeEqualTo BigDecimal("1")

        verify(tokensDao, timeout).search(eq("USD%"))
        verify(etherscanApiService, timeout).getTokenBalance(eq("0x00"))
    }
}