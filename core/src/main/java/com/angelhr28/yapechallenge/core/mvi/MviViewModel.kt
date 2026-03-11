package com.angelhr28.yapechallenge.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ViewModel base que implementa el patron MVI (Model-View-Intent).
 *
 * @param S tipo del estado de la UI.
 * @param I tipo de las intenciones del usuario.
 * @param E tipo de los efectos secundarios.
 * @param initialState estado inicial de la UI.
 */
abstract class MviViewModel<S : UiState, I : UiIntent, E : UiEffect>(
    initialState: S
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)

    /** Flujo observable del estado actual de la UI. */
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)

    /** Flujo de efectos secundarios de un solo uso. */
    val effect = _effect.receiveAsFlow()

    /** Estado actual de la UI. */
    protected val currentState: S get() = _state.value

    /**
     * Procesa una intencion del usuario de forma asincrona.
     *
     * @param intent la intencion a procesar.
     */
    fun processIntent(intent: I) {
        viewModelScope.launch { handleIntent(intent) }
    }

    /**
     * Maneja la intencion recibida. Debe ser implementado por las subclases.
     *
     * @param intent la intencion a manejar.
     */
    protected abstract suspend fun handleIntent(intent: I)

    /**
     * Actualiza el estado de la UI aplicando una funcion reductora.
     *
     * @param reduce funcion que transforma el estado actual en un nuevo estado.
     */
    protected fun setState(reduce: S.() -> S) {
        _state.value = currentState.reduce()
    }

    /**
     * Envia un efecto secundario a la UI.
     *
     * @param effect el efecto a emitir.
     */
    protected fun sendEffect(effect: E) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
