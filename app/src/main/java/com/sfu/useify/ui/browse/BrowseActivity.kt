package com.sfu.useify.ui.browse

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.lifecycle.MutableLiveData
import com.sfu.useify.R
import com.sfu.useify.database.productsViewModel
import com.sfu.useify.models.Product
import com.sfu.useify.ui.productDetails.ProductDetailActivity
import java.text.SimpleDateFormat
import java.util.*

class BrowseActivity: AppCompatActivity() {

    companion object {
        var CURRENT_USER_KEY = "current user"
    }

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
        setContentView(R.layout.activity_browse)

        //Create an action bar and make it go back to home page.
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        getGridData()
        setGridView()
        gridViewOnClickListener()


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun changeTheme(view: View){
        val nightModeFlags = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        if(nightModeFlags == Configuration.UI_MODE_NIGHT_YES){
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
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
                    myDate.add("Date created: $dateString")
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
            OnItemClickListener { parent, view, position, id ->

                val intent = Intent(applicationContext, ProductDetailActivity::class.java)
                var key = view.context.getString(R.string.key_product_clicked)
                intent.putExtra(
                    key,
                    Ids[position]
                )
                startActivity(intent)
            }
    }

}






