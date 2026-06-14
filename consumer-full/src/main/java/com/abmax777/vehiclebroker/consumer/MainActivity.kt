package com.abmax777.vehiclebroker.consumer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.abmax777.vehiclebroker.contract.IVehicleBroker
import com.abmax777.vehiclebroker.contract.IVehicleCallback
import com.abmax777.vehiclebroker.contract.VehicleData

class MainActivity : AppCompatActivity() {

    private val tag = "Consumer"
    private var broker: IVehicleBroker? = null
    private var bound = false

    private val ui = Handler(Looper.getMainLooper())
    private lateinit var speedView: TextView
    private lateinit var rpmView: TextView
    private lateinit var fuelView: TextView

    // The callback the broker pushes data INTO. This runs on a Binder
    // thread (NOT the main thread), so any UI update must be posted
    // back to the main thread — touching views directly here crashes.
    private val callback = object : IVehicleCallback.Stub() {
        override fun onVehicleData(data: VehicleData?) {
            if (data == null) return
            Log.d(tag, "Received speed=%.1f".format(data.speed))
            ui.post {
                speedView.text = "Speed: %.1f km/h".format(data.speed)
                rpmView.text = "RPM: %.0f".format(data.rpm)
                fuelView.text = "Fuel: %.1f%%".format(data.fuelLevel)
            }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            broker = IVehicleBroker.Stub.asInterface(service)
            bound = true
            try {
                val ok = broker?.subscribe(callback) ?: false
                Log.d(tag, "Subscribe result: $ok")
            } catch (e: Exception) {
                Log.w(tag, "Subscribe failed", e)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            broker = null
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        speedView = findViewById(R.id.speed)
        rpmView = findViewById(R.id.rpm)
        fuelView = findViewById(R.id.fuel)

        val brokerIntent = Intent().setComponent(
            ComponentName(
                "com.abmax777.vehiclebroker",
                "com.abmax777.vehiclebroker.BrokerService"
            )
        )
        bindService(brokerIntent, connection, Context.BIND_AUTO_CREATE)
    }

//    override fun onStart() {
//        super.onStart()
//        val intent = Intent().setComponent(
//            ComponentName(
//                "com.abmax777.vehiclebroker",
//                "com.abmax777.vehiclebroker.BrokerService"
//            )
//        )
//
//    }

    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            try { broker?.unsubscribe(callback) } catch (_: Exception) {}
            unbindService(connection)
            bound = false
        }
    }
}