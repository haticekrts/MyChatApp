package com.haticekaratas.mychatapp.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.haticekaratas.mychatapp.R
import com.haticekaratas.mychatapp.databinding.DeleteLayoutBinding
import com.haticekaratas.mychatapp.databinding.SendMsgBinding
import com.haticekaratas.mychatapp.model.Message

class MessageAdapter(var context:Context,
                     messages: ArrayList<Message>?,
                     senderRoom: String,
                     recieverRoom: String,
): RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    lateinit var messages:ArrayList<Message>
    val ITEM_SENT =1
    val ITEM_RECEİVE = 2
    var senderRoom:String
    var receiverRoom:String

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
      return if(viewType== ITEM_SENT){
          val view = LayoutInflater.from(context).inflate(R.layout.send_msg, parent,false)
          SendmsgHolder(view)
      }
        else {
          val view = LayoutInflater.from(context).inflate(R.layout.recieve_msg,parent,false)
            ReceivemsgHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message =messages[position]
        return if (FirebaseAuth.getInstance().uid == message.senderId){
            ITEM_SENT
        }
        else{
            ITEM_RECEİVE
        }

    }
    override fun getItemCount(): Int = messages.size


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder.javaClass == SendmsgHolder::class.java){
            val viewHolder = holder as SendmsgHolder
            if (message.message.equals("photo")){
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.image)
            }
            viewHolder.binding.message.setText(message.message)
            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout,null)
                val binding:DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Mesaajı Sil")
                    .setView(binding.root)
                    .create()
                binding.everyone.setOnClickListener {
                    message.message = "this message is removed "
                    message.messageId.let {it1->
                    FirebaseDatabase.getInstance().reference.child("paths")
                        .child(senderRoom)
                        .child("message")
                        .child(it1!!).setValue(message)

                    }
                    message.messageId.let {it1->
                        FirebaseDatabase.getInstance().reference.child("paths")
                            .child(receiverRoom)
                            .child("message")
                            .child(it1!!).setValue(message)
                    }
                    dialog.dismiss()

                }
                binding.delete.setOnClickListener {
                    message.message.let {it1->
                        FirebaseDatabase.getInstance().reference.child("paths")
                            .child(senderRoom)
                            .child("message")
                            .child(it1!!).setValue(null)
                    }
                    dialog.dismiss()

                }
                binding.cancel.setOnClickListener { dialog.dismiss() }
                dialog.show()

             false
            }

        }
        else {
            val viewHolder = holder as ReceivemsgHolder
            if (message.message.equals("photo")) {
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.image)

            }
            viewHolder.binding.message.setText(message.message)
            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Mesaajı Sil")
                    .setView(binding.root)
                    .create()
                binding.everyone.setOnClickListener {
                    message.message = "this message is removed "
                    message.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("paths")
                            .child(senderRoom)
                            .child("message")
                            .child(it1!!).setValue(message)

                    }
                    message.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("paths")
                            .child(receiverRoom)
                            .child("message")
                            .child(it1!!).setValue(message)
                    }
                    dialog.dismiss()

                }
                binding.delete.setOnClickListener {
                    message.message.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("paths")
                            .child(senderRoom)
                            .child("message")
                            .child(it1!!).setValue(null)
                    }
                    dialog.dismiss()

                }
                binding.cancel.setOnClickListener { dialog.dismiss() }
                dialog.show()
                false
            }
        }
    }


    inner class SendmsgHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var binding:SendMsgBinding =SendMsgBinding.bind(itemView)
    }

    inner class ReceivemsgHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var binding:SendMsgBinding =SendMsgBinding.bind(itemView)
    }
    init {
        if (messages != null){
            this.messages= messages
        }
        this.senderRoom = senderRoom
        this.receiverRoom = recieverRoom
    }



}