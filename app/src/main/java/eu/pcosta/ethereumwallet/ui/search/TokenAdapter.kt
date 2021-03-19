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
import eu.pcosta.ethereumwallet.domain.TokenBalance
import java.math.BigDecimal

class TokenAdapter : ListAdapter<TokenBalance, TokenBalanceViewHolder>(TokenBalanceDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TokenBalanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.token_balance_item, parent, false)
        return TokenBalanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: TokenBalanceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class TokenBalanceViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    private val binding = TokenBalanceItemBinding.bind(view)

    fun bind(tokenBalance: TokenBalance) {
        binding.tokenName.text = view.context.getString(
            R.string.token_label_placeholder,
            tokenBalance.name,
            tokenBalance.id
        )
        binding.tokenBalance.text = tokenBalance.amount.toPlainString()
        binding.tokenBalance.setTextColor(ContextCompat.getColor(view.context, if (tokenBalance.amount > BigDecimal.ZERO) R.color.green else R.color.red))
    }
}

object TokenBalanceDiffCallback : DiffUtil.ItemCallback<TokenBalance>() {
    override fun areItemsTheSame(oldItem: TokenBalance, newItem: TokenBalance): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: TokenBalance, newItem: TokenBalance): Boolean {
        return oldItem == newItem
    }
}
