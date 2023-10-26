package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.FragmentAddPostNextBinding
import com.example.myapplication.extension.hideKeyboard
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask

class AddPostNextFragment : Fragment() {

    private lateinit var binding: FragmentAddPostNextBinding
    private val addPostArgs: AddPostNextFragmentArgs by navArgs()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var currentUserUid: String
    private lateinit var photoInfo: String
    private lateinit var timestamp: String
    private val selectedImage : ArrayList<Uri> = ArrayList()
    private var myUrl = ""
    private val urlList: ArrayList<String> = ArrayList()
    private var storagePostPicRef: StorageReference? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddPostNextBinding.inflate(inflater, container, false)
        val view = binding.root
        firebaseAuth = FirebaseAuth.getInstance()
        storagePostPicRef = FirebaseStorage.getInstance().reference.child("PostPhoto")
        databaseReference = Firebase.database.reference.child("Posts")
        currentUserUid = firebaseAuth.currentUser!!.uid
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getSelectedUserData()

        binding.imgBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnShare.setOnClickListener {
            hideKeyboard()
            binding.llProgress.visibility = View.VISIBLE
            uploadImage()
        }

    }

    private fun addDataToFirebase(
        userUid: String,
        postPhoto: ArrayList<String>,
        photoInfo: String,
        timestamp: String,
        postUid: String
    ) {
        val newPost: HashMap<String, Any> = HashMap()
        newPost["userId"] = userUid
        newPost["postPhoto"] = postPhoto
        newPost["photoInfo"] = photoInfo
        newPost["timestamp"] = timestamp
        newPost["postId"] = postUid

        databaseReference.child(postUid).updateChildren(newPost).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                binding.llProgress.visibility = View.GONE
                navigateToHome()
            } else {
                binding.llProgress.visibility = View.GONE
                Toast.makeText(requireContext(), "Post could not load", Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show()
            binding.llProgress.visibility = View.GONE
        }
    }

    private fun navigateToHome() {
        val action = AddPostNextFragmentDirections.actionAddPostNextFragmentToHomePageFragment()
        findNavController().navigate(action)
    }

    private fun getSelectedUserData() {
        selectedImage.add(addPostArgs.selectedImage.toUri())
        Glide.with(requireContext()).load(selectedImage[0]).into(binding.ivPostImage)
    }

    private fun uploadImage() {
        val fileRef = storagePostPicRef!!.child(System.currentTimeMillis().toString() + ".jpg")
        var uploadTask: StorageTask<*>
        uploadTask = fileRef.putFile(selectedImage[0])
        uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    Log.e(TAG, "errorrr ${it.localizedMessage}")
                    throw it
                }
            } else {
                Log.e(TAG, "NO ERROR")
            }
            fileRef.downloadUrl
        }).addOnSuccessListener { photoUri->
            myUrl = photoUri.toString()
            urlList.add(myUrl)
            Log.e(TAG, "download passed")

            val ref = FirebaseDatabase.getInstance().reference.child("Posts")
            val postId = ref.push().key
            photoInfo = binding.etPhotoInfo.text.toString()
            timestamp = System.currentTimeMillis().toString()

            addDataToFirebase(
                currentUserUid,
                urlList,
                photoInfo,
                timestamp,
                postId.toString()
            )
        }
    }

    companion object {
        private const val TAG = "ecemmmAddPostNextFragment"

    }
}