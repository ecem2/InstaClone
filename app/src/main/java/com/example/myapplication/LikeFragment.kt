package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentLikeBinding
import com.example.myapplication.extension.parcelable
import com.example.myapplication.model.PostModel
import com.example.myapplication.model.ProfileModel
import com.example.myapplication.model.UserModel
//import com.example.myapplication.ui.home.HomeFragmentDirections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class LikeFragment : Fragment() {
    private lateinit var _binding: FragmentLikeBinding
    private val binding get() = _binding!!
    private var likeList: ArrayList<UserModel>? = ArrayList()
    private lateinit var databaseReference: DatabaseReference
    private var likeAdapter: LikeAdapter? = null
    var likeData: ArrayList<UserModel> = ArrayList()
    private lateinit var currentUserUid: String
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLikeBinding.inflate(inflater, container, false)
        val view = binding.root

        currentUserUid = Firebase.auth.currentUser!!.uid
        setupRecyclerLikeView()
        databaseReference = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.likeBack.setOnClickListener {
            findNavController().popBackStack()
        }
        arguments?.let {
            val postModel = it.parcelable<PostModel>("clickedPost")
            if (postModel != null) {
                getLikeData(postModel)
                Log.d("SALIMM", "translationtranslation ${postModel}")
            }
        }
    }


    private fun setupRecyclerLikeView() {
        likeAdapter = LikeAdapter(
            requireContext(),
            likeList!!
        )

        binding.likeRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = likeAdapter
            setHasFixedSize(true)
        }
    }


    private fun getLikeData(postModel: PostModel) {
        val userIdList: ArrayList<String> = ArrayList()

        if (postModel.likeArray?.isNotEmpty() == true) {
            postModel.likeArray.forEach {
                userIdList.add(it)
            }
        }

        getUsers(userIdList)
    }

    private fun getUsers(userList: ArrayList<String>) {
        databaseReference.child("Users").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChildren()) {
                        for (snap in snapshot.children) {
                            val user = snap.getValue(UserModel::class.java)
                            if (user != null) {
                                if (userList.contains(user.userId.toString())) {
                                    likeData.add(user)
                                }
                            }
                        }
                    }
                }

                Log.e("salimmm", "likeData $likeData")

                likeData.let { likeAdapter?.submitLikeList(it) }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("salimmm", "getUsers ${error.message}")
            }
        })
    }
}



