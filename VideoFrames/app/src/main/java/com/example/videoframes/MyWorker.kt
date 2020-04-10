package com.example.videoframes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Base64
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.File
import java.io.FileOutputStream

class MyWorker(context: Context, params: WorkerParameters) :
        CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val name = inputData.getString("array")
            val i = 0
            if (name != null) {

                StringToBitMap(name)?.let { createDirectoryAndSaveFile(it, java.sql.Timestamp(System.currentTimeMillis()).toString()) }

            }
            Result.success()
        } catch (error: Throwable) {
            Result.failure()
        }
    }



    fun StringToBitMap(encodedString: String?): Bitmap? {
        return try {
            val encodeByte = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        } catch (e: Exception) {
            e.message
            null
        }
    }

    private fun createDirectoryAndSaveFile(imageToSave: Bitmap, fileName: String) {
        val direct = File(Environment.getExternalStorageDirectory().toString() + "/DirName")
        if (!direct.exists()) {
            val wallpaperDirectory = File("/sdcard/DirName/")
            wallpaperDirectory.mkdirs()
        }
        val file = File("/sdcard/DirName/", fileName + ".jpg")
        if (file.exists()) {
            file.delete()
        }
        try {
            val out = FileOutputStream(file)
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}
