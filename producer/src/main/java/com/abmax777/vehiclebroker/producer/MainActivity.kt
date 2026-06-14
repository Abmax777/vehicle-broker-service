package com.abmax777.vehiclebroker.producer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.abmax777.vehiclebroker.contract.IVehicleBroker
import com.abmax777.vehiclebroker.contract.VehicleData
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val tag = "Producer"
    private var broker: IVehicleBroker? = null
    private var bound = false

    private val handler = Handler(Looper.getMainLooper())
    private var publishing = false

    // Called when the bind succeeds / the service disconnects.
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // Convert the raw Binder into our typed interface.
            broker = IVehicleBroker.Stub.asInterface(service)
            bound = true
            Log.d(tag, "Bound to broker")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            broker = null
            bound = false
            Log.d(tag, "Broker disconnected")
        }
    }

    // Generates one synthetic sample and publishes it, then reschedules.
    private val publishTask = object : Runnable {
        override fun run() {
            val data = VehicleData(
                speed = Random.nextFloat() * 120f,
                rpm = 800f + Random.nextFloat() * 6000f,
                fuelLevel = Random.nextFloat() * 100f,
                timestampNanos = SystemClock.elapsedRealtimeNanos()
            )
            try {
                broker?.publish(data)
                Log.d(tag, "Published speed=%.1f".format(data.speed))
            } catch (e: Exception) {
                Log.w(tag, "Publish failed", e)
            }
            if (publishing) handler.postDelayed(this, 500) // 2 Hz
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val status = findViewById<TextView>(R.id.status)
        val startBtn = findViewById<Button>(R.id.startBtn)
        val stopBtn = findViewById<Button>(R.id.stopBtn)

        startBtn.setOnClickListener {
            if (bound && !publishing) {
                publishing = true
                handler.post(publishTask)
                status.text = "Publishing…"
            }
        }
        stopBtn.setOnClickListener {
            publishing = false
            handler.removeCallbacks(publishTask)
            status.text = "Stopped"
        }
    }

    override fun onStart() {
        super.onStart()
        // Bind to the broker in the OTHER app by explicit component.
        val intent = Intent().setComponent(
            ComponentName(
                "com.abmax777.vehiclebroker",          // broker app's package
                "com.abmax777.vehiclebroker.BrokerService"
            )
        )
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        publishing = false
        handler.removeCallbacks(publishTask)
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }
}