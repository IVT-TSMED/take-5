package au.com.invitro.take5.data

import android.content.Context
import org.json.JSONObject
import java.io.File

/** Each Take 5 lives in its own folder: record.json, photos, signature and PDF. */
class Take5Repository(context: Context) {
    private val root = File(context.filesDir, "take5").apply { mkdirs() }

    fun dirFor(id: String) = File(root, id).apply { mkdirs() }

    fun save(record: Take5Record) {
        File(dirFor(record.id), RECORD_FILE).writeText(record.toJson().toString(2))
    }

    fun load(id: String): Take5Record? {
        val f = File(File(root, id), RECORD_FILE)
        if (!f.exists()) return null
        return runCatching { Take5Record.fromJson(JSONObject(f.readText())) }.getOrNull()
    }

    fun loadAll(): List<Take5Record> =
        root.listFiles().orEmpty()
            .mapNotNull { load(it.name) }
            .sortedByDescending { it.completedAt }

    fun delete(id: String) {
        File(root, id).deleteRecursively()
    }

    /** Removes folders left behind by Take 5s that were started but never completed. */
    fun cleanupDrafts(keepId: String?) {
        root.listFiles().orEmpty()
            .filter { it.isDirectory && it.name != keepId && !File(it, RECORD_FILE).exists() }
            .forEach { it.deleteRecursively() }
    }

    companion object {
        private const val RECORD_FILE = "record.json"
    }
}
