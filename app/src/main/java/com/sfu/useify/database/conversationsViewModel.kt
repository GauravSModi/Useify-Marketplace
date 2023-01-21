package com.sfu.useify.database

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.sfu.useify.models.Conversation
import com.sfu.useify.models.Message

class conversationsViewModel {

    private val database = FirebaseDatabase.getInstance()
    private val databaseReference = database.getReference("Conversations")

    private var conversation:MutableLiveData<Conversation> = MutableLiveData()
    private var conversationByID:MutableLiveData<Conversation> = MutableLiveData()
    private var messages:MutableLiveData<List<Message>> = MutableLiveData()

//    //Must be called as soon as possible, ideally inside onCreate of the activity,
//    //this function can make sure the conversation exists before you add message or do other things with the conversation
//    fun getOrAddNewConversation(productId: String, userId: String, sellerID: String): MutableLiveData<Conversation> {
//        databaseReference.orderByChild("productID").equalTo(productId).addValueEventListener(object :ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if(snapshot.exists()){
//                    val ret_conversation = snapshot.children.mapNotNull {
//                        it.getValue(Conversation::class.java) }.toList()[0]
//                    conversation.postValue(ret_conversation)
//                } else{
//                    val newConversationRef = databaseReference.push()
//                    val senderIDs: List<String> = listOf(sellerID, userId)
//                    val newConversation = Conversation(newConversationRef.key.toString(), productId,  senderIDs)
//                    newConversationRef.setValue(newConversation).addOnSuccessListener {
//                        Log.i("firebase", "Successfully added new conversation")
//                        conversation.postValue(newConversation)
//                    }.addOnFailureListener {
//                        Log.i("firebase","Error adding new conversation", it)
//                    }
//                }
//            }
//            override fun onCancelled(error: DatabaseError) {
//            }
//        })
//        return conversation
//    }

    fun getOrAddNewConversation(productId: String, userId: String, sellerID: String): MutableLiveData<Conversation> {
        databaseReference.orderByChild("productID").equalTo(productId).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    var ret_conversation : Conversation? = null
                    val conversationList = snapshot.children.mapNotNull {
                        it.getValue(Conversation::class.java) }.toList()
                    conversationList.forEach {
                        if (it.senderIDs.contains(userId)){
                            ret_conversation = it
                        }
                    }
                    if (ret_conversation!=null){
                        conversation.postValue(ret_conversation)
                    } else {
                        addConversation(productId, userId, sellerID)
                    }
                } else{
                    addConversation(productId, userId, sellerID)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        return conversation
    }

    private fun addConversation(productId: String, userId: String, sellerID: String) {
        val newConversationRef = databaseReference.push()
        val senderIDs: List<String> = listOf(sellerID, userId)
        val newConversation = Conversation(newConversationRef.key.toString(), productId,  senderIDs)
        newConversationRef.setValue(newConversation).addOnSuccessListener {
            Log.i("firebase", "Successfully added new conversation")
            conversation.postValue(newConversation)
        }.addOnFailureListener {
            Log.i("firebase","Error adding new conversation", it)
        }
    }

    fun addMessageByProductID(message: Message, productId: String){
        databaseReference.orderByChild("productID").equalTo(productId).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    addMessageWithSnapshot(message, snapshot)
                    Log.i("firebase", "Successfully added new message to the conversation")
                } else{
                    Log.e("firebase", "Failed to add new message to the conversation: This conversation does not exist, please use getOrAddNewConversation to create new conversation")
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun addMessageByConversationID(message: Message, ConversationID: String){
        databaseReference.orderByChild("conversationID").equalTo(ConversationID).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    addMessageWithSnapshot(message, snapshot)
                    Log.i("firebase", "Successfully added new message to the conversation")
                } else{
                    Log.e("firebase", "Failed to add new message to the conversation: This conversation does not exist, please use getOrAddNewConversation to create new conversation")
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    //Only use this one only if you know the conversation is exist
    //Ideally use it inside the observation of getOrAddNewConversation
    //Because getOrAddNewConversation will make sure the conversation is existing
    //Example:
    //    val conversationsViewModel = conversationsViewModel()
    //    conversationsViewModel.getOrAddNewConversation("<productID>", "<buyerID>","<sellerID>").observe(this,{
    //        conversationsViewModel.getAllMessagesByConversationID(it.conversationID).observe(this, {
    //            println("debug: messages " + it)
    //        })
    //    })
    fun getAllMessagesByConversationID(conversationID: String): MutableLiveData<List<Message>>{
        databaseReference.orderByChild("conversationID").equalTo(conversationID).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val current_messages = snapshot.children.mapNotNull {
                        it.getValue(Conversation::class.java) }.toList()[0]
                    messages.postValue(current_messages.messages.values.toList())
                } else {
                    messages.postValue(emptyList())
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        return messages
    }

    fun getconversationByID(conversationID: String): MutableLiveData<Conversation> {
        databaseReference.child(conversationID).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val ret_conversation = snapshot.getValue(Conversation::class.java)
                    conversationByID.postValue(ret_conversation)
                } else{
                    Log.e("firebase", "this conversation does not exist")
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        return conversationByID
    }

    //Helper function
    private fun addMessageWithSnapshot(message: Message, snapshot:DataSnapshot){
        val current_conversation = snapshot.children.mapNotNull { it.getValue(Conversation::class.java) }.toList()[0]
        val messagesListRef = databaseReference.child(current_conversation.conversationID).child("messages")
        val messageRef = messagesListRef.push()
        message.messageID = messageRef.key.toString()
        messageRef.setValue(message)
        databaseReference.child(current_conversation.conversationID).child("lastMessage").setValue(message)
    }
}