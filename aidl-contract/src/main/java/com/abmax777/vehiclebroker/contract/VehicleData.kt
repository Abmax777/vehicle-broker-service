package com.abmax777.vehiclebroker.contract

import android.os.Parcel
import android.os.Parcelable

data class VehicleData(
    val speed: Float,
    val rpm: Float,
    val fuelLevel: Float,
    val timestampNanos: Long
) : Parcelable {

    // Reconstruct from the parcel — order MUST match writeToParcel
    constructor(parcel: Parcel) : this(
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readLong()
    )

    // Serialize into the parcel — order MUST match the read constructor
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(speed)
        parcel.writeFloat(rpm)
        parcel.writeFloat(fuelLevel)
        parcel.writeLong(timestampNanos)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<VehicleData> {
        override fun createFromParcel(parcel: Parcel): VehicleData = VehicleData(parcel)
        override fun newArray(size: Int): Array<VehicleData?> = arrayOfNulls(size)
    }
}