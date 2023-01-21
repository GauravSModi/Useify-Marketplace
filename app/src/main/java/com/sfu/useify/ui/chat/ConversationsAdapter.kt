package com.sfu.useify.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.sfu.useify.R
import com.sfu.useify.Util
import com.sfu.useify.database.conversationsViewModel
import com.sfu.useify.database.productsViewModel
import com.sfu.useify.database.usersViewModel

class ConversationsAdapter(private val owner: LifecycleOwner?,
                           private val mUserID: String,
                           private var conversations: List<String>,
                           private val onClickListener: (String) -> Unit):
    RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder>() {

    class ConversationViewHolder(view: View, onClickConversation:(Int) -> Unit): RecyclerView.ViewHolder(view) {
//        val profilePictureImageView: ImageView
        val usernameTextView: TextView
        val titleTextView: TextView
        val timeTextView: TextView
        init {
//            profilePictureImageView = view.findViewById(R.id.imageview_conversation_profile_pic)
            usernameTextView = view.findViewById(R.id.textview_conversation_username)
            titleTextView = view.findViewById(R.id.textview_conversation_product_title)
            timeTextView = view.findViewById(R.id.textview_conversation_time)
            itemView.setOnClickListener {
                onClickConversation(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        // Create a new view, which defines the UI of the conversation
        // and a clickListener from the activity
        val viewHolder = ConversationViewHolder(LayoutInflater.from(parent.context).
        inflate(R.layout.listview_conversation, parent, false)) {
            onClickListener(conversations[it])
        }

        return viewHolder
    }

    // Replace the contents of the view with user names and icon
    override fun onBindViewHolder(holder: ConversationViewHolder, pos: Int) {
        val conversationsViewModel = conversationsViewModel()
        val productsViewModel = productsViewModel()
        conversationsViewModel.getconversationByID(conversations[pos]).observe(owner!!, Observer {
            val usersViewModel = usersViewModel()
            // Get other user's username
            for (i in it.senderIDs) {
                if (i != mUserID) {
                    usersViewModel.getUserByID(i).observe(owner, Observer { user ->
                        println("Debug-chat: UserID is $i which translates to ${user}")
                        holder.usernameTextView.text = user.username!!.trim()
                    })
                }
            }

            // Get product title
            productsViewModel.getProductByID(it.productID).observe(owner, Observer { product ->
                holder.titleTextView.text = product.name
            })

            // Get time since last message
            holder.timeTextView.text = Util.calculateTimeSincePosted(it.lastMessage!!.sentAt)
        })
//        holder.profilePictureImageView.setImageDrawable()
    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return conversations.size
    }
}