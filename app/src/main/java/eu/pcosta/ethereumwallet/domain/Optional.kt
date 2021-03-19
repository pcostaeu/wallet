package eu.pcosta.ethereumwallet.domain

import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

/**
 * Simple optional class to deal with errors in streams
 */
data class Optional<T>(
    val value: T? = null
)

fun <T> Single<T>.toOptional(): Single<Optional<T>> = this.map { Optional(it) }
fun <T> Flowable<T>.toOptional(): Flowable<Optional<T>> = this.map { Optional(it) }
