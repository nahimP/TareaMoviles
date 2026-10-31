package dev.lchang.appuesan.presentation.apifootball

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import dev.lchang.appuesan.data.local.toTeam
import dev.lchang.appuesan.data.remote.apifootball.Team

@Composable
fun ApiFootballScreen(viewModel: ApiFootballViewModel = viewModel()) {
    val countries by viewModel.countries.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val teams by viewModel.teams.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.error.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val showFavorites by viewModel.showFavorites.collectAsState()

    // Set con los ids favoritos para saber rápido si un equipo lo es
    val favoriteIds = remember(favorites) { favorites.map { it.id }.toSet() }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Equipos de fútbol por país", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        // Botones para cambiar de vista
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.setShowFavorites(false) }) { Text("Equipos") }
            Button(onClick = { viewModel.setShowFavorites(true) }) { Text("Mis favoritos") }
        }

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedCountry?.name ?: "Selecciona un país")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                countries.forEach { country ->
                    DropdownMenuItem(
                        text = { Text(country.name) },
                        onClick = {
                            viewModel.onCountrySelected(country)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showFavorites) {
            // Vista "Mis favoritos": viene de Room, no de la API
            if (favorites.isEmpty()) {
                Text("Aún no tienes equipos favoritos")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(favorites, key = { it.id }) { fav ->
                        val team = fav.toTeam()
                        TeamCard(
                            team = team,
                            isFavorite = true,
                            onToggleFavorite = { viewModel.toggleFavorite(team) }
                        )
                    }
                }
            }
        } else if (isLoading) {
            CircularProgressIndicator()
        } else if (errorMessage != null) {
            Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
        } else {
            // Vista "Equipos": viene de la API
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(teams, key = { it.team.id }) { wrapper ->
                    TeamCard(
                        team = wrapper.team,
                        isFavorite = wrapper.team.id in favoriteIds,
                        onToggleFavorite = { viewModel.toggleFavorite(wrapper.team) }
                    )
                }
            }
        }
    }
}

@Composable
fun TeamCard(team: Team, isFavorite: Boolean, onToggleFavorite: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(team.logo),
                contentDescription = "Logo de ${team.name}",
                modifier = Modifier.size(50.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(team.name, style = MaterialTheme.typography.titleMedium)
                Text(team.country)
                team.founded?.let { Text(it.toString()) }
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                    tint = if (isFavorite) Color.Red else LocalContentColor.current
                )
            }
        }
    }
}