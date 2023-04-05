package com.example.sensorapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import androidx.room.Room
import com.example.sensorapplication.dao.GeomagneticRotationVectorSensorDataDao
import com.example.sensorapplication.dao.LightSensorDataDao
import com.example.sensorapplication.dao.ProximitySensorDataDao
import com.example.sensorapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var sensorManager: SensorManager
    private lateinit var proximitySensor: Sensor
    private lateinit var lightSensor: Sensor
    private lateinit var geomagneticRotationVectorSensor: Sensor

    private lateinit var proximitySensorDataDao: ProximitySensorDataDao
    private lateinit var lightSensorDataDao: LightSensorDataDao
    private lateinit var geomagneticRotationVectorSensorDataDao: GeomagneticRotationVectorSensorDataDao

    private var isCollectingProximityData = false
    private var isCollectingLightData = false
    private var isCollectingGeomagneticData = false

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

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
        binding.togBtnProximity.setOnCheckedChangeListener { _, isChecked ->
            isCollectingProximityData = isChecked
            if (isChecked) {
                binding.togBtnProximity.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F44336"))
                sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
            } else {
                binding.togBtnProximity.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#0B574A"))
                sensorManager.unregisterListener(this, proximitySensor)
            }
        }

        binding.togBtnLight.setOnCheckedChangeListener { _, isChecked ->
            isCollectingLightData = isChecked
            if (isChecked) {
                binding.togBtnLight.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F44336"))
                sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
            } else {
                binding.togBtnLight.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#0B574A"))
                sensorManager.unregisterListener(this, lightSensor)
            }
        }

        binding.togBtnGeomagnetic.setOnCheckedChangeListener { _, isChecked ->
            isCollectingGeomagneticData = isChecked
            if (isChecked) {
                binding.togBtnGeomagnetic.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F44336"))
                sensorManager.registerListener(this, geomagneticRotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL)
            } else {
                binding.togBtnGeomagnetic.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#0B574A"))
                sensorManager.unregisterListener(this, geomagneticRotationVectorSensor)
                binding.tvFeedbackText.text = ""
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
        // Check which sensor triggered and insert data into the appropriate Room database
        when (sensor.type) {
            Sensor.TYPE_PROXIMITY -> {
                if(isCollectingProximityData) {
                    val timestamp = System.currentTimeMillis()
                    val value = event.values[0]
                    // Check if proximity sensor triggered by placing phone near ear or covering phone with hand
                    if (value < proximitySensor.maximumRange) {
                        // Store proximity sensor data in Room database
                        Thread {
                            val data = ProximitySensorData(timestamp = timestamp, value = value)
                            proximitySensorDataDao.insert(data)
                            Log.i("PROXIMITY DATA", proximitySensorDataDao.getAll().toString())
                        }.start()

                    }
                }
            }
            Sensor.TYPE_LIGHT -> {
                if(isCollectingLightData) {
                    val timestamp = System.currentTimeMillis()
                    val value = event.values[0]
                    // Store light sensor data in Room database
                    Thread {
                        val data = LightSensorData(timestamp = timestamp, value = value)
                        lightSensorDataDao.insert(data)
                        Log.i("LIGHT DATA", lightSensorDataDao.getAll().toString())
                    }.start()

                }
            }
            Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> {
                if(isCollectingGeomagneticData) {
                    val timestamp = System.currentTimeMillis()
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
                        Log.i("GEOMAGNETIC DATA", geomagneticRotationVectorSensorDataDao.getAll().toString())
                    }.start()

                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)

                    // Update UI with orientation feedback
                    updateOrientationFeedback()
                }
            }
        }
    }

    private fun updateOrientationFeedback() {
        // Get the azimuth angle (in radians)
        val azimuth = orientationAngles[0]
        // Convert radians to degrees
        val azimuthDegrees = Math.toDegrees(azimuth.toDouble()).toFloat()
        // Calculate the rotation needed to align with the earth's magnetic north pole
        val rotationDegrees = (azimuthDegrees + 360) % 360
        // Update UI with orientation feedback
        if (azimuthDegrees.toInt() == 0) {
            binding.tvFeedbackText.text = "Success!"
        } else {
            var direction = ""
            direction = if(rotationDegrees > 180){
                "clockwise"
            } else{
                "counterclockwise"
            }
            binding.tvFeedbackText.text = "Rotate ${rotationDegrees.toInt()} Degrees $direction to align with the Magnetic North Pole."
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }
}

