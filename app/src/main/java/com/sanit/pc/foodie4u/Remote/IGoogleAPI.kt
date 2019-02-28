package com.sanit.pc.foodie4u.Remote

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface IGoogleAPI {

    @GET
    fun getClientAddress(@Url url:String):Call<String>
}