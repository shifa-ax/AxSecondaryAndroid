package com.ax.axsecondaryapp.Adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ax.axsecondaryapp.R
import com.ax.axsecondaryapp.databinding.ItemFootballBinding
import com.ax.axsecondaryapp.model.sports.Stages


class GetFootballAdapter(context: Activity?) :
    RecyclerView.Adapter<GetFootballAdapter.ItemViewHolder>() {
    var arrayList: ArrayList<Stages>? = ArrayList()
    var activity: Activity? = context


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: ItemFootballBinding = DataBindingUtil.inflate(
            LayoutInflater.from(activity),
            R.layout.item_football,
            parent, false
        )
        return ItemViewHolder(binding.root)
    }


    override fun getItemCount(): Int {
        return if (null != arrayList) arrayList!!.size else 0
    }

    class ItemViewHolder internal constructor(itemView:  View) :
        RecyclerView.ViewHolder(itemView) {
        var binding: ItemFootballBinding? = DataBindingUtil.bind(itemView)
    }

    fun setDataChanged(order: ArrayList<Stages>?) {
        arrayList?.clear()
        if (order != null) {
            arrayList?.addAll(order)
        }
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        var item2 = arrayList!![position]
        Log.e("SHIFA", "onBindViewHolder: hhhhhh", )
        holder.binding?.tvP1?.text=item2.events?.get(0)?.t1?.get(0)?.nm.toString()
        holder.binding?.tvP2?.text=item2.events?.get(0)?.t2?.get(0)?.nm.toString()

        holder.binding?.tvSc1?.text= item2.events?.get(0)?.tr1OR.toString()
        holder.binding?.tvSc2?.text= item2.events?.get(0)?.tr2OR.toString()

        holder.binding?.tvInfo?.text=item2.events?.get(0)?.eCo.toString()
        holder.binding?.tvEpsl?.text=item2.events?.get(0)?.epsL.toString()
        holder.binding?.tvCnm?.text=item2.cnm.toString()
        holder.binding?.tvSdn?.text=item2.sdn.toString()
        var img1 = item2.events?.get(0)?.t1?.get(0)?.img.toString()
        var image1 = "https://lsm-static-prod.livescore.com/medium/$img1"
        var img2 = item2.events?.get(0)?.t2?.get(0)?.img.toString()


//        Glide.with(context).load(outletviewModel.nomemberurl).into(holder.binding?.)
//            .load(imageUrl) // sample image
//            .override(25, 15)
//          .placeholder(android.R.drawable.progress_indeterminate_horizontal) // need placeholder to avoid issue like glide annotations
//          .error(android.R.drawable.stat_notify_error) // need error to avoid issue like glide annotations
//        /
    }


}