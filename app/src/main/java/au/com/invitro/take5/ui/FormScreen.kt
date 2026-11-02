package au.com.invitro.take5.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import au.com.invitro.take5.AppConfig
import au.com.invitro.take5.data.Checklists
import au.com.invitro.take5.data.Take5Record
import au.com.invitro.take5.util.EmailSender
import au.com.invitro.take5.util.Formats
import kotlinx.coroutines.launch
import java.io.File

private fun hasLocationPermission(context: Context) =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

@Composable
fun FormScreen(
    vm: Take5FormViewModel,
    onDiscard: () -> Unit,
    onCompleted: (Take5Record) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var errors by remember { mutableStateOf<List<String>>(emptyList()) }
    var confirmDiscard by remember { mutableStateOf(false) }
    var confirmSend by remember { mutableStateOf(false) }

    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) vm.fetchLocation() else vm.locationPermissionDenied()
    }
    LaunchedEffect(vm.id) {
        if (vm.latitude == null) {
            if (hasLocationPermission(context)) vm.fetchLocation()
            else locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    var pendingCameraFile by rememberSaveable { mutableStateOf<String?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        pendingCameraFile?.let { vm.onCameraResult(File(it), ok) }
        pendingCameraFile = null
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
        uris.forEach(vm::importPhoto)
    }

    BackHandler { confirmDiscard = true }

    val padStroke = dpToPx(2.5f)
    LaunchedEffect(padStroke) { vm.signatureStrokeWidthPx = padStroke }

    Scaffold(
        topBar = { IvtTopBar(title = "New Take 5", onBack = { confirmDiscard = true }) },
        containerColor = IvtBackground,
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Started ${Formats.dateTime(vm.startedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = IvtGrey,
            )

            StepCard("1", "Stop & think", "Job details") {
                FormField("Technician name", vm.technician, { vm.technician = it }, required = true, capitalization = KeyboardCapitalization.Words)
                FormField("Site / hospital", vm.site, { vm.site = it }, required = true, capitalization = KeyboardCapitalization.Words)
                FormField("Department / location (e.g. CSSD, plant room)", vm.department, { vm.department = it })
                DropdownField("Machine type", Checklists.machineTypes, vm.machineType, { vm.machineType = it }, required = true)
                if (vm.machineType == "Other") {
                    FormField("Describe machine type", vm.machineOther, { vm.machineOther = it }, required = true)
                }
                FormField("Make / model", vm.makeModel, { vm.makeModel = it })
                FormField("Serial / asset no.", vm.serialNumber, { vm.serialNumber = it }, capitalization = KeyboardCapitalization.Characters)
                FormField("Job / work order no.", vm.jobNumber, { vm.jobNumber = it }, capitalization = KeyboardCapitalization.Characters)
                DropdownField("Job type", Checklists.jobTypes, vm.jobType, { vm.jobType = it }, required = true)
                FormField("Scope of work", vm.scopeOfWork, { vm.scopeOfWork = it }, required = true, multiline = true)
            }

            StepCard("2", "Hazards and risks") {
                Text("Tick all hazards present for this job:", style = MaterialTheme.typography.bodyMedium, color = IvtGrey)
                Column {
                    Checklists.hazards.forEach { h ->
                        CheckRow(h, h in vm.hazards) { vm.toggle(vm.hazards, h) }
                    }
                }
                FormField("Other hazards (one per line)", vm.otherHazards, { vm.otherHazards = it }, multiline = true)
            }

            StepCard("3", "Controls", "Put controls in place") {
                Column {
                    Checklists.controls.forEach { c ->
                        CheckRow(c, c in vm.controlChecks) { vm.toggle(vm.controlChecks, c) }
                    }
                }
                FormField("Other controls", vm.controlMeasures, { vm.controlMeasures = it }, multiline = true)
            }

            StepCard("4", "Proceed safely", "Readiness check") {
                Checklists.readinessQuestions.forEach { q ->
                    Column {
                        Text(q, style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Checklists.answers.forEach { a ->
                                val selectedColour = when (a) {
                                    "Yes" -> IvtGreen
                                    "No" -> IvtRed
                                    else -> IvtGrey
                                }
                                FilterChip(
                                    selected = vm.readiness[q] == a,
                                    onClick = { vm.readiness[q] = a },
                                    label = { Text(a) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = selectedColour,
                                        selectedLabelColor = Color.White,
                                    ),
                                )
                            }
                        }
                    }
                }
                if (vm.readiness.values.any { it == "No" }) {
                    WarningBox(
                        "You answered NO to a readiness question. Resolve it before starting, and record what you did in the control measures.",
                        IvtOrange,
                    )
                }
            }

            StepCard("📷", "Photos", "Optional – site, hazards, isolation points") {
                if (vm.photos.isNotEmpty() || vm.photoBusy) {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        vm.photos.forEach { name ->
                            Box {
                                FileImage(vm.photoFile(name), Modifier.size(104.dp).clip(RoundedCornerShape(4.dp)))
                                IconButton(
                                    onClick = { vm.removePhoto(name) },
                                    modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).size(28.dp)
                                        .clip(CircleShape).background(Color.Black.copy(alpha = 0.55f)),
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove photo", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        if (vm.photoBusy) {
                            Box(Modifier.size(104.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val file = vm.newCameraFile()
                            pendingCameraFile = file.path
                            camera.launch(EmailSender.uriFor(context, file))
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Take photo")
                    }
                    OutlinedButton(
                        onClick = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.weight(1f),
                    ) { Text("From gallery") }
                }
            }

            StepCard("📍", "Location & time", null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = IvtTeal)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        val lat = vm.latitude
                        val lng = vm.longitude
                        if (lat != null && lng != null) {
                            Text(Formats.coordinates(lat, lng, vm.accuracyMetres), style = MaterialTheme.typography.bodyMedium)
                            vm.locationTime?.let {
                                Text("Fix at ${Formats.dateTime(it)}", style = MaterialTheme.typography.bodySmall, color = IvtGrey)
                            }
                        }
                        if (vm.locationStatus.isNotEmpty()) {
                            Text(vm.locationStatus, style = MaterialTheme.typography.bodySmall, color = IvtGrey)
                        }
                    }
                    if (vm.locating) {
                        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = {
                            if (hasLocationPermission(context)) vm.fetchLocation()
                            else locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                        }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh location", tint = IvtTeal)
                        }
                    }
                }
                Text(
                    "Start and completion times are recorded automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = IvtGrey,
                )
            }

            StepCard("✍", "Declaration & signature", null) {
                Text(Checklists.DECLARATION, style = MaterialTheme.typography.bodyMedium)
                SignaturePad(
                    strokes = vm.signatureStrokes,
                    onSizeChanged = { vm.signaturePadSize = it },
                    strokeWidthPx = vm.signatureStrokeWidthPx,
                    modifier = Modifier.fillMaxWidth().height(170.dp),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (vm.signatureStrokes.isEmpty()) "Sign above with your finger" else "Signed",
                        style = MaterialTheme.typography.bodySmall,
                        color = IvtGrey,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { vm.signatureStrokes.clear() }, enabled = vm.signatureStrokes.isNotEmpty()) {
                        Text("Clear")
                    }
                }
            }

            Button(
                onClick = {
                    val problems = vm.validate()
                    if (problems.isEmpty()) confirmSend = true else errors = problems
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(4.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text("COMPLETE & SEND", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (errors.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { errors = emptyList() },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = IvtOrange) },
            title = { Text("Please complete") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    errors.forEach { Text("•  $it", style = MaterialTheme.typography.bodyMedium) }
                }
            },
            confirmButton = { TextButton(onClick = { errors = emptyList() }) { Text("OK") } },
        )
    }

    if (confirmSend) {
        AlertDialog(
            onDismissRequest = { confirmSend = false },
            title = { Text("Complete Take 5?") },
            text = {
                Text(
                    "Your Take 5 will be saved and your email app will open with the report addressed to " +
                        "${AppConfig.MONITORED_INBOX}.\n\nMake sure you tap SEND in your email app.",
                )
            },
            confirmButton = {
                Button(onClick = {
                    confirmSend = false
                    scope.launch {
                        runCatching { vm.complete() }
                            .onSuccess(onCompleted)
                            .onFailure { errors = listOf("Could not create the report: ${it.message}") }
                    }
                }) { Text("Complete & send") }
            },
            dismissButton = { TextButton(onClick = { confirmSend = false }) { Text("Back") } },
        )
    }

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text("Discard this Take 5?") },
            text = { Text("Everything entered, including photos and signature, will be deleted.") },
            confirmButton = {
                TextButton(onClick = { confirmDiscard = false; onDiscard() }) { Text("Discard", color = IvtRed) }
            },
            dismissButton = { TextButton(onClick = { confirmDiscard = false }) { Text("Keep editing") } },
        )
    }

    if (vm.completing) {
        Dialog(onDismissRequest = {}) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator()
                    Spacer(Modifier.width(16.dp))
                    Text("Creating report…")
                }
            }
        }
    }
}

@Composable
fun WarningBox(text: String, colour: Color) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(colour).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.White)
        Spacer(Modifier.width(10.dp))
        Text(text, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
