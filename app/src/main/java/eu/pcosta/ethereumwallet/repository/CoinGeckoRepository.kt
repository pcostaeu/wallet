package eu.pcosta.ethereumwallet.repository

import android.content.Context
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import eu.pcosta.ethereumwallet.network.CoinValueApi
import eu.pcosta.ethereumwallet.network.Price
import io.reactivex.rxjava3.core.Single
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.math.BigDecimal


object BigDecimalAdapter {
    @FromJson
    fun fromJson(string: String) = BigDecimal(string)

    @ToJson
    fun toJson(value: BigDecimal) = value.toString()
}

enum class Currency(val id: String) {
    EUR("eur"), USD("usd");
}

interface CoinGeckoRepository {
    /**
     * Fetch the current ether price
     */
    fun getEtherPrice(): Single<Price>
}

class CoinGeckoRepositoryImpl(
    private val context: Context
) : CoinGeckoRepository {

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