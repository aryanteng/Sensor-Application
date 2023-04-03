package com.example.sensorapplication

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ProximitySensorData::class, LightSensorData::class, GeomagneticRotationVectorSensorData::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun proximitySensorDataDao(): com.example.sensorapplication.dao.ProximitySensorDataDao
    abstract fun lightSensorDataDao(): com.example.sensorapplication.dao.LightSensorDataDao
    abstract fun geomagneticRotationVectorSensorDataDao(): com.example.sensorapplication.dao.GeomagneticRotationVectorSensorDataDao
}