package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.TextUtils
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

// https://www.youtube.com/watch?v=S-7H72UTiBU&ab_channel=ProgrammingHut -- camera icin
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

        getSelectedUserData()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("Eccco", "$selectedImage")
        Glide.with(requireContext()).load(selectedImage[0]).into(binding.ivPostImage)

        binding.imgBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnShare.setOnClickListener {
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
        binding.progressBar.visibility = View.VISIBLE
        val newPost: HashMap<String, Any> = HashMap()
        newPost["userId"] = userUid
        newPost["postPhoto"] = postPhoto
        newPost["photoInfo"] = photoInfo
        newPost["timestamp"] = timestamp
        newPost["postId"] = postUid

        databaseReference.child(postUid).updateChildren(newPost).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Ecccoo", "SUCCESSFUL === ${task.isSuccessful}")
                binding.progressBar.visibility = View.GONE
                navigateToHome()
            } else {
                Log.d("Ecccoo", "ERROR")
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Post could not load", Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
            Log.d("Ecccoo", "FAILURE === ${exception.message}")
        }
    }

    private fun navigateToHome() {
        val action = AddPostNextFragmentDirections.actionAddPostNextFragmentToHomePageFragment()
        findNavController().navigate(action)
    }

    private fun getSelectedUserData() {
        selectedImage.add(addPostArgs.selectedImage.toUri())
    }

    private fun uploadImage() {
        if (TextUtils.isEmpty(binding.etPhotoInfo.text.toString())) {
            Toast.makeText(requireContext(), "Please write description", Toast.LENGTH_SHORT).show()
        } else {
            val fileRef = storagePostPicRef!!.child(System.currentTimeMillis().toString() + ".jpg")
            var uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(selectedImage[0])
            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        Log.e("salimmmFirebase", "errorrr ${it.localizedMessage}")
                        throw it
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnSuccessListener {
                storagePostPicRef!!.downloadUrl.addOnSuccessListener {
                    myUrl = it.toString()
                    urlList.add(myUrl)
                    Log.e("salimmmFirebase", "download passed")

                    val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                    val postId = ref.push().key

                    addDataToFirebase(
                        currentUserUid,
                        urlList,
                        photoInfo,
                        timestamp,
                        postId.toString()
                    )
                }
            }


//                val fileRef = storagePostPicRef!!.child(System.currentTimeMillis().toString() + ".jpg")
//                var uploadTask: StorageTask<*>
//                uploadTask = fileRef.putFile(selectedImage[0].toUri())
//                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>> { task ->
//                    if (!task.isSuccessful) {
//                        task.exception?.let {
//                            throw it
//                        }
//                    }
//                    return@Continuation fileRef.downloadUrl
//                }).addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        val downloadUrl = task.result
//                        myUrl = downloadUrl.toString()
//
//                        val ref = FirebaseDatabase.getInstance().reference.child("Posts")
//                        val postId = ref.push().key
//
//                        addDataToFirebase(
//                            currentUserUid,
//                            selectedImage,
//                            photoInfo,
//                            timestamp,
//                            postId.toString()
//                        )
//                    }
//                }
        }
    }

    companion object {
        private const val TAG = "AddPostNextFragment"

    }
}