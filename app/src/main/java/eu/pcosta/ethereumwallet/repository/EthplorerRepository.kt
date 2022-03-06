package eu.pcosta.ethereumwallet.repository

import eu.pcosta.ethereumwallet.BuildConfig
import eu.pcosta.ethereumwallet.network.EthplorerApi
import eu.pcosta.ethereumwallet.network.Token
import io.reactivex.rxjava3.core.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory


interface EthplorerRepository {
    /**
     * Get top 50 most active tokens for the last 30 days
     */
    fun getTopTokens(): Single<List<Token>>
}

class EthplorerRepositoryImpl : EthplorerRepository {

    private val api by lazy {
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .baseUrl("https://api.ethplorer.io/")
            .build()
            .create(EthplorerApi::class.java)
    }

    override fun getTopTokens(): Single<List<Token>> {
        return api.getTopTokens(BuildConfig.ETHPLORER_API_KEY).map { it.tokens }
    }
}