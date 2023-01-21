package com.sfu.useify.ui.productDetails

import android.content.Intent
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer
import com.sfu.useify.R
import com.sfu.useify.Util
import com.sfu.useify.Util.calculateTimeSincePosted
import com.sfu.useify.database.productsViewModel
import com.sfu.useify.database.usersViewModel
import com.sfu.useify.models.Product
import com.sfu.useify.ui.chat.ChatActivity
import com.squareup.picasso.Picasso
import java.util.*


class ProductDetailActivity: AppCompatActivity() {
    private lateinit var usersViewModel: usersViewModel
    private lateinit var productsViewModel: productsViewModel
    private lateinit var titleTextView: TextView
    private lateinit var priceTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var sellerIdTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var productImageView: ImageView
    private lateinit var saveProductButton: Button
    private var productSaved: Boolean = false
    private var productID: String? = null
    private var mUserID: String? = null
    private var product: Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)
        setTitle(R.string.product_details_title)

        // Setup share and back buttons in Action Bar
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        // Setup TextViews
        titleTextView = findViewById<TextView>(R.id.textview_product_details_title)
        priceTextView = findViewById<TextView>(R.id.textview_product_details_price)
        dateTextView = findViewById<TextView>(R.id.textview_product_details_date)
        descriptionTextView = findViewById<TextView>(R.id.textview_product_details_description)
        sellerIdTextView = findViewById<TextView>(R.id.textview_product_details_seller_id)
        locationTextView = findViewById<TextView>(R.id.textview_product_details_location)
        productImageView = findViewById<ImageView>(R.id.imageview_product_details_photo)
        saveProductButton = findViewById<Button>(R.id.button_save_product)

        // Setup ViewModels and get productID
        val bundle: Bundle? = intent.extras
        usersViewModel = usersViewModel()
        productsViewModel = productsViewModel()
        productID = bundle?.getString(resources.getString(R.string.key_product_clicked))
        mUserID = Util.getUserID()

        usersViewModel.getAllSavedProducts(mUserID!!).observe(this, Observer {
            if (it.contains(productID)){
                productSaved = true
                val filledFloppy = resources.getDrawable(R.drawable.ic_baseline_save_24_filled)
                filledFloppy.setBounds(0,0,60,60)
                saveProductButton.setCompoundDrawables(filledFloppy, null, null, null)
            }
        })


        // Get product item
        if (productID != null){
            productsViewModel.getProductByID(productID!!).observe(this) {
                updateProductDetails(it)
            }
        } else {
            // TODO: Show 'error: product not found' in some way
        }
    }

    private fun updateProductDetails(newProduct: Product?){
        if (newProduct == null)
            return
        product = newProduct
        titleTextView.text = newProduct.name
        descriptionTextView.text = newProduct.description
        priceTextView.text = String.format("$%.2f", newProduct.price)
        dateTextView.text = calculateTimeSincePosted(newProduct.createAt)
        usersViewModel.getUserByID(newProduct.sellerID).observe(this, Observer {
            sellerIdTextView.text = it.username
        })
        try {
            Picasso.get().load(newProduct.image).resize(500, 0).into(productImageView)        // Setup location TextView link (to GMaps)
        } catch (e: Exception){
            println("Debug: exception = $e")
        }
        updateLocation(newProduct.pickupLat, newProduct.pickupLong)
    }

    private fun updateLocation(latitude: Double, longitude: Double) {
        var latitude = latitude
        var longitude = longitude

        // Get address from coordinates
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses.size == 0){
            locationTextView.text = "No valid location provided"
            return
        }
        val address = addresses[0]
        var addressString: String = ""
        for (i in 0 .. address.maxAddressLineIndex)
            addressString += address.getAddressLine(i)

        // Make the textview a link to google maps for the given coordinates
        locationTextView.movementMethod = LinkMovementMethod.getInstance()
        locationTextView.text = HtmlCompat.fromHtml("<a href>${addressString}</a>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        locationTextView.setOnClickListener {
            onLocationClicked(it, latitude, longitude)
        }
    }

    private fun onLocationClicked(view: View, latitude: Double, longitude: Double){
        // TODO: Replace with address instead of coordinates
        val gmmIntentUri: Uri = Uri.parse("http://maps.google.com/maps?q=loc:$latitude,$longitude")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_product_details, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle Option pressed
        when (item.itemId){
            android.R.id.home -> onBackPressed()
            R.id.menu_share_btn -> onSharePressed()
        }
        return true
    }

    private fun onSharePressed() {
        // TODO: Implement a share button
    }

    // Start chat intent with seller regarding given product
    fun onChatClicked(view: View){
        if (product == null || productID == null)
            return
        val sellerID = product!!.sellerID
        if (mUserID != sellerID){
            val intent = Intent(this, ChatActivity::class.java)
            val bundle = Bundle()
            bundle.putInt(resources.getString(R.string.key_chat_start_type), 1)
            bundle.putString(resources.getString(R.string.key_chat_other_user_id), sellerID)
            bundle.putString(resources.getString(R.string.key_chat_product_id), product!!.productID)
            intent.putExtras(bundle)
            startActivity(intent)
        } else {
            Toast.makeText(this, "This is your post!", Toast.LENGTH_SHORT).show()
        }
    }

    fun onSaveClicked(view: View){
        if (!productSaved){
            usersViewModel.addProductToSavedList(mUserID!!, productID!!)
            Toast.makeText(this,"Saved!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this,"Already saved!", Toast.LENGTH_SHORT).show()
        }
    }
}