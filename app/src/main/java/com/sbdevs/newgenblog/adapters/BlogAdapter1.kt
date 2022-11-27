package com.sbdevs.newgenblog.adapters
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_ANY
import com.google.android.gms.ads.nativead.NativeAdView
import com.sbdevs.newgenblog.R
import com.sbdevs.newgenblog.activities.BlogPostActivity
import com.sbdevs.newgenblog.models.BlogModel
import com.sbdevs.newgenblog.models.HomeModel
import de.hdodenhof.circleimageview.CircleImageView
import java.time.format.DateTimeFormatter
import java.util.*

private const val BLOG_RECYCLER:Int = 2
private const val NATIVE_AD:Int = 3

class BlogAdapter1 ( var list:MutableList<BlogModel>): RecyclerView.Adapter<BlogAdapter1.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_blog_post, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])



    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder (itemView:View):RecyclerView.ViewHolder(itemView) {

        private val postImage : ImageView = itemView.findViewById(R.id.post_image)
        private val postName: TextView = itemView.findViewById(R.id.post_title)
        private val userImage: CircleImageView = itemView.findViewById(R.id.user_image)
        private val userName: TextView = itemView.findViewById(R.id.user_name)
        private val postDate: TextView = itemView.findViewById(R.id.post_date)

        fun bind(item: BlogModel){

            itemView.setOnClickListener {
                val productIntent = Intent(itemView.context, BlogPostActivity::class.java)
                productIntent.putExtra("postContent",item)
                productIntent.putExtra("cameFrom",1)
                // 1- Home / 2- All Post
                itemView.context.startActivity(productIntent)
            }

            postName.text = item.postName.toString().trim()
            Glide.with(itemView.context).load(item.postImage)
                .apply(RequestOptions().placeholder(R.drawable.as_rectangle_placeholder))
                .into(postImage)

            var  icon = ""
            var webName = ""
            val webID = item.siteCode
            if (item.siteCode == "GMAX") {
                icon =
                    "https://firebasestorage.googleapis.com/v0/b/afiby1.appspot.com/o/blogs%2Ficon%2Fgmax.png?alt=media&token=83d4654e-e927-4da4-8201-141df5130adc"
                webName = "Gmax"
            } else if (webID == "SPARTO") {
                icon =
                    "https://firebasestorage.googleapis.com/v0/b/afiby1.appspot.com/o/blogs%2Ficon%2Fsparto.png?alt=media&token=07b480e3-69fb-4484-b36e-4c0f7f3b5580"
                webName = "Sparto"
            } else if (webID == "CNMEDIA") {
                icon =
                    "https://firebasestorage.googleapis.com/v0/b/afiby1.appspot.com/o/blogs%2Ficon%2Fcnmedia.png?alt=media&token=3f86c0f0-e8d1-415e-8425-7050134ff40c"
                webName = "CN Media"
            } else if (webID == "NBANGLA") {
                icon =
                    "https://firebasestorage.googleapis.com/v0/b/afiby1.appspot.com/o/blogs%2Ficon%2Fnbangla.svg?alt=media&token=0d2bc16c-c7ec-444d-8b0d-1c714e5245b5"
                webName = "N Bangla"
            } else if (webID == "ELECTER") {
                icon =
                    "https://firebasestorage.googleapis.com/v0/b/afiby1.appspot.com/o/blogs%2Ficon%2Felecter.png?alt=media&token=2a4d352b-0883-4344-b02a-93eb3a6bbc0d"
                webName = "Electer"
            } else if (webID == "FOODI") {
                icon =
                    "https://firebasestorage.googleapis.com/v0/b/afiby1.appspot.com/o/blogs%2Ficon%2Ffoodi.png?alt=media&token=723c0b9e-4638-4375-ac91-e97657d7de28"
                webName = "Foodi"
            } else if (webID == "TEVER") {
                icon =
                    "https://firebasestorage.googleapis.com/v0/b/afiby1.appspot.com/o/blogs%2Ficon%2Ftever.png?alt=media&token=fb384d08-9a18-48ae-9ea1-be089723fe66"
                webName = "Tever"
            } else if (webID == "PCMAG") {
                icon =
                    "https://firebasestorage.googleapis.com/v0/b/afiby1.appspot.com/o/blogs%2Ficon%2Fpcmag.png?alt=media&token=888feb68-609b-4048-90f0-f1b592d1eff1"
                webName = "PC Mag"
            } else if (webID == "GINSIN") {
                icon =
                    "https://firebasestorage.googleapis.com/v0/b/afiby1.appspot.com/o/blogs%2Ficon%2Fginsin.png?alt=media&token=05ad2e72-86de-43b4-af73-342628c0ae0f"
                webName = "Ginsin"
            } else if (webID == "HITLAB") {
                icon =
                    "https://firebasestorage.googleapis.com/v0/b/afiby1.appspot.com/o/blogs%2Ficon%2Fhitlab.png?alt=media&token=11b6b8d4-26c8-4b74-b1d4-1a939922e5fe"
                webName = "Hitlab"
            }

            Glide.with(itemView.context).load(icon)
                .apply(RequestOptions().placeholder(R.drawable.as_square_placeholder))
                .into(userImage)

            userName.text = webName
            val date = convertLongToTime(item.postDate)
            postDate.text = date





        }

        private fun convertLongToTime(time: Long): String {
            val date = Date(time)
            val format = SimpleDateFormat("dd MMMM yyyy",Locale.ENGLISH)
            return format.format(date)
        }

    }



}