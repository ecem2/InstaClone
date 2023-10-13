package com.example.myapplication.ui.users

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentUserProfileBinding
import com.example.myapplication.extension.parcelable
import com.example.myapplication.model.PostModel
import com.example.myapplication.model.UserModel
import com.example.myapplication.ui.home.HomeViewModel
import com.example.myapplication.ui.profile.ProfileViewPagerAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.util.concurrent.Executors

class UserProfileFragment : Fragment() {

    private lateinit var _binding: FragmentUserProfileBinding
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var postReference: DatabaseReference
    private val followersList: ArrayList<String> = ArrayList()
    private val followingList: ArrayList<String> = ArrayList()
    val user = Firebase.auth.currentUser!!.uid
    val args: UserProfileFragmentArgs by navArgs()
    private var clickedUserId: String? = null
    var incomingUserData: UserModel? = null


    val database = FirebaseDatabase.getInstance().reference
    private var postList: ArrayList<String> = ArrayList()
    val viewModel: HomeViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        firebaseAuth = FirebaseAuth.getInstance()
        postReference = database.child("Posts")
        setUpProfileViewPager()

        return view
    }

    private fun setUpProfileViewPager() {
        val viewPager = binding.userProfileViewPager
        viewPager.adapter = ProfileViewPagerAdapter(requireActivity().supportFragmentManager)
        viewPager.currentItem = 0

        val tabLayout = binding.tabLayout
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.profileBack.setOnClickListener {
            findNavController().popBackStack()
        }

        incomingUserData = args.searchData
        incomingUserData?.let { getUsersData(it) }

        Log.d("salimmm", "translationtranslation ${incomingUserData?.followersArray?.size}")
//        arguments?.let {
//            incomingUserData = it.parcelable("clickedUserId")
//            if (incomingUserData != null) {
//                getUsersData(incomingUserData!!)
//                Log.d("salimmm", "translationtranslation ${incomingUserData?.followersArray?.size}")
//            }
//        }
        getPostData(user)
        getFollowers()
        getFollowing()



        binding.optionButton.setOnClickListener {
            showBottomSheet()
        }
        binding.profileFollowButton.setOnClickListener {
            followUser()
            if (binding.usersLL.visibility == View.GONE) {
                binding.usersLL.visibility = View.VISIBLE
                binding.profileFollowButton.visibility = View.GONE
            }

            binding.messageButton.visibility = View.VISIBLE;
        }
        binding.followedButton.setOnClickListener {
            followUser()
            if (binding.profileFollowButton.visibility == View.GONE) {
                binding.profileFollowButton.visibility = View.VISIBLE
                binding.usersLL.visibility = View.GONE
            }
        }

    }


    private fun getFollowers() {
        var count = 0
        val followers = database.child("Users").child(incomingUserData?.userId.toString())
            .child("followersArray")
        followers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children) {
                    count += 1
                }
                binding.followerCount.text = count.toString()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getFollowing() {
        val followers = database.child("Users").child(incomingUserData?.userId.toString())
            .child("followingArray")
        followers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.followCount.text = snapshot.childrenCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    private fun showBottomSheet() {
        val dialogView = layoutInflater.inflate(R.layout.item_bottom_sheet, null)
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogThemeTrans)

        dialog.setContentView(dialogView)
        dialog.show()
    }

    private fun followUser() {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {

            requireActivity().runOnUiThread {
                setFollowersArray()
                setFollowingArray(incomingUserData?.userId.toString())
                getFollowers()
            }
        }
    }

    private fun setFollowingArray(userId: String) {
        val following = database.child("Users").child(
            firebaseAuth.currentUser!!.uid
        ).child("followingArray")

        followingList.add(userId)
        following.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children) {
                    if (followingList.contains(i.value.toString())) {
                        followingList.remove(i.value.toString())
                        followingList.clear()
                    } else {
                        followingList.add(i.value.toString())
                    }
                }
                following.setValue(followingList)
                followingList.clear()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    private fun setFollowersArray() {
        followersList.add(firebaseAuth.currentUser!!.uid)
        val followers =
            database.child("Users")
                .child(incomingUserData?.userId.toString())
                .child("followersArray")

        followers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children) {
                    Log.d("ecemmm", "QQQQQ $i")
                    if (followersList.contains(i.value.toString())) {
                        followersList.remove(i.value.toString())
                        followersList.clear()
                    } else {
                        followersList.add(i.value.toString())
                    }
                }
                followers.setValue(followersList)
                followersList.clear()
            }


            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    private fun getUsersData(userModel: UserModel) {

        Log.d("salimmm", "TTTTT ${userModel.followingArray}")
        binding.followCount.text = if (userModel.followingArray == null) {
            "0"
        } else {
            userModel.followingArray.size.toString()
        }

        val userName = userModel.userName ?: ""
        val surname = userModel.userSurname ?: ""
        binding.nameTV.text = "$userName $surname"

        binding.toolUserNickName.text = userModel.userNickName
        binding.userBio.text = userModel.bioInfo


        Glide.with(requireContext()).load(userModel.profilePhoto)
            .into(binding.usersProfile)

        Log.d("fatos", "FOLLOWERS COUNT ${userModel.followersArray?.size}")
    }

    private fun getPostData(userId: String) {
        database.child("Posts").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snap in snapshot.children) {
                        val post = snapshot.getValue(PostModel::class.java)
                        val photoUrl = snapshot.getValue(PostModel::class.java)!!.postPhoto
                        if (post?.userId.toString() == userId) {
                            postList.add(photoUrl.toString())
                        }
                    }
                    binding.postCountTitle.text = postList.size.toString()

                }


                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    companion object {
        private const val TAG = "UserProfileFragment"
    }
}