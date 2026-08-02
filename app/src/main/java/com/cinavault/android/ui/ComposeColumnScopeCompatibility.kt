package com.cinavault.android.ui

import androidx.compose.foundation.layout.ColumnScope

/**
 * Compatibility classifier for legacy composable slot declarations that used
 * `Column.() -> Unit` while the callable `Column(...)` remains imported from
 * Compose. Kotlin keeps classifier and callable namespaces separate, so this
 * alias supplies the intended receiver type without changing rendered UI.
 */
typealias Column = ColumnScope
