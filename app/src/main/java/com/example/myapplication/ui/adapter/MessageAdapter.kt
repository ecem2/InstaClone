package com.example.myapplication.ui.adapter

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.ReceiveMsgBinding
import com.example.myapplication.databinding.SendMsgBinding
import com.example.myapplication.model.Message
import com.google.firebase.auth.FirebaseAuth
class MessageAdapter(
    private val context: Context,
    private var messages: ArrayList<Message>
) : RecyclerView.Adapter<MessageAdapter.MsgHolder>() {
    var senderUid: String? = null

    companion object {
        private const val ITEM_SENT = 1
        private const val ITEM_RECEIVE = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MsgHolder {
        val view = when(viewType){
            ITEM_SENT -> LayoutInflater.from(parent.context)
                .inflate(R.layout.send_msg, parent, false)
            ITEM_RECEIVE -> LayoutInflater.from(parent.context)
                .inflate(R.layout.receive_msg, parent, false)
            else -> throw IllegalArgumentException("Invalid view type")

        }

          return MsgHolder(view)

    }
    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: MsgHolder, position: Int) {
        val message = messages[position]
        senderUid = FirebaseAuth.getInstance().currentUser?.uid
        if (message.senderId == senderUid) {
            // Gönderenin mesajını sağ tarafta görüntülemek için gerekli düzenlemeleri yapın
            holder.tvMessage.textAlignment = View.TEXT_ALIGNMENT_TEXT_END // Mesajı sağa hizalayın
            holder.image.visibility = View.GONE // Görüntüyü gizleyin
            // Diğer düzenlemeleri ekleyebilirsiniz.
        } else {
            // Alıcının mesajını solda tarafta görüntülemek için gerekli düzenlemeleri yapın
            holder.tvMessage.textAlignment = View.TEXT_ALIGNMENT_TEXT_START // Mesajı sola hizalayın
            holder.image.visibility = View.GONE // Görüntüyü gizleyin
            // Diğer düzenlemeleri ekleyebilirsiniz.
        }
        holder.tvMessage.text = message.message
//        if (holder.itemViewType == ITEM_SENT) {
//            val viewHolder = holder as MsgHolder
//            if (message.message == "photo") {
//                viewHolder.binding.image.visibility = View.VISIBLE
//                viewHolder.binding.message.visibility = View.VISIBLE
//                viewHolder.binding.mLinear.visibility = View.VISIBLE
//
//                Glide.with(context)
//                    .load(message.imageUrl)
//                    .placeholder(R.drawable.ic_fifth)
//                    .into(viewHolder.binding.image)
//            } else {
//                viewHolder.binding.message.text = message.message
//            }
//        } else {
//            val viewHolder = holder as MsgHolder
//            if (message.message == "photo") {
//                viewHolder.binding.image.visibility = View.VISIBLE
//                viewHolder.binding.message.visibility = View.VISIBLE
//                viewHolder.binding.mLinear.visibility = View.VISIBLE
//
//                Glide.with(context)
//                    .load(message.imageUrl)
//                    .placeholder(R.drawable.ic_fifth)
//                    .into(viewHolder.binding.image)
//            } else {
//                viewHolder.binding.message.text = message.message
//            }
//        }
    }
    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (FirebaseAuth.getInstance().uid == message.senderId ) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    inner class MsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: AppCompatTextView = itemView.findViewById(R.id.message)
        val image : AppCompatImageView = itemView.findViewById(R.id.image)

    }

}
