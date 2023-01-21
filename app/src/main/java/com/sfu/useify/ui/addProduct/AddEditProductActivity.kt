package com.sfu.useify.ui.addProduct

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils.isEmpty
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import com.sfu.useify.MainActivity
import com.sfu.useify.R
import com.sfu.useify.Util
import com.sfu.useify.database.productsViewModel
import com.sfu.useify.models.Product
import com.squareup.picasso.Picasso
import java.io.File
import java.util.*


class AddEditProductActivity : AppCompatActivity() {

    private val AUTOCOMPLETE_REQUEST_CODE: Int = 1

    // editText fields
    private lateinit var titleET: EditText
    private lateinit var priceET: EditText
    private lateinit var descriptionET: EditText
    private lateinit var pickUpLoctionET: EditText
//    private lateinit var pickUpLoctionET: AutoCompleteTextView
    private lateinit var categorySelect: Spinner
    private lateinit var pageTitle: TextView

    // buttons
    private lateinit var deleteBtn: Button
    private lateinit var addProductBtn: Button
    private lateinit var updateProductBtn: Button


    // TODO: get images and seller ID
    private var pickupLat = 0.0
    private var pickupLong = 0.0

    //product image
    private var imgUrl = ""
    private lateinit var profileImgOptions: Array<String>
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<Intent>
    private lateinit var mImageView: ImageView
    private lateinit var mTempImgUri: Uri
    private val mTempImgName = "my_temp_img.jpg"
    private val mProductName = "my_profile.jpg"
    private lateinit var mProductImgUri: Uri
    private lateinit var bitmap: Bitmap
    private var productId = ""
    private var userID = ""
    private var productCreatedAt: Long = 0L
    private var isImageChanged: Boolean = false
    private var FAIL = 0
    private var SUCCESS = 1

    // Models
    val productsViewModel = productsViewModel();
    lateinit var myProduct: MutableLiveData<Product>
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

                val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        //get current User ID
        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser?.uid.toString()

        //get products input fields
        initializeFields()

        // get product ID from intent
        val extras = intent.extras
        if (extras != null) {
            // edit item
            addProductBtn.visibility = GONE
            deleteBtn.visibility = VISIBLE
            updateProductBtn.visibility = VISIBLE
            pageTitle.text = "Edit Product"

            productId = extras.getString("productIdKey", "")
            myProduct = productsViewModel.getProductByID(productId)

            myProduct.observe(this) {
                if (it != null) {
                    setProductinView(it)
                }
            }
        } else {
            //add new
            pageTitle.text = "Add a Product"
            deleteBtn.visibility = GONE
            updateProductBtn.visibility = GONE
            addProductBtn.visibility = VISIBLE
        }

        setupLocationAutocomplete()

        // Photo widget setup
        productImgSetUp(savedInstanceState)

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

