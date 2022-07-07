package eu.pcosta.ethereumwallet.domain.models


/**
 * Favorite Domain object
 * Can hold other data, i.e., amount at the user address, current price, etc.
 */
data class Favorite(
    val id: String,
    val name: String
)
