package com.sfu.useify.ui.categories

import android.content.Context
import android.content.res.TypedArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.sfu.useify.R

class CategoryAdapter(
        private val context: Context,
        private val categoryList: Array<String>,
        private val imgArray: TypedArray
    ): BaseAdapter()
{
    override fun getCount(): Int {
        return categoryList.size
    }

    override fun getItem(pos: Int): Any {
        return categoryList[pos]
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

    override fun getView(pos: Int, view: View?, parent: ViewGroup?): View {
        val categoryView: View

        if (view == null){
            // Inflate listview XML
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            categoryView = inflater.inflate(R.layout.listview_category, null, true)
        } else {
            categoryView = view
        }

        // Get views
        val imageViewIcon = categoryView.findViewById<ImageView>(R.id.imageview_category)
        val textViewCategory = categoryView.findViewById<TextView>(R.id.textview_category)

        // Set values
        imageViewIcon.setImageResource(imgArray.getResourceId(pos, 0))
        textViewCategory.text = categoryList[pos]

        return categoryView
    }
}