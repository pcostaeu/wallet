package eu.pcosta.ethereumwallet.database

import androidx.room.*
import io.reactivex.rxjava3.core.Single

@Entity(tableName = "tokens")
data class TokenRoomEntry(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "address") val address: String,
    @ColumnInfo(name = "decimals") val decimals: Int
)

/**
 * Top Tokens database. It's meant to cache the top tokens for each app session
 */
@Database(entities = [TokenRoomEntry::class], version = 1, exportSchema = false)
abstract class TokensDatabase : RoomDatabase() {
    abstract val dao: TokensDao

    @Dao
    interface TokensDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insert(list: List<TokenRoomEntry>)

        /**
         * Clear all rows from the tokens table
         */
        @Query("DELETE FROM tokens")
        fun clear()

        /**
         * Search for matching IDs. The query
         */
        @Query("SELECT * FROM tokens WHERE id LIKE :id")
        fun search(id: String): Single<List<TokenRoomEntry>>

        @Query("SELECT COUNT(*) FROM tokens")
        fun count(): Single<Int>
    }
}

