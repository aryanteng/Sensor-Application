package com.example.sensorapplication

import ProximitySensorData
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import androidx.room.Room
import com.example.sensorapplication.dao.ProximitySensorDataDao

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var proximitySensor: Sensor
    private lateinit var proximitySensorDataDao: ProximitySensorDataDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Room database and DAOs
        val db = Room.databaseBuilder(
            applicationContext,
            Database::class.java, "sensor-data-db"
        ).build()
        proximitySensorDataDao = db.proximitySensorDataDao()

        // Initialize proximity sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            val value = event.values[0]
            val timestamp = System.currentTimeMillis()

            // Check if proximity sensor triggered by placing phone near ear or covering phone with hand
            if (value < proximitySensor.maximumRange) {
                // Store proximity sensor data in Room database
                val data = ProximitySensorData(timestamp = timestamp, value = value)
                proximitySensorDataDao.insert(data)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do nothing
    }
}
