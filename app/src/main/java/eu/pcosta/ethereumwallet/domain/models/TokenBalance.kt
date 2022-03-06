package eu.pcosta.ethereumwallet.domain.models

import java.math.BigDecimal


/**
 * Current Token Balance with id and name associated
 */
data class TokenBalance(
    val id: String,
    val name: String,
    val amount: BigDecimal
)