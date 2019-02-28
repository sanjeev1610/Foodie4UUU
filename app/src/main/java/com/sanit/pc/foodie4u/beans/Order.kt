package com.sanit.pc.foodie4u.beans

data class Order(
                 var productId:String="",
                 var productName:String="",
                 var price:String="",
                 var quantiy:String="",
                 var discount:String="",
                 var image:String=""
)
{
    var id:Int=0
//    var productId:String
//        var productName:String
//        var price:String
//        var quantiy:String
//        var discount:String
//        var image:String
//    constructor(productId:String,productName:String,price:String,quantiy:String,discount:String,image:String){
//        this.productId = productId
//        this.productName = productName
//        this.price = price
//        this.quantiy = quantiy
//        this.discount = discount
//        this.image = image
//    }

    constructor(id:Int,productId:String,productName:String,price:String,quantiy:String,discount:String,image:String) : this(
    productId,
    productName,
    price,
    quantiy,
    discount,
    image
    ) {
        this.id = id
        this.productId = productId
        this.productName = productName
        this.price = price
        this.quantiy = quantiy
        this.discount = discount
        this.image = image
    }

}








//
//
//    (var id:Int,
//                 var productId:String="",
//                 var productName:String="",
//                 var price:String="",
//                 var quantiy:String="",
//                 var discount:String="",
//                 var image:String=""
//                 )
