package com.example.myapplication


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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class AddPostNextFragment : Fragment() {

    private lateinit var binding: FragmentAddPostNextBinding
    private val addPostArgs: AddPostNextFragmentArgs by navArgs()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var currentUserUid: String
    private val selectedImage : ArrayList<String> = ArrayList()
    private lateinit var photoInfo: String
    private lateinit var timestamp: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddPostNextBinding.inflate(inflater, container, false)
        val view = binding.root
        firebaseAuth = FirebaseAuth.getInstance()

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
            binding.progressBar.visibility = View.VISIBLE
            val postId = databaseReference.push().key.toString()
            photoInfo = binding.etPhotoInfo.text.toString() ?: ""
            timestamp = System.currentTimeMillis().toString()
            addDataToFirebase(
                currentUserUid,
                selectedImage,
                photoInfo,
                timestamp,
                postId
            )
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
        databaseReference = Firebase.database.reference.child("Posts")
        currentUserUid = firebaseAuth.currentUser!!.uid
        selectedImage.add(addPostArgs.selectedImage.toUri().toString())


    }


}