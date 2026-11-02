package au.com.invitro.take5.util

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import au.com.invitro.take5.AppConfig
import au.com.invitro.take5.data.Take5Record
import java.io.File

object EmailSender {

    fun uriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    /** Opens the device email app with the inbox, subject, summary and attachments pre-filled. */
    fun send(context: Context, record: Take5Record, dir: File) {
        val files = (listOf(record.pdfFile) + record.photoFiles).map { File(dir, it) }.filter { it.exists() }
        val uris = ArrayList(files.map { uriFor(context, it) })

        val action = if (uris.size > 1) Intent.ACTION_SEND_MULTIPLE else Intent.ACTION_SEND
        val intent = Intent(action).apply {
            type = when {
                files.size == 1 && files.first().extension.equals("pdf", ignoreCase = true) -> "application/pdf"
                files.size == 1 -> "image/jpeg"
                else -> "*/*"
            }
            putExtra(Intent.EXTRA_EMAIL, arrayOf(AppConfig.MONITORED_INBOX))
            putExtra(Intent.EXTRA_SUBJECT, ReportText.subject(record))
            putExtra(Intent.EXTRA_TEXT, ReportText.body(record))
            if (uris.size > 1) {
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            } else if (uris.size == 1) {
                putExtra(Intent.EXTRA_STREAM, uris.first())
            }
            if (uris.isNotEmpty()) {
                clipData = ClipData.newRawUri(null, uris.first()).also { clip ->
                    uris.drop(1).forEach { clip.addItem(ClipData.Item(it)) }
                }
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val handlers = context.packageManager.queryIntentActivities(intent, 0)
        if (handlers.isNotEmpty()) {
            try {
                context.startActivity(Intent.createChooser(intent, "Send Take 5 with…"))
                return
            } catch (_: ActivityNotFoundException) {
                // Fall through to a mailto intent below.
            }
        }

        // Last-resort fallback for email apps that only advertise mailto: support.
        // Android's mailto intent cannot include file attachments.
        val mailtoUri = Uri.parse(
            "mailto:${AppConfig.MONITORED_INBOX}" +
                "?subject=${Uri.encode(ReportText.subject(record))}" +
                "&body=${Uri.encode(ReportText.body(record))}",
        )
        val mailto = Intent(Intent.ACTION_SENDTO).apply {
            data = mailtoUri
            putExtra(Intent.EXTRA_SUBJECT, ReportText.subject(record))
            putExtra(Intent.EXTRA_TEXT, ReportText.body(record))
        }
        try {
            context.startActivity(mailto)
            Toast.makeText(
                context,
                "Your email app does not accept attachments automatically. The report is included in the email body.",
                Toast.LENGTH_LONG,
            ).show()
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "No email app found. Install or configure Outlook or Gmail.", Toast.LENGTH_LONG).show()
        }
    }

    fun viewPdf(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uriFor(context, file), "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No PDF viewer installed.", Toast.LENGTH_LONG).show()
        }
    }
}
