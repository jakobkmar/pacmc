package net.axay.pacmc.gui.util

import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.axay.pacmc.app.Environment
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.nfd.NativeFileDialog
import javax.swing.JFileChooser

object FileChooser {
    suspend fun chooseDirectory(): String? {
        kotlin.runCatching { chooseDirectoryNative() }
            .onFailure {
                Logger.w("A call to chooseDirectoryNative failed: ${it.message}")
            }
            .getOrNull()?.let { return it }

        return kotlin.runCatching { chooseDirectorySwing() }
            .onFailure {
                Logger.e("A call to chooseDirectorySwing failed ${it.message}")
            }
            .getOrNull()
    }

    private suspend fun chooseDirectoryNative() = withContext(Dispatchers.IO) {
        val pathPointer = MemoryUtil.memAllocPointer(1)
        try {
            return@withContext when (val code = NativeFileDialog.NFD_PickFolder(Environment.userHome.toString(), pathPointer)) {
                NativeFileDialog.NFD_OKAY -> {
                    val path = pathPointer.stringUTF8
                    NativeFileDialog.nNFD_Free(pathPointer[0])

                    path
                }
                NativeFileDialog.NFD_CANCEL -> null
                NativeFileDialog.NFD_ERROR -> {
                    Logger.e("An error occurred while executing NativeFileDialog.NFD_PickFolder")
                    null
                }
                else -> {
                    Logger.w("Unknown return code '${code}' from NativeFileDialog.NFD_PickFolder")
                    null
                }
            }
        } finally {
            MemoryUtil.memFree(pathPointer)
        }
    }

    private suspend fun chooseDirectorySwing() = withContext(Dispatchers.IO) {
        val chooser = JFileChooser(Environment.userHome.toString()).apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            isVisible = true
        }

        when (val code = chooser.showOpenDialog(null)) {
            JFileChooser.APPROVE_OPTION -> chooser.selectedFile.absolutePath
            JFileChooser.CANCEL_OPTION -> null
            JFileChooser.ERROR_OPTION -> {
                Logger.e("An error occurred while executing JFileChooser::showOpenDialog")
                null
            }
            else -> {
                Logger.w("Unknown return code '${code}' from JFileChooser::showOpenDialog")
                null
            }
        }
    }
}
