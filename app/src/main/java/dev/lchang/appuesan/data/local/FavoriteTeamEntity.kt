package dev.lchang.appuesan.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.lchang.appuesan.data.remote.apifootball.Team

@Entity(tableName = "favorite_teams")
data class FavoriteTeamEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val code: String?,
    val logo: String,
    val country: String,
    val founded: Int?,
    val national: Boolean
)

// Convertir un Team de la API a una fila de la tabla
fun Team.toEntity() = FavoriteTeamEntity(id, name, code, logo, country, founded, national)

// Convertir una fila de la tabla a Team, para reutilizar la misma tarjeta en la UI
fun FavoriteTeamEntity.toTeam() = Team(id, name, code, logo, country, founded, national)