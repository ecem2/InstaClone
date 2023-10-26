package com.example.myapplication.ui.profile.currentUserProfile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentProfileBinding
import com.example.myapplication.model.PostModel
import com.example.myapplication.model.ProfileModel
import com.example.myapplication.model.UserModel
import com.example.myapplication.ui.adapter.ProfileVpAdapter
import com.example.myapplication.ui.login.LoginActivity
import com.example.myapplication.ui.users.UserProfileFragmentArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {
    private lateinit var _binding: FragmentProfileBinding
    private val binding get() = _binding!!
    private var postList: ArrayList<String> = ArrayList()
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        firebaseAuth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = Firebase.auth.currentUser!!.uid
       // Log.d("ecemmm", "user ==$user")
        getFollowerCount(user)
        getFollowingCount(user)
        getUsersData(user)
        profileViewPager()
        getPostSize(user)
        binding.settingsButton.setOnClickListener {
            showProfileBottomSheet()
        }

    }
    private fun profileViewPager() {
        val viewPager = binding.profileViewPager
        viewPager.adapter = ProfileVpAdapter(requireActivity().supportFragmentManager)
        viewPager.currentItem = 0

        val tabLayout = binding.profileTabLayout
        tabLayout.setupWithViewPager(viewPager)

        val icons = intArrayOf(
            R.drawable.ic_grid,
            R.drawable.ic_reels,
            R.drawable.ic_tag
        )

        tabLayout.getTabAt(0)?.setIcon(icons[0])
        tabLayout.getTabAt(1)?.setIcon(icons[1])
        tabLayout.getTabAt(2)?.setIcon(icons[2])
    }

    private fun getUsersData(userId: String) {
        database.child("Users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshotUser: DataSnapshot) {
                    if (snapshotUser.exists()) {
                        val profileData = snapshotUser.getValue(UserModel::class.java)
                    //    Log.d("ecemmm", "ALL DATA $profileData")


                        val profilePhoto = profileData?.profilePhoto
                        binding.profileName.text = profileData?.userName
                        binding.toolBarNickName.text = profileData?.userNickName
                        binding.profileBio.text = profileData?.bioInfo


                        if (profileData?.profilePhoto != null) {
                            Glide.with(requireContext())
                                .load(profilePhoto)
                                .into(binding.profileFragmentPhoto)
                        } else {
                            Glide.with(requireContext())
                                .load(R.drawable.ic_fifth)
                                .into(binding.profileFragmentPhoto)
                        }

                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
    private fun showProfileBottomSheet() {
        val dialogView = layoutInflater.inflate(R.layout.item_profile_bottom_sheet, null)
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogThemeTrans)
        dialog.setContentView(dialogView)
        dialog.show()
        val logOut = dialog.findViewById<LinearLayoutCompat>(R.id.logOutLY)

            logOut?.setOnClickListener {
                //val fragmentManager = requireActivity().supportFragmentManager
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
            }


    }

    private fun getFollowerCount(userId: String) {
        val followerCount = database.child("Users").child(userId).child("followersArray")
        followerCount.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                    binding.profileFollowerCount.text = if (snapshot.hasChildren()) {
                        snapshot.childrenCount.toString()

                    } else {
                        "0"
                    }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    private fun getFollowingCount(userId: String) {
        val userFollowingList = ArrayList<String>()
        val followingCount = database.child("Users").child(userId).child("followingArray")
        followingCount.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChildren()) {
                    for (i in snapshot.children) {
                        userFollowingList.add(i.toString())
                    }
                    binding.profileFollowCount.text = userFollowingList.size.toString()
                } else {
                    binding.profileFollowCount.text = "0"
                    }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    private fun getPostSize(userId: String) {
        database.child("Posts")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postList.clear()
                    for (snap in snapshot.children) {
                        val post = snap.getValue(PostModel::class.java)
                        if (post?.userId == userId) {
                            postList.add(post.toString())
                        }
                    }
                    binding.profilePostCountTitle.text = postList.size.toString()
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

}
