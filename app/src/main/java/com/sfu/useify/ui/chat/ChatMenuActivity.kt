package com.sfu.useify.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sfu.useify.R
import com.sfu.useify.Util
import com.sfu.useify.database.conversationsViewModel
import com.sfu.useify.database.productsViewModel
import com.sfu.useify.database.usersViewModel
import java.util.*


class ChatMenuActivity: AppCompatActivity(), LifecycleOwner {
    private lateinit var recyclerView: RecyclerView
    private lateinit var mUserID: String
    private lateinit var usersViewModel: usersViewModel
    private lateinit var conversationsViewModel: conversationsViewModel
    private lateinit var conversationsAdapter: ConversationsAdapter
    private lateinit var productsViewModel: productsViewModel
    private lateinit var conversationsList: MutableLiveData<List<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_menu)
        setTitle(R.string.chat_inbox_title)

        // Setup Actionbar
        setupActionBar()

        // Get userID
        mUserID = Util.getUserID()

        // Get ViewModel
        usersViewModel = usersViewModel()
        conversationsViewModel = conversationsViewModel()
        productsViewModel = productsViewModel()

        // Setup RecyclerView
        recyclerView = findViewById<RecyclerView>(R.id.recyclerview_chat)
        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        setupObserverAndAdapter()
    }


    // Setup back button in Action Bar
    private fun setupActionBar() {
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }


    private fun setupObserverAndAdapter() {
        // Observe changes in conversations and update RecyclerView
        conversationsList = usersViewModel.getAllConversations(mUserID)
        conversationsList.observe(this, Observer { conversationsList ->
            conversationsAdapter = ConversationsAdapter(
                ViewTreeLifecycleOwner.get(recyclerView),
                mUserID,
                sortConversationsByTime(conversationsList!!)
            ) { conversationID ->
                println("Debug: Conversation clicked (ID) = '$conversationID'")
                val intent = Intent(this, ChatActivity::class.java)
                val bundle = Bundle()
                bundle.putInt(resources.getString(R.string.key_chat_start_type), 0)
                bundle.putString(resources.getString(R.string.key_chat_conversation_id), conversationID)
                intent.putExtras(bundle)
                startActivity(intent)
            }
            recyclerView.adapter = conversationsAdapter
        })
    }

    private fun sortConversationsByTime(conversationsList: List<String>): List<String> {
        val conversationsViewModel = conversationsViewModel()
        conversationsList.toMutableList()
        for (i in 0 until (conversationsList.size-1)){
            var max = i
            for (j in i+1 until conversationsList.size){
                conversationsViewModel.getconversationByID(conversationsList[j]).observe(this, Observer { convoJ ->
                    conversationsViewModel.getconversationByID(conversationsList[max]).observe(this, Observer { convoMax ->
                        if (convoJ.lastMessage!!.sentAt < convoMax.lastMessage!!.sentAt)
                            max = j
                    })
                })
            }
            if (max != i)
                Collections.swap(conversationsList, i, max)
        }
        return conversationsList.toList()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}