package com.sanit.pc.foodie4u

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.*
import com.sanit.pc.foodie4u.Common.Common
import com.sanit.pc.foodie4u.Database.Database
import com.sanit.pc.foodie4u.beans.Food
import com.sanit.pc.foodie4u.beans.Order
import com.squareup.picasso.Picasso
import com.stepstone.apprating.AppRatingDialog
import com.stepstone.apprating.listener.RatingDialogListener
import kotlinx.android.synthetic.main.activity_food_detail.*
import java.util.*

class FoodDetail : AppCompatActivity(), RatingDialogListener {

    lateinit var foodID:String
    lateinit var currentFood:Food
    lateinit var ratingTbl:DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_detail)

        //collapsable toolbar layout
        collapsing.setExpandedTitleTextAppearance(R.style.ExpandedAppbar)
        collapsing.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar)

        //Firebase
         ratingTbl = FirebaseDatabase.getInstance().getReference("Rating")


        //get intent
        if(intent!=null){
            foodID = intent.extras!!.getString("FoodID")!!

            if(!foodID.isEmpty()){
                if(Common.isConnectedToInternet(this@FoodDetail)) {
                    getDetailFood(foodID)
                    getFoodRating(foodID)

                }else{
                    Toast.makeText(this@FoodDetail,"Please check your Internet connection",Toast.LENGTH_SHORT).show()

                }

            }
        }

        btnCart.setOnClickListener {
            addToCart()
        }
        btnCart.count = Database(this).getCounterCount()
        btn_rate.setOnClickListener {
            showRatingDialog()
        }
        show_comment.setOnClickListener {
            val intent = Intent(this@FoodDetail, ShowComments::class.java)
            intent.putExtra(Common.COMMENT_FOODID, foodID)
            startActivity(intent)
        }

    }

    private fun getFoodRating(foodID: String) {
        var foodRating:Query = ratingTbl.orderByChild("foodId").equalTo(foodID)
        foodRating.addValueEventListener(object : ValueEventListener{

            var sum:Int = 0
            var count:Int = 0
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                for(datasnapshot in p0!!.children){
                    val item = datasnapshot.getValue(Rating::class.java)
                    sum+= Integer.parseInt(item!!.rateValue)
                    count++
                }
                if(count!=0){
                    var avgRating = (sum/count).toFloat()
                    rateBar.rating = avgRating
                }

            }

        })
    }

    private fun showRatingDialog() {
        AppRatingDialog.Builder()
            .setPositiveButtonText("Submit")
            .setNegativeButtonText("Cancel")
            .setNoteDescriptions(Arrays.asList("Very Bad","Not Good","Quick Ok","Very Good","Excellent"))
            .setDefaultRating(1)
            .setTitle("Rate This Food")
            .setDescription("Please select some stars and give your feedback")
            .setTitleTextColor(R.color.colorPrimary)
            .setDescriptionTextColor(R.color.colorPrimary)
            .setHint("Please write your comment here...")
            .setHintTextColor(R.color.colorAccent)
            .setCommentTextColor(android.R.color.white)
            .setCommentBackgroundColor(R.color.colorPrimaryDark)
            .setWindowAnimation(R.style.MyDialogFadeAnimation)
            .create(this@FoodDetail)
            .show()
    }

    override fun onNegativeButtonClicked() {

    }

    override fun onNeutralButtonClicked() {

    }

    override fun onPositiveButtonClicked(rate: Int, comment: String) {
        var rating = Rating(Common.currentUser.phone, foodID, rate.toString(), comment)
        ratingTbl.push().setValue(rating).addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(this@FoodDetail, "Your Rating added ", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@FoodDetail, "Your Rating not added ", Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun addToCart() {
        Database(this@FoodDetail).addToCart(
            Order(
                foodID,
                currentFood.name,
                currentFood.price,
                number_button.number,
                currentFood.discount,
                currentFood.image
            )
        )
        Toast.makeText(this@FoodDetail,"Added to cart",Toast.LENGTH_SHORT).show()
    }

    private fun getDetailFood(foodID: String) {
        val foodRef = FirebaseDatabase.getInstance().getReference("Food")

        foodRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                 currentFood = dataSnapshot!!.child(foodID).getValue(Food::class.java)!!
                collapsing.title = currentFood.name
                food_name.text = currentFood.name
                food_description.text = currentFood.description
                food_price.text = currentFood.price
                Picasso.with(this@FoodDetail).load(currentFood.image).into(img_food)


            }

        })

    }

    override fun onResume() {
        super.onResume()
        btnCart.count = Database(this).getCounterCount()

    }


}
