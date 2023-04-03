package com.example.sensorapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import android.util.Log
import android.widget.ToggleButton
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
    private lateinit var proximityToggleButton: ToggleButton
    private lateinit var lightToggleButton: ToggleButton
    private lateinit var geomagneticRotationVectorToggleButton: ToggleButton
    private var isCollectingProximityData = false
    private var isCollectingLightData = false
    private var isCollectingGeomagneticData = false

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

        // Initialize toggle buttons
        proximityToggleButton = findViewById(R.id.proximityToggleButton)
        proximityToggleButton.setOnCheckedChangeListener { _, isChecked ->
            isCollectingProximityData = isChecked
            if (isChecked) {
                sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
            } else {
                sensorManager.unregisterListener(this, proximitySensor)
            }
        }

        lightToggleButton = findViewById(R.id.lightToggleButton)
        lightToggleButton.setOnCheckedChangeListener { _, isChecked ->
            isCollectingLightData = isChecked
            if (isChecked) {
                sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
            } else {
                sensorManager.unregisterListener(this, lightSensor)
            }
        }

        geomagneticRotationVectorToggleButton = findViewById(R.id.geomagneticRotationVectorToggleButton)
        geomagneticRotationVectorToggleButton.setOnCheckedChangeListener { _, isChecked ->
            isCollectingGeomagneticData = isChecked
            if (isChecked) {
                sensorManager.registerListener(this, geomagneticRotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL)
            } else {
                sensorManager.unregisterListener(this, geomagneticRotationVectorSensor)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isCollectingProximityData) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        if (isCollectingLightData) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        if (isCollectingGeomagneticData) {
            sensorManager.registerListener(this, geomagneticRotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
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
                if(isCollectingProximityData) {
                    val value = event.values[0]
                    // Check if proximity sensor triggered by placing phone near ear or covering phone with hand
                    if (value < proximitySensor.maximumRange) {
                        // Store proximity sensor data in Room database
                        Thread {
                            val data = ProximitySensorData(timestamp = timestamp, value = value)
                            proximitySensorDataDao.insert(data)
                            Log.i("DATA", proximitySensorDataDao.getAll().toString())
                        }.start()
                    }
                }
            }
            Sensor.TYPE_LIGHT -> {
                if(isCollectingLightData) {
                    val value = event.values[0]
                    // Store light sensor data in Room database
                    Thread {
                        val data = LightSensorData(timestamp = timestamp, value = value)
                        lightSensorDataDao.insert(data)
                    }.start()
                }
            }
            Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> {
                if(isCollectingGeomagneticData) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val cos = event.values[3]
                    // Store geomagnetic sensor data in Room database
                    Thread {
                        val data = GeomagneticRotationVectorSensorData(
                            timestamp = timestamp,
                            x = x,
                            y = y,
                            z = z,
                            cos = cos
                        )
                        geomagneticRotationVectorSensorDataDao.insert(data)
                    }.start()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }
}

