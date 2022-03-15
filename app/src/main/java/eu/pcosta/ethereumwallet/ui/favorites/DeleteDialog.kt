package eu.pcosta.ethereumwallet.ui.favorites

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import eu.pcosta.ethereumwallet.R
import eu.pcosta.ethereumwallet.domain.models.Favorite


/**
 * Confirm the deletion of any favorite with a dialog.
 * In case of positive answer, the callback will be called
 *
 * @param favorite The favorite to check for deletion
 * @param onDelete Callback. Only called in case of confirmation
 */
fun Context.confirmFavoriteDeletion(favorite: Favorite, onDelete: () -> Unit) {
    MaterialAlertDialogBuilder(this)
        .setTitle(R.string.favorite_delete_title)
        .setMessage(getString(R.string.favorite_delete_msg, favorite.name))
        .setCancelable(true)
        .setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }
        .setPositiveButton(android.R.string.ok) { dialog, _ ->
            onDelete()
            dialog.dismiss()
        }
        .show()
}