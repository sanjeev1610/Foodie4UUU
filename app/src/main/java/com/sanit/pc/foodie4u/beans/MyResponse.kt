package com.sanit.pc.foodie4u.beans

data class MyResponse(
    var multicast_id:Long = 0,
    var success: Int = 0,
    var failure: Int = 0,
    var canonical_ids: Int = 0,
    var results: List<Result> = emptyList()

    )
