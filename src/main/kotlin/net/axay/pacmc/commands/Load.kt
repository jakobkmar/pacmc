package net.axay.pacmc.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.rendering.TextStyles.underline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.axay.pacmc.logging.awaitConfirmation
import net.axay.pacmc.logging.printArchive
import net.axay.pacmc.storage.data.DbMod
import net.axay.pacmc.storage.db
import net.axay.pacmc.storage.execAsyncBatch
import net.axay.pacmc.storage.getArchiveMods
import net.axay.pacmc.storage.getArchiveOrWarn
import net.axay.pacmc.terminal
import org.kodein.db.deleteAll
import org.kodein.db.find

object Load : CliktCommand(
    "Loads one archive into another"
) {
    private val targetArchiveName by option("-a", "--target-archive", help = "The name of the archive which you want to load the mods into").default(".minecraft")

    private val sourceArchiveName by argument("archive", "The name of the archive which you want to load")

    override fun run() = runBlocking(Dispatchers.Default) {
        terminal.println("Loading the mods of '$sourceArchiveName' into '$targetArchiveName'")
        terminal.println()

        val targetArchive = db.getArchiveOrWarn(targetArchiveName) ?: return@runBlocking
        val sourceArchive = db.getArchiveOrWarn(sourceArchiveName) ?: return@runBlocking

        terminal.println("This will override all the mods in the following archive:")
        terminal.printArchive(targetArchive)
        terminal.print("Are you sure?")
        if (!awaitConfirmation()) {
            terminal.println()
            terminal.println("Abort.")
            return@runBlocking
        }

        val copiedModsPrinter = ArrayList<() -> Unit>()

        terminal.println()
        terminal.println("Updating the database...")
        val dbJob = db.execAsyncBatch {
            val sourceMods = db.getArchiveMods(sourceArchiveName)

            deleteAll(db.find<DbMod>().byIndex("archive", targetArchiveName))

            sourceMods.forEach { copyMod ->
                put(copyMod.copy(archive = targetArchiveName))
                copiedModsPrinter += {
                    terminal.println("Copied ${bold("${copyMod.repository}/${underline(copyMod.name)}")}")
                }
            }
        }

        targetArchive.javaFiles.forEach { it.delete() }
        terminal.println("Deleted old files")

        sourceArchive.javaFiles.forEach { it.copyTo(targetArchive.directory.resolve(it.name)) }

        dbJob.join()
        if (copiedModsPrinter.isNotEmpty()) {
            terminal.println()
            copiedModsPrinter.forEach { it() }
        }

        terminal.println()
        terminal.success("The archive '$targetArchiveName' now has the same content as '$sourceArchiveName'")
    }
}
