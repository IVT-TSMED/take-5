package au.com.invitro.take5.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import au.com.invitro.take5.data.Take5Record
import au.com.invitro.take5.data.Take5Repository
import au.com.invitro.take5.util.EmailSender
import au.com.invitro.take5.util.Formats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun DetailScreen(
    repository: Take5Repository,
    id: String,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
) {
    val context = LocalContext.current
    var reload by remember { mutableIntStateOf(0) }
    val record by produceState<Take5Record?>(null, id, reload) {
        value = withContext(Dispatchers.IO) { repository.load(id) }
    }
    var confirmDelete by remember { mutableStateOf(false) }

    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            IvtTopBar(title = "Take 5", onBack = onBack, actions = {
                IconButton(onClick = { confirmDelete = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = IvtGrey)
                }
            })
        },
        containerColor = IvtBackground,
    ) { padding ->
        val r = record
        if (r == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        val dir = repository.dirFor(r.id)
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        EmailSender.send(context, r, dir)
                        repository.save(r.copy(emailedAt = System.currentTimeMillis()))
                        reload++
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Resend email")
                }
                OutlinedButton(
                    onClick = { EmailSender.viewPdf(context, File(dir, r.pdfFile)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                ) { Text("View PDF") }
            }

            StepCard("1", "Job details") {
                Info("Technician", r.technician)
                Info("Site", r.site)
                Info("Department / location", r.department)
                Info("Machine type", r.machineType)
                Info("Make / model", r.makeModel)
                Info("Serial / asset no.", r.serialNumber)
                Info("Job / work order no.", r.jobNumber)
                Info("Job type", r.jobType)
                Info("Scope of work", r.scopeOfWork)
            }
            StepCard("2", "Hazards and risks") {
                r.hazards.forEach { Text("•  $it", style = MaterialTheme.typography.bodyMedium) }
                if (r.risksIdentified.isNotBlank()) Info("Risks identified", r.risksIdentified)
            }
            StepCard("3", "Controls") {
                r.controlChecks.forEach { Text("✓  $it", style = MaterialTheme.typography.bodyMedium) }
                Info("Other controls", r.controlMeasures)
            }
            StepCard("4", "Readiness") {
                r.readiness.forEach { (q, a) ->
                    Row {
                        Text(q, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            a.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = when (a) { "Yes" -> IvtGreen; "No" -> IvtRed; else -> IvtGrey },
                        )
                    }
                }
            }
            StepCard("📍", "Time & location") {
                Info("Started", Formats.dateTime(r.startedAt))
                Info("Completed", Formats.dateTime(r.completedAt))
                if (r.latitude != null && r.longitude != null) {
                    Info("GPS", Formats.coordinates(r.latitude, r.longitude, r.accuracyMetres))
                    TextButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(r.mapsLink)))
                    }) { Text("Open in Maps") }
                } else {
                    Info("GPS", "Not available")
                }
            }
            if (r.photoFiles.isNotEmpty()) {
                StepCard("📷", "Photos (${r.photoFiles.size})") {
                    r.photoFiles.forEach { name ->
                        FileImage(
                            File(dir, name),
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                            maxPx = 1200,
                            contentScale = ContentScale.FillWidth,
                        )
                    }
                }
            }
            StepCard("✍", "Signature") {
                FileImage(
                    File(dir, r.signatureFile),
                    Modifier.fillMaxWidth().height(140.dp),
                    maxPx = 1200,
                    contentScale = ContentScale.Fit,
                )
                Text("Signed by ${r.technician}", style = MaterialTheme.typography.bodySmall, color = IvtGrey)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this Take 5?") },
            text = { Text("It will be removed from this device. Copies already emailed are not affected.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    repository.delete(id)
                    onDeleted()
                }) { Text("Delete", color = IvtRed) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun Info(label: String, value: String) {
    if (value.isBlank()) return
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = IvtGrey)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

