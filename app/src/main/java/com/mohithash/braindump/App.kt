package com.mohithash.braindump

import android.app.Application
import androidx.room.Room
import com.mohithash.braindump.ai.AiClient
import com.mohithash.braindump.ai.PlannerAi
import com.mohithash.braindump.data.AppDb
import com.mohithash.braindump.data.JsonStore

class App : Application() {
    lateinit var db: AppDb
    lateinit var store: JsonStore
    val client = AiClient()
    val planner by lazy { PlannerAi(client) }
    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(this, AppDb::class.java, "braindump.db").build()
        store = JsonStore(this)
    }
}
