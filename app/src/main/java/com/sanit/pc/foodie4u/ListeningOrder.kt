package com.sanit.pc.foodie4u

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.NotificationChannel

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.google.firebase.database.*
import com.sanit.pc.foodie4u.Common.Common
import com.sanit.pc.foodie4u.beans.Order
import com.sanit.pc.foodie4u.beans.Requests
import android.graphics.Color
import android.os.Build


class ListeningOrder : Service(), ChildEventListener {


    var requests: DatabaseReference? = null
    override fun onCreate() {
        super.onCreate()
        requests = FirebaseDatabase.getInstance().getReference("Requests")

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        requests!!.addChildEventListener(this)
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onCancelled(p0: DatabaseError?) {


    }

    override fun onChildMoved(p0: DataSnapshot?, p1: String?) {

    }

    override fun onChildChanged(p0: DataSnapshot?, p1: String?) {

        val requestsBean = p0!!.getValue(Requests::class.java)
        showNotification(p0.key, requestsBean)


    }

    private fun showNotification(key: String?, requestsBean: Requests?) {

        val intent = Intent(this@ListeningOrder, OrderStatus::class.java)
           //        intent.putExtra("Phone",Common.currentUser.phone)
        val pIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


            // The id of the channel.
            val id = "my_channel_01"

            // The user-visible name of the channel.
            val name = "NotifiChannelName"

            // The user-visible description of the channel.
            val description = "Notification Description"

            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(id, name, importance)

            // Configure the notification channel.
            mChannel.description = description

            mChannel.enableLights(true)
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.lightColor = Color.RED

            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)


            notificationManager.createNotificationChannel(mChannel)
        }

        val builder = NotificationCompat.Builder(this)
        builder.setAutoCancel(true)
        builder.setTicker("Order Status ")
        builder.setContentInfo("Info")
        builder.setSmallIcon(R.drawable.ic_shopping_cart_black_24dp)
        builder.setContentIntent(pIntent)
        builder.setDefaults(Notification.DEFAULT_ALL)
        builder.setContentText("Your Status at $key was updated to ${Common.convertCodeToStatus(requestsBean!!.status)}) ")
        builder.setChannelId("my_channel_01")
        builder.setContentTitle("ContentZTitle")
        builder.setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(1, builder.build())

    }


    override fun onChildAdded(p0: DataSnapshot?, p1: String?) {

    }

    override fun onChildRemoved(p0: DataSnapshot?) {
    }


    override fun onDestroy() {
        super.onDestroy()
    }

}

































