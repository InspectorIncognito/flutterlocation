package com.lyokone.location;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import com.lyokone.location.models.PreferencesKey;
import com.lyokone.location.models.NotificationOptions;

import org.json.JSONObject;

final class MethodCallHandlerImpl implements MethodCallHandler {
    private static final String TAG = "MethodCallHandlerImpl";

    private FlutterLocation location;
    private FlutterLocationService locationService;

    @Nullable
    private MethodChannel channel;

    private static final String METHOD_CHANNEL_NAME = "lyokone/location";

    void setLocation(FlutterLocation location) {
        this.location = location;
    }

    void setLocationService(FlutterLocationService locationService) {
        this.locationService = locationService;
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        switch (call.method) {
            case "changeSettings":
                onChangeSettings(call, result);
                break;
            case "getLocation":
                onGetLocation(result);
                break;
            case "hasPermission":
                onHasPermission(result);
                break;
            case "requestPermission":
                onRequestPermission(result);
                break;
            case "serviceEnabled":
                onServiceEnabled(result);
                break;
            case "requestService":
                location.requestService(result);
                break;
            case "isBackgroundModeEnabled":
                isBackgroundModeEnabled(result);
                break;
            case "enableBackgroundMode":
                enableBackgroundMode(call, result);
                break;
            case "changeNotificationOptions":
                onChangeNotificationOptions(call, result);
                break;
            case "createChannel":
                createChannel(call, result);
                break;
            case "cancelNotification":
                cancelNotification(call, result);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    /**
     * Registers this instance as a method call handler on the given
     * {@code messenger}.
     */
    void startListening(BinaryMessenger messenger) {
        if (channel != null) {
            Log.wtf(TAG, "Setting a method call handler before the last was disposed.");
            stopListening();
        }

        channel = new MethodChannel(messenger, METHOD_CHANNEL_NAME);
        channel.setMethodCallHandler(this);
    }

    /**
     * Clears this instance from listening to method calls.
     */
    void stopListening() {
        if (channel == null) {
            Log.d(TAG, "Tried to stop listening when no MethodChannel had been initialized.");
            return;
        }

        channel.setMethodCallHandler(null);
        channel = null;
    }

    private void onChangeSettings(MethodCall call, Result result) {
        try {
            final Integer locationAccuracy = location.mapFlutterAccuracy.get((Integer) call.argument("accuracy"));
            final Long updateIntervalMilliseconds = new Long((int) call.argument("interval"));
            final Long fastestUpdateIntervalMilliseconds = updateIntervalMilliseconds / 2;
            final Float distanceFilter = new Float((double) call.argument("distanceFilter"));

            location.changeSettings(locationAccuracy, updateIntervalMilliseconds, fastestUpdateIntervalMilliseconds,
                    distanceFilter);

            result.success(1);
        } catch (Exception e) {
            result.error("CHANGE_SETTINGS_ERROR",
                    "An unexcepted error happened during location settings change:" + e.getMessage(), null);
        }
    }

    private void onGetLocation(Result result) {
        location.getLocationResult = result;
        if (!location.checkPermissions()) {
            location.requestPermissions();
        } else {
            location.startRequestingLocation();
        }
    }

    private void onHasPermission(Result result) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            result.success(1);
            return;
        }

        if (location.checkPermissions()) {
            result.success(1);
        } else {
            result.success(0);
        }
    }

    private void onServiceEnabled(Result result) {
        try {
            result.success(location.checkServiceEnabled() ? 1 : 0);
        } catch (Exception e) {
            result.error("SERVICE_STATUS_ERROR", "Location service status couldn't be determined", null);
        }
    }

