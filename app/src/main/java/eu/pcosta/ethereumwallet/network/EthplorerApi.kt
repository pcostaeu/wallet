package eu.pcosta.ethereumwallet.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.reactivex.rxjava3.core.Single
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
        @Query("apiKey") apiKey: String,
    ): Single<TokensList>
}
