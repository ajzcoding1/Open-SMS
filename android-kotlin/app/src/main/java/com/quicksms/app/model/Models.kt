package com.quicksms.app.model

import com.google.gson.annotations.SerializedName

data class SmsRequest(
    @SerializedName("number")
    val number: String,
    
    @SerializedName("text")
    val text: String,
    
    @SerializedName("sub_text")
    val subText: String? = null
)

data class SmsResponse(
    @SerializedName("stat")
    val stat: Boolean,
    
    @SerializedName("message")
    val message: String? = null
)

data class ServerLog(
    val timestamp: String,
    val message: String,
    val isError: Boolean = false
)
