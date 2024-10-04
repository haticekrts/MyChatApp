package com.haticekaratas.mychatapp

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.haticekaratas.mychatapp.adapter.UserAdapter
import com.haticekaratas.mychatapp.databinding.ActivityMainBinding
import com.haticekaratas.mychatapp.model.User

class MainActivity : AppCompatActivity() {

    var binding:ActivityMainBinding? = null
    var database:FirebaseDatabase? = null
    var users:ArrayList<User>? = null
    var usersAdapter: UserAdapter? = null
    var dialog: ProgressDialog? = null
    var user:User? = null
    var storage:FirebaseStorage? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        dialog = ProgressDialog(this@MainActivity)
        dialog!!.setMessage("fotoğraf yükleniyor...")
        dialog!!.setCancelable(false)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        users = ArrayList<User>()
        usersAdapter = UserAdapter(this@MainActivity,users!!)
        val layoutManager = GridLayoutManager(this@MainActivity,2)
        binding!!.mRec.layoutManager = layoutManager
        database!!.reference.child("users")
            .child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        binding!!.mRec.adapter = usersAdapter
        database!!.reference.child("users").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                users!!.clear()
                for (snapshot1 in snapshot.children){
                    val user:User? =snapshot1.getValue(User::class.java)
                    if (!user!!.uid.equals(FirebaseAuth.getInstance().uid)) users!!.add(user)
                }

                usersAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onResume() {
        super.onResume()
        val currentId =FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!).setValue("online")
    }

    override fun onPause() {
        super.onPause()
        val currentId =FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!).setValue("offline")

    }
}