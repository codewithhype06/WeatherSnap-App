package com.example.weathersnap.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageUtils {

    // File size in KB
    fun getFileSizeKb(file: File): Long {
        if (!file.exists()) return 0
        return file.length() / 1024
    }

    // 🚨 SAFE COMPRESSION: Prevents Out of Memory (SIG 9) Crashes
    fun compressImage(context: Context, sourceFile: File): File {
        return try {
            val options = BitmapFactory.Options()

            // 1. Sirf dimensions check karo (RAM mein poori image load mat karo)
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(sourceFile.absolutePath, options)

            // 2. Image ko chota (downscale) karo taaki memory crash na ho (Max 1024px limit)
            options.inSampleSize = calculateInSampleSize(options, 1024, 1024)

            // 3. Ab actually image ko load karo chhoti size mein
            options.inJustDecodeBounds = false
            val scaledBitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, options)

            // 4. Nayi compressed file banao
            val outputFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")

            // 5. 80% quality par save karo aur .use block se automatically stream close karo
            FileOutputStream(outputFile).use { out ->
                scaledBitmap?.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }

            // 6. Memory ko turant free karo (Bohooooot zaroori hai baar-baar photo click karne par crash rokne ke liye)
            scaledBitmap?.recycle()

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            // Agar kuch fail ho jaye, toh original file hi bhej do taaki crash na ho
            sourceFile
        }
    }

    // Helper function to calculate exact scaling ratio based on Android Best Practices
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Jab tak image limit se badi hai, usko divide-by-2 karte raho
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    // ✅ Teri Date Formatting wali function properly retain ki gayi hai!
    fun formatTimestamp(timestamp: Long): String {
        return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .format(Date(timestamp))
    }
}