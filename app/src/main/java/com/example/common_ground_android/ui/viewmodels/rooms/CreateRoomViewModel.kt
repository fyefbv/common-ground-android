package com.example.common_ground_android.ui.viewmodels.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common_ground_android.network.model.domain.Interest
import com.example.common_ground_android.network.model.domain.Room
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.repository.AuthRepository
import com.example.common_ground_android.network.repository.InterestRepository
import com.example.common_ground_android.network.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateRoomViewModel(
    private val roomRepository: RoomRepository,
    private val interestRepository: InterestRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<CreateRoomState>(CreateRoomState.Idle)
    val state: StateFlow<CreateRoomState> = _state.asStateFlow()

    private val _availableInterests = MutableStateFlow<List<Interest>>(emptyList())
    val availableInterests: StateFlow<List<Interest>> = _availableInterests.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()
    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()
    private val _descriptionError = MutableStateFlow<String?>(null)
    val descriptionError: StateFlow<String?> = _descriptionError.asStateFlow()

    private val _primaryInterestId = MutableStateFlow<String?>(null)
    val primaryInterestId: StateFlow<String?> = _primaryInterestId.asStateFlow()

    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    private val _maxParticipants = MutableStateFlow(50)
    val maxParticipants: StateFlow<Int> = _maxParticipants.asStateFlow()

    private val _isPrivate = MutableStateFlow(false)
    val isPrivate: StateFlow<Boolean> = _isPrivate.asStateFlow()

    init {
        loadInterests()
    }

    private fun loadInterests() {
        viewModelScope.launch {
            when (val result = interestRepository.getAllInterests()) {
                is NetworkResult.Success -> {
                    _availableInterests.value = result.data.map { Interest.fromResponse(it) }
                }
                else -> {}
            }
        }
    }

    fun updateName(name: String) {
        _name.value = name
        _nameError.value = when {
            name.length < 3 -> "Название должно содержать минимум 3 символа"
            name.length > 100 -> "Название не должно превышать 100 символов"
            else -> null
        }
    }

    fun updateDescription(description: String) {
        _description.value = description
        _descriptionError.value = if (description.length > 1000) "Описание не должно превышать 1000 символов" else null
    }

    fun updatePrimaryInterest(interestId: String?) {
        _primaryInterestId.value = interestId
    }

    fun addTag(tag: String) {
        val trimmed = tag.trim()
        when {
            trimmed.isEmpty() -> return
            _tags.value.size >= 10 -> {
                _state.value = CreateRoomState.Error("Максимум 10 тегов")
                return
            }
            trimmed.length > 50 -> {
                _state.value = CreateRoomState.Error("Тег не может превышать 50 символов")
                return
            }
            _tags.value.contains(trimmed) -> return
            else -> {
                _tags.update { it + trimmed }
            }
        }
    }

    fun removeTag(tag: String) {
        _tags.update { it - tag }
    }

    fun updateMaxParticipants(value: Int) {
        _maxParticipants.value = value
    }

    fun updateIsPrivate(isPrivate: Boolean) {
        _isPrivate.value = isPrivate
    }

    private fun getValidationErrors(): List<String> {
        val errors = mutableListOf<String>()
        _nameError.value?.let { errors.add("Поле 'Название': $it") }
        _descriptionError.value?.let { errors.add("Поле 'Описание': $it") }
        if (_name.value.isBlank()) errors.add("Поле 'Название': Введите название комнаты")
        return errors
    }

    fun createRoom() {
        updateName(_name.value)
        updateDescription(_description.value)

        val errors = getValidationErrors()
        if (errors.isNotEmpty()) {
            _state.value = CreateRoomState.Error(errors.joinToString("\n"))
            return
        }

        viewModelScope.launch {
            _state.value = CreateRoomState.Loading
            val result = roomRepository.createRoom(
                name = _name.value.trim(),
                description = _description.value.trim().takeIf { it.isNotEmpty() },
                primaryInterestId = _primaryInterestId.value,
                tags = _tags.value,
                maxParticipants = _maxParticipants.value,
                isPrivate = _isPrivate.value
            )
            when (result) {
                is NetworkResult.Success -> {
                    val room = Room.fromResponse(result.data)
                    _state.value = CreateRoomState.Success(room)
                }
                is NetworkResult.Error -> {
                    _state.value = CreateRoomState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    fun resetState() {
        _state.value = CreateRoomState.Idle
    }

    fun clearTokensAndLogout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}