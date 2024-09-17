package com.lyokone.location.models

import org.json.JSONObject
import java.lang.IllegalStateException
abstract class NotificationOptions(
        val vibration: Boolean,
        val ongoing: Boolean,
        val channelId: String,
        val notificationId: Int,
        val iconName: String?,
) {
    companion object {
        fun factory(data: JSONObject, passedIconName: String?): NotificationOptions {
            val type = data.getString(PreferencesKey.NOTIFICATION_TYPE) ?: ""
            val vibration = data.getInt(PreferencesKey.NOTIFICATION_VIBRATION) == 1
            val ongoing = data.getInt(PreferencesKey.NOTIFICATION_ONGOING) == 1
            val channelId = data.getString(PreferencesKey.NOTIFICATION_CHANNEL_ID)
            val notificationId = data.getInt(PreferencesKey.NOTIFICATION_NOTIFICATION_ID)
            val iconName = passedIconName ?: data.getString(PreferencesKey.NOTIFICATION_ICON_NAME)

            if (!data.has(PreferencesKey.NOTIFICATION_METADATA)) {
                throw IllegalStateException("WRONG METADATA")
            }
            val metadata = data.getString(PreferencesKey.NOTIFICATION_METADATA)

            val metadataObj = JSONObject(metadata)

            when (type) {
                "NotificationType.NORMAL" -> {
                    return NormalNotificationOptions(
                        vibration, ongoing, channelId, notificationId, iconName,
                        metadataObj.getString(PreferencesKey.NOTIFICATION_NORMAL_TITLE),
                        metadataObj.getString(PreferencesKey.NOTIFICATION_NORMAL_MESSAGE),
                    )
                }
                "NotificationType.ARRIVAL" -> {
                    var plate: String? = null
                    if (metadataObj.has(PreferencesKey.NOTIFICATION_ARRIVAL_PLATE)) {
                        plate = metadataObj.getString(PreferencesKey.NOTIFICATION_ARRIVAL_PLATE)
                    }
                    return ArrivalNotificationOptions(
                        vibration, ongoing, channelId, notificationId, iconName,
                        metadataObj.getString(PreferencesKey.NOTIFICATION_ARRIVAL_STOP_CODE),
                        metadataObj.getString(PreferencesKey.NOTIFICATION_ARRIVAL_TOP),
                        metadataObj.getString(PreferencesKey.NOTIFICATION_ARRIVAL_BOTTOM),
                        metadataObj.getInt(PreferencesKey.NOTIFICATION_ARRIVAL_ARRIVING) == 1,
                        plate,
                    )
                }
                "NotificationType.TRAVEL" -> {
                    return TravelNotificationOptions(
                        vibration, ongoing, channelId, notificationId, iconName,
                        metadataObj.getString(PreferencesKey.NOTIFICATION_TRAVEL_CODE),
                        metadataObj.getString(PreferencesKey.NOTIFICATION_TRAVEL_STOPS),
                        metadataObj.getString(PreferencesKey.NOTIFICATION_TRAVEL_NAME),
                        metadataObj.getString(PreferencesKey.NOTIFICATION_TRAVEL_TOP),
                        metadataObj.getString(PreferencesKey.NOTIFICATION_TRAVEL_STOPS_SUFFIX),
                        metadataObj.getInt(PreferencesKey.NOTIFICATION_TRAVEL_MODE),
                        metadataObj.getString(PreferencesKey.NOTIFICATION_TRAVEL_NO_DESTINATION),
                    )
                }
                else -> throw IllegalStateException("WRONG TYPE")
            }
        }
    }
}

class NormalNotificationOptions(vibration: Boolean, ongoing: Boolean, channelId: String, notificationId: Int, iconName: String,
                                val title: String, val message: String, ) :
    NotificationOptions(vibration, ongoing, channelId, notificationId, iconName)

class ArrivalNotificationOptions(vibration: Boolean, ongoing: Boolean, channelId: String, notificationId: Int, iconName: String,
                                 val stopCode: String, val topMessage: String, val bottomMessage: String, val arriving: Boolean, val plate: String?) :
    NotificationOptions(vibration, ongoing, channelId, notificationId, iconName)

class TravelNotificationOptions(vibration: Boolean, ongoing: Boolean, channelId: String, notificationId: Int, iconName: String,
                                val destinationCode: String, val destinationStops: String, val destinationName: String,
                                val topMessage: String, val destinationStopsSuffix: String, val mode: Int, val noDestination: String) :
    NotificationOptions(vibration, ongoing, channelId, notificationId, iconName)