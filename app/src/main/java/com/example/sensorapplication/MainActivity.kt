package com.example.sensorapplication

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

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var proximitySensor: Sensor
    private lateinit var lightSensor: Sensor
    private lateinit var geomagneticRotationVectorSensor: Sensor
    private lateinit var proximitySensorDataDao: ProximitySensorDataDao
    private lateinit var lightSensorDataDao: LightSensorDataDao
    private lateinit var geomagneticRotationVectorSensorDataDao: GeomagneticRotationVectorSensorDataDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Room database and DAOs
        val db = Room.databaseBuilder(
            applicationContext,
            Database
            ::class.java, "sensor-data-db"
        ).build()
        proximitySensorDataDao = db.proximitySensorDataDao()
        lightSensorDataDao = db.lightSensorDataDao()
        geomagneticRotationVectorSensorDataDao = db.geomagneticRotationVectorSensorDataDao()

        // Initialize sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        geomagneticRotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, geomagneticRotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val sensor = event.sensor
        val timestamp = System.currentTimeMillis()

        // Check which sensor triggered and insert data into the appropriate Room database
        when (sensor.type) {
            Sensor.TYPE_PROXIMITY -> {
                val value = event.values[0]
                // Check if proximity sensor triggered by placing phone near ear or covering phone with hand
                if (value < proximitySensor.maximumRange) {
                    // Store proximity sensor data in Room database
                    Thread {
                        val data = ProximitySensorData(timestamp = timestamp, value = value)
                        proximitySensorDataDao.insert(data)
                    }.start()
                }
            }
            Sensor.TYPE_LIGHT -> {
                val value = event.values[0]
                // Store light sensor data in Room database
                Thread {
                    val data = LightSensorData(timestamp = timestamp, value = value)
                    lightSensorDataDao.insert(data)
                }.start()
            }
            Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val cos = event.values[3]
                // Store geomagnetic sensor data in Room database
                Thread {
                    val data = GeomagneticRotationVectorSensorData(timestamp = timestamp, x = x, y = y, z = z, cos = cos)
                    geomagneticRotationVectorSensorDataDao.insert(data)
                }.start()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do nothing
    }
}

