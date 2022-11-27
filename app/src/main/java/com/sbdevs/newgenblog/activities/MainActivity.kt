package com.sbdevs.newgenblog.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.newgenblog.R
import com.sbdevs.newgenblog.adapters.HomeAdapter
import com.sbdevs.newgenblog.classes.ConnectivityObserver
import com.sbdevs.newgenblog.classes.DataCountClass
import com.sbdevs.newgenblog.classes.INetworkState
import com.sbdevs.newgenblog.classes.UserDetailsStore
import com.sbdevs.newgenblog.databinding.ActivityMainBinding
import com.sbdevs.newgenblog.models.HomeModel
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private lateinit var homeRecycler: RecyclerView
    private var uiViewLIst: MutableList<HomeModel> = ArrayList()
    private var homeAdapter: HomeAdapter = HomeAdapter(this,uiViewLIst)
    private lateinit var mAdView : AdView

    private lateinit var networkInterFace:INetworkState
    private val visible = View.VISIBLE
    private val gone = View.GONE
    private val invisible = View.INVISIBLE
    private lateinit var profileText:TextView
    private lateinit var userImage:CircleImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar = binding.toolbar
        setSupportActionBar(actionBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)


        networkInterFace = ConnectivityObserver(applicationContext)
        networkInterFace.observe().onEach {

            when(it.toString()){
                "Lost","UnAvailable", "Losing"->{
                    binding.noInternetLay.visibility = visible
                    Glide.with(this).load(R.drawable.as_lost_connection_2)
                        .apply(RequestOptions().placeholder(R.drawable.as_lost_connection_2))
                        .into(binding.noInternetImage)
                    binding.toolbar.visibility = invisible
                    binding.homeRecycler.visibility = invisible
                }

                "Available" ->{
                    binding.noInternetLay.visibility = invisible
                    binding.toolbar.visibility = visible
                    binding.homeRecycler.visibility = visible
                }

            }

            Log.e("NetWork", it.toString())

        }.launchIn(lifecycleScope)




        MobileAds.initialize(this) {}
        mAdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        homeRecycler = binding.homeRecycler
        homeRecycler.isNestedScrollingEnabled = false
        homeRecycler.layoutManager = LinearLayoutManager(this)

        profileText = binding.profileText
        userImage = binding.userImage
        DataCountClass.getUsername()

        uiViewLIst = mutableListOf(HomeModel("all",1),HomeModel("GMAX",2),HomeModel("PCMAG",2),HomeModel("ELECTER",2))

        homeAdapter = HomeAdapter(this,uiViewLIst)
        homeRecycler.adapter = homeAdapter

        binding.progressBar2.visibility = View.GONE



        userImage.setOnClickListener {
            val registerIntent = Intent(this, MyAccountActivity::class.java)
            startActivity(registerIntent)
        }

        profileText.setOnClickListener {
            val registerIntent = Intent(this, MyAccountActivity::class.java)
            startActivity(registerIntent)
        }



    }

    override fun onResume() {
        super.onResume()
        lifecycle.coroutineScope.launchWhenCreated {
            delay(2000)
            setUsername()
        }

    }



    private fun setUsername() {

        val userName = DataCountClass.userName
        val userImg = DataCountClass.userImage
//        Log.e(userName,userImg)

        if (userImg == "" && userName !=""){

            profileText.visibility = visible
            userImage.visibility = gone
            profileText.text = userName


        }else if (userImg !=""){
            profileText.visibility = gone
            userImage.visibility = visible

            Glide.with(this).load(userImg)
                .apply(RequestOptions().placeholder(R.drawable.ic_outline_account_circle_24))
                .into(userImage)
        }
        else{
            profileText.visibility = gone
            userImage.visibility = visible
        }

    }


}

