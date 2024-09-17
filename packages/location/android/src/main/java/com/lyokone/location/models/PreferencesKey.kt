package com.lyokone.location.models

/**
 * Key values for data stored in SharedPreferences.
 *
 */
object PreferencesKey {
    const val NOTIFICATION_DATA = "notificationData"
    const val NOTIFICATION_METADATA = "notificationMetadata"
    const val NOTIFICATION_TYPE = "notificationType"
    const val NOTIFICATION_VIBRATION = "notificationVibration"

    const val NOTIFICATION_ONGOING = "ongoing"
    const val NOTIFICATION_CHANNEL_ID = "channelId"
    const val NOTIFICATION_NOTIFICATION_ID = "notificationId"
    const val NOTIFICATION_ICON_NAME = "iconName"


    const val NOTIFICATION_NORMAL_TITLE = "title"
    const val NOTIFICATION_NORMAL_MESSAGE = "message"
    const val NOTIFICATION_ARRIVAL_STOP_CODE = "stopCode"
    const val NOTIFICATION_ARRIVAL_TOP = "topMessage"
    const val NOTIFICATION_ARRIVAL_BOTTOM = "bottomMessage"
    const val NOTIFICATION_ARRIVAL_ARRIVING = "arriving"
    const val NOTIFICATION_ARRIVAL_PLATE = "plate"
    const val NOTIFICATION_TRAVEL_CODE = "destinationCode"
    const val NOTIFICATION_TRAVEL_STOPS = "destinationStops"
    const val NOTIFICATION_TRAVEL_STOPS_SUFFIX = "destinationStopsSuffix"
    const val NOTIFICATION_TRAVEL_NAME = "destinationName"
    const val NOTIFICATION_TRAVEL_TOP = "topMessage"
    const val NOTIFICATION_TRAVEL_MODE = "mode"
    const val NOTIFICATION_TRAVEL_NO_DESTINATION = "noDestination"
}
