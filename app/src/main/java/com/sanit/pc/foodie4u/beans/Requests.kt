package com.sanit.pc.foodie4u.beans

data class Requests(
    var phone:String="",
    var name:String="",
    var address:String="",
    var total:String="",
    var foods:List<Order> = emptyList(),
    var status:String="",//0-> placed,1->shipping,2->Shipped
    var comment: String="",
    var paymentStatus:String="",
    var latLng:String = ""
)