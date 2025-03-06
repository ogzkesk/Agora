package com.ogzkesk.agora.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class ViewModel<S, E : ViewEvent>(initialState: S) : ViewModel() {

    protected val mutableState: MutableStateFlow<S> = MutableStateFlow(initialState)
    val state = mutableState.asStateFlow()

    abstract fun onEvent(event: E)

    fun updateState(block: (S) -> S) = mutableState.update(block)
    fun withState(block: S.() -> Unit) = block(state.value)
}