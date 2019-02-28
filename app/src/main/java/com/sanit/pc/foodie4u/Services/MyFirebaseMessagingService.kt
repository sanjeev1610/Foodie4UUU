package com.sanit.pc.foodie4u.Services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sanit.pc.foodie4u.OrderStatus
import com.sanit.pc.foodie4u.R


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)

        if (!remoteMessage!!.data.isEmpty()){
//            var title = remoteMessage.notification!!.title!!
//            var body = remoteMessage.notification!!.body!!
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                sendNotificationAPI26(title,body)
//            } else {
//                sendNotification(title,body)
//            }
            var data = remoteMessage.data
            var title = data["title"].toString()
            var body = data["message"].toString()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sendNotificationAPI26(title,body)
            } else {
                sendNotification(title,body)
            }
        }else{
//             var title = remoteMessage.notification!!.title!!
//            var body = remoteMessage.notification!!.body!!
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                sendNotificationAPI26(title,body)
//            } else {
//                sendNotification(title,body)
//            }
            var title = remoteMessage.notification!!.title!!
            var body = remoteMessage.notification!!.body!!

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sendNotificationAPI26(title,body)
            } else {
                sendNotification(title,body)
            }
        }





    }

    private fun sendNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this@MyFirebaseMessagingService, OrderStatus::class.java)
        val pIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val builder = NotificationCompat.Builder(this)
        builder.setAutoCancel(true)
        builder.setTicker("Order Status ")
        builder.setContentInfo("Info")
        builder.setSmallIcon(R.drawable.ic_shopping_cart_black_24dp)
        builder.setContentIntent(pIntent)
        builder.setDefaults(Notification.DEFAULT_ALL)
        builder.setContentTitle(title)
        builder.setContentText(body)
        builder.setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(1, builder.build())
    }

    private fun sendNotificationAPI26(title: String?, body: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "com.sanit.foodie4u"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var notificationChannel = NotificationChannel(channelId,"Foodie4u",NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.description = ""
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(100,200,300,500,400,300,200,500)
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)
        }
        val intent = Intent(this@MyFirebaseMessagingService, OrderStatus::class.java)
        val pIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val builder = NotificationCompat.Builder(this,channelId)
        builder.setAutoCancel(true)
        builder.setTicker("Order Status ")
        builder.setContentInfo("Info")
        builder.setSmallIcon(R.drawable.ic_shopping_cart_black_24dp)
        builder.setContentIntent(pIntent)
        builder.setDefaults(Notification.DEFAULT_ALL)
        builder.setContentTitle(title)
        builder.setContentText(body)
        builder.setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(1, builder.build())
    }

}

