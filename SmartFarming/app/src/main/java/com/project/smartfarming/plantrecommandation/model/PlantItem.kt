package com.project.smartfarming.plantrecommandation.model

import android.os.Parcel
import android.os.Parcelable

data class PlantItem(
    val image: String,
    val title: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(image)
        parcel.writeString(title)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<PlantItem> {
        override fun createFromParcel(parcel: Parcel): PlantItem {
            return PlantItem(parcel)
        }

        override fun newArray(size: Int): Array<PlantItem?> {
            return arrayOfNulls(size)
        }
    }
}
