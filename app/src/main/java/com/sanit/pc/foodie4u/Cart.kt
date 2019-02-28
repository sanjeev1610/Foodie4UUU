package com.sanit.pc.foodie4u

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.paypal.android.sdk.payments.*
import com.rengwuxian.materialedittext.MaterialEditText
import com.sanit.pc.foodie4u.Common.Common
import com.sanit.pc.foodie4u.Common.Config
import com.sanit.pc.foodie4u.Database.Database
import com.sanit.pc.foodie4u.Remote.APIService
import com.sanit.pc.foodie4u.Remote.IGoogleAPI
import com.sanit.pc.foodie4u.ViewHolder.CartAdapter
import com.sanit.pc.foodie4u.beans.*
import kotlinx.android.synthetic.main.activity_cart.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.math.BigDecimal
import java.util.*

class Cart : AppCompatActivity(),GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener{

     var isCheked:Boolean = false
    var editAddress:PlaceAutocompleteFragment?=null
    var editLatLng:LatLng?=null

    var cart: MutableList<Order> = ArrayList<Order>()
    lateinit var adapter:CartAdapter
    var total1:Int?=null
    lateinit var mService:APIService
    var iGService:IGoogleAPI?=null

    //paypal
    companion object {
        val config = PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID)

    }
    var address:String?=null
    var comment:String?=null

     var shippingAddress:Place?=null

    //integrated Google service
    private val PLAY_SERVICES_RESOLUTION_REQUEST = 1000
    private val LOCATION_PERMISSION_REQUEST = 1001

    private var mlastLocation: Location? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null

    private val UPDATE_INTERVAL = 1000
    private val FATEST_INTERVA = 5000
    private val DISPLACEMENT = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestRuntimePermission()
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient()
                createLocationRequest()
            }
        }

        //init iGoogleAPI
        iGService = Common.getMapService()

        //init paypal
        val paypalIntent = Intent(this@Cart,PayPalService::class.java)
        paypalIntent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config)
        startService(paypalIntent)


        //init APIService
        mService = Common.getFCMService()

        recycler_Cart.setHasFixedSize(true)
        recycler_Cart.layoutManager = LinearLayoutManager(this)
        loadListFood()

        recycler_Cart.adapter = adapter

        place_order.setOnClickListener {
            if(cart.size>0) {
                showAlertDialog()
            }else{
                Toast.makeText(this@Cart,"Your Cart is Empty",Toast.LENGTH_SHORT).show()
            }
        }
    }
    val PAYPAL_REQ_CODE = 777
    private fun showAlertDialog() {
        var alert = AlertDialog.Builder(this)
        alert.setTitle("One More Step")
        alert.setMessage("Plz fill Address")
        alert.setIcon(R.drawable.ic_shopping_cart_black_24dp)
        alert.setCancelable(false)

//        var lp = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            LinearLayout.LayoutParams.MATCH_PARENT
//            )
//
//        val editAddress = EditText(this)
//        editAddress.layoutParams = lp
//
//        alert.setView(editAddress)
        val placeOrderView = LayoutInflater.from(this@Cart).inflate(R.layout.layout_place_order,null,false)
//        val editAddress = placeOrderView.findViewById<MaterialEditText>(R.id.edit_address_place_order)
         editAddress = fragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as PlaceAutocompleteFragment
        val editComment = placeOrderView.findViewById<MaterialEditText>(R.id.edit_comment_placeO)
        val rdbSetHomeAddress = placeOrderView.findViewById<RadioButton>(R.id.rb_setHomeAddress)
        val rdbSetToThisAddress = placeOrderView.findViewById<RadioButton>(R.id.rb_setShipAddress)

        //RADIO event
        rdbSetToThisAddress.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                isCheked = true
                getCompleteAddressString(mlastLocation!!.latitude,mlastLocation!!.longitude)

//                iGService!!.getClientAddress(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&key=AIzaSyBNRffcKu61HyXMjZZ9fuuHTKAZQxK8n6Q",mlastLocation!!.latitude,mlastLocation!!.longitude))
//                    .enqueue(object : Callback<String>{
//                        override fun onFailure(call: Call<String>, t: Throwable) {
//
//                        }
//
//                        override fun onResponse(call: Call<String>, response: Response<String>) {
//                            try {
//                                val jsonObj = response.body().toString() as JSONObject
//                                val jsonArray = jsonObj.getJSONArray("results")
//                                val result = jsonArray.getJSONObject(0)
//                                val format_addr = result.getString("formatted_address")
//                                address = format_addr
//                                editAddress.view.findViewById<EditText>(R.id.place_autocomplete_search_input)
//                                    .setText(address)
//                            }catch (e:JSONException){
//                                e.printStackTrace()
//                            }
//
//
//                        }
//
//                    })

            }
        }
        rdbSetHomeAddress.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                isCheked = true

                if (Common.currentUser.homeAddress != null || !TextUtils.isEmpty(Common.currentUser.homeAddress)) {
                    address = Common.currentUser.homeAddress
                    //  ((EditText) editAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setText(address);
                    editAddress!!.setText(address)
                    getLalng(address!!)

                } else {
                    // ((EditText) editAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setText("");
                    editAddress!!.setText("")
                    Toast.makeText(this@Cart, "Please update your home address", Toast.LENGTH_SHORT).show()

                }
            }
        }

        //hide search icon
        editAddress!!.view.findViewById<AppCompatImageButton>(R.id.place_autocomplete_search_button).visibility = View.GONE
        editAddress!!.view.findViewById<EditText>(R.id.place_autocomplete_search_input).textSize = 14.0f
        //get Address from place auto complete
        editAddress!!.setOnPlaceSelectedListener(object : PlaceSelectionListener{
            override fun onPlaceSelected(p0: Place?) {
                shippingAddress = p0!!

            }

            override fun onError(p0: Status?) {
                Log.e("ERROR PLACE",p0!!.statusMessage)
            }

        })





        alert.setView(placeOrderView)

        alert.setPositiveButton("YES",object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {


//                address = editAddress.text.toString()

                 if (editAddress!!.view.findViewById<EditText>(R.id.place_autocomplete_search_input).text.toString()!="" && editComment.text.toString()!="") {
                     if(!rdbSetToThisAddress.isChecked && !rdbSetHomeAddress.isChecked) {

                         if (shippingAddress != null) {
                             address = shippingAddress!!.address!!.toString()
                         }else{

                             return
                         }
                     }
                     if(TextUtils.isEmpty(address)){
                         return
                     }
                        comment = editComment.text.toString()
                        val formatAmount = total.text.toString().replace("$", "").replace(",", "")
                        val paypalPayment = PayPalPayment(
                            BigDecimal(formatAmount),
                            "USD",
                            "Foodie4u order",
                            PayPalPayment.PAYMENT_INTENT_SALE
                        )
                        val paymentActivityIntent = Intent(this@Cart, PaymentActivity::class.java)
                        paymentActivityIntent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config)
                        paymentActivityIntent.putExtra(PaymentActivity.EXTRA_PAYMENT, paypalPayment)
                        startActivityForResult(paymentActivityIntent, PAYPAL_REQ_CODE)


                }else{
                    Toast.makeText(this@Cart,"Plz fill address,comment",Toast.LENGTH_SHORT).show()
                }


                //remove fragment
                fragmentManager.beginTransaction().remove(fragmentManager.findFragmentById(R.id.place_autocomplete_fragment)).commit()



//
//                 val requests = Requests(
//                     Common.currentUser.phone,
//                     Common.currentUser.username,
//                     editAddress.text.toString(),
//                     total.text.toString(),
//                     cart,
//                     "0",
//                     editComment.text.toString()
//                 )
//
//                val requestRef = FirebaseDatabase.getInstance().getReference("Requests")
//                requestRef.child(System.currentTimeMillis().toString())
//                    .setValue(requests)
//
//                Database(this@Cart).cleanCart()
//
//                //send Notification
//
//                sendNotificationToServer()
//
//                Toast.makeText(this@Cart,"Thank you Your Order placed",Toast.LENGTH_SHORT).show()
//
//                finish()

            }

        })
        alert.setNegativeButton("Cancel",object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog!!.dismiss()
                //remove fragment
                fragmentManager.beginTransaction().remove(fragmentManager.findFragmentById(R.id.place_autocomplete_fragment)).commit()
            }

        })

        alert.show()

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode==PAYPAL_REQ_CODE && resultCode== Activity.RESULT_OK && data!=null){
            val confirmation = data.getParcelableExtra<PaymentConfirmation>(PaymentActivity.EXTRA_RESULT_CONFIRMATION)
            if(confirmation!=null){
                try {
                    val paymentDetail = confirmation.toJSONObject().toString(4)
                    val jsonObject = JSONObject(paymentDetail)
                    var requests:Requests?=null
                    if (isCheked){
                         requests = Requests(
                        Common.currentUser.phone,
                        Common.currentUser.username,
                        address!!,
                        total.text.toString(),
                        cart,
                        "0",
                        comment!!,
                        jsonObject.getJSONObject("response").getString("state"),
                        String.format("%s ,%s",editLatLng!!.latitude,editLatLng!!.longitude)
                      )
                        isCheked = false
                    }else{
                        requests = Requests(
                            Common.currentUser.phone,
                            Common.currentUser.username,
                            address!!,
                            total.text.toString(),
                            cart,
                            "0",
                            comment!!,
                            jsonObject.getJSONObject("response").getString("state"),
                            String.format("%s ,%s",shippingAddress!!.latLng.latitude,shippingAddress!!.latLng.longitude)
                        )
                    }

                    val requestRef = FirebaseDatabase.getInstance().getReference("Requests")
                    requestRef.child(System.currentTimeMillis().toString())
                        .setValue(requests)

                    Database(this@Cart).cleanCart()

                    //send Notification

                    sendNotificationToServer()

                    Toast.makeText(this@Cart,"Thank you Your Order placed",Toast.LENGTH_SHORT).show()

                    finish()

                }catch (e:JSONException){
                    e.printStackTrace()
                }
            }
        }else if(requestCode==PAYPAL_REQ_CODE && resultCode== Activity.RESULT_CANCELED){
            Toast.makeText(this@Cart,"Payment cancel",Toast.LENGTH_SHORT).show()

        }else if(requestCode==PAYPAL_REQ_CODE && resultCode== PaymentActivity.RESULT_EXTRAS_INVALID){
            Toast.makeText(this@Cart,"Invalid payment",Toast.LENGTH_SHORT).show()

        }
    }

    private fun sendNotificationToServer() {
        val tokens = FirebaseDatabase.getInstance().getReference("Tokens")
        val data = tokens.orderByChild("serverToken").equalTo("true")
        data.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    val serverToken = postSnapshot.getValue<Token>(Token::class.java)

                    //create raw payload to send
                    //                    Notification notification = new Notification("You have new order"+order_number, "Routine Basket");
                    //                    Sender content = new Sender(serverToken.getToken(),notification);
                    val dataSend = HashMap<String, String>()
                    dataSend["title"] = "Foodie4U"
                    dataSend["message"] = "You Have New Order at" + System.currentTimeMillis().toString()
                    assert(serverToken != null)
                    val dataMessage = DataMessage(serverToken!!.token!!, dataSend)

                    val test = Gson().toJson(dataMessage)
                    Log.d("Content", test)

                    mService.sendNotification(dataMessage)
                        .enqueue(object : Callback<MyResponse> {
                            override fun onResponse(call: Call<MyResponse>, response: Response<MyResponse>) {
                                if (response.code() == 200) {
                                    assert(response.body() != null)
                                    if (response.body()!!.success == 1) {
                                        Toast.makeText(this@Cart, "Thank You, Order Placed", Toast.LENGTH_LONG).show()
                                        finish()

                                    } else {
                                        Toast.makeText(this@Cart, "Failed!!!", Toast.LENGTH_LONG).show()
                                        finish()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                                Log.e("Error", t.message)
                            }
                        })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }


    private fun loadListFood() {
        cart = Database(this).getCart()
        adapter = CartAdapter(this, cart)
        adapter.notifyDataSetChanged()
        recycler_Cart.adapter= adapter

        //cal total price
        var totalprice = 0
        for (order in cart) {
            totalprice += Integer.parseInt(order.price) * Integer.parseInt(order.quantiy)
        }

        total1 = totalprice
        total.text = total1.toString()
        println(totalprice)



    }//loadListFood

    override fun onContextItemSelected(item: MenuItem?): Boolean {

        if(item!!.title==Common.DELETE){
            deleteCartItem(item.order)
        }
        return super.onContextItemSelected(item)
    }

    private fun deleteCartItem(position: Int) {
        //remove item from cart i.e list<order>
        cart.removeAt(position)
        //clean old data in  database
        Database(this).cleanCart()
        //we will update new data from list<order>
        for(item in cart){
            Database(this).addToCart(item)
        }
        loadListFood()
    }


    private fun requestRuntimePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST
        )

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkPlayServices()) {
                    buildGoogleApiClient()
                    createLocationRequest()

                    displayLocation()
                }
            }
        }
    }//

    private fun checkPlayServices(): Boolean {

        val resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show()
            } else {
                Toast.makeText(this, "This device is not support", Toast.LENGTH_SHORT).show()
                finish()
            }
            return false
        }
        return true

    }

    @Synchronized
    protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API).build()
        mGoogleApiClient!!.connect()
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest.create()
        mLocationRequest!!.setInterval(UPDATE_INTERVAL.toLong())
        mLocationRequest!!.setFastestInterval(FATEST_INTERVA.toLong())
        mLocationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        mLocationRequest!!.setSmallestDisplacement(DISPLACEMENT.toFloat())
    }

    private fun displayLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestRuntimePermission()
        } else {
            mlastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
            if (mlastLocation != null) {

                Log.d("LOCATION", "Your Location:" + mlastLocation!!.latitude + "," + mlastLocation!!.longitude)
            } else {
                Log.d("LOCATION", "Could not get your location")

            }
        }
    }

    override fun onConnected(p0: Bundle?) {
        //To change body of created functions use File | Settings | File Templates.
        displayLocation()
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
            mGoogleApiClient,
            mLocationRequest,
            this as com.google.android.gms.location.LocationListener
        )

    }


    override fun onConnectionSuspended(p0: Int) {
        //To change body of created functions use File | Settings | File Templates.

            mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLocationChanged(location: Location?) {
        //To change body of created functions use File | Settings | File Templates.
        mlastLocation = location
        displayLocation()
    }


    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double): String {
        var strAdd = ""
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if (addresses != null) {
                val returnedAddress = addresses[0]
                val strReturnedAddress = StringBuilder("")

                for (i in 0..returnedAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()
                address = strAdd
                editLatLng = LatLng(LATITUDE,LONGITUDE)
//                getLalng(address!!)
                // ((EditText)editAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setText(address);
                editAddress!!.setText(address)
                Log.w("My Current loction", strReturnedAddress.toString())
            } else {
                Log.w("My Current loction", "No Address returned!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.w("My Current loction", "Canont get Address!")
        }

        return strAdd
    }

    private fun getLalng(s: String) {
        val coder = Geocoder(this)
        val addr: List<Address>?
        var p1: LatLng? = null

        try {
            // May throw an IOException
            addr = coder.getFromLocationName(s, 5)
            if (addr == null) {
                return
            }
            if (addr.size < 1) {
                Toast.makeText(this@Cart, "Null Address", Toast.LENGTH_SHORT).show()
            }

            val location = addr[0]
            p1 = LatLng(location.latitude, location.longitude)

        } catch (ex: IOException) {

            ex.printStackTrace()
        }

        editLatLng= p1
        address = s


    }

}
