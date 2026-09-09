package com.example.data.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ExifScrubber {

    /**
     * Reads an image input stream or Uri, strips all EXIF metadata (GPS coordinates, camera model, date/time),
     * and saves a clean re-encoded JPEG bitmap to target destination file.
     */
    fun scrubExifMetadata(context: Context, sourceUri: Uri, destinationFile: File): Boolean {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            if (inputStream == null) return false

            // Decode image into raw pixel bitmap in memory (stripping all EXIF tags from header)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) return false

            // Write raw pixels to target destination file as fresh JPEG without EXIF header
            val outputStream = FileOutputStream(destinationFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, outputStream)
            outputStream.flush()
            outputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Scrubs EXIF metadata from an existing file path and returns a clean destination file.
     */
    fun scrubExifFromFile(sourceFile: File, destinationFile: File): Boolean {
        return try {
            if (!sourceFile.exists()) return false
            val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath) ?: return false
            val outputStream = FileOutputStream(destinationFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, outputStream)
            outputStream.flush()
            outputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
