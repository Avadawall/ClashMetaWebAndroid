package com.github.kr328.clash.core.model

import android.os.Parcel
import android.os.Parcelable
import com.github.kr328.clash.core.util.Parcelizer
import kotlinx.serialization.Serializable

@Serializable
data class CommonProfile(

    var uuid: String,

    var name: String,

    var ptype: Int,

    var source: String,

    var interval: Long,

    var hasErr: Boolean,

    var err: String

) : Parcelable {
//    enum class PType {
//        File, Url, WebUI, External
//    }

    override fun writeToParcel(dest: Parcel, flags: Int) {

        Parcelizer.encodeToParcel(serializer(), dest, this)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CommonProfile> {
        override fun createFromParcel(parcel: Parcel): CommonProfile {
            return Parcelizer.decodeFromParcel(serializer(), parcel)
        }

        override fun newArray(size: Int): Array<CommonProfile?> {
            return arrayOfNulls(size)
        }
    }
}
