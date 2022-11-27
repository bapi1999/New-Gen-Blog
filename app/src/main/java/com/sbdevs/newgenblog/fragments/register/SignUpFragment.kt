package com.sbdevs.newgenblog.fragments.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.newgenblog.R
import com.sbdevs.newgenblog.activities.MainActivity
import com.sbdevs.newgenblog.databinding.FragmentSignUpBinding
import com.sbdevs.newgenblog.fragments.LoadingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.HashMap


class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding?=null
    private val binding get() = _binding!!
    private val firebaseFirestore = Firebase.firestore
    val firebaseAuth = Firebase.auth

    lateinit var nameInput: TextInputLayout
    lateinit var email: TextInputLayout
    private val emtyArry = ArrayList<String>()

    lateinit var pass: TextInputLayout
    lateinit var errorTxt: TextView
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+"
    private val loadingDialog = LoadingDialog()
    private var fromIntent = 0
    private lateinit var termAndPolicyBox: CheckBox

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        nameInput = binding.signupLay.nameInput
        email = binding.signupLay.emailInput
        errorTxt = binding.signupLay.errorMessageText
        pass = binding.signupLay.passwordInput
        termAndPolicyBox = binding.signupLay.checkBox5

        fromIntent = requireActivity().intent.getIntExtra("from",0)

        binding.signupLay.loginText.setOnClickListener {
            val action = SignUpFragmentDirections.actionSignUpFragmentToLoginFragment()
            findNavController().navigate(action)
        }

        termAndPolicyBox.setOnCheckedChangeListener { buttonView, isChecked ->
            termAndPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),
                R.color.teal_200)
        }




        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signupLay.signupBtn.setOnClickListener {
            loadingDialog.show(childFragmentManager,"show")
            checkAllDetails()
        }

    }

    private fun checkName(): Boolean {
        val nameString: String = nameInput.editText?.text.toString().trim()
        return if (nameString.isEmpty()) {
            nameInput.isErrorEnabled = true
            nameInput.error = "Field can't be empty"
            false
        } else {
            nameInput.isErrorEnabled = false
            nameInput.error = null
            true

        }
    }

    private fun checkMail(): Boolean {
        val emailInput: String = email.editText?.text.toString().trim()
        return if (emailInput.isEmpty()) {
            email.isErrorEnabled = true
            email.error = "Field can't be empty"
            false
        } else {
            if(emailInput.matches(emailPattern.toRegex())){
                email.isErrorEnabled = false
                email.error = null
                true
            }else{
                email.isErrorEnabled = true
                email.error = "Please enter a valid email address"
                false
            }

        }
    }



    private fun checkPassword(): Boolean {
        val passInput: String = pass.editText?.text.toString().trim()
        return if (passInput.isEmpty()) {
            pass.isErrorEnabled = true
            pass.error = "Field can't be empty"
            false
        } else {
            if (passInput.length<8){
                pass.isErrorEnabled = true
                pass.error = "must be at least 8 character"
                false
            }else{
                pass.isErrorEnabled = false
                pass.error = null
                true
            }

        }
    }

    private fun checkTermsAndPolicyBox(): Boolean {
        return if (termAndPolicyBox.isChecked) {
            termAndPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.teal_200)
            true
        } else {
            termAndPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.red_700)
            false
        }
    }

    private fun checkAllDetails() {
        if (!checkName() or !checkMail() or !checkPassword() or !checkTermsAndPolicyBox()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            loadingDialog.dismiss()
            return
        } else {

            checkExistingUser()

        }
    }


    private fun checkExistingUser(){
        val emailId = email.editText?.text.toString().trim()
        val docRef = firebaseFirestore.collection("USER").whereEqualTo("email",emailId).limit(1)
        docRef.get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents
            if (allDocumentSnapshot.size >0){
                //user Aleady exist
                errorTxt.text = "This email is already In use"
                errorTxt.visibility = View.VISIBLE
                loadingDialog.dismiss()
            }else{
                // New User
                firebaseAuth
                    .createUserWithEmailAndPassword(email.editText?.text.toString().trim(),pass.editText?.text.toString())
                    .addOnSuccessListener {
                        lifecycleScope.launch(Dispatchers.IO){
                            createPaths(it.user!!.uid)

                        }
                    }
                    .addOnFailureListener {
                        loadingDialog.dismiss()
                        errorTxt.text = "${it.message}"
                        errorTxt.visibility = View.VISIBLE
                        Log.e("login user","${it.message}")

                    }

            }
        }.addOnFailureListener {

        }
    }



    private fun createPaths(userId:String){
        val userMap: MutableMap<String, Any> = HashMap()
        userMap["name"] = nameInput.editText?.text.toString().trim()
        userMap["email"] = email.editText?.text.toString().trim()
        userMap["profile"] = ""
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

                val docRef = firebaseFirestore.collection("USER").document(userId)

                docRef.set(userMap)
                    .addOnSuccessListener {
                        loadingDialog.dismiss()
                        if(fromIntent==1){
                            val intent = Intent(context, MainActivity::class.java)
                            startActivity(intent)
                            activity?.finish()
                        }else{
                            activity?.finish()
                        }
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


}