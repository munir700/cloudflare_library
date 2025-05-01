package com.sslcf.mohre

import android.os.Parcel
import android.os.Parcelable



data class SecretQuestion(
    val questionId: String? = null,
    val question: String? = null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(questionId)
        parcel.writeString(question)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SecretQuestion> {
        override fun createFromParcel(parcel: Parcel): SecretQuestion {
            return SecretQuestion(parcel)
        }

        override fun newArray(size: Int): Array<SecretQuestion?> {
            return arrayOfNulls(size)
        }
    }
}