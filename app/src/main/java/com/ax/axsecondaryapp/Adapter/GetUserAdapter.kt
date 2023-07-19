package com.ax.axsecondaryapp.Adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ax.axsecondaryapp.R
import com.ax.axsecondaryapp.databinding.ItemBuyAgainBinding
import com.ax.axsecondaryapp.model.sports.Stages


class GetUserAdapter(context: Activity?) :
    RecyclerView.Adapter<GetUserAdapter.ItemViewHolder>() {
    var arrayList: ArrayList<Stages>? = ArrayList()
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

    fun setDataChanged(order: ArrayList<Stages>?) {
        arrayList?.clear()
        if (order != null) {
            arrayList?.addAll(order)
        }
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        var item2 = arrayList!![position]

        holder.binding?.tvP1?.text=item2.events?.get(0)?.t1?.get(0)?.nm.toString()
        holder.binding?.tvP2?.text=item2.events?.get(0)?.t2?.get(0)?.nm.toString()

        var ov1 = item2.events?.get(0)?.tr1CO1?.toString()
        holder.binding?.tvOv1?.text="($ov1)"
        var ov2 = item2.events?.get(0)?.tr2CO1?.toString()
        holder.binding?.tvOv2?.text="($ov2)"

        var score1 = item2.events?.get(0)?.tr1C1?.toInt().toString()
        var wc1 = item2.events?.get(0)?.tr1CW1.toString()
        var score2 = item2.events?.get(0)?.tr2C1?.toInt().toString()
        var wc2 = item2.events?.get(0)?.tr2CW1.toString()
        holder.binding?.tvSc1?.text= "$score1 / $wc1"
        holder.binding?.tvSc2?.text= "$score2 / $wc2"

        holder.binding?.tvInfo?.text=item2.events?.get(0)?.eCo.toString()
        holder.binding?.tvEpsl?.text=item2.events?.get(0)?.epsL.toString()
        holder.binding?.tvCnm?.text=item2.cnm.toString()
        holder.binding?.tvSdn?.text=item2.sdn.toString()



    }


}