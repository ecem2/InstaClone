package com.example.myapplication.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.PostModel
import com.example.myapplication.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// LiveData nedir
// Kotlin Flow nedir

class HomeViewModel : ViewModel() {

    private var database: DatabaseReference = Firebase.database.reference
    private val _postsData: MutableLiveData<ArrayList<PostModel>> = MutableLiveData()
    private val _usersData: MutableLiveData<ArrayList<UserModel>> = MutableLiveData()
    private val _storyData: MutableLiveData<ArrayList<UserModel>> = MutableLiveData()

    val postsData: LiveData<ArrayList<PostModel>> get() = _postsData
    val usersData: LiveData<ArrayList<UserModel>> get() = _usersData
    val storyData: LiveData<ArrayList<UserModel>> get() = _storyData

    private val _likeData: MutableLiveData<ArrayList<String>> = MutableLiveData()
    val likeData: LiveData<ArrayList<String>> get() = _likeData

    fun getPostData() = viewModelScope.launch(Dispatchers.IO) {
        val postList: ArrayList<PostModel> = ArrayList()
        val userLikedList: ArrayList<PostModel> = ArrayList()
        database.child("Posts")
            .orderByChild("timestamp").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children) {
                        if (snapshot.exists()) {
                            val post: PostModel? = i.getValue<PostModel>()
                            if (post != null) {
                                postList.add(post)
                            }
                        }
                    }

                    postList.reverse()
                    _postsData.postValue(postList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ViewModel", "POST ERROR ${error.message}")
                }
            })
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


    // todo usttekinden de cekilebilir
    fun getStoryData() = viewModelScope.launch(Dispatchers.IO) {
        val storyList: ArrayList<UserModel> = ArrayList()
        database.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshotUser: DataSnapshot) {
                for (snap in snapshotUser.children) {
                    val story = snap.getValue(UserModel::class.java)
                    if (story != null) {
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

}