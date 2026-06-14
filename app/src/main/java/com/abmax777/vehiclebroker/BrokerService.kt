package com.abmax777.vehiclebroker

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteCallbackList
import android.util.Log
import com.abmax777.vehiclebroker.contract.IVehicleBroker
import com.abmax777.vehiclebroker.contract.IVehicleCallback
import com.abmax777.vehiclebroker.contract.VehicleData

class BrokerService : Service() {

    private val tag = "BrokerService"

    // Manages registered consumer callbacks. Handles dead clients
    // automatically — if a consumer's process dies without unsubscribing,
    // RemoteCallbackList drops it on the next broadcast. This is why we
    // use it instead of a plain List<IVehicleCallback>.
    private val callbacks = RemoteCallbackList<IVehicleCallback>()

    // The actual implementation of the AIDL interface. This Stub IS the
    // Binder object handed to clients when they bind.
    private val binder = object : IVehicleBroker.Stub() {

        override fun subscribe(callback: IVehicleCallback?): Boolean {
            if (callback == null) return false
            callbacks.register(callback)
            Log.d(tag, "Consumer subscribed")
            return true
        }

        override fun unsubscribe(callback: IVehicleCallback?) {
            if (callback == null) return
            callbacks.unregister(callback)
            Log.d(tag, "Consumer unsubscribed")
        }

        override fun publish(data: VehicleData?) {
            if (data == null) return
            fanOut(data)
        }
    }

    // Push one telemetry sample to every registered consumer.
    private fun fanOut(data: VehicleData) {
        // beginBroadcast() locks the list and returns the count.
        // MUST be paired with finishBroadcast() or the list stays locked.
        val n = callbacks.beginBroadcast()
        Log.d(tag, "fanOut to $n subscribers")
        try {
            for (i in 0 until n) {
                try {
                    callbacks.getBroadcastItem(i).onVehicleData(data)
                } catch (e: Exception) {
                    // One consumer throwing must not stop the fan-out to others.
                    Log.w(tag, "Failed to deliver to consumer $i", e)
                }
            }
        } finally {
            callbacks.finishBroadcast()
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        callbacks.kill()  // releases all registered callbacks
        super.onDestroy()
    }
}