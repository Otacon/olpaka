package org.cyanotic.olpaka.database

import app.cash.sqldelight.db.SqlDriver
import org.cyanotic.olpaka.Database


expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): Database {
    val driver = driverFactory.createDriver()
    val database = Database(driver)

    return database
}