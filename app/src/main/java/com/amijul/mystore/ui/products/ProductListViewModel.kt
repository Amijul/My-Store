package com.amijul.mystore.ui.products


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amijul.mystore.data.remote.ProductFirestoreDataSource
import com.amijul.mystore.domain.product.ProductUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductListUiState(
    val isLoading: Boolean = true,
    val products: List<ProductUiModel> = emptyList(),
    val errorMessage: String? = null
)

class ProductListViewModel(
    private val storeId: String,
    private val productRemote: ProductFirestoreDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListUiState())
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    private val _selectedProduct = MutableStateFlow<ProductUiModel?>(null)
    val selectedProduct: StateFlow<ProductUiModel?> = _selectedProduct.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = ProductListUiState(isLoading = true)

            try {
                val products = productRemote.getProductsForStore(storeId)
                _uiState.value = ProductListUiState(
                    isLoading = false,
                    products = products
                )
            } catch (e: Exception) {
                _uiState.value = ProductListUiState(
                    isLoading = false,
                    products = emptyList(),
                    errorMessage = e.message ?: "Failed to load products"
                )
            }
        }
    }

    fun selectProduct(productId: String) {
        _selectedProduct.value = _uiState.value.products.firstOrNull { it.id == productId}

    }

    fun clearSelectedProduct() {
        _selectedProduct.value = null
    }
}





