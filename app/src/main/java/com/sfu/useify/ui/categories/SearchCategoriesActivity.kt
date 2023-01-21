package com.sfu.useify.ui.categories

import android.content.Intent
import android.content.res.TypedArray
import android.os.Bundle
import android.view.MenuItem
import android.widget.ListView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import com.sfu.useify.R
import com.sfu.useify.Util
import com.sfu.useify.ui.results.ResultsActivity

class SearchCategoriesActivity: AppCompatActivity() {
    private lateinit var CATEGORIES: Array<String>
    private lateinit var CATEGORIES_ICONS: TypedArray
    private lateinit var categoriesListView: ListView
    private lateinit var searchView: SearchView
    private var mUserID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_categories)
        setTitle(R.string.search_categories_title)

        // Get bundle
        val bundle = intent.extras
        mUserID = Util.getUserID()

        // Setup back button in Action Bar
        // https://www.geeksforgeeks.org/how-to-add-and-customize-back-button-of-action-bar-in-android/
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        // Setup categories list view
        CATEGORIES = resources.getStringArray(R.array.categories)
        CATEGORIES_ICONS = resources.obtainTypedArray(R.array.icons_categories)
        categoriesListView = findViewById(R.id.listview_categories)
        val categoryAdapter = CategoryAdapter(this, CATEGORIES, CATEGORIES_ICONS)
        categoriesListView.adapter = categoryAdapter

        // Show results for the appropriate category
        categoriesListView.setOnItemClickListener() { adapterView, view, i, l ->
            onCategoryClicked(i)
        }

        // Setup SearchView
        searchView = findViewById(R.id.searchView_search_categories)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(queryString: String?): Boolean {
                onQuery(queryString)
                // true if the query has been handled by the listener, false to let the
                // SearchView perform the default action.
                return true
            }
            override fun onQueryTextChange(queryString: String?): Boolean {
                // false if the SearchView should perform the default action of showing any
                // suggestions if available, true if the action was handled by the listener.
                return true
            }
        })
    }

    private fun onQuery(queryString: String?){
        val searchIntent = Intent(this, ResultsActivity::class.java)

        val bundle = Bundle()
        bundle.putInt(resources.getString(R.string.key_search_query_type),
            ResultsActivity.SEARCH_STRING)
        bundle.putString(resources.getString(R.string.key_search_query), queryString)

        searchIntent.putExtras(bundle)
        startActivity(searchIntent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Show results given the selected category
    private fun onCategoryClicked(pos: Int) {
        val categoryIntent = Intent(this, ResultsActivity::class.java)

        val bundle = Bundle()
        if (pos == 0){
            bundle.putInt(resources.getString(R.string.key_search_query_type), ResultsActivity.ALL)
        } else {
            bundle.putInt(resources.getString(R.string.key_search_query_type), ResultsActivity.CATEGORY)
            bundle.putInt(resources.getString(R.string.key_category_id), pos)
        }
        bundle.putString(resources.getString(R.string.key_my_user_id), mUserID)

        categoryIntent.putExtras(bundle)
        startActivity(categoryIntent)
    }
}