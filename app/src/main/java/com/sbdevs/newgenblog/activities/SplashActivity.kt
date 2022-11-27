package com.sbdevs.newgenblog.activities

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sbdevs.newgenblog.R
import com.sbdevs.newgenblog.databinding.ActivitySplashBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val myPreference = "ShowLoginPref"
    private val showLoginScreen = "ShowLoginScreen"
    private  var show = true

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)


        sharedPreferences = getSharedPreferences(myPreference, MODE_PRIVATE)
        show = sharedPreferences.getBoolean(showLoginScreen,true)
//        Firebase.auth.signOut()

    }

    override fun onStart() {
        super.onStart()


        if (Firebase.auth.currentUser == null) {
            if (show){
                val loginintent = Intent(this@SplashActivity, RegisterActivity::class.java)
                loginintent.putExtra("from",1)// 1 = from splash/ 2 = from other class
                startActivity(loginintent)
                finish()
            }else{
                val mainintent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(mainintent)
                finish()
            }

        } else {

            lifecycleScope.launch(Dispatchers.IO){
                try {
                    val mainintent = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(mainintent)
                    finish()

                }catch (e:Exception){

                    withContext(Dispatchers.Main){
                        Toast.makeText(this@SplashActivity,e.message, Toast.LENGTH_LONG).show()
                    }

                }


            }

        }

    }
}