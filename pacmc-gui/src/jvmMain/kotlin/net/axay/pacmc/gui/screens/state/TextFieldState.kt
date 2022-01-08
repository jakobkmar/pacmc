package net.axay.pacmc.gui.screens.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

open class TextFieldState {
    var value by mutableStateOf("")
        private set
    var wroteOnce by mutableStateOf(false)
        private set
    var wroteOnceManually by mutableStateOf(false)
        private set

    fun set(value: String, manually: Boolean) {
        this.value = value
        if (!wroteOnce) {
            wroteOnce = true
        }
        if (manually && !wroteOnceManually) {
            wroteOnceManually = true
        }
    }

    fun isError() = if (wroteOnce) !isValid() else false

    open fun isValid() = value.isNotEmpty()
}

class IdentifierState : TextFieldState() {
    companion object {
        val allowedChars = ('a'..'z') + ('A'..'Z') + ('0'..'9') + listOf('_', '-')
    }

    override fun isValid() = super.isValid() && value.all { it in allowedChars }
}