    fun onLocationClicked(view: View) {
        // Add address fields that you need from the autocomplete fragment
        val fields: List<Place.Field> = Arrays.asList(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )
        val intent =
            Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    private fun setupLocationAutocomplete() {
        pickUpLoctionET.isEnabled = false
        Places.initialize(applicationContext, "AIzaSyD3txXQ3Nz2SSWvm-Ay6PKzaRbX_bpUwcw")
        val placesApi = Places.createClient(this)
    }

    //
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    println("Debug: Place = " + place.name + ", " + place.address)
                    pickUpLoctionET.setText("${place.name}")
                    pickupLat = place.latLng.latitude
                    pickupLong = place.latLng.longitude
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(data)
                    println("Debug: " + status.statusMessage)
                }
                RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
        }
    }


    private fun setProductinView(product: Product) {
        titleET.setText(product.name)
        priceET.setText(product.price.toString())
        descriptionET.setText(product.description)

        categorySelect.setSelection(getIndex(categorySelect, product.category));

        // load pick up location
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(product.pickupLat, product.pickupLong, 1)
        if (addresses.size == 0) {
            pickUpLoctionET.setText("No valid location provided")
            return
        }
        val address = addresses[0]
        var addressString: String = ""
        for (i in 0..address.maxAddressLineIndex)
            addressString += address.getAddressLine(i)

        pickUpLoctionET.setText(addressString)
        pickupLat = product.pickupLat
        pickupLong = product.pickupLong
        productId = product.productID
        productCreatedAt = product.createAt
        imgUrl = product.image

        if (imgUrl != "") {
            try {
                Picasso.get().load(imgUrl).resize(500, 0)
                    .placeholder(R.drawable.no_image_available)
                    .into(mImageView)
            } catch (e: Exception) {
                println("Debug: exception = $e")
            }

        }
    }

    private fun getIndex(spinner: Spinner, category: String): Int {
        var index = 0
        for (i in 0 until spinner.getCount()) {
            if (spinner.getItemAtPosition(i).equals(category)) {
                index = i
            }
        }
        return index
    }

    private fun initializeFields() {
        titleET = findViewById(R.id.productTitleEt)
        priceET = findViewById(R.id.productPriceEt)
        descriptionET = findViewById(R.id.productDescEt)
        pickUpLoctionET = findViewById(R.id.productLocationEt)
        categorySelect = findViewById(R.id.categorySpinner)
        mImageView = findViewById(R.id.productImgIv)
        deleteBtn = findViewById(R.id.deleteBtn)
        addProductBtn = findViewById(R.id.addProductBtn)
        pageTitle = findViewById(R.id.addProductTv)
        updateProductBtn = findViewById(R.id.updateProductBtn)
    }

    private fun setPrpductImg(imgName: String, imgUri: Uri) {
        if (File(getExternalFilesDir(null), imgName).exists()) {
            bitmap = Util.getBitmap(this, imgUri)
            mImageView.setImageBitmap(bitmap)
            isImageChanged = true
        }
    }

    private fun productImgSetUp(savedInstanceState: Bundle?) {
        // Credit to Professor Xing-Dong Yang: https://www.sfu.ca/~xingdong/Teaching/CMPT362/lecture14/lecture14.html
        Util.checkPermissions(this)
        val tempImgFile = File(getExternalFilesDir(null), mTempImgName)
        mTempImgUri = FileProvider.getUriForFile(this, "com.sfu.useify", tempImgFile)
        val profileImgFile = File(getExternalFilesDir(null), mProductName)
        mProductImgUri = FileProvider.getUriForFile(this, "com.sfu.useify", profileImgFile)
        if (savedInstanceState != null)
            setPrpductImg(mTempImgName, mTempImgUri)

        cameraResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                setPrpductImg(mTempImgName, mTempImgUri)


            }
        }

        galleryResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intentData = result.data
                val uri = intentData?.data!!
                Util.saveBitmapIntoDevice(this, mTempImgName, uri)
                setPrpductImg(mTempImgName, mTempImgUri)

            }
        }
        // end photo widget setup
    }

    fun onChangeProductImgClicked(view: View) {
        profileImgOptions = arrayOf("Take From Camera", "Select from Gallery")
        var intent: Intent

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Product Image")
        builder.setItems(profileImgOptions)
        { dialog, which ->
            if (profileImgOptions[which].equals("Take From Camera")) {
                intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempImgUri)
                cameraResult.launch(intent)
            } else {
                intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryResult.launch(intent)
            }
        }
        val profileImgPickerDlialog = builder.create()
        profileImgPickerDlialog.show()
    }

    fun validateInput(): Int {
        var name = titleET.text.toString()
        var price = priceET.text.toString()
        var desc = descriptionET.text.toString()
        var pickupLocation = pickUpLoctionET.text.toString()

        if (isEmpty(name) || isEmpty(price) || isEmpty(desc) || isEmpty(pickupLocation)) {
            return FAIL
        } else {
            return SUCCESS
        }
    }

    fun onAddNewProductClicked(view: View) {

        if (validateInput() === SUCCESS) {
            productCreatedAt = Util.getSystemTimeNow()
            val mProduct = Product(
                titleET.text.toString(),
                priceET.text.toString().toDouble(),
                imgUrl,
                descriptionET.text.toString(),
                userID,
                categorySelect.selectedItem.toString(),
                pickupLat,
                pickupLong,
                productCreatedAt
            )

            if (isImageChanged) {
                productsViewModel.addProductWithPhoto(mProduct, bitmap)
            } else {
                productsViewModel.addProduct(mProduct)
            }

            Toast.makeText(this, "new product added", Toast.LENGTH_LONG).show()
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)

        } else {
            Toast.makeText(
                this,
                "Title, Price, description and Pick up location are required! ",
                Toast.LENGTH_LONG
            ).show()
        }


    }

    fun onCancelProductClicked(view: View) {
        Util.deleteImg(this, mTempImgName)
        finish()
    }

    fun onDeleteProductClicked(view: View) {
        if (productId != "") {
            Toast.makeText(this, "remove product Id: " + productId, Toast.LENGTH_LONG).show()
            productsViewModel.deleteProduct(productId)
            finish()
        }
    }

    fun onUpdateProductClicked(view: View) {

        if (productId != "") {

            if (validateInput() === SUCCESS) {
                val mProduct = Product(
                    titleET.text.toString(),
                    priceET.text.toString().toDouble(),
                    imgUrl,
                    descriptionET.text.toString(),
                    userID,
                    categorySelect.selectedItem.toString(),
                    pickupLat,
                    pickupLong,
                    productCreatedAt,
                    productId
                )
                if (isImageChanged) {
                    productsViewModel.updateProductWithPhoto(productId, mProduct, bitmap)
                    Toast.makeText(this, "product updated", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    productsViewModel.updateProduct(productId, mProduct)
                    Toast.makeText(this, "product updated", Toast.LENGTH_LONG).show()
                    finish()
                }
            } else {
                Toast.makeText(
                    this,
                    "Title, Price, description and Pick up location are required! ",
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    }
}

