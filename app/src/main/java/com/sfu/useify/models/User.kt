package com.sfu.useify.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User (
    var username: String? = "",
    var firstName: String? = "",
    var lastName: String? = "",
    var email: String? = "",
    var phoneNumber: String? = "",
    var address: String? = "",
    var conversations: Map<String,String> = emptyMap(),
    var savedProducts: Map<String,String> = emptyMap(),
    var userID:String? ="",
    var avatar:String? = "",


    ){
    @Exclude
    fun toMap(): Map<String, Any?>{
        return mapOf(
            "userID" to userID,
            "username" to username,
            "firstName" to firstName,
            "lastName" to lastName,
            "avatar" to avatar,
            "email" to email,
            "phoneNumber" to phoneNumber,
            "address" to address,
            "conversations" to conversations,
            "savedProducts" to savedProducts
        )
    }
}