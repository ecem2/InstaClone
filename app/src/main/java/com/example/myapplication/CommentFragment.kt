package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.FragmentCommentBinding
import com.example.myapplication.extension.parcelable
import com.example.myapplication.model.Comment
import com.example.myapplication.model.PostModel
import com.example.myapplication.ui.adapter.CommentAdapter

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

private const val TAG = "CommentFragment"

class CommentFragment : Fragment() {
    private var commentAdapter: CommentAdapter? = null
    private var commentList: ArrayList<Comment>? = ArrayList()
    private val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
    private var postId: String? = null
    private var recyclerView: RecyclerView? = null

    private lateinit var currentUserUid: String
    private val commentLikeArray : ArrayList<String> = ArrayList()
    private  var commentId: String?= null
    private lateinit var userId: String
    private lateinit var commentTime: String
    private lateinit var commentText: String
    private lateinit var mRef: DatabaseReference


    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private var _binding: FragmentCommentBinding? = null
    private val binding get() = _binding!!
    val user = Firebase.auth.currentUser!!.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommentBinding.inflate(inflater, container, false)
        val view = binding.root

        mRef = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()
        getSelectedUserData()
        setupRecyclerCommentView()
        setUpCommentWritingSection()
        postId?.let { getComments(it) }
        val prefEditor = activity?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        prefEditor?.putString("postId", postId) // postId değişkeni, kaydedilecek olan değer
        prefEditor?.apply()

        // SharedPreferences'tan "postId" değerini alma ve kullanma işlemi
        val pref = activity?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        this.postId = pref?.getString("postId", null).toString()
        Log.d("AAAAA", "postId from SharedPreferences: $postId")
        commentList = ArrayList()
        commentAdapter = context?.let { CommentAdapter(it, commentList as ArrayList<Comment>) }
        recyclerView?.adapter = commentAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.commentBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.commentSend.setOnClickListener {
            Log.d("PublishComment", "Send button clicked")
            commentText = binding.commentEditText.text.toString().trim()
            commentTime = System.currentTimeMillis().toString()

            // Eğer yorum göndermeden önce postId değeri null ise, kullanıcıya bir uyarı verin.
            if (postId == null) {
                Toast.makeText(
                    requireContext(),
                    "postId is null. Cannot publish comment.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                publishComment(postId!!)
                Log.d("AAAAA", "$postId")
            }
        }
        arguments?.let {
            val translation = it.getParcelable<PostModel>("clickedComment")
            if (translation != null) {
                postId = translation.postId.toString()
                Log.d("SALIMM", "postId from arguments: $postId")
            }
        }

        binding.commentEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.commentEditText.text.toString() == ""
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

    }

    private fun setUpCommentWritingSection() {
        mRef.child("Users").child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentUserProfPic = snapshot.child("profilePhoto").value.toString()
                    arguments?.let {
                        if (currentUserProfPic != null) {
                            Glide.with(requireContext())
                                .load(currentUserProfPic)
                                .into(binding.commentProfilePhoto)
                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
    private fun getComments(postId: String) {
        val userIdList: ArrayList<String> = ArrayList()
        mRef.child("Posts").child(postId).child("comments")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    commentList?.clear()
                    for (dataSnapshot in snapshot.children) {
                        val comment = dataSnapshot.getValue(Comment::class.java)
                        comment?.let {
                            commentList?.add(it)
                            it.userId?.let { userId -> userIdList.add(userId) }
                        }
                    }
                    getUsers(userIdList)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Hata durumu
                }
            })
    }


    private fun getUsers(userList: ArrayList<String>) {
        mRef.child("Users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (snap in snapshot.children) {
                            val userId = snap.child("userId").getValue(String::class.java)
                            val profilePhoto =
                                snap.child("profilePhoto").getValue(String::class.java)
                            val userNickName =
                                snap.child("userNickName").getValue(String::class.java)

//                        if (userId != null && userList.contains(userId)) {
//                            val comment = commentList.find { it.userId == userId }
//                            comment?.profilePhoto = profilePhoto
//                            comment?.userNickName = userNickName
//                        }
                        }
                    }
                    commentList?.let { commentAdapter?.submitCommentList(it) }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("salimmm", "getUsers ${error.message}")
                }
            })
    }
    private fun publishComment(postId: String) {

        val commentId = mRef.child("Posts").child(postId).child("comments").push().key.toString()
        val commentRef = mRef.child("Posts").child(postId).child("comments")

        val commentMap = HashMap<String, Any>()
        commentMap["commentId"] = commentId // Include the commentId in the commentMap
        commentMap["commentText"] = commentText // Make sure commentText is properly assigned
        commentMap["commentTime"] = commentTime // Make sure commentTime is properly assigned
        commentMap["userId"] = FirebaseAuth.getInstance().currentUser!!.uid

        commentRef.updateChildren(commentMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Ecccoo", "SUCCESSFUL === ${task.isSuccessful}")
                } else {
                    Log.d("Ecccoo", "ERROR")
                    Toast.makeText(requireContext(), "Post could not load", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show()
                Log.d("Ecccoo", "FAILURE === ${exception.message}")
            }
        binding.commentEditText.setText("")
        Log.i("COMMENT UPLOADED NOW", "COMMENT UPLOADED")

    }

    private fun setupRecyclerCommentView() {
        commentAdapter = commentList?.let {
            CommentAdapter(
                requireContext(),
                it
            )
        }

        binding.commentsRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = commentAdapter
            setHasFixedSize(true)
        }

    }
    private fun getSelectedUserData() {
        commentId = firebaseAuth.currentUser!!.uid
    }
}