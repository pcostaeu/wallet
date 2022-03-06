package eu.pcosta.ethereumwallet.ui.balance

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import eu.pcosta.ethereumwallet.BuildConfig
import eu.pcosta.ethereumwallet.R
import eu.pcosta.ethereumwallet.databinding.BalanceFragmentBinding
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            addressValue.text = BuildConfig.ETH_ACCOUNT
            addressContainer.setOnClickListener {
                val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(getString(R.string.account_address), BuildConfig.ETH_ACCOUNT)
                clipboard.setPrimaryClip(clip)

                Toast.makeText(requireContext(), getString(R.string.copied_clipboard), Toast.LENGTH_SHORT).show()
            }

            tokensSearchBtn.setOnClickListener {
                findNavController().navigate(R.id.action_BalanceFragment_to_SearchFragment)
            }

            val simpleDateFormat = SimpleDateFormat("h:mm:ss a", Locale.getDefault())
            val format = DecimalFormat("#.##")

            balanceViewModel.observeBalance().observe(viewLifecycleOwner) {
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
}