package com.sbdevs.newgenblog.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.*
import com.google.android.gms.ads.formats.NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_ANY
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.newgenblog.R
import com.sbdevs.newgenblog.models.BlogModel
import com.sbdevs.newgenblog.models.HomeModel
import com.sbdevs.newgenblog.models.SiteNameModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


private const val SITE_LIST: Int = 1
private const val BLOG_RECYCLER:Int = 2
private const val Tag = "HomeAdapter-"


class HomeAdapter (var context:Context ,var homeModelList: MutableList<HomeModel>  ) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {


    override fun getItemViewType(position: Int): Int {
        return when (homeModelList[position].view_type) {
            1 -> SITE_LIST
            2 -> BLOG_RECYCLER
            else -> -1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SITE_LIST -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_site_list, parent, false)
                SiteListViewHolder(view)
            }

            BLOG_RECYCLER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_blog_recycler, parent, false)
                BlogViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_blog_recycler, parent, false)
                BlogViewHolder(view)
            }



        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (getItemViewType(position)) {
            SITE_LIST -> (holder as SiteListViewHolder).bind(homeModelList[position])
            BLOG_RECYCLER -> (holder as BlogViewHolder).bind(homeModelList[position])
        }
    }

    override fun getItemCount(): Int {
        return homeModelList.size
    }

    class SiteListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val firestore = Firebase.firestore
        private val siteRecycler:RecyclerView = itemView.findViewById(R.id.site_recycler)
        private var allSite: ArrayList<SiteNameModel> = ArrayList()
        private var siteAdapter: SiteListAdapter = SiteListAdapter(allSite)
        fun bind(homeModel: HomeModel){
//            Log.e("SiteListViewHolder ","Fired")
            siteRecycler.layoutManager = GridLayoutManager(itemView.context,2)

            siteAdapter = SiteListAdapter(allSite)
            getAvailableSite()
            siteRecycler.adapter = siteAdapter
        }

        private fun getAvailableSite() = CoroutineScope(Dispatchers.IO).launch{
            firestore.collection("BLOGS")
                .whereEqualTo("mobileVisible",true)
                .get().addOnSuccessListener {
                    val allDocumentSnapshot = it.documents
                    for (documentSnapshot in allDocumentSnapshot) {
                        val code:String = documentSnapshot.id
                        val siteTopic:String = documentSnapshot.getString("topic")!!
                        allSite.add(
                            SiteNameModel(siteTopic,code,false,0)
                        )
                    }

                    siteAdapter.list = allSite
                    siteAdapter.notifyDataSetChanged()

                }.addOnFailureListener {
                    Log.e("getAvailableSite","${it.message}")
                }.await()
        }

    }



    class BlogViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val firestore = Firebase.firestore
        private val blogRecycler:RecyclerView = itemView.findViewById(R.id.blog_recycler)
        private var allPost: ArrayList<BlogModel> = ArrayList()
        private var blogAdapter: BlogAdapter1 = BlogAdapter1(allPost)
        fun bind(model: HomeModel){
//            Log.e("BlogViewHolder ","Fired")
            blogRecycler.layoutManager = LinearLayoutManager(itemView.context)
            getPosts(model.ui_web_code)

            blogAdapter = BlogAdapter1(allPost)
            blogRecycler.adapter = blogAdapter
        }

        private fun getPosts(site:String) = CoroutineScope(Dispatchers.IO).launch{
            try{
                firestore.collection("BLOGS")
                    .document(site).collection("POSTS")
                    .orderBy("postAt",Query.Direction.DESCENDING)
                    .whereEqualTo("hidden",false)
                    .limit(4L)
                    .get().addOnSuccessListener {
                        val allDocumentSnapshot = it.documents
                        for (documentSnapshot in allDocumentSnapshot) {
                            val code:String = documentSnapshot.id
                            val title:String = documentSnapshot.getString("title")!!
                            val image:String = documentSnapshot.getString("thumb")!!
                            val description:String = documentSnapshot.getString("zDescription")!!
                            val date:Long = documentSnapshot.getLong("postAt")!!
                            allPost.add(
                                BlogModel(code,title,image,date,description,site)
                            )
                        }

                        blogAdapter.list = allPost
                        blogAdapter.notifyDataSetChanged()

                    }.addOnFailureListener {
                        Log.e("HorizontalViewModel","${it.message}")
                    }.await()

            }
            catch (e:Error){
                Log.e("Error ","$e")
            }


        }

    }

}