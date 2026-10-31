package dev.lchang.appuesan.presentation.apifootball

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.lchang.appuesan.data.local.AppDatabase
import dev.lchang.appuesan.data.local.FavoriteTeamEntity
import dev.lchang.appuesan.data.local.toEntity
import dev.lchang.appuesan.data.remote.apifootball.Country
import dev.lchang.appuesan.data.remote.apifootball.RetrofitInstance
import dev.lchang.appuesan.data.remote.apifootball.Team
import dev.lchang.appuesan.data.remote.apifootball.TeamWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ApiFootballViewModel(application: Application) : AndroidViewModel(application) {

    // --- Datos de la API (igual que antes) ---
    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    val countries: StateFlow<List<Country>> = _countries
    private val _selectedCountry = MutableStateFlow<Country?>(null)
    val selectedCountry: StateFlow<Country?> = _selectedCountry
    private val _teams = MutableStateFlow<List<TeamWrapper>>(emptyList())
    val teams: StateFlow<List<TeamWrapper>> = _teams
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _errorMessages = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _errorMessages

    // --- Favoritos (Room) ---
    private val favoriteDao = AppDatabase.getInstance(application).favoriteTeamDao()

    val favorites: StateFlow<List<FavoriteTeamEntity>> = favoriteDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _showFavorites = MutableStateFlow(false)
    val showFavorites: StateFlow<Boolean> = _showFavorites

    init {
        loadCountries()
    }

    fun setShowFavorites(value: Boolean) {
        _showFavorites.value = value
    }

    fun toggleFavorite(team: Team) {
        viewModelScope.launch {
            if (favoriteDao.isFavorite(team.id)) {
                favoriteDao.deleteById(team.id)
            } else {
                favoriteDao.insert(team.toEntity())
            }
        }
    }

    fun loadCountries() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.getCountries()
                _countries.value = response.response
            } catch (e: Exception) {
                _errorMessages.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTeamsByCountry(country: Country) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.getTeamsByCountry(country.name)
                _teams.value = response.response
                _errorMessages.value = null
                _selectedCountry.value = country
            } catch (e: Exception) {
                _errorMessages.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onCountrySelected(country: Country) {
        _selectedCountry.value = country
        _showFavorites.value = false   // al elegir país, vuelve a la vista "Equipos"
        loadTeamsByCountry(country)
    }
}