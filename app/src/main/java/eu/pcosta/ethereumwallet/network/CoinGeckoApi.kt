package eu.pcosta.ethereumwallet.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.reactivex.rxjava3.core.Single
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
