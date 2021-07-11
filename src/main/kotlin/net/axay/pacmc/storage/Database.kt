package net.axay.pacmc.storage

import net.axay.pacmc.Values
import org.kodein.db.DB
import org.kodein.db.impl.open
import java.io.File

val db = DB.open(File(Values.projectDirectories.dataLocalDir, "/db1").canonicalPath)
