package com.example.myapplication.repositories

import com.example.myapplication.extension.Resource
import com.example.myapplication.model.UserModel

interface UsersDataInterface {

    fun getAllUsersData(): Resource<ArrayList<UserModel>>

}