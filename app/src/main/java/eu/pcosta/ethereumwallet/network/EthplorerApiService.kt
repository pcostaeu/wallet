package eu.pcosta.ethereumwallet.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import eu.pcosta.ethereumwallet.BuildConfig
import io.reactivex.rxjava3.core.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class Token(
    @field:Json(name = "symbol") val id: String,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "address") val address: String,
    @field:Json(name = "decimals") val decimals: String
)

@JsonClass(generateAdapter = true)
data class TokensList(
    @field:Json(name = "tokens") val tokens: List<Token>
)

interface EthplorerApi {

    @GET("getTopTokens")
    fun getTopTokens(
        @Query("limit") limit: Int,
        @Query("apiKey") apiKey: String,
    ): Single<TokensList>
}

interface EthplorerApiService {
    /**
     * Get current 100 top tokens
     *
     * Note: for some reason, using the free key, Ethplorer is only returning 50 tokens
     */
    fun getTopTokens(): Single<List<Token>>
}

class EthplorerApiServiceImpl : EthplorerApiService {
    private val api by lazy {
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .baseUrl("https://api.ethplorer.io/")
            .build()
            .create(EthplorerApi::class.java)
    }

    override fun getTopTokens(): Single<List<Token>> {
        return api.getTopTokens(100, BuildConfig.ETHPLORER_API_KEY).map { it.tokens }
    }
}