package com.sbdevs.newgenblog.classes

import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.newgenblog.R
import java.util.*

class DataCountClass {

    companion object{
        public var clickCount:Int = 0

        fun updateCount(){
            clickCount +=1
        }

        fun resetCount(){
            clickCount = 0
        }

        var getSite  = false

        var userImage:String = ""
        var userName:String = ""

        fun getUsername() {
            val firestore = Firebase.firestore
            val currentUser = Firebase.auth.currentUser
            if (currentUser != null){
                firestore.collection("USER").document(currentUser.uid)
                    .get().addOnSuccessListener {

                        userName = it.getString("name").toString()
                        userImage = it.getString("profile").toString()
                    }


            }else{
                userImage = ""
                userName = ""
            }

        }


    }





}