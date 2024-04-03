// ignore_for_file: public_member_api_docs

part of '../location_platform_interface.dart';

/// Represents a geographical location in the real world.
class LocationData {
  LocationData._(
    this.latitude,
    this.longitude,
    this.accuracy,
    this.altitude,
    this.speed,
    this.speedAccuracy,
    this.heading,
    this.time,
    this.isMock,
    this.verticalAccuracy,
    this.headingAccuracy,
    this.elapsedRealtimeNanos,
    this.elapsedRealtimeUncertaintyNanos,
    this.satelliteNumber,
    this.provider,
  );

  /// Creates a new [LocationData] instance from a map.
  factory LocationData.fromMap(Map<String, dynamic> dataMap) {
    return LocationData._(
      dataMap['latitude'] as double?,
      dataMap['longitude'] as double?,
      dataMap['accuracy'] as double?,
      dataMap['altitude'] as double?,
      dataMap['speed'] as double?,
      dataMap['speed_accuracy'] as double?,
      dataMap['heading'] as double?,
      dataMap['time'] as double?,
      dataMap['isMock'] == 1,
      dataMap['verticalAccuracy'] as double?,
      dataMap['headingAccuracy'] as double?,
      dataMap['elapsedRealtimeNanos'] as double?,
      dataMap['elapsedRealtimeUncertaintyNanos'] as double?,
      dataMap['satelliteNumber'] as int?,
      dataMap['provider'] as String?,
    );
  }

  /// Latitude in degrees
  final double? latitude;

  /// Longitude, in degrees
  final double? longitude;

  /// Estimated horizontal accuracy of this location, radial, in meters
  ///
  /// Will be null if not available.
  final double? accuracy;

  /// Estimated vertical accuracy of altitude, in meters.
  ///
  /// Will be null if not available.
  final double? verticalAccuracy;

  /// In meters above the WGS 84 reference ellipsoid. Derived from GPS informations.
  ///
  /// Will be null if not available.
  final double? altitude;

  /// In meters/second
  ///
  /// Will be null if not available.
  final double? speed;

  /// In meters/second
  ///
  /// Will be null if not available.
  /// Not available on web
  final double? speedAccuracy;

  /// Heading is the horizontal direction of travel of this device, in degrees
  ///
  /// Will be null if not available.
  final double? heading;

  /// timestamp of the LocationData
  final double? time;

  /// Is the location currently mocked
  ///
  /// Always false on iOS
  final bool? isMock;

  /// Get the estimated bearing accuracy of this location, in degrees.
  /// Only available on Android
  /// https://developer.android.com/reference/android/location/Location#getBearingAccuracyDegrees()
  final double? headingAccuracy;

  /// Return the time of this fix, in elapsed real-time since system boot.
  /// Only available on Android
  /// https://developer.android.com/reference/android/location/Location#getElapsedRealtimeNanos()
  final double? elapsedRealtimeNanos;

  /// Get estimate of the relative precision of the alignment of the ElapsedRealtimeNanos timestamp.
  /// Only available on Android
  /// https://developer.android.com/reference/android/location/Location#getElapsedRealtimeUncertaintyNanos()
  final double? elapsedRealtimeUncertaintyNanos;

  /// The number of satellites used to derive the fix.
  /// Only available on Android
  /// https://developer.android.com/reference/android/location/Location#getExtras()
  final int? satelliteNumber;

  /// The name of the provider that generated this fix.
  /// Only available on Android
  /// https://developer.android.com/reference/android/location/Location#getProvider()
  final String? provider;

  @override
  String toString() =>
      'LocationData<lat: $latitude, long: $longitude${(isMock ?? false) ? ', mocked' : ''}>';

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is LocationData &&
          runtimeType == other.runtimeType &&
          latitude == other.latitude &&
          longitude == other.longitude &&
          accuracy == other.accuracy &&
          altitude == other.altitude &&
          speed == other.speed &&
          speedAccuracy == other.speedAccuracy &&
          heading == other.heading &&
          time == other.time &&
          isMock == other.isMock;

  @override
  int get hashCode =>
      latitude.hashCode ^
      longitude.hashCode ^
      accuracy.hashCode ^
      altitude.hashCode ^
      speed.hashCode ^
      speedAccuracy.hashCode ^
      heading.hashCode ^
      time.hashCode ^
      isMock.hashCode;
}

/// Precision of the Location. A lower precision will provide a greater battery
/// life.
///
/// https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest
/// https://developer.apple.com/documentation/corelocation/cllocationaccuracy?language=objc
enum LocationAccuracy {
  /// To request best accuracy possible with zero additional power consumption
  powerSave,

  /// To request "city" level accuracy
  low,

  /// To request "block" level accuracy
  balanced,

  /// To request the most accurate locations available
  high,

  /// To request location for navigation usage (affect only iOS)
  navigation,

  /// On iOS 14.0+, this is mapped to kCLLocationAccuracyReduced.
  /// See https://developer.apple.com/documentation/corelocation/kcllocationaccuracyreduced
  ///
  /// On iOS < 14.0 and Android, this is equivalent to LocationAccuracy.low.
  reduced,
}

/// Status of a permission request to use location services.
enum PermissionStatus {
  /// The permission to use location services has been granted for high accuracy.
  granted,

  /// The permission has been granted but for low accuracy. Only valid on iOS 14+.
  grantedLimited,

  /// The permission to use location services has been denied by the user. May
  /// have been denied forever on iOS.
  denied,

  /// The permission to use location services has been denied forever by the
  /// user. No dialog will be displayed on permission request.
  deniedForever
}

