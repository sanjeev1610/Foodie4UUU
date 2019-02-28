package com.sanit.pc.foodie4u.ViewHolder

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.amulyakhare.textdrawable.TextDrawable
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.sanit.pc.foodie4u.Cart
import com.sanit.pc.foodie4u.Common.Common
import com.sanit.pc.foodie4u.Database.Database
import com.sanit.pc.foodie4u.R
import com.sanit.pc.foodie4u.beans.Order
import com.sanit.pc.foodie4u.interfaces.ItemClickListner
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_cart.*
import java.text.NumberFormat
import java.util.*

class CartViewHolder(itemVIew: View) : RecyclerView.ViewHolder(itemVIew), View.OnCreateContextMenuListener{


    var cartItemName: TextView
    var cartItemPrice: TextView
//    var cartImageViewCount: ImageView
    var elegantNumberButton:ElegantNumberButton
    var cartImage:ImageView

//    lateinit var itemClickListner: ItemClickListner
    init {
        cartItemName = itemVIew.findViewById(R.id.item_name)
        cartItemPrice= itemVIew.findViewById(R.id.item_price)
//        cartImageViewCount= itemVIew.findViewById(R.id.cart_item_count)
        elegantNumberButton = itemVIew.findViewById(R.id.btn_quantity)
        cartImage = itemVIew.findViewById(R.id.cart_image)


//        itemVIew.setOnClickListener(this)
        itemVIew.setOnCreateContextMenuListener(this)
    }

//    fun setItemClickListener(itemClickListner: ItemClickListner){
//        this.itemClickListner = itemClickListner
////    }
//    override fun onClick(v: View?) {
//        itemClickListner.onClick(v!!,adapterPosition,false)
//
//    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        menu!!.setHeaderTitle("Select Action")
        menu.add(0,0,adapterPosition,Common.DELETE)
    }
}

class CartAdapter(var cart: Cart, var listData:List<Order>):RecyclerView.Adapter<CartViewHolder>(){


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CartViewHolder {
        var itemView = LayoutInflater.from(cart).inflate(R.layout.cart_item,p0,false)

        return CartViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onBindViewHolder(vh: CartViewHolder, pos: Int) {
        vh.cartItemName.text = listData[pos].productName

        //text drawable
//        val drawable = TextDrawable.builder().buildRound(""+listData[pos].quantiy,Color.RED)
//        vh.cartImageViewCount.setImageDrawable(drawable)

        Picasso.with(cart).load(listData[pos].image).resize(70,70).centerCrop().into(vh.cartImage)
        vh.elegantNumberButton.setOnValueChangeListener { view, oldValue, newValue ->
           var order = listData[pos]
            order.quantiy = newValue.toString()
            Database(cart).updaateCart(order)
            //update total price
            var total = 0
            val orders = Database(cart).getCart()
            for (item in orders)
                total += Integer.parseInt(item.price) * Integer.parseInt(item.quantiy)
            // Locale locale = new Locale("en","IN");
            //  NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

            // cart.txtTotalPrice.setText(fmt.format(total));
            cart.total.text = total.toString()

            val locale = Locale("en","US")
            val nfmt = NumberFormat.getCurrencyInstance(locale)
            val price = Integer.parseInt(order.price) * Integer.parseInt(order.quantiy)

            vh.cartItemPrice.text = nfmt.format(price)
        }
        vh.elegantNumberButton.number = listData[pos].quantiy
        val locale = Locale("en","US")
        val nfmt = NumberFormat.getCurrencyInstance(locale)
        val price = Integer.parseInt(listData[pos].price) * Integer.parseInt(listData[pos].quantiy)

        vh.cartItemPrice.text = nfmt.format(price)


    }

}
