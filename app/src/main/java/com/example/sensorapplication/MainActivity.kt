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
import androidx.room.Room
import com.example.sensorapplication.dao.GeomagneticRotationVectorSensorDataDao
import com.example.sensorapplication.dao.LightSensorDataDao
import com.example.sensorapplication.dao.ProximitySensorDataDao
import com.example.sensorapplication.models.GeomagneticRotationVectorSensorData
import com.example.sensorapplication.models.LightSensorData
import com.example.sensorapplication.models.ProximitySensorData
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

        // Initialising Room Database
        val db = Room.databaseBuilder(
            applicationContext,
            SensorDatabase
            ::class.java, "sensor-db"
        ).build()

        // Initialising DAOs
        proximitySensorDataDao = db.proximitySensorDataDao()
        lightSensorDataDao = db.lightSensorDataDao()
        geomagneticRotationVectorSensorDataDao = db.geomagneticRotationVectorSensorDataDao()

        // Initialising sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        geomagneticRotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)

        // Initialising Toggle Buttons Listeners
        binding.togBtnProximity.setOnCheckedChangeListener { _, isChecked ->
            isCollectingProximityData = isChecked
            if (isChecked) {
                // Start the Proximity Sensor Listening
                sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
                // Update UI
                binding.togBtnProximity.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F44336"))
            } else {
                // Stop the Proximity Sensor Listening
                sensorManager.unregisterListener(this, proximitySensor)
                // Update UI
                binding.togBtnProximity.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#0B574A"))
                binding.tvProximityFeedback.text = ""
            }
        }

        binding.togBtnLight.setOnCheckedChangeListener { _, isChecked ->
            isCollectingLightData = isChecked
            if (isChecked) {
                // Start the Light Sensor Listening
                sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
                // Update UI
                binding.togBtnLight.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F44336"))
            } else {
                // Stop the Sensor Listening
                sensorManager.unregisterListener(this, lightSensor)
                // Update UI
                binding.togBtnLight.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#0B574A"))
                binding.tvLightFeedback.text = ""
            }
        }

        binding.togBtnGeomagnetic.setOnCheckedChangeListener { _, isChecked ->
            isCollectingGeomagneticData = isChecked
            if (isChecked) {
                // Start the Geomagnetic Sensor Listening
                sensorManager.registerListener(this, geomagneticRotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL)
                // Update UI
                binding.togBtnGeomagnetic.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F44336"))
            } else {
                // Stop the Geomagnetic Sensor Listening
                sensorManager.unregisterListener(this, geomagneticRotationVectorSensor)
                // Update UI
                binding.togBtnGeomagnetic.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#0B574A"))
                binding.tvGeomagneticFeedback.text = ""
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isCollectingGeomagneticData) {
            sensorManager.registerListener(this, geomagneticRotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        if (isCollectingLightData) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        if (isCollectingProximityData) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val sensor = event.sensor
        when (sensor.type) {
            Sensor.TYPE_PROXIMITY -> {
                if(isCollectingProximityData) {
                    val distance = event.values[0]
                    // Updating UI with feedback
                    binding.tvProximityFeedback.text = "Distance = ${distance}cm"
                    // Making a proximity sensor data model
                    val data = ProximitySensorData(timestamp = System.currentTimeMillis(), distance = distance)
                    // Log the data if user places phone near their ear or cover the phone by hands.
                    if (distance < 2) {
                        Log.i("PROXIMITY DATA", data.toString())
                    }
                    // Storing the Proximity Sensor Data in the Room database
                    Thread {
                        proximitySensorDataDao.insert(data)
                    }.start()
                }
            }
            Sensor.TYPE_LIGHT -> {
                if(isCollectingLightData) {
                    val illuminance = event.values[0]
                    // Updating UI with feedback
                    binding.tvLightFeedback.text = "Illuminance = ${illuminance}lx"
                    // Making a light sensor data model
                    val data = LightSensorData(timestamp = System.currentTimeMillis(), illuminance = illuminance)
                    // Log the data if user places phone near their ear or cover the phone by hands.
                    if(illuminance < 5){
                        Log.i("LIGHT DATA", data.toString())
                    }
                    // Storing the Light Sensor Data in the Room Database
                    Thread {
                        lightSensorDataDao.insert(data)
                    }.start()
                }
            }
            Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> {
                if(isCollectingGeomagneticData) {
                    // Making geomagnetic rotation vector data model
                    val data = GeomagneticRotationVectorSensorData(
                        timestamp = System.currentTimeMillis(),
                        x = event.values[0],
                        y = event.values[1],
                        z = event.values[2],
                        cos = event.values[3],
                        headingAccuracy = event.values[4]
                    )
                    // Storing the Geomagnetic Sensor Data in the Room Database
                    Thread {
                        geomagneticRotationVectorSensorDataDao.insert(data)
                    }.start()
                    // Getting orientation angles as per Kotlin Documentation
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    // Update the UI with the correct feedback
                    updateUiWithFeedback()
                }
            }
        }
    }

    private fun updateUiWithFeedback() {
        // Get the azimuth, pitch and roll angles (in radians)
        val azimuth = orientationAngles[0]
        val pitch = orientationAngles[1]
        val roll = orientationAngles[2]

        // Convert them to Degrees
        val azimuthDegrees = Math.toDegrees(azimuth.toDouble()).toFloat()
        val pitchDegrees = Math.toDegrees(pitch.toDouble()).toFloat()
        val rollDegrees = Math.toDegrees(roll.toDouble()).toFloat()

        if (azimuthDegrees.toInt() == 0 && pitchDegrees.toInt() == 0 && rollDegrees.toInt() == 0) {
            binding.tvGeomagneticFeedback.text = "Success!"
        } else {
            // Calculate rotation required
            val zRotation = (azimuthDegrees + 360) % 360
            val xRotation = (-pitchDegrees + 360) % 360
            val yRotation = (-rollDegrees + 360) % 360
            val feedbackText = "Rotate:\n ${xRotation.toInt()} degrees on X-axis\n " +
                    " ${yRotation.toInt()} degrees on Y-axis\n" +
                    " ${zRotation.toInt()} degrees on Z-axis\n" +
                    "to align with the Magnetic North Pole."
            binding.tvGeomagneticFeedback.text = feedbackText
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }
}