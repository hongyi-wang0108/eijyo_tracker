package com.eijyo.tracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. [@HiltAndroidApp] triggers Hilt's code generation and
 * creates the application-level dependency container that every other component
 * (Activities, ViewModels, …) is built from.
 */
@HiltAndroidApp
class EijyoApp : Application()
