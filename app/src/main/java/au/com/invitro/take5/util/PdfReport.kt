package au.com.invitro.take5.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import au.com.invitro.take5.AppConfig
import au.com.invitro.take5.R
import au.com.invitro.take5.data.Checklists
import au.com.invitro.take5.data.Risk
import au.com.invitro.take5.data.Take5Record
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

/** Renders a Take 5 to an A4 PDF using the Android PdfDocument API. */
class PdfReport(private val context: Context) {
    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 36f
    private val contentWidth = pageWidth - 2 * margin
    private val labelWidth = 140f

    private val teal = 0xFF276884.toInt()
    private val darkText = 0xFF1E1E1E.toInt()
    private val greyText = 0xFF5F6B70.toInt()
    private val ruleColour = 0xFFE1E1E1.toInt()

    private val regular = ResourcesCompat.getFont(context, R.font.opensans_regular) ?: Typeface.DEFAULT
    private val bold = ResourcesCompat.getFont(context, R.font.opensans_bold) ?: Typeface.DEFAULT_BOLD

    private val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9.5f; color = darkText; typeface = regular }
    private val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9.5f; color = greyText; typeface = bold }
    private val sectionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 11f; color = 0xFFFFFFFF.toInt(); typeface = bold }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val imagePaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)

    private lateinit var doc: PdfDocument
    private lateinit var canvas: Canvas
    private var page: PdfDocument.Page? = null
    private var pageNumber = 0
    private var y = 0f
    private lateinit var record: Take5Record

    fun write(record: Take5Record, dir: File, out: File) {
        this.record = record
        doc = PdfDocument()
        newPage()
        drawTitle()

        section("1. STOP & THINK – Job details")
        row("Technician", record.technician)
        row("Site", record.site)
        row("Department / location", record.department)
        row("Machine type", record.machineType)
        row("Make / model", record.makeModel)
        row("Serial / asset no.", record.serialNumber)
        row("Job / work order no.", record.jobNumber)
        row("Job type", record.jobType)
        row("Scope of work", record.scopeOfWork)

        section("2. Hazards and risks")
        row("Hazards", record.hazards.joinToString("\n") { "•  $it" })
        if (record.risksIdentified.isNotBlank()) row("Risks identified", record.risksIdentified)

        section("3. Controls")
        row("Controls in place", record.controlChecks.joinToString("\n") { "✓  $it" })
        if (record.controlMeasures.isNotBlank()) row("Other controls", record.controlMeasures)

        section("4. PROCEED SAFELY – Readiness check")
        record.readiness.forEach { (q, a) -> answerRow(q, a) }

        section("Time & location")
        row("Started", Formats.dateTime(record.startedAt))
        row("Completed", Formats.dateTime(record.completedAt))
        if (record.latitude != null && record.longitude != null) {
            row("GPS", Formats.coordinates(record.latitude, record.longitude, record.accuracyMetres))
            record.locationTime?.let { row("GPS fix time", Formats.dateTime(it)) }
            row("Map", record.mapsLink.orEmpty())
        } else {
            row("GPS", "Not available")
        }

        section("Declaration")
        paragraph(Checklists.DECLARATION)
        drawSignature(File(dir, record.signatureFile))
        row("Signed", "${record.technician} – ${Formats.dateTime(record.completedAt)}")

        if (record.photoFiles.isNotEmpty()) {
            section("Photos (${record.photoFiles.size})")
            drawPhotos(record.photoFiles.map { File(dir, it) })
        }

        finishPage()
        FileOutputStream(out).use { doc.writeTo(it) }
        doc.close()
    }

    private fun newPage() {
        finishPage()
        pageNumber++
        val p = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        page = p
        canvas = p.canvas
        y = margin
    }

    private fun finishPage() {
        val p = page ?: return
        val footer = TextPaint(bodyPaint).apply { textSize = 7.5f; color = greyText }
        canvas.drawText(
            "${AppConfig.COMPANY_NAME} · Take 5 / JHA · Ref ${record.id.take(8)} · Page $pageNumber",
            margin, pageHeight - margin / 2, footer,
        )
        doc.finishPage(p)
        page = null
    }

    private fun ensureSpace(height: Float) {
        if (y + height > pageHeight - margin - 14) newPage()
    }

    private fun layout(text: String, paint: TextPaint, width: Float): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1f, 1.1f)
            .build()

    private fun drawLayout(l: StaticLayout, x: Float, top: Float) {
        canvas.save()
        canvas.translate(x, top)
        l.draw(canvas)
        canvas.restore()
    }

    private fun drawTitle() {
        val logo = BitmapFactory.decodeResource(context.resources, R.drawable.ivt_logo)
        if (logo != null) {
            val h = 42f
            val w = h * logo.width / logo.height
            canvas.drawBitmap(logo, null, RectF(margin, y, margin + w, y + h), imagePaint)
            logo.recycle()
        }
        val titlePaint = TextPaint(sectionPaint).apply { color = teal; textSize = 20f; textAlign = Paint.Align.RIGHT }
        val subPaint = TextPaint(bodyPaint).apply { color = greyText; textAlign = Paint.Align.RIGHT }
        canvas.drawText("TAKE 5", pageWidth - margin, y + 20, titlePaint)
        canvas.drawText("Pre-job risk assessment / JHA", pageWidth - margin, y + 34, subPaint)
        y += 50
        fill.color = teal
        canvas.drawRect(margin, y, pageWidth - margin, y + 2.5f, fill)
        y += 10

        val text = "${record.site}    ·    ${record.machineType}"
        val l = layout(text, TextPaint(sectionPaint).apply { textSize = 10f }, contentWidth - 16)
        val h = l.height + 10f
        fill.color = teal
        canvas.drawRoundRect(RectF(margin, y, pageWidth - margin, y + h), 4f, 4f, fill)
        drawLayout(l, margin + 8, y + 5)
        y += h + 6
    }

    private fun section(title: String) {
        ensureSpace(48f)
        y += 8
        fill.color = teal
        canvas.drawRoundRect(RectF(margin, y, pageWidth - margin, y + 19), 3f, 3f, fill)
        canvas.drawText(title, margin + 7, y + 13.5f, sectionPaint)
        y += 25
    }

    private fun row(label: String, value: String) {
        val valueLayout = layout(value.ifBlank { "—" }, bodyPaint, contentWidth - labelWidth)
        val labelLayout = layout(label, labelPaint, labelWidth - 8)
        val h = max(valueLayout.height, labelLayout.height) + 7f
        ensureSpace(h)
        drawLayout(labelLayout, margin, y)
        drawLayout(valueLayout, margin + labelWidth, y)
        y += h
        fill.color = ruleColour
        canvas.drawRect(margin, y - 3.5f, pageWidth - margin, y - 3f, fill)
    }

    private fun answerRow(question: String, answer: String) {
        val answerWidth = 46f
        val qLayout = layout(question, bodyPaint, contentWidth - answerWidth - 8)
        val h = qLayout.height + 7f
        ensureSpace(h)
        drawLayout(qLayout, margin, y)
        val colour = when (answer) {
            "Yes" -> Risk.Level.LOW.argb.toInt()
            "No" -> Risk.Level.EXTREME.argb.toInt()
            else -> greyText
        }
        val aPaint = TextPaint(labelPaint).apply { color = colour; textAlign = Paint.Align.RIGHT }
        canvas.drawText(answer.uppercase(), pageWidth - margin, y + 10, aPaint)
        y += h
        fill.color = ruleColour
        canvas.drawRect(margin, y - 3.5f, pageWidth - margin, y - 3f, fill)
    }

    private fun paragraph(text: String) {
        val l = layout(text, bodyPaint, contentWidth)
        ensureSpace(l.height + 6f)
        drawLayout(l, margin, y)
        y += l.height + 6f
    }

    private fun drawSignature(file: File) {
        val sig = BitmapFactory.decodeFile(file.path) ?: return
        val w = 220f
        val h = w * sig.height / sig.width
        ensureSpace(h + 10)
        canvas.drawBitmap(sig, null, RectF(margin, y, margin + w, y + h), imagePaint)
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = ruleColour; strokeWidth = 1f }
        canvas.drawRect(margin, y, margin + w, y + h, border)
        sig.recycle()
        y += h + 8
    }

    private fun drawPhotos(files: List<File>) {
        val gap = 10f
        val cellWidth = (contentWidth - gap) / 2
        files.chunked(2).forEach { pair ->
            val bitmaps = pair.mapNotNull { ImageUtils.decodeSampled(it, 900) }
            if (bitmaps.isEmpty()) return@forEach
            val heights = bitmaps.map { cellWidth * it.height / it.width }
            val rowHeight = heights.max().coerceAtMost(pageHeight - 2 * margin - 40)
            ensureSpace(rowHeight + gap)
            bitmaps.forEachIndexed { i, bmp: Bitmap ->
                val x = margin + i * (cellWidth + gap)
                val h = heights[i].coerceAtMost(rowHeight)
                val w = h * bmp.width / bmp.height
                canvas.drawBitmap(bmp, null, RectF(x, y, x + w, y + h), imagePaint)
                bmp.recycle()
            }
            y += rowHeight + gap
        }
    }
}
