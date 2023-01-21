package com.sfu.useify.ui.savedProducts

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sfu.useify.R
import com.sfu.useify.Util
import com.sfu.useify.database.productsViewModel
import com.sfu.useify.database.usersViewModel
import com.sfu.useify.ui.productDetails.ProductDetailActivity

class SavedProductsActivity: AppCompatActivity() {
    private lateinit var usersViewModel: usersViewModel
    private lateinit var productsViewModel: productsViewModel
    private lateinit var recyclerView: RecyclerView

    private var mUserID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.saved_products_title)
        setContentView(R.layout.activity_saved_products)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerview_saved_products)
        recyclerView.layoutManager = LinearLayoutManager(this)
        usersViewModel = usersViewModel()
        productsViewModel = productsViewModel()
        mUserID = Util.getUserID()

        usersViewModel.getAllSavedProducts(mUserID!!).observe(this, Observer { savedProductsList ->
            recyclerView.adapter = SavedProductsAdapter(ViewTreeLifecycleOwner.get(recyclerView), savedProductsList) { productID ->
                val intent = Intent(this, ProductDetailActivity::class.java)
                val bundle = Bundle()
                bundle.putString(resources.getString(R.string.key_product_clicked), productID)
                intent.putExtras(bundle)
                startActivity(intent)
            }
        })
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
}