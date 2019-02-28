package com.sanit.pc.foodie4u

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.sanit.pc.foodie4u.Database.Database
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import android.location.Geocoder
import android.content.DialogInterface
import com.google.firebase.database.DatabaseReference
import com.google.firebase.messaging.FirebaseMessaging
import dmax.dialog.SpotsDialog
import android.text.TextUtils
import com.google.firebase.database.DatabaseError
import com.sanit.pc.foodie4u.beans.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.rengwuxian.materialedittext.MaterialEditText
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.content.Intent
import android.location.Address
import com.facebook.accountkit.AccountKit
import com.google.firebase.database.FirebaseDatabase
import com.daimajia.slider.library.SliderLayout
import com.daimajia.slider.library.SliderTypes.BaseSliderView
import com.daimajia.slider.library.SliderTypes.TextSliderView
import com.google.firebase.iid.FirebaseInstanceId
import android.support.v7.widget.GridLayoutManager
import com.sanit.pc.foodie4u.interfaces.ItemClickListner
import com.squareup.picasso.Picasso
import com.sanit.pc.foodie4u.ViewHolder.MenuViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import android.support.design.widget.NavigationView
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import com.sanit.pc.foodie4u.Common.Common
import com.sanit.pc.foodie4u.beans.Banner
import com.sanit.pc.foodie4u.beans.Category
import com.sanit.pc.foodie4u.beans.Token
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.android.synthetic.main.nav_header_home.view.*
import java.io.IOException


class Home : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var category: DatabaseReference

    lateinit var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<Category, MenuViewHolder>



    lateinit var image_list: HashMap<String, String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setSupportActionBar(toolbar)
        toolbar.title = "Menu"

        firebaseDatabase = FirebaseDatabase.getInstance()
        category = firebaseDatabase.getReference("Category")

        fab.setOnClickListener {
            val cartIntent = Intent(this@Home, Cart::class.java)
            startActivity(cartIntent)
        }

        fab.count = Database(this).getCounterCount()

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        //set nav header name
        val headerView = navigationView.getHeaderView(0)
        headerView.textFullname.text = (Common.currentUser.username)


        //swipe refresh
        home_refresh.setColorSchemeResources(
            R.color.colorPrimary,
            android.R.color.holo_green_dark,
            android.R.color.holo_orange_dark,
            android.R.color.holo_blue_dark
        )

        home_refresh.setOnRefreshListener {
            if (Common.isConnectedToInternet(baseContext)) {
                loadMenu()

            } else {
                Toast.makeText(this@Home, "Please Check your Internet Connection!!!", Toast.LENGTH_LONG).show()

            }
        }

        //default first item load
        home_refresh.post {
            if (Common.isConnectedToInternet(baseContext)) {
                loadMenu()

            } else {
                Toast.makeText(this@Home, "Please Check your Internet Connection!!!", Toast.LENGTH_LONG).show()

            }
        }

        //load menu

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Category, MenuViewHolder>(
            Category::class.java,
            R.layout.menu_item,
            MenuViewHolder::class.java,
            category
        ) {
            override fun populateViewHolder(viewHolder: MenuViewHolder, model: Category, position: Int) {
                viewHolder.textMenuName.text = model.name
                Picasso.with(baseContext).load(model.image).into(viewHolder.imgView)

                viewHolder.itemClickListner = object : ItemClickListner {
                    override fun onClick(view: View, pos: Int, isLongClick: Boolean) {
                        val foodListIntent = Intent(this@Home, FoodList::class.java)
                        foodListIntent.putExtra("CategoryID", firebaseRecyclerAdapter.getRef(position).key)
                        startActivity(foodListIntent)
                        Toast.makeText(this@Home, "" + model.name, Toast.LENGTH_LONG).show()
                    }

                }
            }

        }


        //aniamtion

        recycler_view.layoutManager = GridLayoutManager(this, 2)
        if (Common.isConnectedToInternet(baseContext)) {
            loadMenu()
        } else {
            Toast.makeText(this@Home, "Please Check your Internet Connection!!!", Toast.LENGTH_LONG).show()

        }


        updateToken(FirebaseInstanceId.getInstance().token)
        //slider
        setupSlider()


    }

    private fun setupSlider() {
        image_list = HashMap()
        val banners = firebaseDatabase.getReference("Banner")
        banners.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    val banner = postSnapshot.getValue<Banner>(Banner::class.java)
                    //we concat imageName and id like name@@@01 and use name for description id for foodId to click
                    image_list[banner!!.name + "@@@" + banner.id] = banner.image
                }
                for (key in image_list.keys) {
                    val key_split = key.split("@@@".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    val nameOfFood = key_split[0]
                    val idOfFood = key_split[1]

                    //create slider
                    val textSliderView = TextSliderView(baseContext)
                    textSliderView.description(nameOfFood)
                        .setScaleType(BaseSliderView.ScaleType.Fit)
                        .image(image_list[key])
                        .setOnSliderClickListener {
                            val intent = Intent(this@Home, FoodDetail::class.java)
                            //we will send food id to foodDetail
                            intent.putExtras(textSliderView.bundle)
                            startActivity(intent)
                        }
                    //Add extra bundle
                    textSliderView.bundle(Bundle())
                    textSliderView.bundle.putString("foodId", idOfFood)

                    slider_home.addSlider(textSliderView)
                    //remove event after finish
                    banners.removeEventListener(this)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
        slider_home.setPresetTransformer(SliderLayout.Transformer.Background2Foreground)
        slider_home.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom)

        slider_home.moveNextPosition()
        slider_home.movePrevPosition()
        slider_home.setDuration(4000)

    }

    private fun updateToken(token: String?) {

        val db = FirebaseDatabase.getInstance()
        val tokens = db.getReference("Tokens")
        val data = Token(token, "false")
        tokens.child(Common.currentUser.phone).setValue(data)
    }

    private fun loadMenu() {

        recycler_view.adapter = firebaseRecyclerAdapter
//        recycler_view.isRefreshing = false
        recycler_view.adapter!!.notifyDataSetChanged()
//        recycler_view.scheduleLayoutAnimation()
    }

    override fun onStop() {
        slider_home.startAutoCycle()
        super.onStop()

    }

    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        if (item.itemId === R.id.home_refresh) {
//            if (Common.isConnectedToInternet(baseContext)) {
//                loadMenu()
//            } else {
//                Toast.makeText(this@Home, "Please check your Internet connection!!", Toast.LENGTH_LONG).show()
//            }
//        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.getItemId()

        if (id == R.id.nav_menu) {
            // Handle the camera action

        } else if (id == R.id.nav_orders) {

            startActivity(Intent(this@Home, OrderStatus::class.java))

        } else if (id == R.id.nav_logout) {
            //  Paper.book().destroy();
            AccountKit.logOut()
            val signIn = Intent(this@Home, MainActivity::class.java)
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(signIn)

        } else if (id == R.id.nav_cart) {

            startActivity(Intent(this@Home, Cart::class.java))

        } else if (id == R.id.nav_settings) {
            showSubscribeNews()
        } else if (id == R.id.nav_fav) {
            startActivity(Intent(this@Home,  Home::class.java))
        } else if (id == R.id.nav_share) {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Hai,this is Routine Basket App shortly RB,Here you can shop online plz download it from play store and share to others ,thank you."
            )
            sendIntent.type = "text/plain"
            startActivity(Intent.createChooser(sendIntent, "Share via"))

        }

