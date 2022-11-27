package com.sbdevs.newgenblog.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sbdevs.newgenblog.R
import com.sbdevs.newgenblog.activities.AllPostActivity
import com.sbdevs.newgenblog.classes.DataCountClass
import com.sbdevs.newgenblog.models.SiteNameModel

class SiteListAdapter ( var list:ArrayList<SiteNameModel>):
    RecyclerView.Adapter<SiteListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_site_name,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val siteId = list[position]
        holder.bind(siteId)
    }

    override fun getItemCount(): Int {
        return list.size
    }



    class ViewHolder (itemView: View):RecyclerView.ViewHolder(itemView) {
        var nameTxt: TextView = itemView.findViewById(R.id.site_name)
        fun bind(item:SiteNameModel){
            nameTxt.text = item.siteTopic

            itemView.setOnClickListener {
                DataCountClass.resetCount()
                val productIntent = Intent(itemView.context, AllPostActivity::class.java)
                productIntent.putExtra("siteCode",item)
                itemView.context.startActivity(productIntent)
            }
        }
    }
}