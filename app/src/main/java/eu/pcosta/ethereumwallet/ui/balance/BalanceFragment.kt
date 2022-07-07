package eu.pcosta.ethereumwallet.ui.balance

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import eu.pcosta.ethereumwallet.BuildConfig
import eu.pcosta.ethereumwallet.R
import eu.pcosta.ethereumwallet.databinding.BalanceFragmentBinding
import eu.pcosta.ethereumwallet.ui.balance.favorites.FavoriteAdapter
import eu.pcosta.ethereumwallet.ui.balance.favorites.confirmFavoriteDeletion
import eu.pcosta.ethereumwallet.ui.base.BaseFragment
import eu.pcosta.ethereumwallet.ui.base.Status
import eu.pcosta.ethereumwallet.ui.base.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class BalanceFragment : BaseFragment(R.layout.balance_fragment) {

    private val balanceViewModel: BalanceViewModel by viewModel()
    private val binding by viewBinding(BalanceFragmentBinding::bind)

    private lateinit var adapter: FavoriteAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupBalanceObserver()
        setupFavoriteObserver()
    }

    private fun setupViews() {
        with(binding) {
            // ETH Balance and Value
            addressValue.text = BuildConfig.ETH_ACCOUNT
            addressContainer.setOnClickListener {
                val clipboard: ClipboardManager =
                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(getString(R.string.account_address), BuildConfig.ETH_ACCOUNT)
                clipboard.setPrimaryClip(clip)

                Toast.makeText(requireContext(), getString(R.string.copied_clipboard), Toast.LENGTH_SHORT).show()
            }

            // Search Button
            tokensSearchBtn.setOnClickListener {
                findNavController().navigate(R.id.action_BalanceFragment_to_SearchFragment)
            }

            // Favorites list
            adapter = FavoriteAdapter {
                requireContext().confirmFavoriteDeletion(it) {
                    balanceViewModel.deleteFavorite(it)
                }
            }
            favoritesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            favoritesRecyclerView.setHasFixedSize(true)
            favoritesRecyclerView.adapter = adapter
        }
    }

    /**
     * Observe balance updates. Update view according to state sent from flow
     */
    private fun setupBalanceObserver() {
        val simpleDateFormat = SimpleDateFormat("h:mm:ss a", Locale.getDefault())
        val format = DecimalFormat("#.##")

        balanceViewModel.observeBalance().observe(viewLifecycleOwner) {
            with(binding) {
                when (it.status) {
                    Status.ERROR_GENERIC -> {
                        balanceErrorUpdate.text = getString(R.string.error_generic_message)
                        balanceErrorUpdate.visibility = View.VISIBLE
                        progressIndicator.visibility = View.GONE
                    }
                    Status.ERROR_NO_INTERNET -> {
                        balanceErrorUpdate.text = getString(R.string.error_internet_message)
                        balanceErrorUpdate.visibility = View.VISIBLE
                        progressIndicator.visibility = View.GONE
                    }
                    Status.LOADING -> {
                        progressIndicator.visibility = View.VISIBLE
                    }
                    Status.OK -> {
                        balanceErrorUpdate.visibility = View.GONE
                        progressIndicator.visibility = View.GONE

                        it.data?.let { balance ->
                            balanceAmount.visibility = View.VISIBLE
                            balanceAmount.text = balance.amount?.let {
                                getString(R.string.eth_amount_placeholder, balance.amount.toString())
                            } ?: "-"

                            balanceValue.visibility = View.VISIBLE
                            balanceValue.text = if (balance.amount != null && balance.price != null) {
                                val accountValueUsd = format.format(balance.amount * balance.price.usd)
                                val accountValueEur = format.format(balance.amount * balance.price.eur)

                                getString(R.string.eth_value_placeholder, accountValueUsd, accountValueEur)
                            } else {
                                "- / -"
                            }
                        }

                        balanceLastUpdate.visibility = View.VISIBLE
                        balanceLastUpdate.text = getString(
                            R.string.last_update_placeholder,
                            simpleDateFormat.format(it.timestamp)
                        )

                    }
                }
            }
        }
    }

    private fun setupFavoriteObserver() {
        balanceViewModel.observeFavorites().observe(viewLifecycleOwner) {
            with(binding) {
                when (it.status) {
                    Status.LOADING -> {
                        favoritesEmptyError.isVisible = false
                        favoritesRecyclerView.isVisible = false
                    }
                    Status.OK -> {
                        favoritesEmptyError.isVisible = it.data.isNullOrEmpty()
                        favoritesRecyclerView.isVisible = !it.data.isNullOrEmpty()
                        adapter.submitList(it.data)
                    }
                    else -> throw Throwable("Invalid status")
                }
            }
        }
    }
}