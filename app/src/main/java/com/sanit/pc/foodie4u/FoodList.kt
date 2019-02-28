package com.sanit.pc.foodie4u

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.widget.ShareDialog
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.*
import com.mancj.materialsearchbar.MaterialSearchBar
import com.sanit.pc.foodie4u.Common.Common
import com.sanit.pc.foodie4u.Database.Database
import com.sanit.pc.foodie4u.ViewHolder.FoodListVIewHolder
import com.sanit.pc.foodie4u.beans.Food
import com.sanit.pc.foodie4u.beans.Order
import com.sanit.pc.foodie4u.interfaces.ItemClickListner
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.activity_food_list.*
import kotlinx.android.synthetic.main.food_list_item.*

class FoodList : AppCompatActivity() {

    var categoryID:String?=null
    lateinit  var  firebaseRecyclerAdapter:FirebaseRecyclerAdapter<Food,FoodListVIewHolder>
    lateinit var searchAdapter:FirebaseRecyclerAdapter<Food,FoodListVIewHolder>

    internal var suggestList: MutableList<String> = mutableListOf()

    lateinit var food:DatabaseReference

    //facebook share
    lateinit var callbackManager: CallbackManager
    lateinit var shareDialog: ShareDialog


    internal var target: Target = object : Target {
        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            //Create photo from bitmap
            val photo = SharePhoto.Builder()
                .setBitmap(bitmap)
                .build()
            if (ShareDialog.canShow(SharePhotoContent::class.java)) {
                val content = SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .build()
                shareDialog.show(content)
            }
        }

        override fun onBitmapFailed(errorDrawable: Drawable) {

        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable) {

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_list)

        //get intent data

         food = FirebaseDatabase.getInstance().getReference("Food")


        callbackManager = CallbackManager.Factory.create()
        shareDialog = ShareDialog(this)

        //recycler
        recycler_food_list.setHasFixedSize(true)
        //swipe refresh
        foodlist_refresh.setColorSchemeResources(
            R.color.colorPrimary,
            android.R.color.holo_green_dark,
            android.R.color.holo_orange_dark,
            android.R.color.holo_blue_dark
        )

        foodlist_refresh.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener{
            override fun onRefresh() {
                if(intent!=null){
                    categoryID = intent.extras!!.getString("CategoryID")

                    if(categoryID!=null && !categoryID!!.isEmpty()){
                        if(Common.isConnectedToInternet(this@FoodList)) {
                            loadListFood(categoryID!!)
                        }else{
                            Toast.makeText(this@FoodList,"Please check your Internet connection",Toast.LENGTH_SHORT).show()

                        }
                    }
                    loadSuggestions()//load suggestions from firebase
                    searchBar.setCardViewElevation(10)
                    searchBar.addTextChangeListener(object: TextWatcher{
                        override fun afterTextChanged(s: Editable?) {

                        }

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            var suggession:MutableList<String> = mutableListOf()

                            for(search in suggestList){
                                if(search.toLowerCase().contains(searchBar.text.toLowerCase())){
                                    suggession.add(search)
                                }

                            }
                            searchBar.lastSuggestions = suggession
                        }

                    })//addTextChangeListener
                    searchBar.setOnSearchActionListener(object: MaterialSearchBar.OnSearchActionListener{

                        override fun onButtonClicked(buttonCode: Int) {

                        }

                        override fun onSearchStateChanged(enabled: Boolean) {
                            if(!enabled){
                                recycler_food_list.adapter = firebaseRecyclerAdapter
                            }
                        }

                        override fun onSearchConfirmed(text: CharSequence?) {
                            startSearch(text)

                        }

                    })//setOnSearchActionListener

                }
            }

        })
        foodlist_refresh.post {
            if(intent!=null){
                categoryID = intent.extras!!.getString("CategoryID")

                if(categoryID!=null && !categoryID!!.isEmpty()){
                    if(Common.isConnectedToInternet(this@FoodList)) {
                        loadListFood(categoryID!!)
                    }else{
                        Toast.makeText(this@FoodList,"Please check your Internet connection",Toast.LENGTH_SHORT).show()

                    }
                }
                loadSuggestions()//load suggestions from firebase
                searchBar.setCardViewElevation(10)
                searchBar.addTextChangeListener(object: TextWatcher{
                    override fun afterTextChanged(s: Editable?) {

                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        var suggession:MutableList<String> = mutableListOf()

                        for(search in suggestList){
                            if(search.toLowerCase().contains(searchBar.text.toLowerCase())){
                                suggession.add(search)
                            }

                        }
                        searchBar.lastSuggestions = suggession
                    }

                })//addTextChangeListener
                searchBar.setOnSearchActionListener(object: MaterialSearchBar.OnSearchActionListener{

                    override fun onButtonClicked(buttonCode: Int) {

                    }

                    override fun onSearchStateChanged(enabled: Boolean) {
                        if(!enabled){
                            recycler_food_list.adapter = firebaseRecyclerAdapter
                        }
                    }

                    override fun onSearchConfirmed(text: CharSequence?) {
                        startSearch(text)

                    }

                })//setOnSearchActionListener


            }
        }
//        if(intent!=null){
//            categoryID = intent.extras!!.getString("CategoryID")
//
//            if(categoryID!=null && !categoryID!!.isEmpty()){
//                if(Common.isConnectedToInternet(this@FoodList)) {
//                    loadListFood(categoryID!!)
//                }else{
//                    Toast.makeText(this@FoodList,"Please check your Internet connection",Toast.LENGTH_SHORT).show()
//
//                }
//            }
//        }

