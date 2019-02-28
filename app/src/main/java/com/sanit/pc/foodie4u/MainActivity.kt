package com.sanit.pc.foodie4u

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.facebook.FacebookSdk
import com.facebook.accountkit.*
import com.facebook.accountkit.ui.AccountKitActivity
import com.facebook.accountkit.ui.AccountKitConfiguration
import com.facebook.accountkit.ui.LoginType
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.sanit.pc.foodie4u.Common.Common
import com.sanit.pc.foodie4u.beans.User
import dmax.dialog.SpotsDialog
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainActivity : AppCompatActivity() {

    var database: FirebaseDatabase? =null
    var users: DatabaseReference?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        FacebookSdk.sdkInitialize(applicationContext)
//        AccountKit.initialize(this@MainActivity)
        setContentView(R.layout.activity_main)


        database = FirebaseDatabase.getInstance()
        users = database!!.getReference("User")


        btn_continue.setOnClickListener {
//            startLoginSystem()
            startActivity(Intent(this@MainActivity,Home::class.java))
        }


        //check session facebook account kit
//        if (AccountKit.getCurrentAccessToken() != null) {
//            //show dialog
//            val waitDialog = SpotsDialog.Builder().setContext(this).build()
//            waitDialog.show()
//            waitDialog.setMessage("Please Wait")
//            waitDialog.setCancelable(false)
//            AccountKit.getCurrentAccount(object : AccountKitCallback<Account> {
//                override fun onSuccess(account: Account) {
//                    users!!.child(account.phoneNumber.toString())
//                        .addListenerForSingleValueEvent(object : ValueEventListener {
//                            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                                val localUser = dataSnapshot.getValue(User::class.java)
//                                val homeIntent = Intent(this@MainActivity, Home::class.java)
//                                Common.currentUser = localUser!!
//                                startActivity(homeIntent)
//                                waitDialog.dismiss()
//                                finish()
//
//                            }
//
//                            override fun onCancelled(databaseError: DatabaseError) {
//                                Toast.makeText(this@MainActivity, "" + databaseError.message, Toast.LENGTH_SHORT).show()
//
//                            }
//                        })
//                }
//
//                override fun onError(accountKitError: AccountKitError) {
//
//                }
//            })
//
//        }

        //PaperDB
//        Paper.init(this)

//        if(Paper.book()!=null){
//            val user = Paper.book().read<String>(Common.USER)
//            val password = Paper.book().read<String>(Common.PASSWORD)
//
//            if(user!=null && password!=null){
//                if(!user.equals("") && !password.equals("")){
//                    login(user,password)
//                }
//            }
//        }

      //  printKeyHash()
    }//on create


    private val REQUEST_CODE: Int = 7777

    private fun startLoginSystem() {


        val intent = Intent(this@MainActivity, AccountKitActivity::class.java)
        val configurationBuilder = AccountKitConfiguration.AccountKitConfigurationBuilder(
            LoginType.PHONE,
            AccountKitActivity.ResponseType.TOKEN
        )
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configurationBuilder.build())
        startActivityForResult(intent, REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            val result = data!!.getParcelableExtra<AccountKitLoginResult>(AccountKitLoginResult.RESULT_KEY)
            if (result.error != null) {
                Toast.makeText(this, "" + result.error!!.errorType.message, Toast.LENGTH_SHORT).show()
                return
            } else if (result.wasCancelled()) {
                Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show()
                return
            } else if (result.accessToken != null) {
                //show dialog
                val waitDialog = SpotsDialog.Builder().setContext(this).build()
                waitDialog.show()
                waitDialog.setMessage("Please Wait")
                waitDialog.setCancelable(false)

                //get current Phone
                AccountKit.getCurrentAccount(object : AccountKitCallback<Account> {
                    override fun onSuccess(account: Account) {
                        val userPhone = account.phoneNumber.toString()
                        //Check if exists on firebase Users
                        users!!.orderByKey().equalTo(userPhone)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    if (!dataSnapshot.child(userPhone).exists()) { //not exists
                                        //create new user and login
                                        val newUser = User()
                                        newUser.phone = (userPhone)
                                        newUser.username = ""

                                        users!!.child(userPhone).setValue(newUser)
                                            .addOnCompleteListener(OnCompleteListener<Void> { task ->
                                                if (task.isSuccessful) {
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        "Registration Successful",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                users!!.child(userPhone)
                                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                            val localUser = dataSnapshot.getValue(User::class.java)
                                                            val homeIntent =
                                                                Intent(this@MainActivity, Home::class.java)
                                                            Common.currentUser = localUser!!
                                                            startActivity(homeIntent)
                                                            waitDialog.dismiss()
                                                            finish()

                                                        }

                                                        override fun onCancelled(databaseError: DatabaseError) {
                                                            Toast.makeText(
                                                                this@MainActivity,
                                                                "" + databaseError.message,
                                                                Toast.LENGTH_SHORT
                                                            ).show()

                                                        }
                                                    })
                                            })


                                    } else {
                                        users!!.child(userPhone)
                                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                    val localUser = dataSnapshot.getValue(User::class.java)
                                                    val homeIntent =
                                                        Intent(this@MainActivity, Home::class.java)
                                                    Common.currentUser = localUser!!
                                                    startActivity(homeIntent)
                                                    waitDialog.dismiss()
                                                    finish()

                                                }

                                                override fun onCancelled(databaseError: DatabaseError) {
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        "" + databaseError.message,
                                                        Toast.LENGTH_SHORT
                                                    ).show()

                                                }
                                            })

                                    }

                                }

                                override fun onCancelled(databaseError: DatabaseError) {

                                }
                            })

                    }

                    override fun onError(accountKitError: AccountKitError) {

                    }
                })
            }
        }
    }


    private fun printKeyHash() {

        try {
            val info = packageManager.getPackageInfo("com.sanit.pc.foodie4u", PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

    }

    private fun login(userphone: String?, password: String?) {
        val pDialog = ProgressDialog(this@MainActivity)
        pDialog.setMessage("Please wait")
        pDialog.show()
        val db_user = FirebaseDatabase.getInstance().getReference("User")
        db_user.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(dataSnap: DatabaseError) {
                pDialog.dismiss()
                Toast.makeText(this@MainActivity,"Cancelled", Toast.LENGTH_SHORT).show()

            }

            override fun onDataChange(dataSnap: DataSnapshot) {
                pDialog.dismiss()
                if(dataSnap.child(userphone).exists()){


                    val user: User = dataSnap.child(userphone).getValue(User::class.java)!!
                    if(user.password == password){

                        val homeIntent = Intent(this@MainActivity,Home::class.java)
                        user.phone = userphone!!
                        Common.currentUser = user

                        startActivity(homeIntent)
                        finish()

                    }else{
                        Toast.makeText(this@MainActivity,"Pass word Error", Toast.LENGTH_SHORT).show()

                    }
                }else{
                    Toast.makeText(this@MainActivity,"Un Authorized User", Toast.LENGTH_SHORT).show()

                }
            }


        })
    }

}
