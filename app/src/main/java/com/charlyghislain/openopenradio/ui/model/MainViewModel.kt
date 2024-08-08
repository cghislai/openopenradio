package com.charlyghislain.openopenradio.ui.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewModel : ViewModel() {
    val nestedNavState = MutableStateFlow(NestedNavState())
    var nestedNavigationUpHandler: (() -> Boolean)? = null
}

data class NestedNavState(
    val isBackStackEmpty: Boolean? = null,
    val currentTitle: String? = null
)

