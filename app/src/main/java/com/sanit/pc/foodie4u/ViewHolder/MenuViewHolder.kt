package com.sanit.pc.foodie4u.ViewHolder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.sanit.pc.foodie4u.R
import com.sanit.pc.foodie4u.interfaces.ItemClickListner
import kotlinx.android.synthetic.main.menu_item.view.*

class MenuViewHolder(itemVIew:View) : RecyclerView.ViewHolder(itemVIew),View.OnClickListener{

    var imgView:ImageView
    var textMenuName:TextView
    lateinit var itemClickListner:ItemClickListner
    init {
        imgView = itemVIew.findViewById(R.id.menu_image)
        textMenuName= itemVIew.findViewById(R.id.menu_name)
        itemVIew.setOnClickListener(this)
    }

    fun setItemClickListener(itemClickListner: ItemClickListner){
        this.itemClickListner = itemClickListner
    }
    override fun onClick(v: View?) {
        itemClickListner.onClick(v!!,adapterPosition,false)

    }
}