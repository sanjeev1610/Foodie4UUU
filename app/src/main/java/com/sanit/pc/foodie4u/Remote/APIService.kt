package com.sanit.pc.foodie4u.Remote

import com.sanit.pc.foodie4u.beans.DataMessage
import com.sanit.pc.foodie4u.beans.MyResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface APIService {
    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAABBSyP1c:APA91bEmyM3mIM3n203Ectmlll9TZBEPkNEzsiJwqxdMElf8WooI78LDAYb7BxRPDZIr9eZt1Ort2KkjOGozHh-VP03eVTAcwboUkW9sItkZaLIKQ6DlYSAQxpWtnIm3aP7N2cS1Kqhz"
    )

    @POST("fcm/send")
    fun sendNotification(@Body body: DataMessage): Call<MyResponse>


}