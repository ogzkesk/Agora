package com.ogzkesk.agora.cache

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class DataCache<T>(
    private val default: T,
) {
    private val data: MutableStateFlow<T> = MutableStateFlow(default)

    fun stream(): StateFlow<T> = data.asStateFlow()

    fun update(block: (T) -> T) {
        data.update(block)
    }

    fun clear() {
        data.update { default }
    }
}