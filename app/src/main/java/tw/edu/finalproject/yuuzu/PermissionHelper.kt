package tw.edu.finalproject.yuuzu

import android.content.Context
import android.os.Build
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class PermissionHelper(private val context: Context) : MultiplePermissionsListener {

    fun requestPermission() {
        requestCamera()
        requestStorage()
    }

    private fun requestCamera() {
        Dexter.withContext(context)
            .withPermissions(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.INTERNET,
            )
            .withListener(this)
            .check()
    }

    private fun requestStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Dexter.withContext(context)
                .withPermissions(
                    android.Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                )
                .withListener(this)
                .check()
        } else {
            Dexter.withContext(context)
                .withPermissions(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .withListener(this)
                .check()
        }
    }

    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
        p0.let {

        }
    }

    override fun onPermissionRationaleShouldBeShown(
        p0: MutableList<PermissionRequest>?,
        p1: PermissionToken?
    ) {
        p1?.continuePermissionRequest()
    }
}