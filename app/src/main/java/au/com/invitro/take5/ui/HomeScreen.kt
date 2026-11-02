package au.com.invitro.take5.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import au.com.invitro.take5.data.Take5Record
import au.com.invitro.take5.data.Take5Repository
import au.com.invitro.take5.util.Formats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(
    repository: Take5Repository,
    refreshKey: Int,
    snackbar: SnackbarHostState,
    onNew: () -> Unit,
    onOpen: (String) -> Unit,
) {
    val records by produceState<List<Take5Record>?>(null, refreshKey) {
        value = withContext(Dispatchers.IO) { repository.loadAll() }
    }

    Scaffold(
        topBar = { IvtTopBar(title = "Take 5", largeBranding = true) },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = IvtBackground,
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = IvtTeal),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Before every job – Take 5", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Stop · Identify hazards · Apply controls · Proceed safely",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = onNew,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = IvtTeal,
                            ),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("START NEW TAKE 5", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text(
                    "Completed Take 5s on this device",
                    style = MaterialTheme.typography.titleSmall,
                    color = IvtNavy,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                )
            }

            val list = records
            if (list != null && list.isEmpty()) {
                item {
                    Text(
                        "No Take 5s yet. Completed assessments will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IvtGrey,
                        modifier = Modifier.padding(4.dp),
                    )
                }
            }
            items(list.orEmpty(), key = { it.id }) { record ->
                HistoryItem(record) { onOpen(record.id) }
            }
        }
    }
}

@Composable
private fun HistoryItem(record: Take5Record, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(record.site, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${record.machineType} · ${record.jobType}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${record.technician} · ${Formats.dateTime(record.completedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = IvtGrey,
                )
                Text(
                    if (record.emailedAt != null) "Email prepared ${Formats.dateTime(record.emailedAt)}" else "Not yet emailed",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (record.emailedAt != null) IvtGreen else IvtRed,
                    modifier = Modifier.background(Color.Transparent),
                )
            }
        }
    }
}
