package eu.pcosta.ethereumwallet.network

import android.content.Context
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.rxjava3.core.Single
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.math.BigDecimal


@JsonClass(generateAdapter = true)
data class Price(
    @field:Json(name = "eur") val eur: BigDecimal,
    @field:Json(name = "usd") val usd: BigDecimal
)

@JsonClass(generateAdapter = true)
data class Prices(
    @field:Json(name = "ethereum") val ethereum: Price
)

object BigDecimalAdapter {
    @FromJson
    fun fromJson(string: String) = BigDecimal(string)

    @ToJson
    fun toJson(value: BigDecimal) = value.toString()
}

enum class Currency(val id: String) {
    EUR("eur"), USD("usd");
}

interface CoinValueApi {

    /**
     * Get coin prices
     *
     * @param ids Coin Ids, separated by comma
     * @param currencies Currencies Ids, separated by comma
     */
    @GET("simple/price")
    fun getCoinsPrice(
        @Query("ids") ids: String,
        @Query("vs_currencies") currencies: String
    ): Single<Prices>

}

interface CoinGeckoApiService {
    /**
     * Fetch the current ether price
     */
    fun getEtherPrice(): Single<Price>
}

class CoinGeckoApiServiceImpl(
    private val context: Context
) : CoinGeckoApiService {

    /**
     * CoinGecko specifies a 30 second cache so OkHttpClient has a small amount to space to cache the data
     */
    private val api by lazy {
        val cache = Cache(context.cacheDir, (1024 * 1024).toLong())
        val client = OkHttpClient.Builder()
            .cache(cache)
            .build()

        val jsonParser = Moshi.Builder()
            .add(BigDecimalAdapter)
            .add(KotlinJsonAdapterFactory())
            .build()

        Retrofit.Builder()
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(jsonParser))
            .baseUrl("https://api.coingecko.com/api/v3/")
            .client(client)
            .build()
            .create(CoinValueApi::class.java)
    }

    override fun getEtherPrice(): Single<Price> {
        val currencies = Currency.values().joinToString(",") { it.id }
        return api.getCoinsPrice("ethereum", currencies)
            .map { it.ethereum }
    }
}