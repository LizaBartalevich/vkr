package com.example.mynewapplication

import android.app.Application
import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.IOException
import java.io.File
import java.io.FileOutputStream

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
            Log.d("App", "Python initialized successfully")
            copyTessdataToChaquopy()
        }
    }

    private fun copyTessdataToChaquopy() {
        try {
            val chaquopyTessdataDir = File(filesDir, "chaquopy/AssetFinder/tessdata")
            if (!chaquopyTessdataDir.exists()) {
                chaquopyTessdataDir.mkdirs()
            }
            val tessdataFile = File(chaquopyTessdataDir, "jpn.traineddata")
            if (!tessdataFile.exists()) {
                assets.open("tessdata/jpn.traineddata").use { input ->
                    FileOutputStream(tessdataFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d("App", "Tessdata copied to Chaquopy")
            } else {
                Log.d("App", "Tessdata already exists in Chaquopy")
            }
        } catch (e: IOException) {
            Log.e("App", "Error copying tessdata to Chaquopy", e)
        }
    }
}