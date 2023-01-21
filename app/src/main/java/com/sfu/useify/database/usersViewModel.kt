package com.sfu.useify.database

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.sfu.useify.models.Product
import com.sfu.useify.models.User
import java.io.ByteArrayOutputStream

class usersViewModel: ViewModel() {
    private var users: MutableLiveData<List<User>> = MutableLiveData()
    private var userById: MutableLiveData<User> = MutableLiveData()
    private var savedProducts: MutableLiveData<List<String>> = MutableLiveData()
    private var conversations: MutableLiveData<List<String>> = MutableLiveData()

    private val database = FirebaseDatabase.getInstance()
    private val databaseReference = database.getReference("Users")
    private val storage = FirebaseStorage.getInstance()
    private val storageReference = storage.getReference("Users")

    //user_id will be provided when the authentication feature is completed
    //for now, we can use some random string (e.g. "123", "1", "abc", etc.) as a user id
    //user_id must be unique, otherwise, it will override existing user in database
    fun addUser(user: User, user_id: String) {
        user.userID = user_id
        databaseReference.child(user_id).setValue(user).addOnSuccessListener {
            Log.i("firebase", "Success fully added user")
        }.addOnFailureListener {
            Log.i("firebase","Error adding user", it)
        }
    }

    fun addUserWithPhoto(user: User, user_id:String, bitmap: Bitmap){
        val photoReference = storageReference.child(System.currentTimeMillis().toString() + ".jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        var uploadTask = photoReference.putBytes(data)
        uploadTask.addOnSuccessListener( OnSuccessListener {
            photoReference.downloadUrl.addOnSuccessListener(OnSuccessListener {
                user.avatar = it.toString()
                databaseReference.child(user_id).setValue(user).addOnSuccessListener {
                    Log.i("firebase", "Success fully added user")
                }.addOnFailureListener {
                    Log.i("firebase","Error adding user", it)
                }
            })

        })
    }

    fun getAllUsers(): MutableLiveData<List<User>>{
        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val usersList = snapshot.children.mapNotNull { it.getValue(User::class.java) }.toList()
                    users.postValue(usersList)
                } else{
                    users.postValue(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        return users
    }

    fun getUserByID(id:String): MutableLiveData<User>{
        databaseReference.orderByChild("userID").equalTo(id).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.children.mapNotNull { it.getValue(User::class.java) }.toList()[0]
                    userById.postValue(user)
                } else{
                    userById.postValue(User())
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        return userById
    }





    //key: id of the user in database
    fun deleteUser(key:String){
        databaseReference.child(key).removeValue()
    }

    //key: id of the user in database
    fun updateUser(key: String, user:User){
        databaseReference.child(key).setValue(user)
    }

    fun addConversation(userId: String,conversationID: String){
        val newConversationRef = databaseReference.child(userId).child("conversations").push()
        newConversationRef.setValue(conversationID)
    }

    fun getAllConversations(userID: String): MutableLiveData<List<String>>{
        databaseReference.child(userID).child("conversations").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val conversationList = snapshot.children.mapNotNull { it.getValue(String::class.java) }.toList()
                    conversations.postValue(conversationList)
                } else {
                    conversations.postValue(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
        return conversations
    }

    fun addProductToSavedList(userId: String,productID: String){
        val newProductRef = databaseReference.child(userId).child("savedProducts").push()
        newProductRef.setValue(productID)
    }

    fun getAllSavedProducts(userID: String): MutableLiveData<List<String>>{
        databaseReference.child(userID).child("savedProducts").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val productsList = snapshot.children.mapNotNull { it.getValue(String::class.java) }.toList()
                    savedProducts.postValue(productsList)
                } else {
                    savedProducts.postValue(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
        return savedProducts
    }

}