package com.haticekaratas.mychatapp.model

class Message {
    var messageId:String? = null
    var message:String? = null
    var senderId:String? = null
    var receiverId:String? = null
    var imageUrl:String? = null
    var timestamp:Long = 0

    constructor(){}

    constructor(
        message:String,
        senderId:String,
        receiverId:String,
        timestamp:Long
    ){
        this.message = message
        this.senderId = senderId
        this.receiverId = receiverId
        this.timestamp = timestamp
    }
}