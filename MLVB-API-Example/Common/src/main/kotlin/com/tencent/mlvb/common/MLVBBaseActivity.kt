package com.tencent.mlvb.common

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.*

abstract class MLVBBaseActivity : AppCompatActivity() {

    companion object {
        protected const val REQ_PERMISSION_CODE = 0x1000
    }

    protected var grantedCount = 0

    protected abstract fun onPermissionGranted()

    protected fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = mutableListOf<String>()

            val permissionsToCheck = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )

            for (permission in permissionsToCheck) {
                if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, permission)) {
                    permissions.add(permission)
                }
            }

            if (permissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    permissions.toTypedArray(),
                    REQ_PERMISSION_CODE
                )
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_PERMISSION_CODE -> {
                grantedCount = 0
                for (result in grantResults) {
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        grantedCount++
                    }
                }

                if (grantedCount == permissions.size) {
                    onPermissionGranted()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.common_please_input_roomid_and_userid),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                grantedCount = 0
            }
        }
    }

    fun generateStreamId(): String {
        val random = Random()
        var flag = random.nextInt(999999)
        if (flag < 100000) {
            flag += 100000
        }
        return flag.toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor(R.color.common_bg_color)
    }

    fun setStatusBarColor(colorId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(colorId, theme)
        }
    }
}