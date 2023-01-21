package com.sfu.useify.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Message (
    var content: String = "",
    var senderID: String ="",
    var sentAt: Long = System.currentTimeMillis(),    //This createAt is saved in the form of Unix timestamp , need to convert DateTime
    var messageID: String="" ,
){
    @Exclude
    fun toMap(): Map<String, Any?>{
        return mapOf(
            "messageID" to messageID,
            "content" to content,
            "sentAt" to sentAt,
            "senderID" to senderID)
    }
}