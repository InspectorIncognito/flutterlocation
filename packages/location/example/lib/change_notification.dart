import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:location/location.dart';

class ChangeNotificationWidget extends StatefulWidget {
  const ChangeNotificationWidget({super.key});

  @override
  _ChangeNotificationWidgetState createState() =>
      _ChangeNotificationWidgetState();
}

class _ChangeNotificationWidgetState extends State<ChangeNotificationWidget> {
  final Location _location = Location();
  final GlobalKey<FormState> _formKey = GlobalKey<FormState>();
  final TextEditingController _channelController = TextEditingController(
    text: 'Location background service',
  );
  final TextEditingController _titleController = TextEditingController(
    text: 'Location background service running',
  );

  String? _iconName = 'navigation_empty_icon';

  @override
  void dispose() {
    _channelController.dispose();
    _titleController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (kIsWeb) {
      return const Text(
        'Change notification settings not available on this platform',
      );
    }

    return Form(
      key: _formKey,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          ElevatedButton(
            onPressed: () {
              final data = NormalNotificationData(
                'Mensaje de Titulo',
                'Subtitulo',
                'channelId',
                1234,
                vibrationEnabled: true,
              );
              _location.changeNotificationOptions(
                notificationData: data,
                iconName: _iconName,
              );
            },
            child: const Text('Change'),
          ),
          const SizedBox(height: 4),
          ElevatedButton(
            onPressed: () {
              _location.cancelNotification(1234);
            },
            child: const Text('Cancel'),
          ),
        ],
      ),
    );
  }
}
