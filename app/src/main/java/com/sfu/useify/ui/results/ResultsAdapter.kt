package com.sfu.useify.ui.results

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sfu.useify.R
import com.sfu.useify.Util
import com.sfu.useify.models.Product
import com.squareup.picasso.Picasso
import java.lang.Exception

// RecyclerView adapter for Browse Product Results page
// References:
// https://www.youtube.com/watch?v=GvLgWjPigmQ&ab_channel=KonstantinosReppas
class ResultsAdapter(private var products: List<Product>?,
                     private val onClickListener: (String) -> Unit):
    RecyclerView.Adapter<ResultsAdapter.ResultsViewHolder>()
{
    class ResultsViewHolder(view: View, onClickPosition:(Int) -> Unit): RecyclerView.ViewHolder(view){
        val productTitleTextView: TextView
        val productPriceTextView: TextView
        val productDateTextView: TextView
        val productImage: ImageView
        init {
            productTitleTextView = view.findViewById(R.id.textview_results_product_title)
            productPriceTextView = view.findViewById(R.id.textview_results_product_price)
            productDateTextView = view.findViewById(R.id.textview_results_product_date)
            productImage = view.findViewById(R.id.imageview_results_product_image)
            view.tag = this
            itemView.setOnClickListener{
                onClickPosition(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsViewHolder {
        // Create a new view, which defines the UI of the CardView item
        // and a clickListener from the activity
        val viewHolder = ResultsViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.cardview_product, parent, false)) {
            onClickListener(products?.get(it)!!.productID)
        }
        return viewHolder
    }

    // Replace the contents of the view with product data
    override fun onBindViewHolder(holder: ResultsViewHolder, pos: Int) {
        val product = products?.get(pos) ?: return
        try {
            Picasso.get().load(product.image).into(holder.productImage)
        } catch (e: Exception){
            println("Debug: exception = $e")
        }
        holder.productTitleTextView.text = product.name
        holder.productPriceTextView.text = String.format("$%.2f", product.price)
        holder.productDateTextView.text = Util.calculateTimeSincePosted(product.createAt)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        if (products == null)
            return 0
        return products!!.size
    }

    fun replaceList(it: List<Product>?) {
        products = it
    }

}

