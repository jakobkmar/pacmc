package net.axay.pacmc.cli.terminal

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import net.axay.pacmc.app.data.ModId
import net.axay.pacmc.app.features.Archive
import net.axay.pacmc.app.repoapi.model.CommonProjectVersion
import net.axay.pacmc.app.utils.OperatingSystem

suspend fun Terminal.handleTransaction(
    headline: String,
    archive: Archive,
    transaction: Archive.Transaction,
) {
    val modStrings = transaction.resolveModStrings()

    val upSymbol = if (OperatingSystem.notWindows) "↑" else "u"
    val dep = TextColors.brightCyan("(dependency)")

    println(headline)

    transaction.add.forEach {
        println("${TextColors.brightGreen("+")} add ${modStrings[it.modId]}")
    }
    transaction.addDependencies.forEach {
        println("${TextColors.brightGreen("+")} add ${modStrings[it.modId]} $dep")
    }
    transaction.update.forEach {
        println(TextColors.brightGreen(upSymbol) + " update " + modStrings[it.modId])
    }
    transaction.updateDependencies.forEach {
        println("${TextColors.brightGreen(upSymbol)} update ${modStrings[it.modId]} $dep")
    }
    transaction.remove.forEach {
        println("${TextColors.brightRed("-")} remove ${modStrings[it]}")
    }
    transaction.removeDependencies.forEach {
        println("${TextColors.brightRed("-")} remove ${modStrings[it]} ${TextColors.brightCyan("(unused dependency)")}")
    }
    transaction.makeDependency.forEach {
        println("${TextColors.brightYellow("!")} downgrade status to dependency ${modStrings[it]} $dep")
    }

    println()
    if (!askYesOrNo("Is this okay?", default = true)) {
        println("Abort.")
        return
    }
    println()

    val downloadAnimation = DownloadAnimation()

    val semaphore = Semaphore(10)

    archive.applyTransaction(transaction, semaphore, modStrings) { progress ->
        val animationState = when (progress) {
            is Archive.TransactionProgress.Update -> DownloadAnimation.AnimationState(progress.progress)
            is Archive.TransactionProgress.Finished -> {
                val message = when (progress.result) {
                    Archive.TransactionPartResult.INSTALLED -> TextColors.brightGreen("done")
                    Archive.TransactionPartResult.ALREADY_INSTALLED -> TextColors.brightGreen("already installed")
                    Archive.TransactionPartResult.UPDATED -> TextColors.brightGreen("updated")
                    Archive.TransactionPartResult.REMOVED -> TextColors.brightYellow("removed")
                    Archive.TransactionPartResult.ALREADY_REMOVED -> TextColors.brightYellow("already removed")
                    Archive.TransactionPartResult.MADE_DEPENDENCY -> TextColors.brightYellow("made dependency")
                    Archive.TransactionPartResult.NO_PROJECT_INFO -> TextColors.brightRed("no project info")
                    Archive.TransactionPartResult.NO_FILE -> TextColors.brightRed("no downloadable file")
                    Archive.TransactionPartResult.NOT_INSTALLED -> TextColors.brightRed("not installed")
                }.let { TextStyles.bold(it) }

                val lineColor = if (!progress.result.success) TextColors.brightRed else null

                DownloadAnimation.AnimationState(1.0, message, lineColor)
            }
        }
        downloadAnimation.update(progress.key, animationState)
    }
}

private suspend fun Archive.Transaction.resolveModStrings(): Map<ModId, String> = coroutineScope {
    val returnMap = HashMap<ModId, String>()
    val addMutex = Mutex()

    fun Collection<CommonProjectVersion>.handleVersions() = forEach {
        launch {
            val string = it.optimalTerminalString()
            addMutex.withLock { returnMap[it.modId] = string }
        }
    }
    fun Collection<ModId>.handleIds() = forEach {
        launch {
            val string = it.optimalTerminalString()
            addMutex.withLock { returnMap[it] = string }
        }
    }

    add.handleVersions()
    addDependencies.handleVersions()
    update.handleVersions()
    updateDependencies.handleVersions()
    remove.handleIds()
    removeDependencies.handleIds()
    makeDependency.handleIds()

    returnMap
}
