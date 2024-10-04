package com.haticekaratas.mychatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.haticekaratas.mychatapp.databinding.ActivityVerificationBinding

class VerificationActivity : AppCompatActivity() {
    var binding : ActivityVerificationBinding? = null
    var auth:FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        auth =FirebaseAuth.getInstance()

        supportActionBar?.hide()
        binding!!.editNumber.requestFocus()
        binding!!.contiuneBttn.setOnClickListener {
           val intent = Intent(this@VerificationActivity,OTPActivity::class.java)
            intent.putExtra("phoneNumber",binding!!.editNumber.text.toString())
            startActivity(intent)
        }

    }
}