package com.example.fitnessrun

import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task

/** Keeps a single operation and its result across rotations without retaining an Activity. */
class ProfileRequestViewModel : ViewModel() {
    sealed interface State {
        data object Idle : State
        data object Loading : State
        data class Ready(val profile: CloudProfile) : State
        data class Failed(val error: Exception) : State
    }
    var state: State = State.Idle
        private set
    var listener: ((State) -> Unit)? = null

    fun execute(operation: () -> Task<CloudProfile>) {
        if (state is State.Loading) return
        publish(State.Loading)
        try {
            operation().addOnCompleteListener { result ->
                publish(if (result.isSuccessful) State.Ready(result.result)
                    else State.Failed(result.exception ?: IllegalStateException()))
            }
        } catch (error: Exception) { publish(State.Failed(error)) }
    }

    fun consume() { state = State.Idle }
    private fun publish(value: State) { state = value; listener?.invoke(value) }
    override fun onCleared() { listener = null }
}
