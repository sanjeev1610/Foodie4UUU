package com.sanit.pc.foodie4u

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.sanit.pc.foodie4u.Common.Common
import com.sanit.pc.foodie4u.ViewHolder.CommentHolder
import kotlinx.android.synthetic.main.activity_show_comments.*

class ShowComments : AppCompatActivity() {
    internal lateinit var comments: DatabaseReference
    internal lateinit var adapter: FirebaseRecyclerAdapter<Rating, CommentHolder>

    internal lateinit var comment_recView: RecyclerView
    internal lateinit var layoutManager: RecyclerView.LayoutManager
    internal  lateinit var swipeRefreshLayout: SwipeRefreshLayout
    internal lateinit var foodid: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_comments)

        comment_recView = findViewById(R.id.show_cmnt_recview) as RecyclerView
        comment_recView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        comment_recView.layoutManager = layoutManager
        comments = FirebaseDatabase.getInstance().getReference("Rating")

        foodid = intent.getStringExtra(Common.COMMENT_FOODID)
        swipeRefreshLayout = findViewById(R.id.comment_refresh) as SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            android.R.color.holo_green_dark,
            android.R.color.holo_orange_dark,
            android.R.color.holo_blue_dark
        )

        swipeRefreshLayout.setOnRefreshListener {
            if (Common.isConnectedToInternet(baseContext)) {

                loadComments()
            } else {
                Toast.makeText(this@ShowComments, "Please Check your Internet Connection!!!", Toast.LENGTH_LONG).show()

            }
        }

        //default first item load
        swipeRefreshLayout.post {
            if (Common.isConnectedToInternet(baseContext)) {
                loadComments()
            } else {
                Toast.makeText(this@ShowComments, "Please Check your Internet Connection!!!", Toast.LENGTH_LONG).show()

            }
        }

        loadComments()
        if(adapter.itemCount<0){
            title_comment.text= "No Comments found"
        }
    }

    private fun loadComments() {
        adapter = object : FirebaseRecyclerAdapter<Rating, CommentHolder>(
            Rating::class.java,
            R.layout.layout_show_comments,
            CommentHolder::class.java,
            comments.orderByChild("foodId").equalTo(foodid)
        ) {
            override fun populateViewHolder(viewHolder: CommentHolder, model: Rating, position: Int) {

                viewHolder.userComment.setText(model.comment)
                viewHolder.userPhone.setText(
                    "XXXXXX" + model.userPhone.substring(
                        model.userPhone.length - 4,
                        model.userPhone.length
                    )
                )
                viewHolder.userRating.setRating(java.lang.Float.valueOf(model.rateValue))

            }
        }
        comment_recView.adapter = adapter
        swipeRefreshLayout.isRefreshing = false
    }
}
