package com.example.myapplication.repositories

import android.util.Log
import com.example.myapplication.extension.Resource
import com.example.myapplication.model.PostModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
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

    override suspend fun getAllPost(): Resource<ArrayList<PostModel>> = withContext(Dispatchers.IO) {
        try {
            Resource.loading(null)
            database.child("Posts")
                .orderByChild("timestamp")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (i in snapshot.children) {
                                val postHashMap = i.getValue<PostModel>()
                                if (postHashMap != null) {
                                    postList.add(postHashMap)
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
                        for (i in snapshot.children) {
                            val postHashMap = i.getValue<PostModel>()
                            if (postHashMap != null) {
                                postList.add(postHashMap)
                            }
                        }
                        postList.reverse()
                        Log.e("ecco", "POST DATA REPO postList111111 = $postList")
                    } else {
                        Log.e("ecco", "POST DATA REPO postList222 = $postList")
                    }

                    trySend(Resource.success(postList)).isSuccess
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(Resource.error(error.message, null)).isFailure
                }
            })
        awaitClose { database.removeEventListener(listener) }
    }

}