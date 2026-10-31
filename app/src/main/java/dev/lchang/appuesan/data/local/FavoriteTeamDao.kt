package dev.lchang.appuesan.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteTeamDao {

    @Query("SELECT * FROM favorite_teams ORDER BY name")
    fun getAll(): Flow<List<FavoriteTeamEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_teams WHERE id = :id)")
    suspend fun isFavorite(id: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(team: FavoriteTeamEntity)

    @Query("DELETE FROM favorite_teams WHERE id = :id")
    suspend fun deleteById(id: Int)
}