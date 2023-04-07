package com.example.sensorapplication

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ProximitySensorData::class, LightSensorData::class, GeomagneticRotationVectorSensorData::class], version = 1)
abstract class SensorDatabase : RoomDatabase() {
    abstract fun proximitySensorDataDao(): com.example.sensorapplication.DAOs.ProximitySensorDataDao
    abstract fun lightSensorDataDao(): com.example.sensorapplication.DAOs.LightSensorDataDao
    abstract fun geomagneticRotationVectorSensorDataDao(): com.example.sensorapplication.DAOs.GeomagneticRotationVectorSensorDataDao
}