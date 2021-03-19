package eu.pcosta.ethereumwallet.network

import android.content.Context
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import eu.pcosta.ethereumwallet.BuildConfig
import io.reactivex.rxjava3.core.Single
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


@JsonClass(generateAdapter = true)
data class Balance(
    @field:Json(name = "result") val balance: String,
    @field:Json(name = "status") val status: String,
    @field:Json(name = "message") val message: String,
)

interface EtherscanApi {
    @GET("api?module=account&action=balance&tag=latest")
    fun getBalance(
        @Query("address") address: String,
        @Query("apikey") apiKey: String,
    ): Single<Balance>

    @GET("api?module=account&action=tokenbalance&tag=latest")
    fun getTokenBalance(
        @Query("address") address: String,
        @Query("contractaddress") contractAddress: String,
        @Query("apikey") apiKey: String,
    ): Single<Balance>
}

interface EtherscanApiService {

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

class EtherscanApiServiceImpl(
    context: Context
) : EtherscanApiService {

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