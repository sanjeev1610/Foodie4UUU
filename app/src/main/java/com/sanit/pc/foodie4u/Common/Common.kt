package com.sanit.pc.foodie4u.Common

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.sanit.pc.foodie4u.Remote.APIService
import com.sanit.pc.foodie4u.Remote.IGoogleAPI
import com.sanit.pc.foodie4u.Remote.RetrofitClient
import com.sanit.pc.foodie4u.beans.User

object Common {
  lateinit  var currentUser:User
     val DELETE:String = "Delete"
    val USER:String = "User"
    val PASSWORD:String = "Password"
    var TOPIC_NEWS = "News"
    var COMMENT_FOODID = "userPhone"


    private val BASE_URL = "https://fcm.googleapis.com/"
    private val GOOGLE_API_URL = "https://maps.googleapis.com/"

    fun getFCMService(): APIService {
        return RetrofitClient.getClient(BASE_URL).create(APIService::class.java)
    }
    fun getMapService(): IGoogleAPI {
        return RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPI::class.java)
    }
    fun isConnectedToInternet(context: Context):Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val networkInfo = connectivityManager.allNetworkInfo
            if (networkInfo != null) {
                for (i in networkInfo) {
                    if (i.state == NetworkInfo.State.CONNECTED) {
                        return true
                    }
                }
            }
        }
        return false
    }

     fun convertCodeToStatus(status: String): String {
        when(status){
            "0" -> return "Placed"
            "1" -> return "Shipping"
            "2" -> return "Shipped"
            else -> return "Shipped"
        }

    }
}