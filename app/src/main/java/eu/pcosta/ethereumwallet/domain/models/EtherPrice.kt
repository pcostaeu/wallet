package eu.pcosta.ethereumwallet.domain.models

import java.math.BigDecimal


/**
 * Has the prices values for the currencies supported by the UI
 */
data class EtherPrice(
    val eur: BigDecimal,
    val usd: BigDecimal
)