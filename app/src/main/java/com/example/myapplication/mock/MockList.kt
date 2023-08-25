package com.example.myapplication.mock

import com.example.myapplication.R
import com.example.myapplication.model.ItemModel
import com.example.myapplication.model.PostModel

object MockList {

    fun getPostModelList(): List<PostModel> {
        val postModel = PostModel(
            userId = "P0iBhq5Gz4MiLRrPI04oZJOAVcU2",
            postId = "MveMBcMLjInroUKyy7C",
            postPhoto = arrayListOf("https://firebasestorage.googleapis.com/v0/b/canberk-odev.appspot.com/o/PostPhoto%2F1644609454528.jpg?alt=media&token=adc5f8c3-7491-45e0-81be-b87ac0aea268")
        )
        val itemList: ArrayList<PostModel> = ArrayList()
        itemList.add(postModel)
        return  itemList
    }

    fun getMockedItemList(): List<ItemModel> {
        val itemModel1 = ItemModel(
            R.drawable.ic_first,
            R.drawable.ic_fifth,
        "Ecem Çığ"
        )
        val itemModel2 = ItemModel(
            R.drawable.ic_first,
            R.drawable.ic_fifth,
            "Ecem Çığ"
        )
        val itemModel3 = ItemModel(
            R.drawable.ic_first,
            R.drawable.ic_fifth,
            "Ecem Çığ"
        )
        val itemModel4 = ItemModel(
            R.drawable.ic_first,
            R.drawable.ic_fifth,
            "Ecem Çığ"
        )
        val itemModel5 = ItemModel(
            R.drawable.ic_first,
            R.drawable.ic_fifth,
            "Ecem Çığ"
        )
        val itemModel6 = ItemModel(
            R.drawable.ic_first,
            R.drawable.ic_fifth,
            "Ecem Çığ"

        )
        val itemModel7 = ItemModel(
            R.drawable.ic_first,
            R.drawable.ic_fifth,
            "Ecem Çığ"
        )
        val itemModel8 = ItemModel(
            R.drawable.ic_first,
            R.drawable.ic_fifth,
            "Ecem Çığ"
        )
        val itemModel9 = ItemModel(
            R.drawable.ic_first,
            R.drawable.ic_fifth,
            "Ecem Çığ"
        )
        val itemModel10 = ItemModel(
            R.drawable.ic_first,
            R.drawable.ic_fifth,
            "Ecem Çığ"
        )
        val itemModel11 = ItemModel(
            R.drawable.ic_first,
            R.drawable.ic_fifth,
            "Ecem Çığ"
        )
        val itemModel12 = ItemModel(
            R.drawable.ic_first,
            R.drawable.ic_fifth,
            "Ecem Çığ"
        )

        val itemList: ArrayList<ItemModel> = ArrayList()
        itemList.add(itemModel1)
        itemList.add(itemModel2)
        itemList.add(itemModel3)
        itemList.add(itemModel4)
        itemList.add(itemModel5)
        itemList.add(itemModel6)
        itemList.add(itemModel7)
        itemList.add(itemModel8)
        itemList.add(itemModel9)
        itemList.add(itemModel10)
        itemList.add(itemModel11)
        itemList.add(itemModel12)

        return itemList
    }
}