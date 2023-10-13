package com.example.myapplication.repositories

import com.example.myapplication.extension.Resource
import com.example.myapplication.model.PostModel
import kotlinx.coroutines.flow.Flow

interface PostsDataRepositoryInterface {

    suspend fun getAllPost(): Resource<ArrayList<PostModel>>

    suspend fun getAllPosts(): Flow<Resource<ArrayList<PostModel>>>

}