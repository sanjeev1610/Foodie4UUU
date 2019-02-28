package com.sanit.pc.foodie4u.Database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper
import com.sanit.pc.foodie4u.beans.Order
import java.util.ArrayList

class Database(context: Context?, DB_NAME: String="Foodie4UDB.db", DB_VER: Int=2) :
    SQLiteAssetHelper(context, DB_NAME, null, DB_VER) {

    fun getCart(): MutableList<Order> {
        val db = readableDatabase
        val qb = SQLiteQueryBuilder()

        val sqlSelect = arrayOf("id","productId", "productName", "price","quantity", "discount", "image")
        val tableName = "order_detail"

        qb.tables = tableName
        val c = qb.query(db, sqlSelect, null, null, null, null, null)
        val result = ArrayList<Order>()
        if (c.moveToFirst()) {
            do {
                result.add(
                    Order(
                        c.getInt(c.getColumnIndex("id")),
                        c.getString(c.getColumnIndex("productId")),
                        c.getString(c.getColumnIndex("productName")),
                        c.getString(c.getColumnIndex("price")),
                        c.getString(c.getColumnIndex("quantity")),
                        c.getString(c.getColumnIndex("discount")),
                        c.getString(c.getColumnIndex("image"))
                    )
                )
            } while (c.moveToNext())
        }
        return result
    }//getCart

    fun addToCart(order: Order) {
        val db = readableDatabase
        val query = String.format(
            "INSERT OR REPLACE INTO order_detail(productId,productName,price,quantity,discount,image) VALUES('%s','%s','%s','%s','%s','%s');",
            order.productId,
            order.productName,
            order.price,
            order.quantiy,
            order.discount,
            order.image
        )
        db.execSQL(query)
    }

    fun cleanCart() {
        val db = readableDatabase
        val query = String.format("DELETE FROM order_detail")
        db.execSQL(query)
    }

    fun addToFavFood(foodId:String){
        val db = readableDatabase
        val query = String.format("INSERT INTO favourites(foodId) VALUES('%s');",foodId)
        db.execSQL(query)
    }

    fun removeFromFav(foodId:String){
        val db = readableDatabase
        val query = String.format("DELETE FROM favourites WHERE foodId='%s';",foodId)
        db.execSQL(query)
    }

    fun isFavFood(foodID:String):Boolean{
        val db = readableDatabase
        val query = String.format("SELECT * FROM favourites WHERE foodId='%s';",foodID)
        var cursor = db.rawQuery(query,null)
        if(cursor.count<=0){
            cursor.close()
            return false
        }
        cursor.close()
        return true
    }

    fun getCounterCount(): Int {
        var count = 0
        val db = readableDatabase
        val query = String.format("SELECT COUNT(*) FROM order_detail")
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {

            do {
                count = cursor.getInt(0)
            } while (cursor.moveToNext())
        }


        return count
    }
    fun updaateCart(order:Order){
        val db = readableDatabase
        val query = String.format("UPDATE order_detail SET quantity='%s' WHERE id='%s'",order.quantiy, order.id)
        db.execSQL(query)
    }


}