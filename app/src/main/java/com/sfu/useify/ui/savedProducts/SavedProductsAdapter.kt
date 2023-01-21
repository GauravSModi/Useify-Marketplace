package com.sfu.useify.ui.savedProducts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.sfu.useify.R
import com.sfu.useify.Util
import com.sfu.useify.database.productsViewModel
import com.sfu.useify.ui.results.ResultsAdapter
import com.squareup.picasso.Picasso
import java.lang.Exception

class SavedProductsAdapter(private val owner: LifecycleOwner?,
                           private var savedProductsList: List<String>,
                           private val onClickListener: (String) -> Unit):
    RecyclerView.Adapter<ResultsAdapter.ResultsViewHolder>() {

    class SavedProductsViewHolder(view: View,
                                  onClickPosition: (Int) -> Unit
    ): RecyclerView.ViewHolder(view){
        val productTitleTextView: TextView
        val productPriceTextView: TextView
        val productDateTextView: TextView
        val productImage: ImageView
        init {
            productTitleTextView = view.findViewById(R.id.textview_results_product_title)
            productPriceTextView = view.findViewById(R.id.textview_results_product_price)
            productDateTextView = view.findViewById(R.id.textview_results_product_date)
            productImage = view.findViewById(R.id.imageview_results_product_image)
            itemView.setOnClickListener{
                onClickPosition(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ResultsAdapter.ResultsViewHolder {
        // Create a new view, which defines the UI of the CardView item
        // and a clickListener from the activity
        val viewHolder = ResultsAdapter.ResultsViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.cardview_product, parent, false)
        ) {
            onClickListener(savedProductsList[it])
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ResultsAdapter.ResultsViewHolder, pos: Int) {
        productsViewModel().getProductByID(savedProductsList[pos]).observe(owner!!, Observer { product ->
            try {
                Picasso.get().load(product.image).into(holder.productImage)
            } catch (e: Exception){
                println("Debug: exception = $e")
            }
            holder.productTitleTextView.text = product.name
            holder.productPriceTextView.text = String.format("$%.2f", product.price)
            holder.productDateTextView.text = Util.calculateTimeSincePosted(product.createAt)
        })
    }

    override fun getItemCount(): Int {
        return savedProductsList.size
    }
}