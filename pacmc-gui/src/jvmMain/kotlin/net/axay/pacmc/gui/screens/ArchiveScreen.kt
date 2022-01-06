package net.axay.pacmc.gui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.realm.objects
import io.realm.realmListOf
import kotlinx.coroutines.launch
import net.axay.pacmc.app.data.MinecraftVersion
import net.axay.pacmc.app.data.ModLoader
import net.axay.pacmc.app.database.model.DbArchive
import net.axay.pacmc.app.database.realm
import net.axay.pacmc.app.features.Archive
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArchiveScreen() = Column(Modifier.padding(horizontal = 20.dp)) {
    val archiveScope = rememberCoroutineScope()

    var archives by produceState<List<DbArchive>>(emptyList()) {
        value = Archive.getArchives()
    } as MutableState<List<DbArchive>>

    Row {
        Button(
            onClick = {
                archiveScope.launch {
                    val uuid = UUID.randomUUID().toString()
                    Archive.create(DbArchive(
                        uuid,
                        uuid,
                        "/test/path",
                        MinecraftVersion(1, 18, 1),
                        ModLoader.FABRIC,
                        realmListOf()
                    ))
                    archives = Archive.getArchives()
                }
            }
        ) {
            Text("Add new archive")
        }

        Button(
            onClick = {
                archiveScope.launch {
                    realm.write {
                        objects<DbArchive>().delete()
                    }
                    archives = Archive.getArchives()
                }
            }
        ) {
            Text("Clear archives")
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

@Composable
fun ArchiveItem(archive: DbArchive) {
    Box(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        SelectionContainer {
            Column {
                Text(archive.displayName, fontWeight = FontWeight.Bold)
                Text(archive.name)
                Text(archive.path)
                Text(archive.readLoader().displayName)
            }
        }
    }
}
