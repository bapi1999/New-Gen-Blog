package com.sbdevs.newgenblog.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sbdevs.newgenblog.R
import de.hdodenhof.circleimageview.CircleImageView

class AchievementAdapter(var list:ArrayList<String>):
    RecyclerView.Adapter<AchievementAdapter.ViweHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViweHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_achievement_title,parent,false)
        return ViweHolder(view)
    }

    override fun onBindViewHolder(holder: ViweHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }


    class ViweHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var img: CircleImageView = itemView.findViewById(R.id.title_img)

        fun bind(title:String){
            var titleImage = when (title) {
                "GMAX" -> {
                     R.drawable.badge_gmax
                }
                "SPARTO" -> {
                    R.drawable.badge_sparto
                }
                "ELECTER" -> {
                    R.drawable.badge_electer
                }
                "PCMAG" -> {
                    R.drawable.badge_pcmag
                }
                else -> {
                    R.drawable.as_circle_placeholder
                }
            }

            Glide.with(itemView.context).load(titleImage).placeholder(R.drawable.as_person_placeholder).into(img)



        }
    }

}