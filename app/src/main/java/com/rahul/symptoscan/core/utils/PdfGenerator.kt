package com.rahul.symptoscan.core.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.rahul.symptoscan.domain.model.FullAssessmentReport
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun generateAssessmentPdf(context: Context, report: FullAssessmentReport): File? {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 20f
        }
        val headerPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 16f
        }
        val textPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 12f
        }
        val smallPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            textSize = 10f
            color = Color.GRAY
        }

        var pageNumber = 1
        var myPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var myPage = pdfDocument.startPage(myPageInfo)
        var canvas = myPage.canvas
        var y = 50f
        val margin = 50f
        val pageWidth = 595f - (margin * 2)

        fun checkNewPage() {
            if (y > 750f) {
                pdfDocument.finishPage(myPage)
                pageNumber++
                myPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                myPage = pdfDocument.startPage(myPageInfo)
                canvas = myPage.canvas
                y = 50f
            }
        }

        fun drawWrappedText(text: String, currentPaint: Paint) {
            val words = text.split(" ")
            var line = ""
            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                val width = currentPaint.measureText(testLine)
                if (width > pageWidth) {
                    canvas.drawText(line, margin, y, currentPaint)
                    y += currentPaint.descent() - currentPaint.ascent()
                    line = word
                    checkNewPage()
                } else {
                    line = testLine
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, margin, y, currentPaint)
                y += currentPaint.descent() - currentPaint.ascent()
                checkNewPage()
            }
        }

        // Title
        canvas.drawText("SYMPTOSCAN", margin, y, titlePaint)
        y += 25f
        canvas.drawText("AI HEALTH ASSESSMENT REPORT", margin, y, headerPaint)
        y += 40f

        // Info
        canvas.drawText("Date: ${report.assessment.createdAt ?: "N/A"}", margin, y, textPaint)
        y += 20f
        canvas.drawText("Urgency: ${report.result.urgencyLevel?.uppercase() ?: "UNKNOWN"}", margin, y, textPaint)
        y += 30f

        // Symptoms
        canvas.drawText("SYMPTOMS", margin, y, headerPaint)
        y += 20f
        report.symptoms.forEach { s ->
            canvas.drawText("• ${s.symptomName}", margin, y, textPaint)
            y += 15f
            canvas.drawText("  Severity: ${s.severity}/10 | Duration: ${s.duration} | Onset: ${s.onset ?: "N/A"}", margin + 10f, y, textPaint)
            y += 20f
            checkNewPage()
        }
        y += 10f

        // Temperature & Notes
        if (report.assessment.bodyTemperature != null) {
            canvas.drawText("BODY TEMPERATURE: ${report.assessment.bodyTemperature}°C", margin, y, textPaint)
            y += 20f
        }
        if (!report.assessment.additionalNotes.isNullOrBlank()) {
            canvas.drawText("ADDITIONAL NOTES:", margin, y, textPaint)
            y += 15f
            drawWrappedText(report.assessment.additionalNotes!!, textPaint)
            y += 10f
        }

        // Summary
        canvas.drawText("AI SUMMARY", margin, y, headerPaint)
        y += 20f
        drawWrappedText(report.result.summary ?: "No summary available", textPaint)
        y += 15f

        // Possible Causes
        if (!report.result.possibleCauses.isNullOrEmpty()) {
            canvas.drawText("POSSIBLE EXPLANATIONS", margin, y, headerPaint)
            y += 20f
            report.result.possibleCauses!!.forEach { cause ->
                drawWrappedText("- $cause", textPaint)
            }
            y += 10f
        }

        // Recommendations
        if (!report.result.recommendations.isNullOrEmpty()) {
            canvas.drawText("RECOMMENDATIONS", margin, y, headerPaint)
            y += 20f
            report.result.recommendations!!.forEach { rec ->
                drawWrappedText("✓ $rec", textPaint)
            }
            y += 10f
        }

        // Warning Signs
        if (!report.result.warningSigns.isNullOrEmpty()) {
            canvas.drawText("WARNING SIGNS", margin, y, headerPaint.apply { color = Color.RED })
            y += 20f
            headerPaint.color = Color.BLACK // reset
            report.result.warningSigns!!.forEach { sign ->
                drawWrappedText("⚠️ $sign", textPaint)
            }
            y += 10f
        }

        // Q&A
        if (report.questions.isNotEmpty()) {
            canvas.drawText("FOLLOW-UP QUESTIONS", margin, y, headerPaint)
            y += 20f
            report.questions.forEach { q ->
                drawWrappedText("Q: ${q.question}", textPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
                drawWrappedText("A: ${q.answer ?: "No answer"}", textPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) })
                y += 10f
                checkNewPage()
            }
        }

        // Disclaimer
        y += 20f
        checkNewPage()
        canvas.drawText("MEDICAL DISCLAIMER", margin, y, headerPaint)
        y += 15f
        drawWrappedText(report.result.disclaimer ?: "This information is for general educational purposes only.", smallPaint)

        y += 30f
        checkNewPage()
        drawWrappedText("This report is generated from the user's completed SymptoScan assessment. It is not a medical diagnosis.", smallPaint)

        pdfDocument.finishPage(myPage)

        val file = File(context.cacheDir, "SymptoScan_Assessment_${System.currentTimeMillis()}.pdf")
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
