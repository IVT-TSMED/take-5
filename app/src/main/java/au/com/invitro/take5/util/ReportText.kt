package au.com.invitro.take5.util

import au.com.invitro.take5.AppConfig
import au.com.invitro.take5.data.Take5Record

object ReportText {

    fun subject(r: Take5Record): String =
        "Take 5 – ${r.site} – ${r.machineType} – ${r.technician} – ${Formats.shortDate(r.completedAt)}"

    fun body(r: Take5Record): String = buildString {
        appendLine("${AppConfig.COMPANY_NAME} – TAKE 5 / JHA PRE-JOB RISK ASSESSMENT")
        appendLine("Full report (PDF) and photos attached.")
        appendLine()
        appendLine("JOB DETAILS")
        appendLine("Technician: ${r.technician}")
        appendLine("Site: ${r.site}")
        if (r.department.isNotBlank()) appendLine("Department / location: ${r.department}")
        appendLine("Machine type: ${r.machineType}")
        if (r.makeModel.isNotBlank()) appendLine("Make / model: ${r.makeModel}")
        if (r.serialNumber.isNotBlank()) appendLine("Serial / asset no: ${r.serialNumber}")
        if (r.jobNumber.isNotBlank()) appendLine("Job / work order no: ${r.jobNumber}")
        appendLine("Job type: ${r.jobType}")
        appendLine("Scope of work: ${r.scopeOfWork}")
        appendLine()
        appendLine("HAZARDS IDENTIFIED")
        r.hazards.forEach { appendLine("• $it") }
        if (r.risksIdentified.isNotBlank()) appendLine("Risks identified: ${r.risksIdentified}")
        appendLine()
        appendLine("CONTROLS")
        r.controlChecks.forEach { appendLine("✓ $it") }
        if (r.controlMeasures.isNotBlank()) appendLine("Other controls: ${r.controlMeasures}")
        appendLine()
        appendLine("READINESS CHECK")
        r.readiness.forEach { (q, a) -> appendLine("[$a] $q") }
        appendLine()
        appendLine("TIME & LOCATION")
        appendLine("Started: ${Formats.dateTime(r.startedAt)}")
        appendLine("Completed: ${Formats.dateTime(r.completedAt)}")
        if (r.latitude != null && r.longitude != null) {
            appendLine("GPS: ${Formats.coordinates(r.latitude, r.longitude, r.accuracyMetres)}")
            appendLine("Map: ${r.mapsLink}")
        } else {
            appendLine("GPS: not available")
        }
        appendLine()
        appendLine("Signed by ${r.technician}. Photos attached: ${r.photoFiles.size}")
        appendLine("Ref: ${r.id}")
    }
}
