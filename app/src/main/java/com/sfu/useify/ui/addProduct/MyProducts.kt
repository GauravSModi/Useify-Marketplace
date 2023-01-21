package com.sfu.useify.ui.addProduct

import android.os.Bundle
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.sfu.useify.R
import com.sfu.useify.database.productsViewModel
import com.sfu.useify.models.Product

class MyProducts: AppCompatActivity() {

    private lateinit var mProductsLv: ListView

    //general
    private lateinit var myProductsList: MutableLiveData<List<Product>>

    private lateinit var titlesList: MutableList<String>
    private lateinit var pricesList: MutableList<Float>
    private lateinit var descList: MutableList<String>
    private lateinit var imagesList: MutableList<String>
    private lateinit var idsList: MutableList<String>


    // Models
    val productsViewModel = productsViewModel();
    private lateinit var auth: FirebaseAuth
    private var userID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_products)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        //get current User ID
        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser?.uid.toString()

        initializeFields()

    }

    // Handle clicks on menu items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initializeFields() {

        myProductsList = productsViewModel.getAllProducts()
        idsList = ArrayList()
        titlesList = ArrayList()
        descList = ArrayList()
        pricesList = ArrayList()
        imagesList = ArrayList()

        myProductsList.observe(this) {
            val myProduct: List<Product>? = it
            if (myProduct != null) {
                idsList.clear()
                titlesList.clear()
                descList.clear()
                pricesList.clear()
                imagesList.clear()

                for (product in myProduct) {
                    if(product.sellerID == userID){
                        idsList.add(product.productID)
                        titlesList.add(product.name)
                        descList.add(product.description)
                        pricesList.add(product.price.toFloat())
                        imagesList.add(product.image)
                    }

                }
                setListView()
            }
        }

    }


    private fun setListView() {
        //Adapter setup
        mProductsLv = findViewById((R.id.myproductsLv))
        val mProductAdapter =
            MyProductAdapter(this, idsList, titlesList, pricesList, descList, imagesList)
        mProductsLv.adapter = mProductAdapter
    }



}