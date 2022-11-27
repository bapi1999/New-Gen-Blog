package com.sbdevs.newgenblog.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class SiteNameModel (
    val siteTopic:String = "" ,
    val siteCode:String = "" ,
    val viewed: Boolean = false,
    val progress: Int = 0
) : Parcelable