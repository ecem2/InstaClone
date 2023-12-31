package com.example.myapplication.ui.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.databinding.FragmentUserPostBinding
import com.example.myapplication.model.PostModel
import com.example.myapplication.ui.adapter.ProfileUsersAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfilePostFragment : Fragment() {

    private lateinit var _binding: FragmentUserPostBinding
    private val binding get() = _binding!!
    private lateinit var usersAdapter: ProfileUsersAdapter
    var postsData: ArrayList<String> = ArrayList()

    val database = FirebaseDatabase.getInstance().reference.child("Posts")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserPostBinding.inflate(inflater, container, false)
        val view = binding.root



        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = FirebaseAuth.getInstance().currentUser
        profileRecyclerView()
        getPostData(user?.uid.toString())
    }

    private fun profileRecyclerView() {
        usersAdapter = ProfileUsersAdapter(requireContext())
        binding.postsRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = usersAdapter
            setHasFixedSize(true)
        }
    }


    private fun getPostData(userUUID: String) {
        database.orderByChild("timestamp").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postsData.clear()

                for (snapshot in dataSnapshot.children) {
                    if (snapshot.exists()) {
                        val post: PostModel? = snapshot.getValue(PostModel::class.java)
                        if (post != null && post.userId == userUUID) {
                            val photoUrl = post.postPhoto
                            photoUrl?.let {
                                postsData.add(it.toString())
                            }
                        }
                    }
                }
                postsData.reverse()

                usersAdapter.submitPostPhotoList(postsData)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        postsData.clear()

    }
}
