package com.sfu.useify.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Product (
    var name:String = "",
    var price: Double = 0.0,
    var image: String = "",
    var description: String = "",
    var sellerID: String = "",
    var category: String = "",
    var pickupLat: Double = 0.0,
    var pickupLong: Double = 0.0,
    var createAt: Long = System.currentTimeMillis(),    //This createAt is saved in the form of Unix timestamp , need to convert DateTime
    var productID: String="" ,
){
    @Exclude
    fun toMap(): Map<String, Any?>{
        return mapOf(
            "productID" to productID,
            "name" to name,
            "price" to price,
            "image" to image,
            "description" to description,
            "sellerID" to sellerID,
            "category" to category,
            "pickupLat" to pickupLat,
            "pickupLong" to pickupLong,
            "createAt" to createAt)
    }
}