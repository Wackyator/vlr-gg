package dev.unusedvariable.vlr.ui.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.statusBarsPadding
import dev.unusedvariable.vlr.data.api.response.TournamentInfo
import dev.unusedvariable.vlr.ui.CARD_ALPHA
import dev.unusedvariable.vlr.ui.VlrViewModel
import dev.unusedvariable.vlr.ui.theme.VLRTheme
import dev.unusedvariable.vlr.utils.Waiting
import dev.unusedvariable.vlr.utils.onFail
import dev.unusedvariable.vlr.utils.onPass
import dev.unusedvariable.vlr.utils.onWaiting

@Composable
fun EventScreen(viewModel: VlrViewModel) {

    val allTournaments by remember(viewModel) {
        viewModel.getTournaments()
    }.collectAsState(initial = Waiting())

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .statusBarsPadding()
        )

        allTournaments.onPass {
            data?.let { list -> TournamentPreviewContainer(viewModel = viewModel, list = list) }
        }.onWaiting {
            LinearProgressIndicator()
        }.onFail {
            Text(text = message())
        }
    }
}

@Composable
fun TournamentPreviewContainer(viewModel: VlrViewModel, list: List<TournamentInfo.TournamentPreview>) {
    var tabPosition by remember(viewModel) { mutableStateOf(0) }
    val (ongoing, upcoming, completed) = list.groupBy { it.status.startsWith("ongoing", ignoreCase = true) }.let {
        Triple(
            it[true].orEmpty(),
            it[false]?.groupBy { it.status.startsWith("upcoming", ignoreCase = true) }?.get(true).orEmpty(),
            it[false]?.groupBy { it.status.startsWith("upcoming", ignoreCase = true) }?.get(false).orEmpty()
        )
    }
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tabPosition, backgroundColor = VLRTheme.colorScheme.primaryContainer) {
            Tab(selected = tabPosition == 0, onClick = { tabPosition = 0 }) {
                Text(text = "Ongoing", modifier = Modifier.padding(16.dp))
            }
            Tab(selected = tabPosition == 1, onClick = { tabPosition = 1 }) {
                Text(text = "Upcoming", modifier = Modifier.padding(16.dp))
            }
            Tab(selected = tabPosition == 2, onClick = { tabPosition = 2 }) {
                Text(text = "Completed", modifier = Modifier.padding(16.dp))
            }
        }
        when (tabPosition) {
            0 -> {
                if (ongoing.isEmpty()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "No ongoing events")
                    Spacer(modifier = Modifier.weight(1f))
                } else {
                    LazyColumn() {
                        items(ongoing) {
                            TournamentPreview(tournamentPreview = it)
                        }
                    }
                }
            }
            1 -> {
                if (upcoming.isEmpty()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "No ongoing events")
                    Spacer(modifier = Modifier.weight(1f))
                } else {
                    LazyColumn() {
                        items(upcoming) {
                            TournamentPreview(tournamentPreview = it)
                        }
                    }
                }
            }
            else -> {
                if (completed.isEmpty()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "No ongoing events")
                    Spacer(modifier = Modifier.weight(1f))
                } else {
                    LazyColumn() {
                        items(completed) {
                            TournamentPreview(tournamentPreview = it)
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun TournamentPreview(tournamentPreview: TournamentInfo.TournamentPreview) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(16.dp),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(CARD_ALPHA)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = tournamentPreview.title,
                style = VLRTheme.typography.titleSmall,
                modifier = Modifier.padding(4.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, contentDescription = "Location", modifier = Modifier.size(16.dp))
                Text(text = tournamentPreview.location.uppercase(), style = VLRTheme.typography.labelMedium)
                Text(
                    text = tournamentPreview.prize,
                    modifier = Modifier
                        .padding(4.dp)
                        .weight(1f),
                    textAlign = TextAlign.Center,
                    style = VLRTheme.typography.labelMedium
                )
                Icon(Icons.Outlined.DateRange, contentDescription = "Date", modifier = Modifier.size(16.dp))
                Text(text = tournamentPreview.dates, style = VLRTheme.typography.labelMedium)
            }
        }
    }
}