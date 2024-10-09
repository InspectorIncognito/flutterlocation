package com.lyokone.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lyokone.location.models.ArrivalNotificationOptions
import com.lyokone.location.models.NormalNotificationOptions
import com.lyokone.location.models.NotificationOptions
import com.lyokone.location.models.TravelNotificationOptions
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry

const val kDefaultChannelName: String = "Location background service"
const val kDefaultNotificationTitle: String = "Location background service running"
const val kDefaultNotificationIconName: String = "navigation_empty_icon"
const val kDefaultNotificationId = 75418
const val kDefaultChannelId = "flutter_location_channel_01"

class NotificationBuilder(
    private val context: Context,
) {
    private var options: NotificationOptions = NormalNotificationOptions(
            vibration = false,
            ongoing = true,
            channelId = kDefaultChannelId,
            notificationId = kDefaultNotificationId,
            iconName = kDefaultNotificationIconName,
            title = kDefaultNotificationTitle,
            message = ""
    )
    private var builder: NotificationCompat.Builder = NotificationCompat.Builder(context, kDefaultChannelId)
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    init {
        updateNotification(options, false)
    }

    private fun getDrawableId(iconName: String): Int {
        return context.resources.getIdentifier(iconName, "drawable", context.packageName)
    }

    private fun buildBringToFrontIntent(): PendingIntent? {
        val intent: Intent? = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.setPackage(null)
            ?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

        return if (intent != null) {
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            null
        }
    }

    private fun createDefaultChannel() {
        Log.d("flutter", "createDefaultChannel")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = NotificationManagerCompat.from(context)
            val channel = NotificationChannel(
                options.channelId,
                kDefaultChannelName,
                NotificationManager.IMPORTANCE_NONE
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateNotification(
        options: NotificationOptions,
        notify: Boolean
    ) {
        Log.d("flutterLocationService", "iconName: ${options.iconName}")
        val iconId = getDrawableId(options.iconName ?: "").let {
            if (it != 0) it else getDrawableId(kDefaultNotificationIconName)
        }
        builder = when (options) {
            is ArrivalNotificationOptions -> {
                buildArrivalNotification(options.stopCode, options.topMessage, options.bottomMessage, options.sharing)
            }
            is NormalNotificationOptions -> {
                buildNormalNotification(options.title, options.message)
            }
            is TravelNotificationOptions -> {
                buildTravelNotification(options)
            }
            else -> {
                buildNormalNotification("Unknown", "Unknown message")
            }
        }
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(iconId)
        if (options.ongoing) {
            builder = builder
                    .setOngoing(true)
                    .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        }

        builder.setContentIntent(buildBringToFrontIntent())

        if (notify) {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(options.notificationId, builder.build())
        }
    }

    private fun buildNormalNotification(title: String, message: String): NotificationCompat.Builder {
        return  NotificationCompat.Builder(context, options.channelId)
                .setContentTitle(title)
                .setContentText(message)
    }

    private fun buildArrivalNotification(stopCode: String, top: String, bottom: String, sharing: String): NotificationCompat.Builder {
        var topMessage = top
        if (sharing.isNotEmpty()) {
            topMessage = "${sharing}: $topMessage"
        }
        val notificationLayout = RemoteViews(context.packageName, R.layout.notification_arrival_data)
        notificationLayout.setTextViewText(R.id.stop_code_text, stopCode)
        notificationLayout.setTextViewText(R.id.data_top_text, topMessage)
        notificationLayout.setTextViewText(R.id.data_bottom_text, bottom)

        return NotificationCompat.Builder(context, options.channelId)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
    }

    private fun buildTravelNotification(data: TravelNotificationOptions): NotificationCompat.Builder {
        val notificationLayout = if (data.destinationCode == "") {
            var topMessage = data.topMessage
            if (data.sharing.isNotEmpty()) {
                topMessage = "${data.sharing}: ${data.topMessage}"
            }
            val layout = RemoteViews(context.packageName, R.layout.notification_travel_empty)
            layout.setTextViewText(R.id.notification_top, topMessage)
            layout.setTextViewText(R.id.no_destination, data.noDestination)
            layout
        } else {
            val visibility = if (data.sharing.isNotEmpty()) View.VISIBLE else View.GONE
            val layout = RemoteViews(context.packageName, R.layout.notification_travel_data)
            layout.setViewVisibility(R.id.sharing, visibility)
            layout.setTextViewText(R.id.stop_code, data.destinationCode)
            layout.setTextViewText(R.id.sharing, "${data.sharing}.")
            layout.setTextViewText(R.id.stations_quantity, data.destinationStops)
            layout.setTextViewText(R.id.stop_name, data.destinationName)
            layout.setTextViewText(R.id.station_plural, data.destinationStopsSuffix)
            if (data.mode == 3) {
                layout.setImageViewResource(R.id.stop_image_, R.drawable.iconos_paradero_mapa)
            } else if (data.mode == 1) {
                layout.setImageViewResource(R.id.stop_image_, R.drawable.iconos_estacion_mapa)
            }
            layout
        }

        return NotificationCompat.Builder(context, options.channelId)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
    }

    fun updateOptions(options: NotificationOptions, isVisible: Boolean) {
        if (options.channelId == kDefaultChannelId) {
            createDefaultChannel()
        }

        this.options = options
        updateNotification(options, isVisible)
    }

    fun build(): Notification {
        if (options.channelId == kDefaultChannelId) {
            createDefaultChannel()
        }
        return builder.build()
    }
}

class FlutterLocationService : Service(), PluginRegistry.RequestPermissionsResultListener {
    companion object {
        private const val TAG = "FlutterLocationService"

        private const val REQUEST_PERMISSIONS_REQUEST_CODE: Int = 641
    }

    // Binder given to clients
    private val binder = LocalBinder()

    // Service is foreground
    private var isForeground = false

    private var activity: Activity? = null

    private var backgroundNotification: NotificationBuilder? = null

    var location: FlutterLocation? = null
        private set

    // Store result until a permission check is resolved
    var result: MethodChannel.Result? = null

    val locationActivityResultListener: PluginRegistry.ActivityResultListener?
        get() = location

    val locationRequestPermissionsResultListener: PluginRegistry.RequestPermissionsResultListener?
        get() = location

    val serviceRequestPermissionsResultListener: PluginRegistry.RequestPermissionsResultListener
        get() = this

    inner class LocalBinder : Binder() {
        fun getService(): FlutterLocationService = this@FlutterLocationService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Creating service.")

        location = FlutterLocation(applicationContext, null)
        backgroundNotification = NotificationBuilder(
            applicationContext
        )
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "Binding to location service.")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Unbinding from location service.")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.d(TAG, "Destroying service.")

        location = null
        backgroundNotification = null

        super.onDestroy()
    }

    fun checkBackgroundPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity?.let {
                val locationPermissionState = ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
                locationPermissionState == PackageManager.PERMISSION_GRANTED
            } ?: throw ActivityNotFoundException()
        } else {
            location?.checkPermissions() ?: false
        }
    }

    fun requestBackgroundPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    REQUEST_PERMISSIONS_REQUEST_CODE
                )
            } ?: throw ActivityNotFoundException()
        } else {
            location?.result = this.result
            location?.requestPermissions()
            // result passed to Location reference here won't be needed
            this.result = null
        }
    }

    fun isInForegroundMode(): Boolean = isForeground

    fun enableBackgroundMode() {
        if (isForeground) {
            Log.d(TAG, "Service already in foreground mode.")
        } else {
            Log.d(TAG, "Start service in foreground mode.")

            val notification = backgroundNotification!!.build()
            startForeground(kDefaultNotificationId, notification)

            isForeground = true
        }
    }

    fun disableBackgroundMode() {
        Log.d(TAG, "Stop service in foreground.")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }

        isForeground = false
    }

    fun changeNotificationOptions(options: NotificationOptions): Map<String, Any>? {
        backgroundNotification?.updateOptions(options, isForeground)

        return if (isForeground) {
            mapOf("channelId" to kDefaultChannelId, "notificationId" to kDefaultNotificationId)
        } else {
            null
        }
    }

    fun setActivity(activity: Activity?) {
        this.activity = activity
        location?.setActivity(activity)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && requestCode == REQUEST_PERMISSIONS_REQUEST_CODE && permissions.size == 2 &&
            permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION && permissions[1] == Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, background mode can be enabled
                enableBackgroundMode()
                result?.success(1)
                result = null
            } else {
                if (!shouldShowRequestBackgroundPermissionRationale()) {
                    result?.error(
                        "PERMISSION_DENIED_NEVER_ASK",
                        "Background location permission denied forever - please open app settings",
                        null
                    )
                } else {
                    result?.error("PERMISSION_DENIED", "Background location permission denied", null)
                }
                result = null
            }
        }
        return false
    }

    private fun shouldShowRequestBackgroundPermissionRationale(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } ?: throw ActivityNotFoundException()
        } else {
            false
        }
}
