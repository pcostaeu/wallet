package eu.pcosta.ethereumwallet.domain

import eu.pcosta.ethereumwallet.database.FavoriteRoomEntry
import eu.pcosta.ethereumwallet.database.FavoritesDatabase
import eu.pcosta.ethereumwallet.domain.models.Favorite
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable


interface FavoritesService {

    /**
     * Observe changes to Favorites table. Start's with an event as soon
     * someone subscribes
     */
    fun observeFavorites(): Flowable<List<Favorite>>

    /**
     * Add favorite to database
     *
     * @param favorite Domain favorite object
     */
    fun addFavorite(favorite: Favorite): Completable

    /**
     * Remove favorite from database
     *
     * @param favorite Domain favorite object
     */
    fun removeFavorite(favorite: Favorite): Completable
}

class FavoritesServiceImpl(
    private val favoritesDao: FavoritesDatabase.FavoritesDao
) : FavoritesService {

    override fun observeFavorites(): Flowable<List<Favorite>> {
        return favoritesDao.observeFavorites()
            .map { list ->
                list.map { it.asDomain() }
            }
    }

    override fun addFavorite(favorite: Favorite): Completable {
        return Completable.fromCallable {
            favoritesDao.insert(favorite.toRoomEntry())
        }
    }

    override fun removeFavorite(favorite: Favorite): Completable {
        return Completable.fromCallable {
            favoritesDao.delete(favorite.toRoomEntry())
        }
    }

    private fun FavoriteRoomEntry.asDomain() = Favorite(id, name)
    private fun Favorite.toRoomEntry() = FavoriteRoomEntry(id, name)
}

