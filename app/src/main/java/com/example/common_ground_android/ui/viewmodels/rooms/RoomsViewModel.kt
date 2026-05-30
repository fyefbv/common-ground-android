package com.example.common_ground_android.ui.viewmodels.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common_ground_android.network.client.KtorClientFactory
import com.example.common_ground_android.network.model.domain.Interest
import com.example.common_ground_android.network.model.domain.Room
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.repository.AuthRepository
import com.example.common_ground_android.network.repository.InterestRepository
import com.example.common_ground_android.network.repository.RoomRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RoomFilters(
    val query: String = "",
    val interestIds: Set<String> = emptySet(),
    val tags: Set<String> = emptySet(),
    val myRooms: Boolean = false,
    val sortBy: String = "created_at",
    val sortOrder: String = "desc"
)

class RoomsViewModel(
    private val roomRepository: RoomRepository,
    private val interestRepository: InterestRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<RoomsState>(RoomsState.Loading)
    val state: StateFlow<RoomsState> = _state.asStateFlow()

    private val _shouldScrollToTop = MutableStateFlow(false)
    val shouldScrollToTop: StateFlow<Boolean> = _shouldScrollToTop.asStateFlow()

    private val _availableInterests = MutableStateFlow<List<Interest>>(emptyList())
    val availableInterests: StateFlow<List<Interest>> = _availableInterests.asStateFlow()

    private val _availableTags = MutableStateFlow<List<String>>(emptyList())
    val availableTags: StateFlow<List<String>> = _availableTags.asStateFlow()

    private val _filters = MutableStateFlow(RoomFilters())
    val filters: StateFlow<RoomFilters> = _filters.asStateFlow()

    private val _interestsMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val interestsMap: StateFlow<Map<String, String>> = _interestsMap.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            loadInterests()
            loadTags()

            loadRooms(scrollToTop = false)

            _searchQuery
                .debounce(500)
                .onEach { query ->
                    updateFilters(_filters.value.copy(query = query))
                }
                .launchIn(viewModelScope)
        }
    }

    private suspend fun loadInterests() {
        when (val result = interestRepository.getAllInterests()) {
            is NetworkResult.Success -> {
                _availableInterests.value = result.data.map { Interest.fromResponse(it) }
                _interestsMap.value = result.data.associate { it.id to it.name }
            }
            else -> {}
        }
    }

    private suspend fun loadTags() {
        when (val result = roomRepository.getAllTags()) {
            is NetworkResult.Success -> {
                _availableTags.value = result.data
            }
            else -> {}
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilters(newFilters: RoomFilters) {
        _filters.value = newFilters
        refreshRooms()
    }

    fun resetFilters() {
        _filters.value = RoomFilters()
        _searchQuery.value = ""
        refreshRooms()
    }

    fun loadRooms(scrollToTop: Boolean = false) {
        viewModelScope.launch {
            _state.value = RoomsState.Loading
            val filter = _filters.value
            val isValidQuery = filter.query.isBlank() || (filter.query.length in 2..100)
            val queryParam = if (isValidQuery) filter.query.takeIf { it.isNotBlank() } else null

            val result = roomRepository.searchRooms(
                query = queryParam,
                interestIds = filter.interestIds.toList().takeIf { it.isNotEmpty() },
                tags = filter.tags.toList().takeIf { it.isNotEmpty() },
                myRooms = filter.myRooms,
                sortBy = filter.sortBy,
                sortOrder = filter.sortOrder,
                limit = null,
                offset = 0
            )

            when (result) {
                is NetworkResult.Success -> {
                    val rooms = result.data.map { Room.fromResponse(it) }
                    _shouldScrollToTop.value = scrollToTop
                    _state.value = if (rooms.isEmpty()) RoomsState.Empty else RoomsState.Success(rooms)
                }
                is NetworkResult.Error -> {
                    _state.value = RoomsState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    fun refreshRooms() {
        loadRooms(scrollToTop = true)
    }

    fun resetScrollToTop() {
        _shouldScrollToTop.value = false
    }

    fun joinRoom(room: Room) {
        viewModelScope.launch {
            when (val result = roomRepository.joinRoom(room.id)) {
                is NetworkResult.Success -> {
                    loadRooms(scrollToTop = false)
                }
                is NetworkResult.Error -> {
                    _state.value = RoomsState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    fun leaveRoom(room: Room) {
        viewModelScope.launch {
            when (val result = roomRepository.leaveRoom(room.id)) {
                is NetworkResult.Success -> {
                    loadRooms(scrollToTop = false)
                }
                is NetworkResult.Error -> {
                    _state.value = RoomsState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    fun openRoom(room: Room) { }

    fun getCurrentProfileId(): String {
        return try {
            KtorClientFactory.getTokenManager().getProfileIdSync() ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun clearTokensAndLogout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}