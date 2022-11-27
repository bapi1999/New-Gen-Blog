package com.sbdevs.newgenblog.models
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class BlogModel  (
    val docId:String = "",
    val postName:String = "",
    val postImage:String = "",
    val postDate:Long = 0,
    val description:String = "",
    val siteCode:String = ""
        ) : Parcelable