package com.sslcf.mohre

import com.google.gson.annotations.SerializedName


data class LoginAccessRequest(
    @SerializedName("grant_type")
    val grantType: String = "password",
    val username: String,
    val password: String,
    val answer: String = "test", //TODO Test data
    val scope: String = "",
    val questionId: String = ""
) {
    companion object {
        fun getAuthenticateRequest(
            password: String,
            username: String,
        ) = LoginAccessRequest(
            username = username,
            password = password
        )

        fun getAuthTokenRequest(
            password: String,
            username: String,
            secretAnswer: String = "test", //TODO Test data
            questionId: String
        ) = LoginAccessRequest(
            username = username,
            password = password,
            answer = secretAnswer,
            scope = "molapi offline_access",
            questionId = questionId
        )
    }
}

fun LoginAccessRequest.toFieldMap(): Map<String, String> {
    return mapOf(
        "grant_type" to grantType,
        "username" to username,
        "password" to password,
        "answer" to answer,
        "scope" to scope,
        "questionId" to questionId
    )
}