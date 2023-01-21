package com.sfu.useify.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Conversation(
    var conversationID: String="",
    var productID: String = "",
    var senderIDs: List<String> = emptyList(), //contain 2 strings of 2 people in the conversation
    var messages: Map<String,Message> = emptyMap(),
    var lastMessage: Message? = null,

){
    @Exclude
    fun toMap(): Map<String, Any?>{
        return mapOf(
            "messages" to messages,
            "senderIDs" to senderIDs,
            "productID" to productID,
            "lastMessage" to lastMessage,
            "conversationID" to conversationID)
    }
}