package eu.pcosta.ethereumwallet.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.reactivex.rxjava3.core.Single
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
