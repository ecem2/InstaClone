package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.FragmentAddPostBinding
import com.example.myapplication.ui.users.AddPostAdapter
import kotlinx.coroutines.flow.collectLatest



class AddPostFragment : Fragment() {
    private lateinit var _binding: FragmentAddPostBinding
    private val binding get() = _binding!!
    private lateinit var addPostAdapter: AddPostAdapter
    private val viewModel: AddPostViewModel by activityViewModels()
    var addPostData: ArrayList<String>? = ArrayList()
    private var selectedImagePath: String? = null //
    private val list: ArrayList<String> = ArrayList()
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var mediaPermissionLauncher: ActivityResultLauncher<String>
    var isReadGranted = false
    var isWriteGranted = false
    var isMediaGranted = false


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddPostBinding.inflate(inflater, container, false)
        val view = binding.root

        checkGalleryPermission()
        askRequiredPermission()
        viewModel.loadAllImages()
        setupCollecting()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnNext.setOnClickListener {
            val action = AddPostFragmentDirections.actionPostAddFragmentToAddPostNextFragment(selectedImagePath.toString())
            findNavController().navigate(action)
        }
        binding.imgClose.setOnClickListener {
            findNavController().popBackStack()
        }
    }


    private fun setupCollecting() {
        lifecycleScope.launchWhenStarted {
            viewModel.allImagesFromGallery.collectLatest { uriList ->
                if (uriList.isNotEmpty()) {
                    val firstUri = uriList[0]
                    val uriString = firstUri.toString()
                    Glide.with(requireContext())
                        .load(uriString)
                        .into(binding.addPhotoIV)

                    for (uri in uriList) {
                        list.add(uri.toString())
                    }
                    setupRecyclerView()
                    addPostAdapter.submitPostPhotoList(list)
                }

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkGalleryPermission() {
        mediaPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Log.d("ECO", "TRUE$isGranted")


                } else {
                    askPermissionForApi33()
                    //Log.d("ECCO", "FALSE$isGranted")
                }

            }

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isReadGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
                isWriteGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
            }
    }

    private fun askRequiredPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            askPermissionForApi33()
        } else {
            askPermissionForBelow11()
        }
    }

    companion object {
        fun newInstance(selectedImage: String): AddPostNextFragment {
            val fragment = AddPostNextFragment()
            val args = Bundle()
            args.putString("selectedImage", selectedImage)
            fragment.arguments = args
            return fragment
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun askPermissionForApi33() {
        isMediaGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED

        val mediaPermissionRequest: MutableList<String> = ArrayList()
        if (!isMediaGranted) {
            mediaPermissionRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
        }
        if (mediaPermissionRequest.isNotEmpty()) {
            mediaPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
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
        }
        if (!isWriteGranted) {
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }
    }


    private fun setupRecyclerView() {
        addPostAdapter = AddPostAdapter(
            requireContext(),
            addPostData!!,
            AddPostAdapter.OnClickListener { postItem ->
                if (postItem.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(postItem)
                        .into(binding.addPhotoIV)
                }

              selectedImagePath = postItem
            },
        )

        binding.postsAddRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = addPostAdapter
            setHasFixedSize(true)
        }
    }


}