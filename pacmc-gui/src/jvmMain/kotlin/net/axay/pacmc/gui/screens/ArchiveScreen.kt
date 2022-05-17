package net.axay.pacmc.gui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.CursorDropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.github.ajalt.colormath.model.HSL
import com.github.ajalt.colormath.model.RGBInt
import compose.icons.TablerIcons
import compose.icons.tablericons.Brush
import compose.icons.tablericons.Plant2
import compose.icons.tablericons.Tool
import io.realm.query
import io.realm.realmListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.axay.pacmc.app.database.model.DbArchive
import net.axay.pacmc.app.database.realm
import net.axay.pacmc.app.features.Archive
import net.axay.pacmc.app.repoapi.repoApiContext
import net.axay.pacmc.common.data.ContentType
import net.axay.pacmc.common.data.MinecraftVersion
import net.axay.pacmc.common.data.ModLoader
import net.axay.pacmc.gui.screens.state.IdentifierState
import net.axay.pacmc.gui.util.FileChooser
import okio.Path.Companion.toPath
import java.awt.Toolkit
import java.util.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalUnitApi::class)
@Composable
fun ArchiveScreen() = Box(Modifier.fillMaxSize()) {
    val archiveScope = rememberCoroutineScope { Dispatchers.Default }

    var archives by produceState(emptyList()) {
        value = withContext(Dispatchers.IO) {
            Archive.getArchives().toList()
        }
    } as MutableState<List<DbArchive>>

    var newArchiveDialog by remember { mutableStateOf(false) }

    if (newArchiveDialog) {
        fun randomColor() = HSL((0..360).random(), 1f, 0.75f).toSRGB()

        var displayName by remember { mutableStateOf("") }
        val identifier = remember { IdentifierState() }
        var path by remember { mutableStateOf("") }
        var color by remember { mutableStateOf(randomColor()) }

        Column(Modifier.width(800.dp).padding(horizontal = 20.dp, vertical = 10.dp).align(Alignment.TopCenter)) {
            Text("Create new archive", fontSize = TextUnit(25f, TextUnitType.Sp))

            Spacer(Modifier.height(10.dp))
            Row {
                TextField(
                    displayName,
                    onValueChange = { input ->
                        displayName = input
                        if (!identifier.wroteOnceManually) {
                            val identifierName = input.lowercase().replace(' ', '_')
                                .filter { it in IdentifierState.allowedChars }
                            identifier.set(identifierName, false)
                        }
                    },
                    label = { Text("Display Name") },
                    modifier = Modifier.weight(0.5f)
                )
                Spacer(Modifier.requiredWidth(10.dp))
                TextField(
                    identifier.value,
                    onValueChange = { identifier.set(it, true) },
                    label = { Text("Identifier") },
                    modifier = Modifier.weight(0.5f),
                    isError = identifier.isError()
                )
            }

            Spacer(Modifier.height(15.dp))
            Text(buildAnnotatedString {
                append("Choose the ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("path")
                }
                append(" where all downloaded project files will be stored")
            })
            Spacer(Modifier.height(5.dp))
            Row(Modifier.height(50.dp)) {
                TextField(
                    path,
                    onValueChange = { path = it },
                    label = { Text("Path") },
                    modifier = Modifier.weight(0.8f)
                )
                Spacer(Modifier.width(10.dp))
                OutlinedButton(
                    onClick = {
                        archiveScope.launch {
                            FileChooser.chooseDirectory()?.let { path = it }
                        }
                    },
                    modifier = Modifier.fillMaxHeight().weight(0.3f)
                ) {
                    Text("Choose directory", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
                Column(Modifier.width(200.dp)) {
                    Text("Archive type", fontWeight = FontWeight.Bold)
                    Text("Choose the type of resources you wish to install to this archive")
                }
                Spacer(Modifier.width(10.dp))
                TabRow(
                    0,
                    containerColor = Color.Unspecified,
                    modifier = Modifier.width(300.dp),
                    divider = {},
                ) {
                    Tab(true, onClick = {}) {
                        Icon(TablerIcons.Tool, "Modding Tool")
                        Text("Mods", Modifier.padding(bottom = 4.dp))
                    }
                    Tab(false, onClick = { kotlin.runCatching { Toolkit.getDefaultToolkit() }.getOrNull()?.beep() }) {
                        Icon(TablerIcons.Brush, "Texture Brush")
                        Text("Textures", Modifier.padding(bottom = 4.dp))
                    }
                    Tab(false, onClick = { kotlin.runCatching { Toolkit.getDefaultToolkit() }.getOrNull()?.beep() }) {
                        Icon(TablerIcons.Plant2, "Shader Vegetation")
                        Text("Shaders", Modifier.padding(bottom = 4.dp))
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
                Column {
                    Text("Archive color", fontWeight = FontWeight.Bold)
                    Text("Click to randomize")
                }
                Spacer(Modifier.width(10.dp))
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color.run { Color(redInt, greenInt, blueInt) }, CircleShape)
                        .border(BorderStroke(2.dp, Color.Black), CircleShape)
                        .mouseClickable {
                            color = randomColor()
                        }
                )
            }

            val minecraftVersions by produceState<List<MinecraftVersion>?>(null) {
                value = repoApiContext { it.getMinecraftReleases() }
            }

            var minecraftVersion by remember(minecraftVersions != null) { mutableStateOf(minecraftVersions?.firstOrNull()) }

            Row(modifier = Modifier.padding(10.dp)) {
                var selectMinecraftVersion by remember { mutableStateOf(false) }

                Column(Modifier.width(200.dp)) {
                    Text("Minecraft version", fontWeight = FontWeight.Bold)
                    Text("Choose a preferred minecraft version")
                }
                OutlinedButton(
                    onClick = {
                        if (!selectMinecraftVersion) selectMinecraftVersion = true
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) {
                    Text(minecraftVersion?.toString() ?: "Fetching version...", fontWeight = FontWeight.SemiBold)
                }

                if (selectMinecraftVersion) {
                    CursorDropdownMenu(
                        expanded = true,
                        onDismissRequest = { selectMinecraftVersion = false }
                    ) {
                        minecraftVersions?.forEach {
                            DropdownMenuItem(
                                onClick = {
                                    minecraftVersion = it
                                    selectMinecraftVersion = false
                                },
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(it.toString())
                            }
                        }
                    }
                }
            }

            Row(Modifier.align(Alignment.End)) {
                OutlinedButton(
                    onClick = {
                        newArchiveDialog = false
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancel")
                }
                Spacer(Modifier.width(10.dp))
                Button(
                    onClick = {
                        archiveScope.launch {
                            // TODO actual input validation
                            Archive.create(DbArchive(
                                identifier.value,
                                displayName,
                                path.toPath(),
                                minecraftVersion!!,
                                ContentType.MOD,
                                listOf(ModLoader.FABRIC),
                                realmListOf(),
                                color.toRGBInt().argb.toInt()
                            ))
                            archives = Archive.getArchives()
                        }
                        newArchiveDialog = false
                    }
                ) {
                    Text("Create")
                }
            }
        }
    } else {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Row {
                OutlinedButton(
                    onClick = {
                        newArchiveDialog = true
                    }
                ) {
                    Text("Add new archive")
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = {
                        archiveScope.launch {
                            realm.write {
                                delete(query<DbArchive>().find())
                            }
                            archives = Archive.getArchives()
                        }
                    }
                ) {
                    Text("Clear archives")
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = {
                        archiveScope.launch {
                            val uuid = UUID.randomUUID().toString()
                            Archive.create(DbArchive(
                                uuid,
                                uuid,
                                "/test/path".toPath(),
                                MinecraftVersion(1, 18, 1),
                                ContentType.MOD,
                                listOf(ModLoader.FABRIC),
                                realmListOf(),
                                (0x000000..0xFFFFFF).random()
                            ))
                            archives = Archive.getArchives()
                        }
                    }
                ) {
                    Text("Add random archive")
                }
            }

            Box {
                val listState = rememberLazyListState()

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(end = 15.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(archives) { archive ->
                        ArchiveItem(archive)
                    }
                }

                VerticalScrollbar(
                    modifier = Modifier.fillMaxHeight().width(9.dp).align(Alignment.CenterEnd),
                    style = remember { defaultScrollbarStyle().copy(shape = RectangleShape) },
                    adapter = rememberScrollbarAdapter(listState)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveItem(archive: DbArchive) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp)) {
            Box(
                Modifier
                    .size(60.dp)
                    .align(Alignment.CenterVertically)
                    .background(RGBInt(archive.color.toUInt()).toSRGB().run { Color(redInt, greenInt, blueInt) }, RoundedCornerShape(15.dp))
                    .border(BorderStroke(4.dp, Color.Black), RoundedCornerShape(15.dp))
            )
            Spacer(Modifier.width(16.dp))
            SelectionContainer {
                Column {
                    Text(archive.displayName, fontWeight = FontWeight.Bold)
                    Text(archive.name)
                    Text(archive.path)
                    Text(archive.readLoaders().joinToString())
                }
            }
        }
    }
}
