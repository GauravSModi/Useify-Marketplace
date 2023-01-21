package com.sfu.useify.ui.results

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sfu.useify.R
import com.sfu.useify.Util
import com.sfu.useify.database.productsViewModel
import com.sfu.useify.models.Product
import com.sfu.useify.ui.productDetails.ProductDetailActivity
import java.util.*

class ResultsActivity: AppCompatActivity() {
    // Sorted by most recent by default
    private var selected: Int = 0
    private var recyclerViewSpanCount: Int = 2
    private var searchType: Int = ALL
    // Grid layout by default
    private var GRID_LAYOUT: Int = 0
    private var LINEAR_LAYOUT: Int = 1
    private var layoutType: Int = GRID_LAYOUT
    private lateinit var popup: PopupMenu
    private lateinit var productsViewModel: productsViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var resultsAdapter: ResultsAdapter
    private lateinit var productsList: LiveData<List<Product>>
    private lateinit var searchCategory: String
    private lateinit var searchQuery: String
    private var mUserID: String? = null

    companion object{
        const val ALL: Int = 0
        const val CATEGORY: Int = 1
        const val SEARCH_STRING: Int = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.search_results_title)
        setContentView(R.layout.activity_results)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Get Products ViewModel
        productsViewModel = productsViewModel()

        // Setup RecyclerView
        recyclerView = findViewById<RecyclerView>(R.id.recyclerview_results)
        recyclerView.layoutManager = GridLayoutManager(this, recyclerViewSpanCount)

        // Get search type (All products, specific category, search string) from bundle
        val bundle: Bundle? = intent.extras
        searchType = bundle?.getInt(resources.getString(R.string.key_search_query_type))!!
        mUserID = Util.getUserID()

