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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import jp.shts.android.storiesprogressview.StoriesProgressView

class StoryClickFragment : Fragment(), StoriesProgressView.StoriesListener {

    private var counter = 0
    private lateinit var database: DatabaseReference
    private var storiesProgressView: StoriesProgressView? = null
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var binding: FragmentStoryClickBinding
    private var pressTime = 0L
    private var limit = 2000L
    var currentIndex = 0
    val userHasStory: ArrayList<Story> = ArrayList()
    private val storyList: ArrayList<String> = ArrayList()
    private var storyIndex = 0
    private var clickedUserId: String? = null
    private var userIndex = 0
    var userId: String? = null
    var storyData: ArrayList<String> = ArrayList()

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


        firebaseAuth = FirebaseAuth.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid
        binding.storyExit.setOnClickListener {
            findNavController().popBackStack()
        }
        storiesProgressView = binding.storiesProgressView

        database = FirebaseDatabase.getInstance().reference
        fetchUserStoriesFromFirebase()
        getSelectedUserData()
        clickListeners()
//        database.child("Users").child(clickedUserId!!).child("userStory")
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.exists()) {
//                        for (childSnapshot in snapshot.children) {
//                            val storyUrl = childSnapshot.getValue(String::class.java)
//                            if (storyUrl != null) {
//                                storyList.add(storyUrl)
//                            }
//                        }
//                        // Verileri aldıktan sonra setupStoryView'ı çağırın
//                        setupStoryView(storyList)
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    // Hata durumunda yapılacak işlemler burada
//                }
//            })

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
                binding.storiesProgressView.visibility = View.VISIBLE
                Log.d("salimmm", "translationtranslation ${translation}")
            }
        }
    }
    private fun fetchUserStoriesFromFirebase() {
        database.child("Users").child(userId!!).child("userStory")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val storyData = snapshot.getValue(UserModel::class.java)
                        for (childSnapshot in snapshot.children) {
                            val storyUrl = childSnapshot.getValue(String::class.java)
                            if (storyUrl != null) {
                                storyList.add(storyUrl)
                                Log.d("storyList", "$storyList")
                                currentIndex++
                                userHasStory.add(
                                    Story(
                                        index = currentIndex,
                                        user = storyData!!
                                    )
                                )
                            }
                        }

                        // Verileri aldıktan sonra setupStoryView'ı çağırın
                       setupStoryView(storyList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Hata durumunda yapılacak işlemler burada
                }
            })
    }

    private fun setupStoryView(storyList: ArrayList<String>) {
        storiesProgressView = binding.storiesProgressView
        Log.d("StoryClickFragment", "storiesProgressView: $storiesProgressView")

        storiesProgressView?.setStoriesCount(storyList.size)
        Log.d("StoryClickFragment", "setStoriesCount called")

        storiesProgressView?.setStoryDuration(3000L)
        Log.d("StoryClickFragment", "setStoryDuration called")

        storiesProgressView?.setStoriesListener(this)
        Log.d("StoryClickFragment", "setStoriesListener called")

      //  storiesProgressView?.startStories()
        Log.d("StoryClickFragment", "startStories called")
    }

    private fun clickListeners() {
        binding.reverse.setOnClickListener {
            binding.storiesProgressView.reverse()
            Log.d("Ecemmm", "REVERSE CLICKED")

        }
        binding.reverse.setOnTouchListener(onTouchListener)

        binding.skip.setOnClickListener {
            binding.storiesProgressView.skip()
            counter++
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
        if (counter < userHasStory.size - 1) {
            counter++
            Glide.with(requireContext()).load(userHasStory[counter].user?.userStory?.get(storyIndex))
                .into(binding.storyPhoto)
        } else {
            // Tüm hikayeleri tamamladıysanız bir sonraki sayfaya gitmek için
            onStoryCompleted()
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