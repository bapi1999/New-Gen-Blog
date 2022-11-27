package com.sbdevs.newgenblog.activities

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.newgenblog.R
import com.sbdevs.newgenblog.adapters.BlogAdapter
import com.sbdevs.newgenblog.classes.CounterViewModel
import com.sbdevs.newgenblog.classes.DataCountClass
import com.sbdevs.newgenblog.classes.UserDetailsStore
import com.sbdevs.newgenblog.databinding.ActivityAllPostBinding
import com.sbdevs.newgenblog.fragments.AchievementFragment
import com.sbdevs.newgenblog.models.BlogModel
import com.sbdevs.newgenblog.models.SiteNameModel
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList

class AllPostActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAllPostBinding
    private var  firestore = Firebase.firestore
    private val firebaseAuth = Firebase.auth

    private lateinit var blogPostRecycler: RecyclerView
    private var postList: MutableList<BlogModel> = ArrayList()

    private var blogAdapter: BlogAdapter = BlogAdapter(6,postList)
    private var siteCode:String = "";

    private var lastResult: DocumentSnapshot? =null
    private var postTimes: Long = 0
    private var isReachLast:Boolean = false

    private val visible = View.VISIBLE
    private val gone = View.GONE
    private val invisible = View.INVISIBLE

    private var firstRun = false
    private lateinit var pageTitle:TextView
    private lateinit var postClickCount:TextView
    private lateinit var countLayout:LinearLayout
    private lateinit var siteCompleteLay:LinearLayout
    private lateinit var profileText:TextView
    private lateinit var userImage: CircleImageView

    private var alreadyViewed:Boolean = false

    var siteContent:SiteNameModel = SiteNameModel()
//    lateinit var userDataStore: UserDetailsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllPostBinding.inflate(layoutInflater)
        setContentView(binding.root)



        val actionBar = binding.toolbar
        setSupportActionBar(actionBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        profileText = binding.profileText
        userImage = binding.userImage
        countLayout = binding.countLayout
        siteCompleteLay = binding.siteComplete
        pageTitle = binding.pageTitle
        postClickCount = binding.clickCounter
        blogPostRecycler = binding.blogPostRecycler
        blogPostRecycler.isNestedScrollingEnabled = false
        blogPostRecycler.layoutManager = LinearLayoutManager(this)


        siteContent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("siteCode", SiteNameModel::class.java)!!
        }else{
            intent.getParcelableExtra<SiteNameModel>("siteCode")!!
        }
        pageTitle.text = siteContent.siteTopic
        siteCode = siteContent.siteCode

        if (firebaseAuth.currentUser ==null){
            binding.containerLay.visibility = invisible
        }



        getPosts(siteCode)
        blogAdapter = BlogAdapter(6,postList)
        blogPostRecycler.adapter = blogAdapter




    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onStart() {
        super.onStart()



        blogPostRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener(){

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN) && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (isReachLast){
                        Log.w("Query item","Last item is reached already")
                        binding.progressBar2.visibility = View.GONE
                    }else{
                        binding.progressBar2.visibility = View.VISIBLE

                        if (!firstRun){
                            getPosts(siteCode)
                        }

                    }
                }

            }
        })

        setUsername()
        getToDaysSite()


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
        val count = DataCountClass.clickCount
        if (count >= 25){
            alreadyViewed = true
            countLayout.visibility = gone
            siteCompleteLay.visibility = visible

        }else{
            binding.clickCounter.text = "${count}/25"
            alreadyViewed = false
            countLayout.visibility = visible
            siteCompleteLay.visibility = gone
        }




    }

    private fun getPosts(site:String) = CoroutineScope(Dispatchers.IO).launch{
        firstRun = true
        val newPosts: ArrayList<BlogModel> = ArrayList()

        val query:Query = if(lastResult == null){
            firestore.collection("BLOGS")
                .document(site).collection("POSTS")
                .whereEqualTo("hidden",false)
                .orderBy("postAt", Query.Direction.DESCENDING)
        }else{
            firestore.collection("BLOGS")
                .document(site).collection("POSTS")
                .whereEqualTo("hidden",false)
                .orderBy("postAt", Query.Direction.DESCENDING)
                .startAfter(postTimes)
        }

                query.limit(15L).get().addOnSuccessListener {
                    val allDocumentSnapshot = it.documents
                    Log.e("site list ", allDocumentSnapshot.size.toString())

                    if (allDocumentSnapshot.isNotEmpty()){

                    for (documentSnapshot in allDocumentSnapshot) {
                        val code:String = documentSnapshot.id
                        val title:String = documentSnapshot.getString("title")!!
                        val image:String = documentSnapshot.getString("thumb")!!
                        val description:String = documentSnapshot.getString("zDescription")!!
                        val date:Long = documentSnapshot.getLong("postAt")!!
                        newPosts.add(
                            BlogModel(code,title,image,date,description,site)
                        )
                    }
                        isReachLast = allDocumentSnapshot.size < 15 // limit is 10


                    }else{
                        isReachLast = true
                    }

                    postList.addAll(newPosts)

                    if (postList.isEmpty()){
                        binding.progressBar2.visibility = gone
                    }
                    else{
                        binding.progressBar2.visibility = visible


                        blogAdapter.list = postList

                        if (lastResult == null ){
                            blogAdapter.notifyItemRangeInserted(0,newPosts.size)
                        }else{
                            blogAdapter.notifyItemRangeInserted(postList.size-1,newPosts.size)
                        }


                        if (allDocumentSnapshot.isNotEmpty()){
                            val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                            lastResult = lastR
                            postTimes = lastR.getLong("postAt")!!
                        }

                        binding.progressBar2.visibility = View.GONE
                    }

                    firstRun = false
                }.addOnFailureListener {
                    Log.e("HorizontalViewModel","${it.message}")
                    firstRun = false
                }.await()




    }


    private fun getToDaysSite(){
        val date = Date()
        val format = SimpleDateFormat("dd_MM_yyyy", Locale.ENGLISH)
        val dateString = format.format(date).trim()

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
        Log.e(userName,userImg)

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