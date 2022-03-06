package eu.pcosta.ethereumwallet

import eu.pcosta.ethereumwallet.database.TokenRoomEntry
import eu.pcosta.ethereumwallet.database.TokensDatabase
import eu.pcosta.ethereumwallet.domain.BalanceServiceImpl
import eu.pcosta.ethereumwallet.domain.ConnectivityService
import eu.pcosta.ethereumwallet.network.Balance
import eu.pcosta.ethereumwallet.network.Price
import eu.pcosta.ethereumwallet.network.Token
import eu.pcosta.ethereumwallet.repository.CoinGeckoRepository
import eu.pcosta.ethereumwallet.repository.EtherscanRepository
import eu.pcosta.ethereumwallet.repository.EthplorerRepository
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import org.amshove.kluent.any
import org.amshove.kluent.itReturns
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
import org.mockito.kotlin.timeout
import org.mockito.kotlin.whenever
import java.math.BigDecimal


class BalanceServiceTest {

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
    private lateinit var mocks: AutoCloseable

    @Mock
    private lateinit var tokensDao: TokensDatabase.TokensDao

    @Mock
    private lateinit var connectivityService: ConnectivityService

    @Mock
    private lateinit var coinGeckoRepository: CoinGeckoRepository

    @Mock
    private lateinit var etherscanRepository: EtherscanRepository

    @Mock
    private lateinit var ethplorerRepository: EthplorerRepository

    private fun buildService() =
        BalanceServiceImpl(
            connectivityService,
            ethplorerRepository,
            tokensDao,
            coinGeckoRepository,
            etherscanRepository
        )

    @Before
    fun setup() {
        mocks = MockitoAnnotations.openMocks(this)

        whenever(connectivityService.observeIsConnectedToInternet()) itReturns Flowable.just(true)
        whenever(coinGeckoRepository.getEtherPrice()) itReturns Single.just(Price(BigDecimal("1"), BigDecimal("1.15")))
        whenever(ethplorerRepository.getTopTokens()) itReturns Single.just(listOf(tokenNetwork))

        whenever(etherscanRepository.getBalance()) itReturns Single.just(etherBalance)
        whenever(etherscanRepository.getTokenBalance(any())) itReturns Single.just(tokenBalance)

        doNothing().whenever(tokensDao).clear()
        doNothing().whenever(tokensDao).insert(any())
        whenever(tokensDao.count()) itReturns Single.just(1)
        whenever(tokensDao.search(any())) itReturns Single.just(listOf(tokenRoomEntry))
    }

    @After
    fun tearDown() {
        mocks.close()
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

        verify(etherscanRepository, timeout).getBalance()
        verify(coinGeckoRepository, timeout).getEtherPrice()
    }

    @Test
    fun testSearchTokens() {
        val service = buildService()

        val balance = service.searchTokens("USD").blockingGet()
        balance.size shouldBeEqualTo 1
        balance[0].id shouldBeEqualTo "USDT"
        balance[0].name shouldBeEqualTo "Tether USD"
        balance[0].amount shouldBeEqualTo BigDecimal("1")

        verify(tokensDao, timeout).search(eq("%USD%"))
        verify(etherscanRepository, timeout).getTokenBalance(eq("0x00"))
    }
}