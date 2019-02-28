package com.sanit.pc.foodie4u.ViewHolder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.sanit.pc.foodie4u.R
import com.sanit.pc.foodie4u.interfaces.ItemClickListner
import kotlinx.android.synthetic.main.layout_order_status.view.*

class OrderViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{


    var orderId:TextView
    var orderStatus:TextView
    var orderPhone:TextView
    var orderAddress:TextView

    lateinit var itemClickListner:ItemClickListner

    init {

        orderId = itemView.findViewById(R.id.order_id)
        orderStatus = itemView.findViewById(R.id.order_status)
        orderAddress = itemView.findViewById(R.id.order_address)
        orderPhone = itemView.findViewById(R.id.order_phone)

        itemView.setOnClickListener(this)

    }


     fun SetOnItemClickListener(itemClickListner: ItemClickListner){
        this.itemClickListner = itemClickListner
    }


    override fun onClick(v: View?) {
        itemClickListner.onClick(v!!,adapterPosition,false)
    }


}