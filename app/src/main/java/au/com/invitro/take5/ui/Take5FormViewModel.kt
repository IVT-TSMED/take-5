package au.com.invitro.take5.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import au.com.invitro.take5.data.Checklists
import au.com.invitro.take5.data.Take5Record
import au.com.invitro.take5.data.Take5Repository
import au.com.invitro.take5.util.Formats
import au.com.invitro.take5.util.ImageUtils
import au.com.invitro.take5.util.PdfReport
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.util.UUID

class Take5FormViewModel(private val app: Application) : AndroidViewModel(app) {
    val repository = Take5Repository(app)
    private val preferences = app.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    var active by mutableStateOf(false)
        private set
    var id by mutableStateOf("")
        private set
    var startedAt by mutableLongStateOf(0L)
        private set

    var technician by mutableStateOf("")
    var site by mutableStateOf("")
    var department by mutableStateOf("")
    var machineType by mutableStateOf("")
    var machineOther by mutableStateOf("")
    var makeModel by mutableStateOf("")
    var serialNumber by mutableStateOf("")
    var jobNumber by mutableStateOf("")
    var jobType by mutableStateOf("")
    var scopeOfWork by mutableStateOf("")

    val hazards = mutableStateListOf<String>()
    var otherHazards by mutableStateOf("")
    var risksIdentified by mutableStateOf("")
    val controlChecks = mutableStateListOf<String>()
    var controlMeasures by mutableStateOf("")

    val readiness = mutableStateMapOf<String, String>()

    val photos = mutableStateListOf<String>()
    var photoBusy by mutableStateOf(false)
        private set

    val signatureStrokes = mutableStateListOf<List<Offset>>()
    var signaturePadSize by mutableStateOf(IntSize.Zero)
    var signatureStrokeWidthPx by mutableFloatStateOf(6f)

    var latitude by mutableStateOf<Double?>(null)
        private set
    var longitude by mutableStateOf<Double?>(null)
        private set
    var accuracyMetres by mutableStateOf<Float?>(null)
        private set
    var locationTime by mutableStateOf<Long?>(null)
        private set
    var locationStatus by mutableStateOf("")
        private set
    var locating by mutableStateOf(false)
        private set

    var completing by mutableStateOf(false)
        private set

    fun startNew() {
        id = UUID.randomUUID().toString()
        startedAt = System.currentTimeMillis()
        technician = preferences.getString(KEY_TECHNICIAN_NAME, "").orEmpty()
        site = ""; department = ""; machineType = ""; machineOther = ""
        makeModel = ""; serialNumber = ""; jobNumber = ""; jobType = ""; scopeOfWork = ""
        hazards.clear(); otherHazards = ""; risksIdentified = ""
        controlChecks.clear(); controlMeasures = ""
        readiness.clear()
        photos.clear()
        signatureStrokes.clear()
        latitude = null; longitude = null; accuracyMetres = null; locationTime = null
        locationStatus = ""
        active = true
        viewModelScope.launch(Dispatchers.IO) { repository.cleanupDrafts(id) }
    }

    fun discard() {
        val draftId = id
        active = false
        viewModelScope.launch(Dispatchers.IO) { repository.delete(draftId) }
    }

    fun finish() {
        active = false
    }

    fun toggle(list: MutableList<String>, item: String) {
        if (item in list) list.remove(item) else list.add(item)
    }

    // ---- Location ----

