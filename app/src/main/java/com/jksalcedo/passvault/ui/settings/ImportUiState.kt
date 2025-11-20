package com.jksalcedo.passvault.ui.settings

sealed class ImportUiState {
    object Idle : ImportUiState()
    object Loading : ImportUiState()
    data class Success(val count: Int) : ImportUiState()
    data class Error(val exception: Throwable) : ImportUiState()
}