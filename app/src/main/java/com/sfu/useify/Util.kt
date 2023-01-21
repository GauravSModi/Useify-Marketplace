package com.sfu.useify

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import java.util.*

object Util {
    fun checkPermissions(activity: Activity?) {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                0
            )
        }
    }

    fun getBitmap(context: Context, imgUri: Uri): Bitmap {
        var bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imgUri))
        val matrix = Matrix()
//        matrix.setRotate(90f)
        var ret = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        return ret
    }


    // Credit to : https://stackoverflow.com/questions/11274715/save-bitmap-to-file-function
    fun saveBitmapIntoDevice(context: Context, imgName: String, imgUri: Uri) {
        val tempImgFile = File(context.getExternalFilesDir(null), imgName)
        val bitmap = getBitmap(context, imgUri)
        val fOut = FileOutputStream(tempImgFile)
        val matrix = Matrix()
        matrix.setRotate(0f)
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).compress(
            Bitmap.CompressFormat.JPEG,
            85,
            fOut
        )
        fOut.flush()
        fOut.close()
    }

    fun deleteImg(context: Context, mTempImgName: String) {
        val tempImgFile = File(context.getExternalFilesDir(null), mTempImgName)
        tempImgFile.delete()
    }

    fun getSystemTimeNow(): Long {
        return System.currentTimeMillis()
    }

    fun calculateTimeSincePosted(postTime: Long): String{
        val SECONDS_IN_DAY = 86400
        val SECONDS_IN_HOUR = 3600
        val SECONDS_IN_MINUTE = 60
        val timeDiffSecs = (Calendar.getInstance().timeInMillis - postTime)/1000.0

        return if (timeDiffSecs > SECONDS_IN_DAY)
            (timeDiffSecs / SECONDS_IN_DAY).toInt().toString() + " days ago"
        else if (timeDiffSecs < SECONDS_IN_DAY && timeDiffSecs > SECONDS_IN_HOUR)
            (timeDiffSecs / SECONDS_IN_HOUR).toInt().toString() + " hours ago"
        else
            (timeDiffSecs / SECONDS_IN_MINUTE).toInt().toString() + " minutes ago"
    }

    fun getUserID(): String{
        val auth = FirebaseAuth.getInstance()
        return auth.currentUser?.uid.toString()
    }
}
