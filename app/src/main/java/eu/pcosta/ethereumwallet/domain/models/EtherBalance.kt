package eu.pcosta.ethereumwallet.domain.models

import java.math.BigDecimal


/**
 * Current Ether Balance with the price associated
 */
data class EtherBalance(
    val amount: BigDecimal?,
    val price: EtherPrice?
)