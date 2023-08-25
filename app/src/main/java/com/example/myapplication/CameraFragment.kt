package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Camera
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentCameraBinding
import com.example.myapplication.databinding.FragmentProfileBinding
import com.example.myapplication.ui.login.LoginActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.otaliastudios.cameraview.CameraView

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class CameraFragment : Fragment() {
    private var myCamera: CameraView? = null
    private val REQUEST_CODE = 200
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("Permission: ", "Granted")
               capturePhoto() // Kamera izni alındıktan sonra fotoğraf çekimini başlat
            } else {
                Log.i("Permission: ", "Denied")
            }
        }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        requestCameraPermission(View(requireContext()))
        return view
    }

    private fun requestCameraPermission(view: View) {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Kamera izni zaten verilmiş, burada gerekirse kamera görünümünü başlatabilirsiniz.
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.CAMERA
            ) -> {
                showSnackbar(
                    view,
                    getString(R.string.permission_required),
                    Snackbar.LENGTH_INDEFINITE,
                    getString(R.string.ok)
                ) {
                    requestPermissionLauncher.launch(
                        Manifest.permission.CAMERA
                    )
                }
            }

            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }


    private fun capturePhoto() {
        val cameraIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
        startActivityForResult(cameraIntent, REQUEST_CODE)
    }

    private fun showSnackbar(
        view: View,
        msg: String,
        length: Int,
        actionMessage: CharSequence?,
        action: (View) -> Unit
    ) {
        val snackbar = Snackbar.make(view, msg, length)
        if (actionMessage != null) {
            snackbar.setAction(actionMessage) {
                action(view)
            }.show()
        } else {
            snackbar.show()
        }
    }


    override fun onResume() {
        super.onResume()
        Log.e("ERROR", "CAMERA FRAGMENT ON RESUME")
        //myCamera!!.start()
    }

    override fun onPause() {
        super.onPause()
        Log.e("ERROR", "CAMERA FRAGMENT ON PAUSE")
        //myCamera.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("ERROR", "CAMERA FRAGMENT ON DESTROY")
        if (myCamera != null) {
            myCamera!!.destroy()
        }
    }


}