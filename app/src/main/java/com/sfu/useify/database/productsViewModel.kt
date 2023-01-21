package com.sfu.useify.database

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.sfu.useify.models.Product
import java.io.ByteArrayOutputStream

class productsViewModel: ViewModel() {
    private var products: MutableLiveData<List<Product>> = MutableLiveData()
    private var productById: MutableLiveData<Product> = MutableLiveData()
    private var productsByCategory: MutableLiveData<List<Product>> = MutableLiveData()

    private val database = FirebaseDatabase.getInstance()
    private val databaseReference = database.getReference("Products")
    private val storage = FirebaseStorage.getInstance()
    private val storageReference = storage.getReference("Products")


    fun addProduct(product: Product) {
        val newproductRef = databaseReference.push()
        product.productID = newproductRef.key.toString()
        newproductRef.setValue(product).addOnSuccessListener {
            Log.i("firebase", "Success fully added product")
        }.addOnFailureListener {
            Log.i("firebase","Error adding product", it)
        }
    }

    fun addProductWithPhoto(product: Product, bitmap: Bitmap){
        val photoReference = storageReference.child(System.currentTimeMillis().toString() + ".jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        var uploadTask = photoReference.putBytes(data)
        uploadTask.addOnSuccessListener( OnSuccessListener {
            photoReference.downloadUrl.addOnSuccessListener(OnSuccessListener {
                product.image = it.toString()
                val newproductRef = databaseReference.push()
                product.productID = newproductRef.key.toString()
                newproductRef.setValue(product).addOnSuccessListener {
                    Log.i("firebase", "Success fully added product")
                }.addOnFailureListener {
                    Log.i("firebase","Error adding product", it)
                }
            })

        })
    }



    fun getAllProducts(): MutableLiveData<List<Product>>{
        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val productsList = snapshot.children.mapNotNull { it.getValue(Product::class.java) }.toList()
                    products.postValue(productsList)
                } else{
                    products.postValue(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        return products
    }

    fun getProductByID(id:String): MutableLiveData<Product>{
        databaseReference.orderByChild("productID").equalTo(id).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val productsList = snapshot.children.mapNotNull { it.getValue(Product::class.java) }.toList()[0]
                    productById.postValue(productsList)
                } else{
                    productById.postValue(Product())
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        return productById
    }

    fun getProductByCategory(category:String): MutableLiveData<List<Product>>{
        databaseReference.orderByChild("category").equalTo(category).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val productsList = snapshot.children.mapNotNull { it.getValue(Product::class.java) }.toList()
                    productsByCategory.postValue(productsList)
                } else{
                    productsByCategory.postValue(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        return productsByCategory
    }

    //key: productId of the product in database
    fun deleteProduct(key:String){
        databaseReference.child(key).removeValue()
    }

    //key: productId of the product in database
    fun updateProduct(key: String, product: Product){
        product.productID = key
        databaseReference.child(key).setValue(product)
    }

    fun updateProductWithPhoto(key: String, product: Product, bitmap: Bitmap){
        val photoReference = storageReference.child(System.currentTimeMillis().toString() + ".jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        var uploadTask = photoReference.putBytes(data)
        uploadTask.addOnSuccessListener( OnSuccessListener {
            photoReference.downloadUrl.addOnSuccessListener(OnSuccessListener {
                product.image = it.toString()
                product.productID = key
                databaseReference.child(key).setValue(product)
            })

        })
    }




}