package com.example.myapplication.repositories

import android.util.Log
import com.example.myapplication.extension.Resource
import com.example.myapplication.model.Comment
import com.example.myapplication.model.PostModel
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class PostsDataRepositoryRepositoryImpl(

): PostsDataRepositoryInterface {

    val database: DatabaseReference = Firebase.database.reference
    val postList: ArrayList<PostModel> = ArrayList()
    var filteredPosts: List<PostModel> = emptyList()

    fun removeRepeats(list: List<Any>): List<Any> {
        val last = if (list.size > 1) removeRepeats(list.drop(1)) else emptyList()
        return if (last.isNotEmpty() && last.first() == list.first()) last else listOf(list.first()) + last
    }
    override suspend fun getAllPost(): Resource<ArrayList<PostModel>> = withContext(Dispatchers.IO) {
        try {
            Resource.loading(null)
            database.child("Posts")
                .orderByChild("timestamp")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (i in snapshot.children) {
                                val post = i.getValue<PostModel>()
                                if (post != null) {
                                    postList.add(post)
                                }
                            }
                            postList.reverse()
                            Log.e("ecco", "POST DATA REPO postList111111 = $postList")
                        } else {
                            Log.e("ecco", "POST DATA REPO postList222 = $postList")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("", "POST ERROR ${error.message}")
                        Resource.error(error.message, null)
                    }
                })
            Resource.success(postList)
        } catch (e: Exception) {
            Resource.error(e.message.toString(), null)
        }
    }

    override suspend fun getAllPosts(): Flow<Resource<ArrayList<PostModel>>> = callbackFlow {
        val listener = database.child("Posts")
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var filteredPosts = mutableListOf<PostModel>() // Create a list to store valid posts

                        for (i in snapshot.children) {
                            try {
                                val post = i.getValue(PostModel::class.java)
                                if (post != null) {
                                    filteredPosts.add(post) // Add valid posts to the list
                                }
                            } catch (e: Exception) {
                                Log.e("ecco", "EORR = ${e.localizedMessage}")
                            }
                        }

                        filteredPosts = removeRepeats(filteredPosts) as MutableList<PostModel>
                        filteredPosts.reverse()
                        Log.e("ecco", "POST DATA REPO postList111111 = $filteredPosts")

                        trySend(Resource.success(filteredPosts) as Resource<ArrayList<PostModel>>).isSuccess
                    } else {
                        Log.e("ecco", "POST DATA REPO postList222 = $postList")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(Resource.error(error.message, null)).isFailure
                }
            })
        awaitClose { database.removeEventListener(listener) }
    }
}