package com.example.myapplication.ui.home


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.*
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.extension.Resource
import com.example.myapplication.extension.Status
import com.example.myapplication.model.UserModel
import com.example.myapplication.repositories.PostsDataRepositoryInterface
import com.example.myapplication.ui.adapter.ItemListAdapter
import com.example.myapplication.ui.adapter.StoryAdapter
import com.example.myapplication.ui.camera.CameraFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var postReference: DatabaseReference
    private lateinit var postAdapter: ItemListAdapter
    private lateinit var storyAdapter: StoryAdapter
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    var searchData: ArrayList<UserModel>? = ArrayList()
    private lateinit var selectedSize: String
//    private val viewModel: HomeViewModel by viewModels()
    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory }
    var storyData: ArrayList<UserModel>? = ArrayList()
    val user = Firebase.auth.currentUser!!.uid
    val currentUserStory = UserModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val view = binding.root
        database = Firebase.database.reference
        postReference = database.child("Posts")
        setupRecyclerView()
        setupRecyclerStoryView()

        return view
    }


    private fun setupRecyclerView() {
        postAdapter = ItemListAdapter(
            requireContext(),

            ItemListAdapter.OnLikeCountListener { likeItem ->
                val bundle = Bundle()
                bundle.putParcelable("clickedPost", likeItem)
                findNavController().navigate(R.id.likeFragment, bundle)
            },

            ItemListAdapter.OnLikeClickListener { likeItem ->

            },
            ItemListAdapter.OnCommentClickListener { commentItem ->
                val bundle = Bundle()
                bundle.putParcelable("clickedComment", commentItem)
                findNavController().navigate(R.id.commentFragment, bundle)
//                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCommentFragment(commentItem))
            },
            ItemListAdapter.OnCommentCountClickListener { commentItem ->
                val bundle = Bundle()
                bundle.putParcelable("clickedComment", commentItem)
                findNavController().navigate(R.id.commentFragment, bundle)

            },
            ItemListAdapter.OnNickNameClickListener { incomingUserData ->
                val currentUser = Firebase.auth.currentUser
                if (currentUser != null) {
                    findNavController().navigate(R.id.profileFragment)
                } else {
                    val bundle = Bundle()
                    bundle.putParcelable("clickedUserId", incomingUserData)
                    findNavController().navigate(R.id.userProfileFragment, bundle)
                }
            }
        )
        binding.postRV.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupRecyclerStoryView() {
        storyAdapter = StoryAdapter(
            requireContext(),
            storyData!!,
            StoryAdapter.OnClickListener { storyItem ->
                if (storyItem.userId == Firebase.auth.currentUser!!.uid) {
                    val cameraFragment = CameraFragment()
                    val fragmentTransaction = parentFragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.home_fragment_container, cameraFragment)
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                } else {
                    val bundle = Bundle()
                    bundle.putParcelable("clickedUserId", storyItem)
                    findNavController().navigate(R.id.storyClickFragment, bundle)
                }
            })

        binding.storiesRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = storyAdapter
            setHasFixedSize(true)
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        lifecycleScope.launch {
//            viewModel.getPostData()
//        }
        getPostData()
        viewModel.getUsersData()
        getUsersData()
        viewModel.getStoryData()
        getStoryData()
        selectedSize = getString(R.string.size_one)

        binding.messengerButton.setOnClickListener {
            val messageFragment = MessageFragment()
            val fragmentTransaction = parentFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.home_fragment_container,messageFragment )
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

    }


    private fun getPostData() {
        viewModel.fetchAllPostsData.observe(viewLifecycleOwner) { postArrayList ->
            when (postArrayList.status) {
                Status.SUCCESS -> {
                    binding.tvError.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.postRV.visibility = View.VISIBLE
                    postArrayList.data?.let { postAdapter.submitPostsList(it) }
                }

                Status.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.postRV.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                }

                Status.ERROR -> {
                    binding.postRV.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = postArrayList.message
                }
            }
        }
    }

    private fun getUsersData() {
        viewModel.usersData.observe(viewLifecycleOwner) { userArrayList ->
            postAdapter.submitUsersList(userArrayList)
        }
    }

    private fun getStoryData() {
        viewModel.storyData.observe(viewLifecycleOwner) { storyArrayList ->
            val user = Firebase.auth.currentUser!!.uid

            val databaseReference = FirebaseDatabase.getInstance().reference.child("Users").child(user)

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val userSnapshot = dataSnapshot.getValue(UserModel::class.java)
                        val userProfilePhoto = userSnapshot?.profilePhoto
                        val userName = userSnapshot?.userNickName

                        val currentUser = UserModel(
                            userId = user,
                            userNickName = userName,
                            profilePhoto = userProfilePhoto
                        )

                        if (storyArrayList.isEmpty()) {
                            storyArrayList.add(0, currentUser)
                        } else {
                            storyArrayList.add(0, currentUser)
                        }

                        storyAdapter.submitStoryList(storyArrayList)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }
    }
}