//        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showSubscribeNews() {
        val alert = AlertDialog.Builder(this@Home)
        alert.setTitle("Settings")
        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.layout_settings, null)
        val ckb_sub_news = view.findViewById(R.id.ckb_checkbox_sub_news) as CheckBox
        val home_address1 = view.findViewById(R.id.edit_home_address1) as MaterialEditText
        val editName1 = view.findViewById(R.id.edit_update_name1) as MaterialEditText
        val editEmail = view.findViewById(R.id.edit_email) as MaterialEditText

        val usr = firebaseDatabase.getReference("User")
        usr.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(Common.currentUser.phone).exists()) {
                    var u =dataSnapshot.child(Common.currentUser.phone).getValue(User::class.java)
                    if (u!!.email != null) {
                        editEmail.setText(u.email)
                    } else {
                        editEmail.setText("")
                    }
                    if (u.homeAddress != null) {
                        home_address1.setText(u.homeAddress)
                    } else {
                        home_address1.setText("")
                    }
                    if (u.username != null) {
                        editName1.setText(u.username)

                    } else {
                        editName1.setText("")

                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })



        alert.setView(view)
        alert.setIcon(R.drawable.ic_settings_black_24dp)
        Paper.init(this)
        val isSub_news = Paper.book().read<String>("sub_news")
        if (isSub_news == null || TextUtils.isEmpty(isSub_news) || isSub_news == "false") {
            ckb_sub_news.isChecked = (false)
        } else {
            ckb_sub_news.isChecked = (true)
        }
        alert.setPositiveButton(
            "Yes/Update", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
                val waitDialog = SpotsDialog.Builder().setContext(this@Home).build()
                waitDialog.show()
                if (ckb_sub_news.isChecked()) {
                    waitDialog.dismiss()
                    FirebaseMessaging.getInstance().subscribeToTopic(Common.TOPIC_NEWS)
                    Paper.book().write("sub_news", "true")
                    Toast.makeText(this@Home, "Your Subscribed ", Toast.LENGTH_SHORT).show()
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.TOPIC_NEWS)
                    Paper.book().write("sub_news", "false")
                    Toast.makeText(this@Home, "Your UnSubscribed ", Toast.LENGTH_SHORT).show()

                }//checkbox

                getLalng(home_address1.text!!.toString())
                val dbRef = firebaseDatabase.getReference("User")
                Common.currentUser.homeAddress = (home_address1.text!!.toString())
                Common.currentUser.username = (editName1.text!!.toString())
                Common.currentUser.email = (editEmail.text!!.toString())
                dbRef.child(Common.currentUser.phone)
                    .setValue(Common.currentUser)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this@Home, "Updated Name Email Address", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@Home, "error in updated", Toast.LENGTH_SHORT).show()

                        }
                    }
            }//onclick
        )
            .setCancelable(false)
            .setNegativeButton("No"){ dialog, which -> dialog.dismiss() }
            .show()


    }


    private fun getLalng(s: String) {
        val coder = Geocoder(this)
        val address: List<Address>?
        var p1: LatLng? = null

        try {
            // May throw an IOException
            address = coder.getFromLocationName(s, 5)
            if (address == null) {
                return
            }
            if (address.size < 1) {
                Toast.makeText(this@Home, "Null Address", Toast.LENGTH_SHORT).show()
            }

            val location = address[0]
            p1 = LatLng(location.getLatitude(), location.getLongitude())

        } catch (ex: IOException) {

            ex.printStackTrace()
        }

        Common.currentUser.latLng = (String.format("%s,%s", p1!!.latitude, p1.longitude))
        Toast.makeText(this@Home, "Address" + p1.latitude.toString(), Toast.LENGTH_SHORT).show()
        Log.d("LATLNG", p1!!.latitude.toString())


    }

    override fun onResume() {
        super.onResume()
        fab.count = Database(this).getCounterCount()
    }
}
