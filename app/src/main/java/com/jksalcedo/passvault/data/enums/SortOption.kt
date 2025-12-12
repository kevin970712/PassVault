package com.jksalcedo.passvault.data.enums

enum class SortOption {
    NAME_ASC,
    NAME_DESC,
    DATE_CREATED_DESC,
    DATE_CREATED_ASC,
    DATE_MODIFIED_DESC,
    DATE_MODIFIED_ASC,
    CATEGORY_ASC;

    companion object {
        fun fromString(value: String?): SortOption {
            return try {
                valueOf(value ?: "NAME_ASC")
            } catch (_: IllegalArgumentException) {
                NAME_ASC
            }
        }
    }
}
