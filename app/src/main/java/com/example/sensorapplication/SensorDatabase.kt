package com.example.sensorapplication

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sensorapplication.models.GeomagneticRotationVectorSensorData
import com.example.sensorapplication.models.LightSensorData
import com.example.sensorapplication.models.ProximitySensorData

@Database(entities = [ProximitySensorData::class, LightSensorData::class, GeomagneticRotationVectorSensorData::class], version = 1)
abstract class SensorDatabase : RoomDatabase() {
    abstract fun proximitySensorDataDao(): com.example.sensorapplication.dao.ProximitySensorDataDao
    abstract fun lightSensorDataDao(): com.example.sensorapplication.dao.LightSensorDataDao
    abstract fun geomagneticRotationVectorSensorDataDao(): com.example.sensorapplication.dao.GeomagneticRotationVectorSensorDataDao
}