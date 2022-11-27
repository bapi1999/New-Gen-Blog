package com.sbdevs.newgenblog.activities

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.newgenblog.R
import com.sbdevs.newgenblog.classes.CounterViewModel
import com.sbdevs.newgenblog.classes.DataCountClass
import com.sbdevs.newgenblog.databinding.ActivityBlogPostBinding
import com.sbdevs.newgenblog.fragments.AchievementFragment
import com.sbdevs.newgenblog.models.BlogModel
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.delay
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class BlogPostActivity : AppCompatActivity() {

    private val firestore = Firebase.firestore
    private val firebaseAuth = Firebase.auth
    private lateinit var binding:ActivityBlogPostBinding

    var postContent:BlogModel = BlogModel()
    private lateinit var postName: TextView
    private lateinit var postThumb: ImageView
    private lateinit var postDesc : WebView
    private lateinit var countLayout:LinearLayout
    private lateinit var siteCompleteLay:LinearLayout
    private lateinit var animContLay:LinearLayout
    private var alreadyViewed:Boolean = false

    lateinit var counterLivedata: CounterViewModel

    private var dateString = ""
    private var siteList:ArrayList<String> = ArrayList()
    private var siteCode:String = ""

    private val visible = View.VISIBLE
    private val gone = View.GONE
    private val invisible = View.INVISIBLE

    private var interstitialAdUnitId = ""

    private var mInterstitialAd: InterstitialAd? = null

    companion object{
        const val TAG = "Interstitial"
    }

    private lateinit var profileText:TextView
    private lateinit var userImage: CircleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlogPostBinding.inflate(layoutInflater)
        setContentView(binding.root)



        val actionBar = binding.toolbar
        setSupportActionBar(actionBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        interstitialAdUnitId = resources.getString(R.string.g_inter_ad)
        MobileAds.initialize(this) {}
        loadAds()
        postName=binding.postTitle
        postThumb = binding.postImage
        postDesc = binding.postDescription
        countLayout = binding.countLayout
        siteCompleteLay = binding.siteComplete
        profileText = binding.profileText
        userImage = binding.userImage
        animContLay =binding.animContLay





        counterLivedata = ViewModelProvider(this)[CounterViewModel::class.java]
        counterLivedata.counterLiveData.observe(this) {
            binding.clickCounter.text = "${it}/25"
            if (it>=25){
                alreadyViewed = true
                countLayout.visibility = gone
                siteCompleteLay.visibility = visible

            }else{
                alreadyViewed = false
                countLayout.visibility = visible
                siteCompleteLay.visibility = gone
            }
        }






        postContent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("postContent", BlogModel::class.java)!!
        }else{
            intent.getParcelableExtra<BlogModel>("postContent")!!
        }

        val cameFrom = intent.getIntExtra("cameFrom",0)

         if (cameFrom==2 && firebaseAuth.currentUser !=null){
             animContLay.visibility =visible
            checkAndUpdateCount()

        }else{
             animContLay.visibility = invisible
            lifecycleScope.launchWhenCreated {
                delay(3000)
                binding.loadingBox.visibility = gone
            }
        }





        siteCode = postContent.siteCode
        var content = postContent.description.replace("img","img width=\"400\"")

        postName.text = postContent.postName.trim()

        Glide.with(this).load(postContent.postImage)
            .apply(RequestOptions().placeholder(R.drawable.as_rectangle_placeholder))
            .into(postThumb)

        postDesc.getSettings().setJavaScriptEnabled(true);
        postDesc.loadData(content, "text/html; charset=utf-8", "UTF-8");






        var webName = ""
        when (postContent.siteCode) {
            "GMAX" -> {
                webName = "Mobile Game"
            }
            "SPARTO" -> {
                webName = "Sports"
            }
            "ELECTER" -> {
                webName = "Electronics"
            }
            "PCMAG" -> {
                webName = "PC Games"
            }
            "HITLAB" -> {
                webName = "Weird fact"
            }
        }
        binding.pageTitle.text = webName



        val date = Date()
        val format = SimpleDateFormat("dd_MM_yyyy",Locale.ENGLISH)
        dateString = format.format(date).trim()





    }

    private fun checkAndUpdateCount(){
        lifecycleScope.launchWhenCreated {
            delay(2000)
            if(!alreadyViewed){
                val mSlideRight = Slide()
                mSlideRight.slideEdge = Gravity.END
                TransitionManager.beginDelayedTransition(binding.animContLay, mSlideRight)
                binding.countPlusLay.visibility = View.VISIBLE


                DataCountClass.updateCount()
                binding.clickCounter.text = "${DataCountClass.clickCount}/25"

                if(DataCountClass.clickCount ==25 ){
                    val fragment = AchievementFragment.newInstance(siteCode)
                    fragment.show(supportFragmentManager, "myFragment")
                    sentSiteViewStatus()
                    countLayout.visibility = gone
                    siteCompleteLay.visibility = visible

                    delay(4000)
                    showInterstitial()
                }

                delay(1000)
                mSlideRight.slideEdge = Gravity.END
                TransitionManager.beginDelayedTransition(binding.animContLay, mSlideRight)
                binding.countPlusLay.visibility = View.INVISIBLE
            }else{
                countLayout.visibility = gone
                siteCompleteLay.visibility = visible
            }
            binding.loadingBox.visibility = gone

        }
    }


    private fun loadAds(){
        var adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this,interstitialAdUnitId , adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                adError?.toString()?.let { Log.d("Blog Post Activity", it) }
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
        })
    }

    private fun showInterstitial(){
        if (mInterstitialAd != null) {

            mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                override fun onAdClicked() {
                    // Called when a click is recorded for an ad.
                    Log.d(TAG, "Ad was clicked.")
                }

                override fun onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    Log.d(TAG, "Ad dismissed fullscreen content.")
                    mInterstitialAd = null
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    // Called when ad fails to show.
                    Log.e(TAG, "Ad failed to show fullscreen content.")
                    mInterstitialAd = null
                }

                override fun onAdImpression() {
                    // Called when an impression is recorded for an ad.
                    Log.d(TAG, "Ad recorded an impression.")
                }

                override fun onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    Log.d(TAG, "Ad showed fullscreen content.")
                }
            }

            mInterstitialAd?.show(this)
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }
    }

    private fun sentSiteViewStatus(){

        try {
            if (firebaseAuth.currentUser!=null){
                val docRef:DocumentReference =  firestore.collection("USER").document(firebaseAuth.currentUser!!.uid)
                    .collection("TODAYS_ADS_SITE")
                    .document(dateString)
                docRef.get().addOnSuccessListener { document->
                        if (document.exists()){
                            // update sitelist
                            siteList = document.get("siteList") as ArrayList<String>
                            siteList.add(siteCode)
                            val updateMap: MutableMap<String, Any> = HashMap()
                            updateMap["siteList"] = siteList
                            updateMap["base"] = 72056

                            docRef.update(updateMap)
                                .addOnSuccessListener {
                                    binding.loadingBox.visibility = View.GONE
                                }
                                .addOnFailureListener {

                                }


                        }else{
                            // create sitelist
                            siteList.add(siteCode)
                            val setMap: MutableMap<String, Any> = HashMap()
                            setMap["siteList"] = siteList
                            setMap["ponitsGiven"] = false
                            setMap["points"] = 0
                            setMap["base"] = 79089

                            docRef.set(setMap)
                                .addOnSuccessListener {
                                    binding.loadingBox.visibility = View.GONE

                                }
                                .addOnFailureListener {

                                }

                        }
                    }
            }
        }catch (e:Exception){
            Log.e("createPaths","$e")
        }
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        val count = DataCountClass.clickCount
        binding.clickCounter.text = "${count}/25"

        getToDaysSite()
        setUsername()

        userImage.setOnClickListener {
            val registerIntent = Intent(this, MyAccountActivity::class.java)
            startActivity(registerIntent)
        }

        profileText.setOnClickListener {
            val registerIntent = Intent(this, MyAccountActivity::class.java)
            startActivity(registerIntent)
        }

    }

    private fun getToDaysSite(){
        val date = Date()
        val format = SimpleDateFormat("dd_MM_yyyy", Locale.ENGLISH)
        var dateString = format.format(date).trim()

        if (firebaseAuth.currentUser != null){
            firestore.collection("USER").document(firebaseAuth.currentUser!!.uid)
                .collection("TODAYS_ADS_SITE").document(dateString)
                .get().addOnSuccessListener {
                    if (it.exists()){
                        val siteList: ArrayList<String> = it.get("siteList") as ArrayList<String>
                        if (siteList.contains(siteCode)){
                            alreadyViewed = true
                            countLayout.visibility = gone
                            siteCompleteLay.visibility = visible
                        }
                    }else{
                        alreadyViewed = false
                        countLayout.visibility = visible
                        siteCompleteLay.visibility = gone
                    }


                }.addOnFailureListener {
                    alreadyViewed = false
                    countLayout.visibility = visible
                    siteCompleteLay.visibility = gone
                }
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