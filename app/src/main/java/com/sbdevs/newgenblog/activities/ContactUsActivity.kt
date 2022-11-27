package com.sbdevs.newgenblog.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sbdevs.newgenblog.R
import com.sbdevs.newgenblog.databinding.ActivityContactUsBinding

class ContactUsActivity : AppCompatActivity() {
    private lateinit var binding:ActivityContactUsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.submitBtn.setOnClickListener {
            binding.summaryEditText.setText("")
            binding.messageEditText.setText("")
        }

    }
}