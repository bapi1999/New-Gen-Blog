package com.sbdevs.newgenblog.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sbdevs.newgenblog.R
import com.sbdevs.newgenblog.databinding.FragmentAchievementBinding


class AchievementFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentAchievementBinding?=null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(siteCode: String): AchievementFragment {
            val f = AchievementFragment()
            // Supply isStarted input as an argument.
            val args = Bundle()
            args.putString("siteCode",siteCode)
            f.arguments = args
            return f
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentAchievementBinding.inflate(inflater, container, false)

        isCancelable = true

        val siteCode = arguments?.getString("siteCode")


        Glide.with(this@AchievementFragment).load(R.drawable.gif_check)
            .apply(RequestOptions().placeholder(R.drawable.gif_check))
            .into(binding.imageView7)


        var message = ""
        when (siteCode) {
            "GMAX" -> {
                message = "You became a Mobile Gamer."
            }
            "SPARTO" -> {
                message = ""
            }
            "ELECTER" -> {
                message = "You became a Technical Guruji."
            }
            "PCMAG" -> {
                message = "You became a Pro Gamer."
            }
            "HITLAB" -> {
                message = ""
            }
            else->{
                message = ""
            }
        }
        binding.textView11.text = message

        return binding.root
    }




}