    private void onRequestPermission(Result result) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            result.success(1);
            return;
        }

        location.result = result;
        location.requestPermissions();
    }

    private void isBackgroundModeEnabled(Result result) {
        if (locationService != null) {
            result.success(this.locationService.isInForegroundMode() ? 1 : 0);
        } else {
            result.success(0);
        }
    }

    private void enableBackgroundMode(MethodCall call, Result result) {
        final Boolean enable = call.argument("enable");
        if (locationService != null && enable != null) {
            if (locationService.checkBackgroundPermissions()) {
                if (enable) {
                    locationService.enableBackgroundMode();

                    result.success(1);
                } else {
                    locationService.disableBackgroundMode();

                    result.success(0);
                }
            } else {
                if (enable) {
                    locationService.setResult(result);
                    locationService.requestBackgroundPermissions();
                } else {
                    locationService.disableBackgroundMode();

                    result.success(0);
                }
            }
        } else {
            result.success(0);
        }
    }

    private void onChangeNotificationOptions(MethodCall call, Result result) {
        try {
            HashMap<String, Object> notificationRaw = call.argument(PreferencesKey.NOTIFICATION_DATA);
            NotificationOptions notificationOptions = null;
            String passedIconName = call.argument("iconName");

            if (notificationRaw != null) {
                notificationOptions = NotificationOptions.Companion.factory(new JSONObject(notificationRaw), passedIconName);
            }

            if (notificationOptions != null) {
                if (notificationOptions.getOngoing()) {
                    Map<String, Object> notificationMeta = this.locationService.changeNotificationOptions(notificationOptions);
                    result.success(notificationMeta);
                    return;
                } else {
                    NotificationBuilder notification = new NotificationBuilder(this.locationService);
                    notification.updateOptions(notificationOptions, true);
                }
            }
            result.success(null);
        } catch (Exception e) {
            result.error("CHANGE_NOTIFICATION_OPTIONS_ERROR",
                    "An unexpected error happened during notification options change:" + e.getMessage(), null);
        }
    }

    private void cancelNotification(MethodCall call, Result result) {
        try {
            int notificationId = call.argument("notificationId");
            NotificationManager notificationManager = (NotificationManager) locationService.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
            result.success(true);
        } catch (Exception e) {
            result.error("CANCEL_NOTIFICATION_ERROR",
                    "An unexpected error happened during notification options change:" + e.getMessage(), null);
        }
    }

    private void createChannel(MethodCall call, Result result) {
        try {
            HashMap<String, Object> dataRaw = call.argument("channelData");
            if (dataRaw != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                JSONObject data = new JSONObject(dataRaw);
                NotificationManager notificationManager = (NotificationManager) locationService.getSystemService(Context.NOTIFICATION_SERVICE);

                String rawImportance = data.getString("channelImportance");
                int importance = mapImportance(rawImportance);
                String rawVisibility = data.getString("channelVisibility");
                int visibility = mapVisibility(rawVisibility);
                NotificationChannel channel = new NotificationChannel(
                        data.getString("channelId"), data.getString("channelName"),
                        importance);
                channel.setShowBadge(data.getInt("channelShowBadge") == 1);
                long[] vibrationPattern = {200, 500, 200, 500, 200, 500};
                channel.setVibrationPattern(vibrationPattern);
                channel.enableVibration(data.getInt("channelVibrationEnabled") == 1);
                channel.setLockscreenVisibility(visibility);
                notificationManager.createNotificationChannel(channel);
            }
            result.success(true);
        } catch (Exception e) {
            result.error("CREATE_CHANNEL_ERROR",
                    "An unexpected error happened during notification options change:" + e.getMessage(), null);
        }
    }

    private int mapImportance(String importance) {
        switch (importance) {
            case "IMPORTANCE_MAX":
                return 5;
            case "IMPORTANCE_HIGH":
                return 4;
            case "IMPORTANCE_DEFAULT":
                return 3;
            case "IMPORTANCE_LOW":
                return 2;
            case "IMPORTANCE_MIN":
                return 1;
            case "IMPORTANCE_NONE":
                return 0;
        }
        return 0;
    }

    private int mapVisibility(String visibility) {
        switch (visibility) {
            case "VISIBILITY_PUBLIC":
                return 1;
            case "VISIBILITY_PRIVATE":
                return 0;
            case "VISIBILITY_SECRET":
                return -1;
        }
        return -1;
    }
}
