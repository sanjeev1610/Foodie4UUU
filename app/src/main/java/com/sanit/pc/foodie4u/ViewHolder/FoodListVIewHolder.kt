package com.sanit.pc.foodie4u.ViewHolder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.sanit.pc.foodie4u.R
import com.sanit.pc.foodie4u.interfaces.ItemClickListner

class FoodListVIewHolder(itemView:View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
    var imgView: ImageView
    var textFoodName: TextView
    var favouritesImg: ImageView
    var btnShare: ImageView
    var foodPrice: TextView
    var btnCart: ImageView


    lateinit var itemClickListner: ItemClickListner
    init {
        imgView = itemView.findViewById(R.id.menu_image_food)
        textFoodName= itemView.findViewById(R.id.food_name)
        favouritesImg = itemView.findViewById(R.id.menu_fav_food)
        btnShare = itemView.findViewById(R.id.food_image_share)
        foodPrice = itemView.findViewById(R.id.food_price)
        btnCart = itemView.findViewById(R.id.menu_add_cart)

        itemView.setOnClickListener(this)
    }

    fun setItemClickListener(itemClickListner:ItemClickListner){
        this.itemClickListner = itemClickListner
    }
    override fun onClick(v: View?) {
        itemClickListner.onClick(v!!,adapterPosition,false)

    }

}