/// The response object of `Location.changeNotificationOptions`.
///
/// Contains native information about the notification shown on Android, when
/// running in background mode.
class AndroidNotificationData {
  const AndroidNotificationData._(this.channelId, this.notificationId);

  /// Creates a new [AndroidNotificationData] instance from a map.
  factory AndroidNotificationData.fromMap(Map<dynamic, dynamic> data) {
    return AndroidNotificationData._(
      data['channelId'] as String,
      data['notificationId'] as int,
    );
  }

  /// The id of the used Android notification channel.
  final String channelId;

  /// The id of the shown Android notification.
  final int notificationId;

  @override
  String toString() =>
      'AndroidNotificationData<channelId: $channelId, notificationId: $notificationId>';

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is AndroidNotificationData &&
          runtimeType == other.runtimeType &&
          channelId == other.channelId &&
          notificationId == other.notificationId;

  @override
  int get hashCode => channelId.hashCode ^ notificationId.hashCode;
}

enum NotificationType { ARRIVAL, TRAVEL, NORMAL }

enum NotificationImportance {
  IMPORTANCE_MAX,
  IMPORTANCE_HIGH,
  IMPORTANCE_DEFAULT,
  IMPORTANCE_LOW,
  IMPORTANCE_MIN,
  IMPORTANCE_NONE
}

enum NotificationVisibility {
  VISIBILITY_PRIVATE,
  VISIBILITY_PUBLIC,
  VISIBILITY_SECRET
}

class NotificationChannel {
  NotificationChannel(
      {required NotificationImportance importance,
      required NotificationVisibility visibility,
      required String name,
      required String id,
      required bool showBadge,
      required bool vibrationEnabled})
      : _importance = importance,
        _visibility = visibility,
        _name = name,
        _id = id,
        _showBadge = showBadge,
        _vibrationEnabled = vibrationEnabled;

  /// Constructs an instance of [NotificationChannel].
  final NotificationImportance _importance;
  final NotificationVisibility _visibility;
  final String _name;
  final String _id;
  final bool _showBadge;
  final bool _vibrationEnabled;

  /// Returns the data fields of [NotificationChannel] in JSON format.
  Map<String, dynamic> toJson() {
    return {
      'channelImportance': _importance.name,
      'channelVisibility': _visibility.name,
      'channelName': _name,
      'channelId': _id,
      'channelShowBadge': _showBadge ? 1 : 0,
      'channelVibrationEnabled': _vibrationEnabled ? 1 : 0,
    };
  }
}

/// Notification options for Android platform.
abstract class NotificationData {
  /// Constructs an instance of [NotificationData].
  const NotificationData(
    this._metadata,
    this._type,
    this._vibrationEnabled,
    this._channelId,
    this._notificationId,
    this._ongoing,
    this._iconName,
  );
  final Map<String, dynamic> _metadata;
  final NotificationType _type;
  final bool _vibrationEnabled;
  final bool _ongoing;
  final String _channelId;
  final String? _iconName;
  final int _notificationId;

  /// Returns the data fields of [NotificationData] in JSON format.
  Map<String, dynamic> toJson() {
    return {
      'notificationMetadata': jsonEncode(_metadata),
      'notificationType': _type.toString(),
      'notificationVibration': _vibrationEnabled ? 1 : 0,
      'ongoing': _ongoing ? 1 : 0,
      'channelId': _channelId,
      'notificationId': _notificationId,
      'iconName': _iconName,
    };
  }
}

class NormalNotificationData extends NotificationData {
  NormalNotificationData(
    String title,
    String message,
    String channelId,
    int notificationId, {
    bool vibrationEnabled = false,
    bool ongoing = false,
    String? iconName,
  }) : super(
          {'title': title, 'message': message},
          NotificationType.NORMAL,
          vibrationEnabled,
          channelId,
          notificationId,
          ongoing,
          iconName,
        );
}

class ArrivalNotificationData extends NotificationData {
  ArrivalNotificationData(
    String stopCode,
    String topMessage,
    String bottomMessage,
    String channelId,
    int notificationId, {
    bool arriving = false,
    bool vibrationEnabled = false,
    String? iconName,
  }) : super(
          {
            'stopCode': stopCode,
            'topMessage': topMessage,
            'bottomMessage': bottomMessage,
            'arriving': arriving ? 1 : 0,
          },
          NotificationType.ARRIVAL,
          vibrationEnabled,
          channelId,
          notificationId,
          true,
          iconName,
        );
  ArrivalNotificationData.plate(
    String stopCode,
    String topMessage,
    String bottomMessage,
    String plate,
    String channelId,
    int notificationId, {
    bool arriving = false,
    bool vibrationEnabled = false,
    String? iconName,
  }) : super(
          {
            'stopCode': stopCode,
            'topMessage': topMessage,
            'bottomMessage': bottomMessage,
            'arriving': arriving ? 1 : 0,
            'plate': plate,
          },
          NotificationType.ARRIVAL,
          vibrationEnabled,
          channelId,
          notificationId,
          true,
          iconName,
        );
}

class TravelNotificationData extends NotificationData {
  TravelNotificationData(
    String destinationCode,
    String destinationStops,
    String destinationName,
    String topMessage,
    String channelId,
    int notificationId, {
    bool vibrationEnabled = false,
    String destinationStopsSuffix = '',
    String? iconName,
  }) : super(
          {
            'destinationCode': destinationCode,
            'destinationStops': destinationStops,
            'destinationName': destinationName,
            'topMessage': topMessage,
            'destinationStopsSuffix': destinationStopsSuffix,
          },
          NotificationType.TRAVEL,
          vibrationEnabled,
          channelId,
          notificationId,
          true,
          iconName,
        );
}
