package com.sanit.pc.foodie4u

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.FirebaseDatabase
import com.sanit.pc.foodie4u.Common.Common
import com.sanit.pc.foodie4u.ViewHolder.OrderViewHolder
import com.sanit.pc.foodie4u.beans.Order
import com.sanit.pc.foodie4u.beans.Requests
import kotlinx.android.synthetic.main.activity_order_status.*

class OrderStatus : AppCompatActivity() {

    lateinit var adapter:FirebaseRecyclerAdapter<Requests,OrderViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_status)


        recycler_order_status.setHasFixedSize(true)
        recycler_order_status.layoutManager = LinearLayoutManager(this)

//        if(intent==null){
//            loadOrders(Common.currentUser.phone)
//        }else{
//            val phone = intent.getStringExtra("Phone")
//            if(phone!=null){
//                loadOrders(phone)
//
//            }
//
//        }
        loadOrders(Common.currentUser.phone)



    }

    private fun loadOrders(phone: String) {
        val requests = FirebaseDatabase.getInstance().getReference("Requests")

        adapter = object:FirebaseRecyclerAdapter<Requests,OrderViewHolder>(

            Requests::class.java,
            R.layout.layout_order_status,
            OrderViewHolder::class.java,
            requests.orderByChild("phone").equalTo(phone)
            )
        {
            override fun populateViewHolder(vh: OrderViewHolder?, model: Requests?, position: Int) {
                vh!!.orderPhone.text = model!!.phone
                vh.orderAddress.text = model.address
                vh.orderStatus.text = Common.convertCodeToStatus(model.status)
                vh.orderId.text = adapter.getRef(position).key
            }

        }

        recycler_order_status.adapter = adapter

    }


}
