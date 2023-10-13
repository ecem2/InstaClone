package com.example.myapplication.ui.users

import android.content.Context
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


class UserProfilePostFragment : Fragment() {
    private lateinit var _binding: FragmentUserPostBinding
    private val binding get() = _binding!!
    private lateinit var usersAdapter: ProfileUsersAdapter
    var postsData: ArrayList<String> = ArrayList()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var postReference: DatabaseReference

    val database = FirebaseDatabase.getInstance().reference.child("Posts")


    private var clickedUserId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserPostBinding.inflate(inflater, container, false)
        val view = binding.root
        setupRecyclerView()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        clickedUserId = sharedPref.getString(
            "clickedUserId",
            ""
        ) // burda localden tiklanan kullaniciyi cekiyoruz
        Log.d("fatoss", "clickedUserId11111 $clickedUserId")


        clickedUserId?.let { getPostData(it) }
    }

    private fun setupRecyclerView() {
        usersAdapter = ProfileUsersAdapter(requireContext())
        binding.postsRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = usersAdapter
            setHasFixedSize(true)
        }
    }

//    private fun getPostData() {
//        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
//
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//
//
//
//
//               // usersAdapter.submitPostPhotoList(postsData)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//        })
//    }

    private fun getPostData(userUUID: String) {
        database.orderByChild("timestamp").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {

                        if (snapshot.exists()) {
                            val post: PostModel? = snapshot.getValue(PostModel::class.java)
                        //    Log.d("ecemmm", "post $post")
                            if (post?.userId == clickedUserId){
                                if (post?.postPhoto?.isNotEmpty() == true) {
                                    postsData.add(post.postPhoto[0])
                                    postsData.reverse()
                                }

                            }
                        }
                    }

                    usersAdapter.submitPostPhotoList(postsData)

                    Log.d("fatoss", "DATA $postsData")
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}
