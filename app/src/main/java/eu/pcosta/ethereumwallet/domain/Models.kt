package eu.pcosta.ethereumwallet.domain

import java.math.BigDecimal

/**
 * Domain data classes. They represent the things in the app. These are the
 * objects that should be displayed on screen, or manipulated by the app
 */

/**
 * Has the prices values for the currencies supported by the UI
 */
data class EtherPrice(
    val eur: BigDecimal,
    val usd: BigDecimal
)

/**
 * Current Ether Balance with the price associated
 */
data class EtherBalance(
    val amount: BigDecimal?,
    val price: EtherPrice?
)

/**
 * Current Token Balance with id and name associated
 */
data class TokenBalance(
    val id: String,
    val name: String,
    val amount: BigDecimal
)