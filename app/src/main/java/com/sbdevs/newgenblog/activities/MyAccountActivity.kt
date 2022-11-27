package com.sbdevs.newgenblog.activities

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.newgenblog.R
import com.sbdevs.newgenblog.adapters.AchievementAdapter
import com.sbdevs.newgenblog.databinding.ActivityMyAccountBinding
import com.sbdevs.newgenblog.fragments.LoadingDialog
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class MyAccountActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMyAccountBinding

    private var  firestore = Firebase.firestore
    private val firebaseAuth = Firebase.auth

    private lateinit var userImage: CircleImageView
    private lateinit var achievementRecycler:RecyclerView
    private var siteList: ArrayList<String> = ArrayList()
    private var achievementAdapter: AchievementAdapter = AchievementAdapter(siteList)

    var profilePicture:String = ""
    var buyerName:String =""

    private var loadingDialog = LoadingDialog()
    private val gone = View.GONE
    private val visible = View.VISIBLE



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMyAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val actionBar = binding.toolbar
        setSupportActionBar(actionBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userImage = binding.lay1.userImage
        achievementRecycler = binding.achievementRecycler
        achievementRecycler.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL,false)



        loadingDialog.show(supportFragmentManager,"Show")

        if( Firebase.auth.currentUser != null){
            binding.myAcnLay.visibility = View.VISIBLE
            binding.notLoginContainer.visibility = View.GONE

            lifecycleScope.launchWhenCreated{
                getMyAccount()
                getToDaysSite()

                achievementAdapter = AchievementAdapter(siteList)
                achievementRecycler.adapter = achievementAdapter

            }

        }else{
            binding.myAcnLay.visibility = View.GONE
            binding.notLoginContainer.visibility = View.VISIBLE
            loadingDialog.dismiss()

        }


    }



    override fun onStart() {
        super.onStart()

        binding.notLoginLay.loginOrSignupBtn.setOnClickListener {
            val registerIntent = Intent(this, RegisterActivity::class.java)
            registerIntent.putExtra("from", 2)// 1 = from splash/ 2 = from other class
            startActivity(registerIntent)
        }

        binding.lay1.logout.setOnClickListener {
            logOutUser()
            binding.myAcnLay.visibility = View.GONE
            binding.notLoginContainer.visibility = View.VISIBLE


        }

        binding.termsConditionText.setOnClickListener {
            gotoPolicies(1)
        }
        binding.privacyPolicyText.setOnClickListener {
            gotoPolicies(2)
        }

        binding.contactUsText.setOnClickListener {
            val contactusIntent = Intent(this, ContactUsActivity::class.java)
            startActivity(contactusIntent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }




    private suspend fun getMyAccount(){
        val lay1 = binding.lay1
        val userRef = firestore.collection("USER").document(Firebase.auth.currentUser!!.uid).get()

        userRef.addOnSuccessListener {
            val title = it.getString("name").toString()
            val gmail = it.getString("email")
            val profile:String? = it.get("profile").toString().trim()

            if (title!=""){
                lay1.userName.text = title
            }else{
                lay1.userName.text = "No Name"
            }

            if (profile != null) {
                profilePicture = profile
            }
            buyerName = title



            lay1.userMail.text = gmail


            if (profile!=""){
                Glide.with(this).load(profile).placeholder(R.drawable.as_person_placeholder).into(userImage)
            }


            loadingDialog.dismiss()
        }.addOnFailureListener {
            Log.e("User","${it.message}")
            loadingDialog.dismiss()
        }.await()
    }

    private fun logOutUser(){
        val userId = FirebaseAuth.getInstance().currentUser
        if (userId != null){

            Firebase.auth.signOut()

        }
    }


    private fun gotoPolicies(code:Int){
        val url = when(code){
            1-> {"https://firebasestorage.googleapis.com/v0/b/afiby1.appspot.com/o/policies%2Fngb_T_C.htm?alt=media&token=8de9ee41-2166-4f3e-a02e-574d86d1f1b4"}
            2-> {"https://firebasestorage.googleapis.com/v0/b/afiby1.appspot.com/o/policies%2Fngb_Pirvacy.htm?alt=media&token=15737aa7-d324-4594-b1f8-cf771158e327"}
            else -> {"http://www.google.com"}
        }

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
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
                        siteList = it.get("siteList") as ArrayList<String>

                        achievementAdapter.list = siteList
                        achievementAdapter.notifyDataSetChanged()

                    }else{
                        //
                    }


                }.addOnFailureListener {
                    //
                }
        }


    }



}