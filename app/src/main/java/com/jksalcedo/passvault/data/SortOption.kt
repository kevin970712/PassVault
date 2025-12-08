package com.jksalcedo.passvault.data

/**
 * Enum representing different sorting options for password entries.
 */
enum class SortOption {
    NAME_ASC,
    NAME_DESC,
    DATE_CREATED_DESC,
    DATE_CREATED_ASC,
    DATE_MODIFIED_DESC,
    DATE_MODIFIED_ASC,
    CATEGORY_ASC;

    companion object {
        /**
         * Gets a SortOption from its string name, with a fallback default.
         */
        fun fromString(value: String?): SortOption {
            return try {
                valueOf(value ?: "NAME_ASC")
            } catch (_: IllegalArgumentException) {
                NAME_ASC
            }
        }
    }
}
