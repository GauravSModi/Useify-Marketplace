package com.sfu.useify

import android.content.Intent
import android.content.res.Configuration

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sfu.useify.database.conversationsViewModel
import com.sfu.useify.database.productsViewModel
import com.sfu.useify.models.Product
import com.sfu.useify.resetPages.GeneralSettings
import com.sfu.useify.resetPages.ResetPassword
import com.sfu.useify.ui.addProduct.AddEditProductActivity
import com.sfu.useify.ui.addProduct.MyProducts
import com.sfu.useify.ui.authentication.login.LoginActivity
import com.sfu.useify.ui.browse.BrowseActivity
import com.sfu.useify.ui.categories.SearchCategoriesActivity
import com.sfu.useify.ui.chat.ChatMenuActivity
import com.sfu.useify.ui.productDetails.ProductDetailActivity
import com.sfu.useify.ui.authentication.signup.SignupActivity
import com.sfu.useify.ui.browse.ProfileAdapter
import com.sfu.useify.ui.savedProducts.SavedProductsActivity
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var gridView: GridView
    private lateinit var titles: MutableList<String>
    private lateinit var Ids: MutableList<String>
    private lateinit var price: MutableList<Float>
    private lateinit var images: MutableList<String>
    private lateinit var productDatabase: productsViewModel
    private lateinit var myArrayList: MutableLiveData<List<Product>>
    private lateinit var myDate: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        setContentView(R.layout.activity_browse)

        getGridData()
        setGridView()
        gridViewOnClickListener()
    }

    fun changeTheme(view: View){
        val nightModeFlags = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        if(nightModeFlags == Configuration.UI_MODE_NIGHT_YES){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

    }



    /**
     * Description: gets names and images from database
     * Post-condition: changes names and images variables
     */
    private fun getGridData(){
        productDatabase = productsViewModel()
        myArrayList = productDatabase.getAllProducts()
        titles = ArrayList()
        Ids = ArrayList()
        price = ArrayList()
        images = ArrayList()
        myDate = ArrayList()

        myArrayList.observe(this) {
            val myProduct : List<Product>? = it
            if (myProduct != null) {
                Ids.clear()
                titles.clear()
                price.clear()
                images.clear()
                myDate.clear()
                for(product in myProduct) {
                    titles.add(product.name)
                    Ids.add(product.productID)
                    price.add(product.price.toFloat())
                    images.add(product.image)
                    val formatter = SimpleDateFormat("MMMM-dd-yyyy", Locale.CANADA)
                    val dateString: String = formatter.format(Date(product.createAt))
                    myDate.add(dateString)
                }
                setGridView()
            }

        }
    }

    /**
     * Description: sets gridview elements with name and photo of each profile
     * Post-condition: gridview is set to xml element and adapter is set
     */
    private fun setGridView() {
        gridView = findViewById(R.id.browseGridView)
        val profileAdapter = ProfileAdapter(this, titles, price, images, myDate)
        gridView.adapter = profileAdapter
    }

    /**
     * Description: opens main menu on grid item selected
     * Post-condition: current user changed to selected item
     */
    private fun gridViewOnClickListener() {
        gridView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->

                val intent = Intent(applicationContext, ProductDetailActivity::class.java)
                var key = view.context.getString(R.string.key_product_clicked)
                intent.putExtra(
                    key,
                    Ids[position]
                )
                startActivity(intent)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search ->{
                val intent = Intent(this, SearchCategoriesActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_inbox ->{
                val intent = Intent(this, ChatMenuActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_add ->{
                val newScreen = Intent(this, AddEditProductActivity::class.java)
                startActivity(newScreen)
                return true
            }
            R.id.action_view_my_products ->{
                val newScreen = Intent(this, MyProducts::class.java)
                startActivity(newScreen)
                return true
            }
            R.id.action_saved_products ->{
                val newScreen = Intent(this, SavedProductsActivity::class.java)
                startActivity(newScreen)
                return true
            }
            R.id.action_settings -> {
                val newScreen = Intent(this, GeneralSettings::class.java)
                startActivity(newScreen)
                true
            }
            R.id.action_sign_out -> {
                Firebase.auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}