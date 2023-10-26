package com.example.myapplication.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.myapplication.AddPostNextFragment
import com.example.myapplication.InstaApplication
import com.example.myapplication.extension.Resource
import com.example.myapplication.model.Comment
import com.example.myapplication.model.PostModel
import com.example.myapplication.model.UserModel
import com.example.myapplication.repositories.PostsDataRepositoryInterface
import com.example.myapplication.repositories.PostsDataRepositoryRepositoryImpl
import com.example.myapplication.ui.adapter.ItemListAdapter
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// LiveData nedir
// Kotlin Flow nedir

class HomeViewModel(
    private val repo: PostsDataRepositoryRepositoryImpl
) : ViewModel() {

    private var database: DatabaseReference = Firebase.database.reference

    private val _postsData: MutableLiveData<Resource<ArrayList<PostModel>>> = MutableLiveData()
    val postsData: LiveData<Resource<ArrayList<PostModel>>> get() = _postsData

    private val _usersData: MutableLiveData<ArrayList<UserModel>> = MutableLiveData()
    val usersData: LiveData<ArrayList<UserModel>> get() = _usersData

    private val _storyData: MutableLiveData<ArrayList<UserModel>> = MutableLiveData()
    val storyData: LiveData<ArrayList<UserModel>> get() = _storyData

    private val _likeData: MutableLiveData<ArrayList<String>> = MutableLiveData()
    val likeData: LiveData<ArrayList<String>> get() = _likeData


    val fetchAllPostsData = liveData(Dispatchers.IO) {
        emit(Resource.loading(null))
        try{
            repo.getAllPosts().collect { posts->
                emit(posts)
            }
        } catch (e: Exception){
            emit(Resource.error(e.message.toString(), null))
        }
    }

    suspend fun getPostData() = viewModelScope.launch {
//         _postsData.postValue(Resource.loading(null))
//        val postList = repo.getAllPosts()
//
//        if (postList.data.isNullOrEmpty()) {
//            _postsData.postValue(Resource.error(postList.message.toString(), null))
//        } else {
//            _postsData.postValue(postList)
//        }


//        val postList: ArrayList<PostModel> = ArrayList()
//        database.child("Posts")
//            .orderByChild("timestamp")
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.exists()) {
//                        for (i in snapshot.children) {
//                            val postHashMap = i.getValue<HashMap<String, Any>>()
//                            val postModel = PostModel(
//                                userId = postHashMap?.get("userId") as? String,
//                                timestamp = postHashMap?.get("timestamp") as? String,
//                                isLike = postHashMap?.get("isLike") as? Boolean ?: false,
//                                postPhoto = postHashMap?.get("postPhoto") as? ArrayList<String>,
//                                likeArray = postHashMap?.get("likeArray") as? ArrayList<String>,
//                                postId = postHashMap?.get("postId") as? String,
//                                //comments = postHashMap?.get("comments") as? ArrayList<Comment>
//                            )
//                            postList.add(postModel)
//                        }
//                        //_postsData.value?.clear()
//                        postList.reverse()
//                        _postsData.postValue(postList)
//                    } else {
//                        Log.d("ecco", "aaaa $_postsData")
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Log.e("ViewModel", "POST ERROR ${error.message}")
//                }
//            })
    }


    fun getUsersData() = viewModelScope.launch(Dispatchers.IO) {
        val userList: ArrayList<UserModel> = ArrayList()

        database.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshotUser: DataSnapshot) {
                for (snap in snapshotUser.children) {
                    val users = snap.getValue(UserModel::class.java)
                    if (users != null) {
                        userList.add(users)
                    }
                }

                _usersData.postValue(userList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ecemmm", "DatabaseError === ${error.message}")
            }
        })
    }

    fun getStoryData() = viewModelScope.launch(Dispatchers.IO) {
        val storyList: ArrayList<UserModel> = ArrayList()

        database.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshotUser: DataSnapshot) {
                for (snap in snapshotUser.children) {
                    val story = snap.getValue(UserModel::class.java)

                    if (story != null && story.userStory != null && story.userStory.isNotEmpty()) {
                        storyList.add(story)
                    }
                }
                _storyData.postValue(storyList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ecemmm", "DatabaseError === ${error.message}")
            }
        })
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
            ): T {
                return HomeViewModel(
                    PostsDataRepositoryRepositoryImpl()
                ) as T
            }
        }
    }
}