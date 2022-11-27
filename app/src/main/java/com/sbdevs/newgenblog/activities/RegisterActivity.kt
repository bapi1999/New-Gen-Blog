package com.sbdevs.newgenblog.activities

import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.newgenblog.R
import com.sbdevs.newgenblog.databinding.ActivityRegisterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.math.log

class RegisterActivity : AppCompatActivity() {
    private val firestore =Firebase.firestore
    private val firebaseAuth = Firebase.auth

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private lateinit var binding:ActivityRegisterBinding
    private lateinit var loginBtn:LinearLayout
    private lateinit var progressBar:ProgressBar
    private lateinit var skipBtn:Button
    private val emtyArry = ArrayList<String>()
    private val invisible = View.INVISIBLE
    private val visible = View.VISIBLE
    private lateinit var termAndPolicyBox: CheckBox

    private lateinit var sharedPreferences: SharedPreferences
    private val myPreference = "ShowLoginPref"
    private val showLoginScreen = "ShowLoginScreen"

    var fromIntent = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(myPreference, MODE_PRIVATE)

//        termAndPolicyBox = binding.checkBox5
        progressBar = binding.progressBar3
        loginBtn = binding.startByG
        skipBtn = binding.skipBtn

        oneTapClient = Identity.getSignInClient(this)

        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.your_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(true)
                    .build()
            )
            .build()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment2) as NavHostFragment
        navHostFragment.findNavController().setGraph(R.navigation.register_nav_graph,intent.extras)



//        termAndPolicyBox.setOnCheckedChangeListener { buttonView, isChecked ->
//            if (isChecked){
//                termAndPolicyBox.buttonTintList = AppCompatResources.getColorStateList(this,R.color.blue_700)
//            }else{
//                termAndPolicyBox.buttonTintList = AppCompatResources.getColorStateList(this,R.color.red_700)
//            }
//
//        }


        loginBtn.setOnClickListener {
//            if(!checkTermsAndPolicyBox()){
//
//                return@setOnClickListener
//            }else{
//
//            }

            logInHandel()

        }

        fromIntent = intent.getIntExtra("from",0)

        if (fromIntent == 1){
            skipBtn.visibility = View.VISIBLE
        }else{
            skipBtn.visibility = View.GONE
        }



        skipBtn.setOnClickListener {
            val newIntent = Intent(this,MainActivity::class.java)
            startActivity(newIntent)

            val prefEditor:SharedPreferences.Editor = sharedPreferences.edit()
            prefEditor.putBoolean(showLoginScreen,false)
            prefEditor.apply()
            finish()
        }




    }

    override fun onStart() {
        super.onStart()

        binding.termsConditionText.setOnClickListener {
            gotoPolicies(1)
        }
        binding.privacyPolicyText.setOnClickListener {
            gotoPolicies(2)
        }
    }

//    private fun checkTermsAndPolicyBox(): Boolean {
//        return if (termAndPolicyBox.isChecked) {
//            termAndPolicyBox.buttonTintList = AppCompatResources.getColorStateList(this,R.color.teal_200)
//            true
//        } else {
//            termAndPolicyBox.buttonTintList = AppCompatResources.getColorStateList(this,R.color.red_700)
//            false
//        }
//    }



    private val oneTapResultSignIn = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()){ result ->
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken
            when {
                idToken != null -> {
                    // Got an ID token from Google. Use it to authenticate
                    // with your backend.
//                    val msg = "idToken: $idToken"
//                    Log.d("one tap", msg)

                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    firebaseAuth.signInWithCredential(firebaseCredential)
                        .addOnSuccessListener(this) {

                            val user = firebaseAuth.currentUser
                            val email:String = user?.email!!
                            val name:String = user.displayName!!
                            val img:String = user.photoUrl.toString()
                            val userId = user.uid
                            checkExistedUser(userId,email,name,img)



                        }.addOnFailureListener {
//                            Log.e("Google SignIn", "signInWithCredential:failure ${it.message}")
                            progressBar.visibility = invisible
                        }
                }
                else -> {
                    // Shouldn't happen.
//                    Log.d("one tap", "No ID token!")
                    progressBar.visibility = invisible
                }
            }
        } catch (e: ApiException) {
            progressBar.visibility = invisible
            when (e.statusCode) {

                CommonStatusCodes.CANCELED -> {
                    Log.d("one tap", "One-tap dialog was closed.")
                    // Don't re-prompt the user.

                }
                CommonStatusCodes.NETWORK_ERROR -> {
                    Log.d("one tap", "One-tap encountered a network error.")
                    // Try again or just ignore.
                }
                else -> {
                    Log.d("one tap", "Couldn't get credential from result." +
                            " (${e.localizedMessage})")

                }
            }
        }
    }



    private fun logInHandel(){
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    val ib = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    oneTapResultSignIn.launch(ib)
                    progressBar.visibility = visible
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("btn click", "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(this) { e ->
                // No Google Accounts found. Just continue presenting the signed-out UI.
//                displaySignUp()
                progressBar.visibility = invisible
                binding.errorMassage.text = e.localizedMessage.toString()
//                Log.d("btn click", e.localizedMessage!!)
            }
    }

    private  fun checkExistedUser(userId: String,email:String,name:String,img:String) {
        firestore.collection("USER").document(userId)
            .get().addOnSuccessListener { document->
                if (document.exists()) {
                    progressBar.visibility = invisible
                    val newIntent = Intent(this@RegisterActivity,MainActivity::class.java)
                    startActivity(newIntent)
                    finish()

                } else {
                    createPaths(userId, email, name,img)
                }

            }
    }

    private  fun createPaths(userId:String,email:String,name:String,img: String){

        val userMap: MutableMap<String, Any> = HashMap()
        userMap["name"] = name
        userMap["email"] = email
        userMap["profile"] = img
        userMap["Address"] = emtyArry
        userMap["My_Cart"] = emtyArry
        userMap["ELDERS"] = emtyArry
        userMap["created_At"] = Date().time
        userMap["SUBORDINATES"] = 0
        userMap["referFrom"] = ""
        userMap["direct_downline"] = 0
        userMap["last_seen"] = Date().time
        userMap["base"] = 978053

        try {
            if (firebaseAuth.currentUser!=null){

                val docRef = firestore.collection("USER").document(userId)

                docRef.set(userMap)
                    .addOnSuccessListener {
                        progressBar.visibility = invisible
                        finish()
                    }
                    .addOnFailureListener {
                        Log.e("Create user","${it.message}")
                        Firebase.auth.signOut()
                    }
            }
        }catch (e:Exception){
            Log.e("createPaths","$e")
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

}