package eu.pcosta.ethereumwallet.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eu.pcosta.ethereumwallet.R
import eu.pcosta.ethereumwallet.databinding.TokenBalanceItemBinding
import eu.pcosta.ethereumwallet.domain.models.TokenBalance
import java.math.BigDecimal

class TokenAdapter(
    private val onClick: (token: TokenBalance, isFavorite: Boolean) -> Unit
) : ListAdapter<Pair<TokenBalance, Boolean>, TokenBalanceViewHolder>(TokenBalanceDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TokenBalanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.token_balance_item, parent, false)
        return TokenBalanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: TokenBalanceViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }
}

class TokenBalanceViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    private val binding = TokenBalanceItemBinding.bind(view)

    fun bind(item: Pair<TokenBalance, Boolean>, onClick: (token: TokenBalance, isFavorite: Boolean) -> Unit) {
        val (token, isFavorite) = item
        with(binding) {
            tokenName.text = view.context.getString(
                R.string.token_label_placeholder,
                token.name,
                token.id
            )
            tokenBalance.text = token.amount.toPlainString()
            tokenBalance.setTextColor(
                ContextCompat.getColor(
                    view.context,
                    if (token.amount > BigDecimal.ZERO) R.color.green else R.color.red
                )
            )
            tokenFavoriteImg.setImageResource(if (isFavorite) R.drawable.ic_star else R.drawable.ic_star_border)
            tokenFavoriteBtn.setOnClickListener { onClick(token, isFavorite) }
        }
    }
}

object TokenBalanceDiffCallback : DiffUtil.ItemCallback<Pair<TokenBalance, Boolean>>() {
    override fun areItemsTheSame(oldItem: Pair<TokenBalance, Boolean>, newItem: Pair<TokenBalance, Boolean>): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: Pair<TokenBalance, Boolean>,
        newItem: Pair<TokenBalance, Boolean>
    ): Boolean {
        return oldItem == newItem
    }
}
