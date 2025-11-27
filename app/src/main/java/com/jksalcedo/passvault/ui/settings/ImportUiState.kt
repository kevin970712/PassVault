package com.jksalcedo.passvault.ui.settings

/**
 * Represents the state of the import UI.
 */
sealed class ImportUiState {
    /**
     * The initial state of the import UI.
     */
    object Idle : ImportUiState()
    /**
     * The state of the import UI when it is loading.
     */
    object Loading : ImportUiState()
    /**
     * The state of the import UI when the import is successful.
     * @param count The number of entries imported.
     */
    data class Success(val count: Int) : ImportUiState()
    /**
     * The state of the import UI when an error occurs.
     * @param exception The exception that occurred.
     */
    data class Error(val exception: Throwable) : ImportUiState()
}