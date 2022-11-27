package com.sbdevs.newgenblog.fragments.register

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sbdevs.newgenblog.activities.MainActivity
import com.sbdevs.newgenblog.databinding.FragmentLoginBinding
import com.sbdevs.newgenblog.fragments.LoadingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding?=null
    private val binding get() = _binding!!

    val firebaseAuth = Firebase.auth
    lateinit var emailInput:TextInputLayout
    lateinit var passwordInput:TextInputLayout
    lateinit var errorTxt:TextView

    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+"
    private var fromIntent = 0
    private val loadingDialog = LoadingDialog()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        errorTxt  = binding.loginLay.errorMessageText
        errorTxt.visibility =View.GONE
        emailInput = binding.loginLay.emailInput
        passwordInput = binding.loginLay.passwordInput
        binding.loginLay.signupText.setOnClickListener {
            val action  = LoginFragmentDirections.actionLoginFragmentToSignUpFragment()
            findNavController().navigate(action)
        }

        fromIntent = requireActivity().intent.getIntExtra("from",0)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginLay.loginBtn.setOnClickListener {
            loadingDialog.show(childFragmentManager,"Show")
            checkAllDetails()

        }

        binding.loginLay.forgotPassword.setOnClickListener {
            val action1 = LoginFragmentDirections.actionLoginFragmentToForgotPasswordFragment()
            findNavController().navigate(action1)
        }

    }

    private fun checkMail(): Boolean {
        val emailInput: String = emailInput.editText?.text.toString().trim()
        return if (emailInput.isEmpty()) {
            this.emailInput.isErrorEnabled = true
            this.emailInput.error = "Field can't be empty"
            false
        } else {
            if(emailInput.matches(emailPattern.toRegex())){
                this.emailInput.isErrorEnabled = false
                this.emailInput.error = null
                true
            }else{
                this.emailInput.isErrorEnabled = true
                this.emailInput.error = "Please enter a valid email address"
                false
            }

        }
    }
    private fun checkPassword(): Boolean {
        val passInput: String = passwordInput.editText?.text.toString().trim()
        return if (passInput.isEmpty()) {
            passwordInput.isErrorEnabled = true
            passwordInput.error = "Field can't be empty"
            false
        } else {
            if (passInput.length<8){
                passwordInput.isErrorEnabled = true
                passwordInput.error = "must be at least 8 character"
                false
            }else{
                passwordInput.isErrorEnabled = false
                passwordInput.error = null
                true
            }

        }
    }


    private fun checkAllDetails() {
        if (!checkMail() or !checkPassword()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            loadingDialog.dismiss()
            return
        } else {
//            lifecycleScope.launch(Dispatchers.IO){
//                try {
                    val email = emailInput.editText?.text.toString().trim().lowercase()
                    firebaseAuth.signInWithEmailAndPassword(email,passwordInput.editText?.text.toString())
                        .addOnSuccessListener {

                            lifecycleScope.launch(Dispatchers.IO){
                                delay(2000)
                                loadingDialog.dismiss()
                                if(fromIntent==1){
                                    val intent = Intent(context, MainActivity::class.java)
                                    startActivity(intent)
                                    activity?.finish()
                                }else{
                                    activity?.finish()
                                }
                            }
                        }
                        .addOnFailureListener {
                            loadingDialog.dismiss()
                            Toast.makeText(context,it.message,Toast.LENGTH_LONG).show()
                        }


        }
    }





}