    @SuppressLint("MissingPermission")
    fun fetchLocation() {
        if (locating) return
        locating = true
        locationStatus = "Getting GPS location…"
        viewModelScope.launch {
            try {
                val client = LocationServices.getFusedLocationProviderClient(app)
                val location = withTimeoutOrNull(20_000) {
                    client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token).await()
                } ?: client.lastLocation.await()
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    accuracyMetres = if (location.hasAccuracy()) location.accuracy else null
                    locationTime = location.time
                    locationStatus = ""
                } else {
                    locationStatus = "Location unavailable. Check GPS is on, then tap Refresh."
                }
            } catch (e: SecurityException) {
                locationStatus = "Location permission denied."
            } catch (e: Exception) {
                locationStatus = "Location unavailable: ${e.message ?: "unknown error"}"
            } finally {
                locating = false
            }
        }
    }

    fun locationPermissionDenied() {
        locationStatus = "Location permission denied. Enable it in Settings to record GPS."
    }

    // ---- Photos ----

    fun newCameraFile(): File = File(repository.dirFor(id), "camera_${System.currentTimeMillis()}.jpg")

    fun onCameraResult(raw: File, success: Boolean) {
        if (!success) {
            raw.delete()
            return
        }
        addPhoto { raw.inputStream() }.invokeOnCompletion { raw.delete() }
    }

    fun importPhoto(uri: Uri) {
        addPhoto { app.contentResolver.openInputStream(uri) }
    }

    private fun addPhoto(open: () -> java.io.InputStream?) = viewModelScope.launch {
        photoBusy = true
        val name = "photo_${System.currentTimeMillis()}.jpg"
        val ok = withContext(Dispatchers.IO) {
            runCatching { ImageUtils.saveCompressed(open, File(repository.dirFor(id), name)) }.getOrDefault(false)
        }
        if (ok) photos.add(name)
        photoBusy = false
    }

    fun removePhoto(name: String) {
        photos.remove(name)
        File(repository.dirFor(id), name).delete()
    }

    fun photoFile(name: String) = File(repository.dirFor(id), name)

    // ---- Validation & completion ----

    private val effectiveMachineType get() =
        if (machineType == "Other") machineOther.trim().ifEmpty { "Other" } else machineType

    fun validate(): List<String> = buildList {
        if (technician.isBlank()) add("Technician name")
        if (site.isBlank()) add("Site / hospital")
        if (machineType.isBlank()) add("Machine type")
        if (machineType == "Other" && machineOther.isBlank()) add("Describe the 'Other' machine type")
        if (jobType.isBlank()) add("Job type")
        if (scopeOfWork.isBlank()) add("Scope of work")
        if (hazards.isEmpty() && otherHazards.isBlank()) add("Select at least one hazard (or describe other hazards)")
        val unanswered = Checklists.readinessQuestions.count { readiness[it] == null }
        if (unanswered > 0) add("Answer all readiness questions ($unanswered remaining)")
        if (signatureStrokes.isEmpty()) add("Technician signature")
    }

    suspend fun complete(): Take5Record = withContext(Dispatchers.IO) {
        completing = true
        try {
            val dir = repository.dirFor(id)
            val signatureFile = "signature.png"
            ImageUtils.renderSignature(
                signatureStrokes.toList(), signaturePadSize.width, signaturePadSize.height,
                signatureStrokeWidthPx, File(dir, signatureFile),
            )
            val completedAt = System.currentTimeMillis()
            val allHazards = hazards.toList() +
                otherHazards.lines().map { it.trim() }.filter { it.isNotEmpty() }.map { "Other: $it" }
            val record = Take5Record(
                id = id,
                startedAt = startedAt,
                completedAt = completedAt,
                technician = technician.trim(),
                site = site.trim(),
                department = department.trim(),
                machineType = effectiveMachineType,
                makeModel = makeModel.trim(),
                serialNumber = serialNumber.trim(),
                jobNumber = jobNumber.trim(),
                jobType = jobType,
                scopeOfWork = scopeOfWork.trim(),
                hazards = allHazards,
                risksIdentified = risksIdentified.trim(),
                initialLikelihood = 0,
                initialConsequence = 0,
                controlChecks = Checklists.controls.filter { it in controlChecks },
                controlMeasures = controlMeasures.trim(),
                residualLikelihood = 0,
                residualConsequence = 0,
                supervisorConsulted = "",
                readiness = Checklists.readinessQuestions.map { it to (readiness[it] ?: "—") },
                photoFiles = photos.toList(),
                signatureFile = signatureFile,
                pdfFile = "Take5_${Formats.fileStamp(completedAt)}_${Formats.safeFileName(site)}.pdf",
                latitude = latitude,
                longitude = longitude,
                accuracyMetres = accuracyMetres,
                locationTime = locationTime,
                emailedAt = null,
            )
            preferences.edit().putString(KEY_TECHNICIAN_NAME, record.technician).apply()
            PdfReport(app).write(record, dir, File(dir, record.pdfFile))
            repository.save(record)
            record
        } finally {
            completing = false
        }
    }

    companion object {
        private const val PREFERENCES_NAME = "take5_preferences"
        private const val KEY_TECHNICIAN_NAME = "technician_name"
    }
}