        when (searchType){
            ALL -> showAllResults()
            CATEGORY -> {
                searchCategory = resources.getStringArray(R.array.categories)[
                        bundle?.getInt(resources.getString(R.string.key_category_id))!!
                ]
                showCategoryResults(searchCategory)
            }
            SEARCH_STRING -> {
                searchQuery = bundle?.getString(resources.getString(R.string.key_search_query))!!
                showSearchQueryResults(searchQuery)
            }
        }
    }

    private fun showAllResults() {
        productsList = sortByRecent(productsViewModel.getAllProducts())
        setupObserverAndAdapter()
    }


    private fun showCategoryResults(category: String) {
        productsList = sortByRecent(productsViewModel.getProductByCategory(category))

        setupObserverAndAdapter()
    }

    private fun showSearchQueryResults(query: String?) {
        if (query == null)
            return
        productsList = performSearchQuery(productsViewModel.getAllProducts(), query)

        setupObserverAndAdapter()
    }


    private fun performSearchQuery(products: MutableLiveData<List<Product>>, query: String): LiveData<List<Product>> {
        val sortedList = Transformations.map(products){
            var newList = it.toMutableList()

            for (i in it){
                if (!(i.name.contains(query, true)) && !(i.description.contains(query, true)))
                    newList.remove(i)
            }

            newList.toList()
            return@map newList.toList()
        }

        return sortedList
    }


    // Setup Products list observer and adapter
    private fun setupObserverAndAdapter(){
        if (productsList == null)
            return
        // Observe changes in entries list
        productsList.observe(this, Observer {
            resultsAdapter = ResultsAdapter(it) { productID ->
                println("Debug: ProductID of product clicked = '$productID'")
                val intent = Intent(this, ProductDetailActivity::class.java)
                val bundle = Bundle()
                bundle.putString(resources.getString(R.string.key_product_clicked), productID)
                intent.putExtras(bundle)
                startActivity(intent)
            }
            recyclerView.adapter = resultsAdapter
        })
    }


    // Sort Products list LiveData from most recent to oldest
    private fun sortByRecent(products: LiveData<List<Product>>): LiveData<List<Product>> {
        val sortedList = Transformations.map(products){
            for (i in 0 until (it.size-1)){
                var max = i
                for (j in i+1 until it.size){
                    if (it[j].createAt > it[max].createAt)
                        max = j
                }
                if (max != i)
                    Collections.swap(it, i, max)
            }
            return@map it
        }
        return sortedList
    }


    // Sort Products list LiveData from lowest to highest price
    private fun sortByPriceLowToHigh(products: LiveData<List<Product>>): LiveData<List<Product>> {
        val sortedList = Transformations.map(products){
            for (i in 0 until (it.size-1)){
                var min = i
                for (j in i+1 until it.size){
                    if (it[j].price < it[min].price)
                        min = j
                }
                if (min != i)
                    Collections.swap(it, i, min)
            }
            return@map it
        }
        return sortedList
    }


    // Sort Products list LiveData from highest to lowest price
    private fun sortByPriceHighToLow(products: LiveData<List<Product>>): LiveData<List<Product>> {
        val sortedList = Transformations.map(products){
            for (i in 0 until (it.size-1)){
                var max = i
                for (j in i+1 until it.size){
                    if (it[j].price > it[max].price)
                        max = j
                }
                if (max != i)
                    Collections.swap(it, i, max)
            }
            return@map it
        }
        return sortedList
    }


    // Inflate results menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_results, menu)
        return true
    }



    // Handle clicks on menu items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.menu_sort_by -> showSortMenu()
            R.id.menu_view_changer -> {
                if (layoutType == GRID_LAYOUT){
                    item.setIcon(R.drawable.ic_baseline_grid_view_24)
                    recyclerView.layoutManager = LinearLayoutManager(this)
                    layoutType = LINEAR_LAYOUT
                } else {
                    item.setIcon(R.drawable.ic_outline_view_agenda_24)
                    recyclerView.layoutManager = GridLayoutManager(this, recyclerViewSpanCount)
                    layoutType = GRID_LAYOUT
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    // Show Sort-by options using a PopupMenu
    private fun showSortMenu() {
        popup = PopupMenu(this, findViewById(R.id.menu_sort_by))
        popup.menuInflater.inflate(R.menu.menu_results_sort_options, popup.menu)
        popup.menu.getItem(selected).isChecked = true

        // Set listeners
        popup.setOnMenuItemClickListener(this.SortMenuListener())
        popup.setOnDismissListener(this.SortMenuListener())

        popup.show()
    }


    // Listener class for PopupMenu
    inner class SortMenuListener: PopupMenu.OnMenuItemClickListener, PopupMenu.OnDismissListener{

        override fun onMenuItemClick(item: MenuItem?): Boolean {
            println("Debug: ${item?.title}")
            when (item?.title){
                resources.getStringArray(R.array.search_sort_menu_items)[0] -> setSortingOptions(0)
                resources.getStringArray(R.array.search_sort_menu_items)[1] -> setSortingOptions(1)
                resources.getStringArray(R.array.search_sort_menu_items)[2] -> setSortingOptions(2)
            }
            return false
        }

        override fun onDismiss(popup: PopupMenu?) {
            // Do nothing
        }
    }

    private fun getProductList(): LiveData<List<Product>> {
        when (searchType){
            ALL -> return productsViewModel.getAllProducts()
            CATEGORY -> return productsViewModel.getProductByCategory(searchCategory)
            SEARCH_STRING -> return performSearchQuery(productsViewModel.getAllProducts(), searchQuery)
        }
        return productsViewModel.getAllProducts()
    }


    // Show selection of desired sort option
    private fun setSortingOptions(option: Int){
        if (selected == option)
            return
        popup.menu.getItem(selected).isChecked = false
        popup.menu.getItem(option).isChecked = true
        selected = option
        when (option){
            0 -> productsList = sortByRecent(getProductList())
            1 -> productsList = sortByPriceLowToHigh(getProductList())
            2 -> productsList = sortByPriceHighToLow(getProductList())
        }
        productsList.observe(this) {
            resultsAdapter.replaceList(it)
        }
        Toast.makeText(this, "Sorting by ${popup.menu.getItem(option).title}", Toast.LENGTH_SHORT).show()
    }
}