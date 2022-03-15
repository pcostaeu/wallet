package eu.pcosta.ethereumwallet.database

import androidx.room.*
import io.reactivex.rxjava3.core.Flowable


@Entity(tableName = "favorites")
data class FavoriteRoomEntry(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String
)

/**
 * Favorites database
 */
@Database(entities = [FavoriteRoomEntry::class], version = 1, exportSchema = false)
abstract class FavoritesDatabase : RoomDatabase() {
    abstract val dao: FavoritesDao

    @Dao
    interface FavoritesDao {

        /**
         * Add new favorite to favorites database
         */
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insert(favorite: FavoriteRoomEntry)

        /**
         * Delete favorite from favorites database
         */
        @Delete
        fun delete(favorite: FavoriteRoomEntry)

        /**
         * Observe favorites flow
         * Any change will be sent to subscribers
         */
        @Query("SELECT * FROM favorites")
        fun observeFavorites(): Flowable<List<FavoriteRoomEntry>>
    }
}

