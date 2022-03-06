package eu.pcosta.ethereumwallet.repository

import android.content.Context
import eu.pcosta.ethereumwallet.BuildConfig
import eu.pcosta.ethereumwallet.network.Balance
import eu.pcosta.ethereumwallet.network.EtherscanApi
import io.reactivex.rxjava3.core.Single
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory


interface EtherscanRepository {

    /**
     * Fetch the balance for the hardcoded Ethereum account address in build.gradle
     */
    fun getBalance(): Single<Balance>

    /**
     * Fetch the token balance for the hardcoded Ethereum account address in build.gradle
     *
     * @param address Contract address that is running the token
     */
    fun getTokenBalance(address: String): Single<Balance>
}

class EtherscanRepositoryImpl(
    context: Context
) : EtherscanRepository {

    /**
     * Since the account balance doesn't change that much, I decided to cache the requests
     * for 1 minute
     */
    private val api by lazy {
        val cache = Cache(context.cacheDir, (5 * 1024 * 1024).toLong())
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .header("Cache-Control", "public, max-age=${60}")
                    .build()
                chain.proceed(request)
            }
            .cache(cache)
            .build()

        Retrofit.Builder()
            .client(client)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .baseUrl("https://api.etherscan.io/")
            .build()
            .create(EtherscanApi::class.java)
    }

    override fun getBalance(): Single<Balance> {
        return api.getBalance(
            address = BuildConfig.ETH_ACCOUNT,
            apiKey = BuildConfig.ETHERSCAN_API_KEY
        )
    }

    override fun getTokenBalance(address: String): Single<Balance> {
        return api.getTokenBalance(
            address = BuildConfig.ETH_ACCOUNT,
            contractAddress = address,
            apiKey = BuildConfig.ETHERSCAN_API_KEY
        )
    }
}