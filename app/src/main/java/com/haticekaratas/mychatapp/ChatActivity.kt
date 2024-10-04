package com.haticekaratas.mychatapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.haticekaratas.mychatapp.adapter.MessageAdapter
import com.haticekaratas.mychatapp.databinding.ActivityChatBinding
import com.haticekaratas.mychatapp.model.Message
import java.util.Calendar
import java.util.Date

class ChatActivity : AppCompatActivity() {

    var binding:ActivityChatBinding? = null
    var adapter:MessageAdapter? = null
    var messages: ArrayList<Message>? = null
    var senderRoom: String? = null
    var recieverRoom :String? = null
    var stogare:FirebaseStorage? = null
    var database:FirebaseDatabase? = null
    var dialog:ProgressDialog? =  null
    var senderUid:String? = null
    var receiverUid:String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)
        database= FirebaseDatabase.getInstance()
        stogare= FirebaseStorage.getInstance()
        dialog= ProgressDialog(this@ChatActivity)
        dialog!!.setCancelable(false)
        dialog!!.setMessage("fotoğraf yükleniyor..")
        messages = ArrayList()
        val name = intent.getStringExtra("name")
        val profile = intent.getStringExtra("image")
        binding!!.name.text =name
        Glide.with(this@ChatActivity)
            .load(profile)
            .placeholder(R.drawable.placeholder)
            .into(binding!!.profile01)
        binding!!.imageView02.setOnClickListener { finish() }
        receiverUid = intent.getStringExtra("uid")
        senderUid = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence ").child(receiverUid!!)
            .addValueEventListener(object :  ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                   if (snapshot.exists()){
                       val status = snapshot.getValue(String::class.java)
                       if (status == "offline"){
                           binding!!.status.visibility = View.GONE
                       }
                       else{
                           binding!!.status.setText(status)
                           binding!!.status.visibility = View.VISIBLE
                       }

                   }
                }

                override fun onCancelled(error: DatabaseError) { }

            })
        senderRoom = senderUid + recieverRoom
        recieverRoom = recieverRoom + senderUid
        adapter = MessageAdapter(this@ChatActivity,messages,senderRoom!!,recieverRoom!!)
        binding!!.reycleView.layoutManager= LinearLayoutManager(this@ChatActivity)
        binding!!.reycleView.adapter = adapter
        database!!.reference.child("chats")
            .child(senderRoom!!)
            .child("messages")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages!!.clear()
                   for (snapshot1 in snapshot.children){
                        val message:Message? = snapshot1.getValue(Message::class.java)
                        message!!.messageId = snapshot1.key

                        if(receiverUid == message.receiverId){
                            messages!!.add(message)
                        }
                    }
                    adapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        binding!!.sendBtn.setOnClickListener {
            val messageText:String = binding!!.messageBox.text.toString()
            val date = Date()
            val message = Message(messageText, senderUid!!, receiverUid!!, date.time)

            binding!!.messageBox.setText("")
            val randomKey = database!!.reference.push().key
            val lastMsgObj = HashMap<String,Any>()

            lastMsgObj["lastMsg"]= message.message!!
            lastMsgObj["lastMsgTime"]= date.time

            database!!.reference.child("chats").child(senderRoom!!)
                .updateChildren(lastMsgObj)

            database!!.reference.child("chats").child(recieverRoom!!)
                .updateChildren(lastMsgObj)

            database!!.reference.child("chats").child(senderRoom!!)
                .child("messages")
                .child(randomKey!!)
                .setValue(message).addOnSuccessListener {
                    database!!.reference.child("chats")
                        .child(recieverRoom!!)
                        .child("message")
                        .child(randomKey)
                        .setValue(message)
                        .addOnSuccessListener {  }

                }

        }

        binding!!.attachment.setOnClickListener {
            val intent  = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type ="image/*"
            startActivityForResult(intent,25)

        }

        val handler = Handler()
        binding!!.messageBox.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
              database!!.reference.child("Presence")
                  .child(senderUid!!)
                  .setValue("yazıyor..")
              handler.removeCallbacksAndMessages(null )
              handler.postDelayed(userStoppingTyping,1000)
            }
            var userStoppingTyping = Runnable {
                database!!.reference.child("Presence")
                    .child(senderUid!!)
                    .setValue("çevrimiçi")
            }

        })
        supportActionBar?.setDisplayShowTitleEnabled(false)




    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25){
            if (data != null){
                if (data.data != null){
                    val selectedImage = data.data
                    val calendar = Calendar.getInstance()
                    val refence = stogare!!.reference.child("chats")
                        .child(calendar.timeInMillis.toString()+"")
                    dialog!!.show()
                    refence.putFile(selectedImage!!).addOnCompleteListener {task->
                        dialog!!.dismiss()
                        if (task.isSuccessful){
                            refence.downloadUrl.addOnSuccessListener {uri->
                                val filePath = uri.toString()
                                val messageText:String = binding!!.messageBox.text.toString()
                                val date = Date()
                                val message = Message(messageText,senderUid!!, receiverUid!!, date.time)
                                message.message = "photo"
                                message.imageUrl = filePath
                                val randomKey = database!!.reference.push().key
                                val  lastMsgObj = HashMap<String,Any>()
                                lastMsgObj["lastMsg"]= message.message!!
                                lastMsgObj["lastObjTime"]= date.time
                                database!!.reference.child("chats")
                                    .updateChildren(lastMsgObj)
                                database!!.reference.child("chats")
                                    .child(recieverRoom!!)
                                    .updateChildren(lastMsgObj)
                                database!!.reference.child("chats")
                                    .child(senderRoom!!)
                                    .child("messages")
                                    .child(randomKey!!)
                                    .setValue(message).addOnSuccessListener {
                                        database!!.reference.child("chats")
                                            .child(recieverRoom!!)
                                            .child("messages")
                                            .child(randomKey)
                                            .setValue(message)
                                            .addOnSuccessListener {  }
                                    }



                            }
                        }

                    }

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val currnetId = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currnetId!!)
            .setValue("çevrimiçi")

    }

    override fun onPause() {
        super.onPause()
        val currnetId = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currnetId!!)
            .setValue("çevrimdışı")

    }
}