package eu.pcosta.ethereumwallet.ui.search

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding4.widget.queryTextChanges
import eu.pcosta.ethereumwallet.R
import eu.pcosta.ethereumwallet.databinding.SearchFragmentBinding
import eu.pcosta.ethereumwallet.ui.base.BaseFragment
import eu.pcosta.ethereumwallet.ui.base.Status
import eu.pcosta.ethereumwallet.ui.base.viewBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit


class SearchFragment : BaseFragment(R.layout.search_fragment) {

    private enum class EmptyStateAnimation(val id: String) {
        SEARCH_NO_RESULTS("search_not_found.json"),
        SEARCH_EMPTY("search_keyboard.json"),
        ERROR("search_error.json"),
    }

    private val searchViewModel: SearchViewModel by viewModel()
    private val binding by viewBinding(SearchFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            NavigationUI.setupWithNavController(toolbar, findNavController())

            val adapter = TokenAdapter()

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = adapter

            searchViewModel.observeTokens().observe(viewLifecycleOwner) { response ->
                when (response.status) {
                    Status.ERROR_NO_INTERNET,
                    Status.ERROR_GENERIC -> {
                        adapter.submitList(emptyList())
                        updateAnimation(true, EmptyStateAnimation.ERROR)
                        progressIndicator.visibility = View.INVISIBLE
                    }
                    Status.LOADING -> {
                        progressIndicator.visibility = View.VISIBLE
                    }
                    Status.OK -> {
                        progressIndicator.visibility = View.INVISIBLE
                        updateAnimation(
                            response.data.isNullOrEmpty(),
                            EmptyStateAnimation.SEARCH_NO_RESULTS
                        )

                        adapter.submitList(response.data)
                    }
                }
            }

            searchView.queryTextChanges()
                .debounce(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    if (it.length < 2) {
                        updateAnimation(true, EmptyStateAnimation.SEARCH_EMPTY)
                        adapter.submitList(emptyList())
                    }
                }
                .filter { it.length >= 2 }
                .subscribe {
                    searchViewModel.onSearchChanged(it)
                }
                .bind()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.searchView.showKeyboard()
    }

    override fun onPause() {
        super.onPause()
        binding.searchView.hideKeyboard()
    }

    private fun View.showKeyboard() {
        if (requestFocus()) {
            (requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
                showSoftInput(this@showKeyboard, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    private fun View.hideKeyboard() {
        clearFocus()
        (requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            hideSoftInputFromWindow(binding.searchView.windowToken, 0)
        }
    }

    private fun updateAnimation(isVisible: Boolean, animation: EmptyStateAnimation) {
        binding.emptyState.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.emptyState.setAnimation(animation.id)
        binding.emptyState.playAnimation()
    }
}