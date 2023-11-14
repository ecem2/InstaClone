package com.example.myapplication.ui.camera

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.*
import android.hardware.display.DisplayManager
import android.os.*
import android.provider.MediaStore
import android.provider.Telephony.Mms.Part.FILENAME
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import androidx.window.layout.WindowMetricsCalculator
import androidx.work.await
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.FragmentCameraBinding
import com.example.myapplication.ui.main.MainActivity.Companion.KEY_EVENT_ACTION
import com.example.myapplication.ui.main.MainActivity.Companion.KEY_EVENT_EXTRA
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import android.provider.Telephony.Mms.Part.FILENAME
import com.example.myapplication.AddPostNextFragmentDirections
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


typealias LumaListener = (luma: Double) -> Unit

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var mediaStoreUtils: MediaStoreUtils

    var isReadGranted = false
    var isWriteGranted = false
    var isMediaGranted = false

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, Boolean> ->
        val cameraPermissionGranted = permissions[Manifest.permission.CAMERA] == true
        val readStoragePermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        val writeStoragePermissionGranted = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
        } else {
            true
        }

        if (cameraPermissionGranted && readStoragePermissionGranted && writeStoragePermissionGranted) {
            openCamera()
        } else {
            isCameraPermissionGranted()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        val view = binding.root
        askRequiredPermission()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        checkAndRequestCameraPermissions()

        if(hasCameraFeature()){
            binding.cameraButton.setOnClickListener {
                takePhoto()
            }
        }
        binding.cameraExit.setOnClickListener {
            val viewPager = requireActivity().findViewById<ViewPager2>(com.example.myapplication.R.id.homeViewPager)
            viewPager.currentItem = 1
        }

        if (isCameraPermissionGranted() && isReadStoragePermissionGranted() ) {
            Handler(Looper.getMainLooper()).postDelayed({
                openCamera()
            }, 3000)
        } else {
            requestCameraPermissions()
        }
    }
    private fun askRequiredPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            askPermissionForApi33()
        } else {
            askPermissionForBelow11()
        }
    }

    private fun askPermissionForApi33() {
        val cameraPermission = Manifest.permission.CAMERA
        val readPermission = Manifest.permission.READ_EXTERNAL_STORAGE
        val writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE

        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(requireContext(), cameraPermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(cameraPermission)
        }

        if (ContextCompat.checkSelfPermission(requireContext(), readPermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(readPermission)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(requireContext(), writePermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(writePermission)
        }

        if (permissionsToRequest.isNotEmpty()) {
            cameraPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            openCamera()
        }
    }

    private fun askPermissionForBelow11() {
        isReadGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isWriteGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val permissionRequest: MutableList<String> = ArrayList()
        if (!isReadGranted) {
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {

        }
        if (!isWriteGranted) {
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionRequest.isNotEmpty()) {
            cameraPermissionLauncher.launch(permissionRequest.toTypedArray())
        }
    }

    private fun requestCameraPermissions() {
        val permissionList = mutableListOf<String>()

        if (!isCameraPermissionGranted()) {
            permissionList.add(Manifest.permission.CAMERA)
        }
        if (!isReadStoragePermissionGranted()) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        cameraPermissionLauncher.launch(permissionList.toTypedArray())
    }

    private fun checkAndRequestCameraPermissions() {
        val permissionList = mutableListOf<String>()
        if (!isCameraPermissionGranted()) {
            permissionList.add(Manifest.permission.CAMERA)
        }
        if (!isReadStoragePermissionGranted()) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !isWriteStoragePermissionGranted()) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        cameraPermissionLauncher.launch(permissionList.toTypedArray())
    }
    private fun isReadStoragePermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isWriteStoragePermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun openCamera() {
        val preview = Preview.Builder().build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        imageCapture = ImageCapture.Builder()
            .setTargetRotation(Surface.ROTATION_0)
            .build()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get() ?: return@addListener

                // Unbind any existing use cases before rebinding
                cameraProvider.unbindAll()

                // Bind the camera use cases
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                // Set the surface provider for the preview
                preview.setSurfaceProvider(binding.pvCamera.surfaceProvider)
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing camera: ${e.message}", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
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
    private fun hasCameraFeature(): Boolean {
        return requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }
    private fun takePhoto() {
        Log.d(TAG, "takePhoto called")

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApplication")
            }
        }
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Log.d(TAG, msg)

                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

                    binding.apply {
                        cameraButton.visibility = View.GONE
                        imagePreview.visibility = View.VISIBLE

                        Glide.with(requireContext()).load(output.savedUri).into(imagePreview)
                    }

                    // Stop the camera after taking a photo
                    cameraProvider.unbindAll()
                }
            }
        )
    }
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraFragment"
        private const val PHOTO_TYPE = "image/jpeg"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}
