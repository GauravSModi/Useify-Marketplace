package com.sfu.useify.ui.chat

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setMargins
import androidx.recyclerview.widget.RecyclerView
import com.sfu.useify.R
import com.sfu.useify.models.Message

class ChatAdapter(private val context: Context, private var messages: List<Message>, private val mUserID: String):
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val messageTextView: TextView
        // TODO: Add a timestamp (optional)
        init {
            messageTextView = view.findViewById(R.id.textview_message)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        // Create a new view, which defines the UI of the chat message
        return ChatViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.listview_chat_message, parent, false)
        )
    }

    // Replace the contents of the view with user names and icon
    override fun onBindViewHolder(holder: ChatViewHolder, pos: Int) {
        val message = messages[pos]
        if (message.senderID == mUserID){
            holder.messageTextView.background = context.getDrawable(R.drawable.textview_chat_send)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.apply {
                weight = 1.0f
                gravity = Gravity.RIGHT
                setMargins(150, 15, 30, 15)
            }
            holder.messageTextView.layoutParams = layoutParams
        } else {
            holder.messageTextView.background = context.getDrawable(R.drawable.textview_chat_receive)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.apply {
                weight = 1.0f
                gravity = Gravity.LEFT
                setMargins(30, 15, 150, 15)
            }
            holder.messageTextView.layoutParams = layoutParams
        }
        holder.messageTextView.text = message.content // dataSet[position]
    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return messages.size
    }

}