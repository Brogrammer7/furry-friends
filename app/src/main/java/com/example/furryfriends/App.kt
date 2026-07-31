package com.example.furryfriends

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application()

/*
* Dependency Injection is initialized here (already done above with annotation)
*
* Initialize future analytics here:
* ex: Analytics.init(this)
*
* Set up future logging here:
* ex: Logger.init()
*
* */
