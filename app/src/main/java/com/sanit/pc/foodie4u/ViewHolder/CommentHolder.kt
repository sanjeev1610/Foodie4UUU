package com.sanit.pc.foodie4u.ViewHolder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import com.sanit.pc.foodie4u.R

class CommentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var userPhone: TextView
    var userComment: TextView
    var userRating: RatingBar

    init {
        userComment = itemView.findViewById(R.id.comment_text) as TextView
        userPhone = itemView.findViewById(R.id.user_phone) as TextView
        userRating = itemView.findViewById(R.id.show_comment_ratebar) as RatingBar
    }
}