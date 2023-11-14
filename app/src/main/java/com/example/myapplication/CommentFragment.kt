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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.FragmentCommentBinding
import com.example.myapplication.extension.parcelable
import com.example.myapplication.model.Comment
import com.example.myapplication.model.PostModel
import com.example.myapplication.model.UserModel
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
    private lateinit var firebaseAuth: FirebaseAuth
    private  var commentId: String?= null
    private  var commentTime: String?= null
    private  var commentText: String?= null

    private lateinit var mRef: DatabaseReference
    private var _binding: FragmentCommentBinding? = null
    private val binding get() = _binding!!
    private var userList: ArrayList<UserModel>? = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommentBinding.inflate(inflater, container, false)
        val view = binding.root
       // commentId = firebaseUser.uid
        mRef = FirebaseDatabase.getInstance().reference
        //firebaseAuth = FirebaseAuth.getInstance()
       // getSelectedUserData()
        setupRecyclerCommentView()
        setUpCommentWritingSection()

        val translation = arguments?.parcelable<PostModel>("clickedComment")
        if (translation != null) {
            postId = translation.postId.toString()
            Log.d("SALIMM", "postId from arguments: $postId")
        }


//        userList = ArrayList()
//        commentAdapter = commentList?.let { CommentAdapter(requireContext(), it, userList!!) }
//        binding.commentsRecyclerView.adapter = commentAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getComments()

        binding.commentBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.commentSend.setOnClickListener {
            Log.d("PublishComment", "Send button clicked")
            commentText = binding.commentEditText.text.toString().trim()
            commentTime = System.currentTimeMillis().toString()

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



        binding.commentEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.commentEditText.text.toString() != s.toString()) {
                    binding.commentEditText.setText(s)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

    }

    private fun setUpCommentWritingSection() {
        mRef.child("Users").child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val currentUserProfPic = snapshot.child("profilePhoto").value.toString()
                        val commentProfilePhoto = binding.commentProfilePhoto

                        if (!currentUserProfPic.isNullOrEmpty()) {
                            Glide.with(requireContext())
                                .load(currentUserProfPic)
                                .into(commentProfilePhoto)
                        } else {
                            Glide.with(requireContext())
                                .load(R.drawable.ic_fifth)
                                .into(commentProfilePhoto)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
    private fun getComments() {
        val commentRef = mRef.child("Posts").child(postId!!).child("commentList")

        commentRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val comment = snap.getValue(Comment::class.java)
                        if (comment != null) {
                            commentList?.add(comment)
                            comment.userId?.let { getUsers(it) }
                        }
                    }
                }

                Log.i("COMMENT FRAGMENT", "COMMENT LIST: $commentList")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value GET COMMENTS.", error.toException())
            }
        })
    }

    private fun getUsers(userId: String) {
        mRef.child("Users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val profilePhoto = snapshot.child("profilePhoto").getValue(String::class.java)
                    val userNickName = snapshot.child("userNickName").getValue(String::class.java)

                    val user = UserModel()
                    user.userId = userId
                    user.profilePhoto = profilePhoto
                    user.userNickName = userNickName

                    userList?.add(user)

                    // Check if all users are retrieved before submitting to the adapter
                    if (userList?.size == commentList?.size) {
                        commentAdapter?.submitCommentList(userList!!)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("salimmm", "getUsers ${error.message}")
            }
        })
    }
    private fun publishComment(postId:String) {
        val commentId = mRef.child("Posts").child(postId).child("commentList").push().key.toString()

        val commentRef = mRef.child("Posts").child(postId).child("commentList").child(commentId)

        val commentMap = HashMap<String, Any>()

        commentMap["userId"] = FirebaseAuth.getInstance().currentUser!!.uid
        commentMap["commentId"] = commentId
        commentMap["commentText"] = binding.commentEditText.text.toString()
        commentMap["commentTime"] = System.currentTimeMillis().toString()

        commentRef.setValue(commentMap)

        binding.commentEditText.setText("")

        Toast.makeText(context, "Comment Uploaded Successfully", Toast.LENGTH_SHORT).show()
        Log.i("COMMENT UPLOADED NOW", "COMMENT UPLOADED")
    }
    private fun setupRecyclerCommentView() {
        commentAdapter = CommentAdapter(
            requireContext(),
            commentList ?: ArrayList(),
            userList ?: ArrayList()
        )

        binding.commentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = commentAdapter
            setHasFixedSize(true)
        }
    }

}