//        loadSuggestions()//load suggestions from firebase
//        searchBar.setCardViewElevation(10)
//        searchBar.addTextChangeListener(object: TextWatcher{
//            override fun afterTextChanged(s: Editable?) {
//
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                var suggession:MutableList<String> = mutableListOf()
//
//                for(search in suggestList){
//                    if(search.toLowerCase().contains(searchBar.text.toLowerCase())){
//                        suggession.add(search)
//                    }
//
//                }
//                searchBar.lastSuggestions = suggession
//            }
//
//        })//addTextChangeListener
//        searchBar.setOnSearchActionListener(object: MaterialSearchBar.OnSearchActionListener{
//
//            override fun onButtonClicked(buttonCode: Int) {
//
//            }
//
//            override fun onSearchStateChanged(enabled: Boolean) {
//                if(!enabled){
//                    recycler_food_list.adapter = firebaseRecyclerAdapter
//                }
//            }
//
//            override fun onSearchConfirmed(text: CharSequence?) {
//                startSearch(text)
//
//            }
//
//        })//setOnSearchActionListener


    }//onCreate

    private fun startSearch(text: CharSequence?) {
        searchAdapter = object:FirebaseRecyclerAdapter<Food,FoodListVIewHolder>(
            Food::class.java,
            R.layout.food_list_item,
            FoodListVIewHolder::class.java,
            food.orderByChild("name").equalTo(text.toString())
        ){

            override fun populateViewHolder(viewHolder: FoodListVIewHolder?, model: Food?, position: Int) {
                viewHolder!!.textFoodName.text = model!!.name
                Picasso.with(baseContext).load(model.image).into(viewHolder.imgView)

                viewHolder.setItemClickListener(object : ItemClickListner{
                    override fun onClick(view: View, pos: Int, isLongClick: Boolean) {
                        val foodDetailIntent = Intent(this@FoodList, FoodDetail::class.java)
                        foodDetailIntent.putExtra("FoodID", searchAdapter.getRef(pos).key)
                        startActivity(foodDetailIntent)
                    }

                })
            }

        }
        recycler_food_list.adapter = searchAdapter
    }

    private fun loadSuggestions() {
        food.orderByChild("menuId").equalTo(categoryID).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(dataSnap: DataSnapshot?) {
                for(item in dataSnap!!.children){
                    val eachItem = item.getValue(Food::class.java)

                    suggestList.add(eachItem!!.name)


                }
                searchBar.lastSuggestions = suggestList


            }

        })
    }

    private fun loadListFood(categoryID: String) {

        val food = FirebaseDatabase.getInstance().getReference("Food")

        firebaseRecyclerAdapter = object:FirebaseRecyclerAdapter<Food,FoodListVIewHolder>(
            Food::class.java,
            R.layout.food_list_item,
            FoodListVIewHolder::class.java,
            food.orderByChild("menuId").equalTo(categoryID)
        ){
            override fun populateViewHolder(viewHolder: FoodListVIewHolder?, model: Food?, position: Int) {

                val foodmodel = model
                viewHolder!!.textFoodName.text = model!!.name
                Picasso.with(this@FoodList).load(model.image).into(viewHolder.imgView)
                viewHolder.foodPrice.text = model.price


                //add to favourites
                if(Database(this@FoodList).isFavFood(firebaseRecyclerAdapter.getRef(position).key)){
                    viewHolder.favouritesImg.setImageResource(R.drawable.ic_favorite_black_24dp)
                }

                //click to change the state of the favourites
                viewHolder.favouritesImg.setOnClickListener {

                    if(!Database(this@FoodList).isFavFood(firebaseRecyclerAdapter.getRef(position).key)){

                        Database(this@FoodList).addToFavFood(firebaseRecyclerAdapter.getRef(position).key)

                        viewHolder.favouritesImg.setImageResource(R.drawable.ic_favorite_black_24dp)

                        Toast.makeText(this@FoodList,"${model.name} was added to favourites",Toast.LENGTH_SHORT).show()
                    }else{

                        viewHolder.favouritesImg.setImageResource(R.drawable.ic_favorite_border_black_24dp)
                        Toast.makeText(this@FoodList,"${model.name} was removed from favourites",Toast.LENGTH_SHORT).show()

                    }

                }


                viewHolder.btnCart.setOnClickListener {
                    Database(this@FoodList).addToCart(
                        Order(firebaseRecyclerAdapter.getRef(position).key,model.name,model.price,"1",model.discount,model.image)
                    )
                    Toast.makeText(this@FoodList,"Added to cart",Toast.LENGTH_SHORT).show()

                }



                viewHolder.setItemClickListener(object: ItemClickListner{
                    override fun onClick(view: View, pos: Int, isLongClick: Boolean) {
//                        Toast.makeText(this@FoodList,"clicked on"+foodmodel.name,Toast.LENGTH_SHORT).show()

                        val foodDetailIntent = Intent(this@FoodList,FoodDetail::class.java)
                        foodDetailIntent.putExtra("FoodID",firebaseRecyclerAdapter.getRef(pos).key)
                        startActivity(foodDetailIntent)


                    }

                })

                viewHolder.btnShare.setOnClickListener {
                    Toast.makeText(this@FoodList,"btnshare",Toast.LENGTH_SHORT).show()
                    Picasso.with(baseContext).load(model.image).into(target)
                }

            }

        }//firebaseRecyclerAdapter

        recycler_food_list.adapter = firebaseRecyclerAdapter
        foodlist_refresh.isRefreshing = false

    }//loodListFood
}
