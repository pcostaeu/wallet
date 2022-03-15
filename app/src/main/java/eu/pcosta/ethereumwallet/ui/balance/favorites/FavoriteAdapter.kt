package eu.pcosta.ethereumwallet.ui.balance.favorites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eu.pcosta.ethereumwallet.R
import eu.pcosta.ethereumwallet.databinding.FavoriteItemBinding
import eu.pcosta.ethereumwallet.domain.models.Favorite

class FavoriteAdapter(
    private val onClick: (favorite: Favorite) -> Unit
) : ListAdapter<Favorite, FavoriteViewHolder>(FavoriteDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.favorite_item, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }
}

class FavoriteViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    private val binding = FavoriteItemBinding.bind(view)

    fun bind(favorite: Favorite, onClick: (favorite: Favorite) -> Unit) {
        with(binding) {
            favoriteName.text = view.context.getString(R.string.favorite_label_placeholder, favorite.name, favorite.id)
            favoriteDeleteBtn.setOnClickListener { onClick(favorite) }
        }
    }
}

object FavoriteDiffCallback : DiffUtil.ItemCallback<Favorite>() {
    override fun areItemsTheSame(oldItem: Favorite, newItem: Favorite): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Favorite, newItem: Favorite): Boolean {
        return oldItem == newItem
    }
}
