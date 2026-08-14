package com.rahul.symptoscan.core.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.rahul.symptoscan.data.remote.model.DbAssessmentResult
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {

    fun generateAssessmentPdf(
        context: Context,
        result: DbAssessmentResult,
        symptoms: List<String>
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        val titlePaint = Paint()
        
        var y = 50f
        val margin = 50f
        val pageWidth = 595f
        val contentWidth = pageWidth - 2 * margin

        // Header
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 24f
        titlePaint.color = 0xFF2563EB.toInt() // BluePrimary
        canvas.drawText("SymptoScan Health Report", margin, y, titlePaint)
        y += 40f

        paint.textSize = 12f
        paint.color = Color.GRAY
        val sdf = SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault())
        canvas.drawText("Generated on: ${sdf.format(Date())}", margin, y, paint)
        y += 40f

        // Risk Level
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 18f
        paint.color = Color.BLACK
        val riskScore = result.riskScore ?: 0
        canvas.drawText("Overall Risk Score: $riskScore/100", margin, y, paint)
        y += 25f
        paint.textSize = 16f
        canvas.drawText("Risk Assessment: ${result.urgencyLevel?.uppercase() ?: "UNKNOWN"}", margin, y, paint)
        y += 35f

        // Symptoms
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        canvas.drawText("Reported Symptoms:", margin, y, paint)
        y += 25f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.DKGRAY
        symptoms.forEach { symptom ->
            canvas.drawText("• $symptom", margin + 10, y, paint)
            y += 20f
        }
        y += 20f

        // Summary
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.BLACK
        canvas.drawText("AI Explanation:", margin, y, paint)
        y += 25f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.BLACK
        
        val summaryLines = wrapText(result.summary ?: "", paint, contentWidth)
        summaryLines.forEach { line ->
            canvas.drawText(line, margin, y, paint)
            y += 20f
        }
        y += 25f

        // Possible Conditions
        val conditions = result.conditions
        if (!conditions.isNullOrEmpty()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Possible Conditions:", margin, y, paint)
            y += 25f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            conditions.forEach { condition ->
                canvas.drawText("${condition.name} - ${condition.confidence}% Confidence (${condition.severity})", margin + 10, y, paint)
                y += 20f
            }
            y += 20f
        }

        // Recommendations
        val recs = result.recommendationItems ?: result.recommendations?.map { com.rahul.symptoscan.data.remote.model.DbRecommendationItem(it, "•") }
        if (!recs.isNullOrEmpty()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Recommendations:", margin, y, paint)
            y += 25f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            recs.forEach { rec ->
                val recLines = wrapText("• ${rec.title}", paint, contentWidth)
                recLines.forEach { line ->
                    canvas.drawText(line, margin + 10, y, paint)
                    y += 20f
                }
            }
            y += 20f
        }

        // Specialist
        result.specialist?.let { spec ->
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Recommended Specialist:", margin, y, paint)
            y += 25f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("${spec.title}: ${spec.description}", margin + 10, y, paint)
            y += 30f
        }

        // Disclaimer
        if (y > 700f) {
            pdfDocument.finishPage(page)
            val newPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, 2).create())
            val newCanvas = newPage.canvas
            drawDisclaimer(newCanvas, paint, margin, 50f, contentWidth, result.disclaimer)
            pdfDocument.finishPage(newPage)
        } else {
            drawDisclaimer(canvas, paint, margin, 750f, contentWidth, result.disclaimer)
            pdfDocument.finishPage(page)
        }

        val directory = File(context.cacheDir, "reports")
        if (!directory.exists()) directory.mkdirs()
        
        val file = File(directory, "SymptoScan_Report_${result.assessmentId}.pdf")

        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    private fun drawDisclaimer(canvas: Canvas, paint: Paint, margin: Float, y: Float, width: Float, disclaimer: String?) {
        paint.textSize = 10f
        paint.color = Color.RED
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        val disclaimerLines = wrapText("Disclaimer: ${disclaimer ?: "This is not a medical diagnosis."}", paint, width)
        var currentY = y
        disclaimerLines.forEach { line ->
            canvas.drawText(line, margin, currentY, paint)
            currentY += 15f
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine.append(if (currentLine.isEmpty()) word else " $word")
            } else {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
        return lines
    }
    
    fun savePdfToDownloads(context: Context, sourceFile: File): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, sourceFile.name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        FileInputStream(sourceFile).use { input ->
                            input.copyTo(output)
                        }
                    }
                    true
                } else {
                    false
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val destFile = File(downloadsDir, sourceFile.name)
                sourceFile.inputStream().use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
