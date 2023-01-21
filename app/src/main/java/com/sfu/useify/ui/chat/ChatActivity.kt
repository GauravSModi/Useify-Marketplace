package com.sfu.useify.ui.chat

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sfu.useify.R
import com.sfu.useify.Util
import com.sfu.useify.database.conversationsViewModel
import com.sfu.useify.database.productsViewModel
import com.sfu.useify.database.usersViewModel
import com.sfu.useify.models.Message
import java.util.*

class ChatActivity: AppCompatActivity() {
    private lateinit var usernameTextView: TextView
    private lateinit var chatInputEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var usersViewModel: usersViewModel
    private lateinit var productsViewModel: productsViewModel
    private lateinit var conversationsViewModel: conversationsViewModel
    private lateinit var chatAdapter: ChatAdapter

    // Used to avoid checking for conversation after every new message sent
    private var newConversation: Boolean = false
    private var conversationExistenceCheck: Boolean = false
    private var mUserID: String? = null
    private var otherUserID: String? = null
    private var productID: String? = null
    private var conversationID: String? = null
    private var intentStartType: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        setupView()

        // Get ViewModels
        usersViewModel = usersViewModel()
        productsViewModel = productsViewModel()
        conversationsViewModel = conversationsViewModel()

        // Get bundle contents
        val bundle = intent.extras
        getBundleAndCreateAdapter(bundle)
    }


    private fun setupView() {
        // Hide Action Bar
        supportActionBar?.hide()

        // Get views
        usernameTextView = findViewById<TextView>(R.id.textview_chat_user)
        chatInputEditText = findViewById<EditText>(R.id.edittext_chat_input)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerview_chat_messages)

        // Specify keyboard action (send message)
        chatInputEditText.setOnEditorActionListener { textView, actionID, keyEvent ->
            when (actionID){
                EditorInfo.IME_ACTION_SEND -> sendMessage()
            }
            return@setOnEditorActionListener false
        }
    }


    // Get bundle contents and set the respective variables
    // Start observer and create adapter after
    private fun getBundleAndCreateAdapter(bundle: Bundle?) {
        mUserID = Util.getUserID()
        intentStartType = bundle?.getInt(resources.getString(R.string.key_chat_start_type))!!
        if (intentStartType == 0){          // Called from chat inbox activity
            conversationID = bundle?.getString(resources.getString(R.string.key_chat_conversation_id))
            conversationsViewModel.getconversationByID(conversationID!!).observe(this, Observer {
                productID = it.productID
                for (i in it.senderIDs)
                    if (i != mUserID)
                        otherUserID = i

                getConversationPartnerUsername()
                setupObserverAndAdapter()
            })
        } else if (intentStartType == 1) {  // Called from product details activity
            otherUserID = bundle?.getString(resources.getString(R.string.key_chat_other_user_id))
            productID = bundle?.getString(resources.getString(R.string.key_chat_product_id))

            getConversationPartnerUsername()
            setupObserverAndAdapter()
        }
    }

    // Get other chatter's username
    private fun getConversationPartnerUsername(){
        usersViewModel.getUserByID(otherUserID!!).observe(this, Observer {
            usernameTextView.text = it.username!!.trim()
        })
    }


    // References:
    // https://stackoverflow.com/questions/46168245/recyclerview-reverse-order
    private fun setupObserverAndAdapter(){
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
//        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager
        println("Debug: productID = $productID, mUserID = $mUserID, otherUserID = $otherUserID")


        // Get conversation
        conversationsViewModel.
        getOrAddNewConversation(productID!!, mUserID!!, otherUserID!!).observe(this, Observer { conversation ->
            if (intentStartType == 1){
                conversationID = conversation.conversationID
            }

            // Populate chat messages
            conversationsViewModel.
            getAllMessagesByConversationID(conversationID!!).observe(this, Observer { messages ->
                val sortedMessageList = sortMessages(messages!!)
                chatAdapter = ChatAdapter(this, sortedMessageList, mUserID!!)
                recyclerView.adapter = chatAdapter
            })

            // Get user conversations to check if this is a new conversation or not
            if (!conversationExistenceCheck) {
                usersViewModel.getAllConversations(mUserID!!)
                    .observe(this, Observer { userConversations ->
                        if (!(userConversations.contains(conversationID))) {
                            newConversation = true
                        }
                    })
            }
        })
    }


    // Sort conversation message list by time
    private fun sortMessages(messages: List<Message>): List<Message> {
        messages.toMutableList()
        for (i in 0 until (messages.size-1)){
            var max = i
            for (j in i+1 until messages.size){
                if (messages[j].sentAt > messages[max].sentAt)
                    max = j
            }
            if (max != i){
                Collections.swap(messages, i, max)
            }
        }
        return messages.toList()
    }


    // Send message to user
    private fun sendMessage() {
        // Check to make sure message isn't empty
        val messageContent = chatInputEditText.text.toString()  // Get message contents from editText
        if (messageContent == "")
            return

        // Reset to blank
        chatInputEditText.setText("")

        // Create new message object
        val message = Message(messageContent, mUserID!!)

        // Add message to conversation livedata
        conversationsViewModel.addMessageByConversationID(message, conversationID!!)

        // Check if the conversation exists in the users list of conversations
        // Add it if it doesn't
        if (!conversationExistenceCheck) {
            if (newConversation){
                usersViewModel.addConversation(mUserID!!, conversationID!!)
                usersViewModel.addConversation(otherUserID!!, conversationID!!)
                newConversation = false
            }
            conversationExistenceCheck = true
        }
    }

    fun onSendMessageClicked(view: View){
        sendMessage()
    }

    fun onBackClicked(view: View){
        finish()
    }
}