package com.example.myapplication.ui.story

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.FragmentStoryClickBinding
import com.example.myapplication.extension.parcelable
import com.example.myapplication.model.Story
import com.example.myapplication.model.UserModel
import com.example.myapplication.ui.adapter.StoryClickAdapter
import com.google.firebase.database.*
import jp.shts.android.storiesprogressview.StoriesProgressView

class StoryClickFragment : Fragment(), StoriesProgressView.StoriesListener {

    private var counter = 0
    private lateinit var database: DatabaseReference
    var storyData: ArrayList<String> = ArrayList()
    private lateinit var storyClickAdapter: StoryClickAdapter

    private var storiesProgressView: StoriesProgressView? = null
    private lateinit var binding: FragmentStoryClickBinding
    private var pressTime = 0L
    private var limit = 2000L
    var currentIndex = 0
    val userHasStory: ArrayList<Story> = ArrayList()
    private val storyList: ArrayList<String> = ArrayList()

    private var storyIndex = 0
    private var clickedUserId: String? = null
    private var userIndex = 0
    private val onTouchListener: View.OnTouchListener = object : View.OnTouchListener {
        override fun onTouch(view: View?, motionEvent: MotionEvent): Boolean {
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    pressTime = System.currentTimeMillis()
                    binding.storiesProgressView.pause()
                    return false
                }
                MotionEvent.ACTION_UP -> {
                    val now = System.currentTimeMillis()
                    binding.storiesProgressView.resume()
                    return limit < now - pressTime
                }
            }

            return false
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStoryClickBinding.inflate(inflater, container, false)
        val view = binding.root


        binding.storyExit.setOnClickListener {
            findNavController().popBackStack()
        }
        storiesProgressView = binding.storiesProgressView
        database = FirebaseDatabase.getInstance().reference

        // setupStoryView(storyList)
        getSelectedUserData()
        clickListeners()
        database.child("Users").orderByChild("userStory")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val storyData = snapshot.getValue(UserModel::class.java)
                        for (i in snapshot.children) {
                            currentIndex++
                            userHasStory.add(
                                Story(
                                    index = currentIndex,
                                    user = storyData!!
                                )
                            )
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val translation = it.parcelable<UserModel>("clickedUserId")
            if (translation != null) {
                Glide.with(requireContext())
                    .load(translation.profilePhoto)
                    .into(binding.storyProfilePhoto)
                binding.storyNickname.text = translation.userNickName
                Glide.with(requireContext())
                    .load(translation.userStory?.get(0))
                    .into(binding.storyPhoto)

                Log.d("salimmm", "translationtranslation ${translation}")
            }
        }
    }

    private fun setupStoryView(storyList: ArrayList<String>) {
        storiesProgressView?.setStoriesCount(storyList.size); // <- set stories
        storiesProgressView?.setStoryDuration(3000L); // <- set a story duration
        storiesProgressView?.setStoriesListener(this); // <- set listener
        storiesProgressView?.startStories()
    }

    private fun clickListeners() {
        binding.reverse.setOnClickListener {
            binding.storiesProgressView.reverse()
            Log.d("Ecemmm", "REVERSE CLICKED")

        }
        binding.reverse.setOnTouchListener(onTouchListener)

        binding.skip.setOnClickListener {
            binding.storiesProgressView.skip()
            Log.d("Ecemmm", "SKIP CLICKED")

        }
        binding.skip.setOnTouchListener(onTouchListener)
    }

    private fun getSelectedUserData() {
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        clickedUserId = sharedPref.getString(
            "clickedUserId",
            ""
        )
    }

    fun onStoryCompleted() {
        val navController = findNavController()
        navController.popBackStack()
    }

    override fun onNext() {
        for (i in userHasStory) {
            if (i.index == storyIndex) {
                Glide.with(requireContext()).load(i.user?.userStory?.get(++counter))
                    .into(binding.storyPhoto)
            }
        }
    }

    override fun onPrev() {
        if (counter - 1 < 0) return
        Glide.with(requireContext()).load(userHasStory[--counter])
            .into(binding.storyPhoto)
    }

    override fun onComplete() {
        onStoryCompleted()
    }

    override fun onDestroy() {
        binding.storiesProgressView.destroy()
        super.onDestroy()
    }
}