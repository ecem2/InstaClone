package com.example.myapplication.ui.home


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.AddPostFragmentDirections
import com.example.myapplication.HomePageFragmentDirections
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.model.UserModel
import com.example.myapplication.ui.adapter.ItemListAdapter
import com.example.myapplication.ui.adapter.StoryAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


// TODO Fragmentta Activitydki 'this' yerine requireContext() KULLANILIR!!!!

class HomeFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var postReference: DatabaseReference
    private lateinit var postAdapter: ItemListAdapter
    private lateinit var storyAdapter: StoryAdapter
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    var searchData: ArrayList<UserModel>? = ArrayList()
    val viewModel: HomeViewModel by viewModels()
    var storyData: ArrayList<UserModel>? = ArrayList()
    val user = Firebase.auth.currentUser!!.uid

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
                Log.d("Ecemmm", "AAAAAA  $likeItem")
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
            },
            ItemListAdapter.OnCommentCountClickListener { commentItem ->
                val bundle = Bundle()
                bundle.putParcelable("clickedComment", commentItem)
                findNavController().navigate(R.id.commentFragment, bundle)

            },
            ItemListAdapter.OnNickNameClickListener { textItem ->
                Log.d("salimmm", "textItem ${textItem}")
                val bundle = Bundle()
                bundle.putParcelable("clickedUserId", textItem)
                findNavController().navigate(R.id.userProfileFragment, bundle)
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
                val bundle = Bundle()
                bundle.putParcelable("clickedUserId", storyItem)
                findNavController().navigate(R.id.storyClickFragment, bundle)


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
        viewModel.getPostData()
        getPostData()
        viewModel.getUsersData()
        getUsersData()
        viewModel.getStoryData()
        getStoryData()

    }

    private fun getPostData() {
        viewModel.postsData.observe(viewLifecycleOwner) { postArrayList ->
            postAdapter.submitPostsList(postArrayList)
        }
    }

    private fun getUsersData() {
        viewModel.usersData.observe(viewLifecycleOwner) { userArrayList ->
            postAdapter.submitUsersList(userArrayList)
        }
    }

    private fun getStoryData() {
        viewModel.storyData.observe(viewLifecycleOwner) { storyArrayList ->
            storyAdapter.submitStoryList(storyArrayList)

        }
    }
}

