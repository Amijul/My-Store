package com.amijul.mystore.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amijul.mystore.data.remote.StoreFirestoreDataSource
import com.amijul.mystore.domain.home.StoreUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class HomeUiState(
    val isLoading: Boolean = false,
    val stores: List<StoreUiModel> = emptyList(),
    val errorMessage: String? = null
)
class HomeViewModel(
    private val storeRemote: StoreFirestoreDataSource = StoreFirestoreDataSource.default()
): ViewModel(){

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()


    init {
        loadStores()
    }


    fun loadStores() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)

            try {

                val stores = storeRemote.getStores()

                _uiState.value = HomeUiState(
                    isLoading = false,
                    stores = stores,
                    errorMessage = null
                )

            } catch (e: Exception) {
                _uiState.value = HomeUiState(
                    isLoading = false,
                    stores = emptyList(),
                    errorMessage = e.message ?: "Failed to load stores"
                )
            }
        }
    }


}





