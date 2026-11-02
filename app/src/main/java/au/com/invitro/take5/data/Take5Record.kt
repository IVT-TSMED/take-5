package au.com.invitro.take5.data

import org.json.JSONArray
import org.json.JSONObject

data class Take5Record(
    val id: String,
    val startedAt: Long,
    val completedAt: Long,
    val technician: String,
    val site: String,
    val department: String,
    val machineType: String,
    val makeModel: String,
    val serialNumber: String,
    val jobNumber: String,
    val jobType: String,
    val scopeOfWork: String,
    val hazards: List<String>,
    val risksIdentified: String,
    val initialLikelihood: Int,
    val initialConsequence: Int,
    val controlChecks: List<String>,
    val controlMeasures: String,
    val residualLikelihood: Int,
    val residualConsequence: Int,
    val supervisorConsulted: String,
    val readiness: List<Pair<String, String>>,
    val photoFiles: List<String>,
    val signatureFile: String,
    val pdfFile: String,
    val latitude: Double?,
    val longitude: Double?,
    val accuracyMetres: Float?,
    val locationTime: Long?,
    val emailedAt: Long?,
) {
    val initialLevel get() = Risk.level(initialLikelihood, initialConsequence)
    val residualLevel get() = Risk.level(residualLikelihood, residualConsequence)

    val mapsLink: String?
        get() = if (latitude != null && longitude != null)
            "https://maps.google.com/?q=$latitude,$longitude" else null

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("startedAt", startedAt)
        put("completedAt", completedAt)
        put("technician", technician)
        put("site", site)
        put("department", department)
        put("machineType", machineType)
        put("makeModel", makeModel)
        put("serialNumber", serialNumber)
        put("jobNumber", jobNumber)
        put("jobType", jobType)
        put("scopeOfWork", scopeOfWork)
        put("hazards", JSONArray(hazards))
        put("risksIdentified", risksIdentified)
        put("initialLikelihood", initialLikelihood)
        put("initialConsequence", initialConsequence)
        put("controlChecks", JSONArray(controlChecks))
        put("controlMeasures", controlMeasures)
        put("residualLikelihood", residualLikelihood)
        put("residualConsequence", residualConsequence)
        put("supervisorConsulted", supervisorConsulted)
        put("readiness", JSONArray(readiness.map { JSONObject().put("q", it.first).put("a", it.second) }))
        put("photoFiles", JSONArray(photoFiles))
        put("signatureFile", signatureFile)
        put("pdfFile", pdfFile)
        latitude?.let { put("latitude", it) }
        longitude?.let { put("longitude", it) }
        accuracyMetres?.let { put("accuracyMetres", it.toDouble()) }
        locationTime?.let { put("locationTime", it) }
        emailedAt?.let { put("emailedAt", it) }
    }

    companion object {
        private fun JSONArray.strings() = List(length()) { getString(it) }

        fun fromJson(o: JSONObject) = Take5Record(
            id = o.getString("id"),
            startedAt = o.getLong("startedAt"),
            completedAt = o.getLong("completedAt"),
            technician = o.optString("technician"),
            site = o.optString("site"),
            department = o.optString("department"),
            machineType = o.optString("machineType"),
            makeModel = o.optString("makeModel"),
            serialNumber = o.optString("serialNumber"),
            jobNumber = o.optString("jobNumber"),
            jobType = o.optString("jobType"),
            scopeOfWork = o.optString("scopeOfWork"),
            hazards = o.optJSONArray("hazards")?.strings().orEmpty(),
            risksIdentified = o.optString("risksIdentified"),
            initialLikelihood = o.optInt("initialLikelihood"),
            initialConsequence = o.optInt("initialConsequence"),
            controlChecks = o.optJSONArray("controlChecks")?.strings().orEmpty(),
            controlMeasures = o.optString("controlMeasures"),
            residualLikelihood = o.optInt("residualLikelihood"),
            residualConsequence = o.optInt("residualConsequence"),
            supervisorConsulted = o.optString("supervisorConsulted"),
            readiness = o.optJSONArray("readiness")?.let { arr ->
                List(arr.length()) { i -> arr.getJSONObject(i).let { it.getString("q") to it.getString("a") } }
            }.orEmpty(),
            photoFiles = o.optJSONArray("photoFiles")?.strings().orEmpty(),
            signatureFile = o.optString("signatureFile"),
            pdfFile = o.optString("pdfFile"),
            latitude = if (o.has("latitude")) o.getDouble("latitude") else null,
            longitude = if (o.has("longitude")) o.getDouble("longitude") else null,
            accuracyMetres = if (o.has("accuracyMetres")) o.getDouble("accuracyMetres").toFloat() else null,
            locationTime = if (o.has("locationTime")) o.getLong("locationTime") else null,
            emailedAt = if (o.has("emailedAt")) o.getLong("emailedAt") else null,
        )
    }
}
