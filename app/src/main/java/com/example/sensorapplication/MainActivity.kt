package com.example.sensorapplication

import GeomagneticRotationVectorSensorData
import LightSensorData
import ProximitySensorData
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import androidx.room.Room
import com.example.sensorapplication.dao.GeomagneticRotationVectorSensorDataDao
import com.example.sensorapplication.dao.LightSensorDataDao
import com.example.sensorapplication.dao.ProximitySensorDataDao
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var proximitySensor: Sensor
    private lateinit var lightSensor: Sensor
    private lateinit var geomagneticSensor: Sensor
    private lateinit var proximitySensorDataDao: ProximitySensorDataDao
    private lateinit var lightSensorDataDao: LightSensorDataDao
    private lateinit var geomagneticSensorDataDao: GeomagneticRotationVectorSensorDataDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Room database and DAOs
        val db = Room.databaseBuilder(
            applicationContext,
            Database::class.java, "sensor-data-db"
        ).build()
        proximitySensorDataDao = db.proximitySensorDataDao()
        lightSensorDataDao = db.lightSensorDataDao()
        geomagneticSensorDataDao = db.geomagneticRotationVectorSensorDataDao()

        // Initialize sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        geomagneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, geomagneticSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val value = event.values[0]
        val timestamp = System.currentTimeMillis()

        when (event.sensor.type) {
            Sensor.TYPE_PROXIMITY -> {
                // Check if proximity sensor triggered by placing phone near ear or covering phone with hand
                if (value < proximitySensor.maximumRange) {
                    // Store proximity sensor data in Room database
                    val data = ProximitySensorData(timestamp = timestamp, value = value)
                    proximitySensorDataDao.insert(data)
                }
            }
            Sensor.TYPE_LIGHT -> {
                // Store light sensor data in Room database
                val data = LightSensorData(timestamp = timestamp, value = value)
                lightSensorDataDao.insert(data)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val cos = sqrt(x * x + y * y + z * z.toDouble()).toFloat()

                // Store geomagnetic sensor data in Room database
                val data = GeomagneticRotationVectorSensorData(timestamp = timestamp, x = x, y = y, z = z, cos = cos)
                geomagneticSensorDataDao.insert(data)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do nothing
    }
}
