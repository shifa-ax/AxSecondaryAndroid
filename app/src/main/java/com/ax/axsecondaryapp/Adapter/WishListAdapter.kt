package com.ax.axsecondaryapp.Adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ax.axsecondaryapp.R
import com.ax.axsecondaryapp.databinding.ItemBuyAgainBinding
import com.ax.axsecondaryapp.model.Photo

class WishListAdapter(context: Activity?) :
    RecyclerView.Adapter<WishListAdapter.ItemViewHolder>() {
    var arrayList: ArrayList<Photo>? = ArrayList()
    var activity: Activity? = context


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: ItemBuyAgainBinding = DataBindingUtil.inflate(
            LayoutInflater.from(activity),
            R.layout.item_buy_again,
            parent, false
        )
        return ItemViewHolder(binding.root)
    }


    override fun getItemCount(): Int {
        return if (null != arrayList) arrayList!!.size else 0
    }

    class ItemViewHolder internal constructor(itemView:  View) :
        RecyclerView.ViewHolder(itemView) {
        var binding: ItemBuyAgainBinding? = DataBindingUtil.bind(itemView)
    }

    fun setDataChanged(order: ArrayList<Photo>?) {
        arrayList?.clear()
        if (order != null) {
            arrayList?.addAll(order)
        }
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        var item = arrayList!![position]
        holder.binding?.item = item
    }

}