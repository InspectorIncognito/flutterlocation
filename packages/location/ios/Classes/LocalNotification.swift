//
//  LocalNotification.swift
//  location
//
//  Created by Agustin Antoine on 04-04-24.
//

import Foundation

public class LocalNotification: NSObject {
    
    @objc public static func cancelNotification(with data: NSDictionary) {
        let notificationId = data["notificationId"] as! Int
        UNUserNotificationCenter.current().removeDeliveredNotifications(withIdentifiers: ["\(notificationId)"])
    }
    
    @objc public static func addNotification(with data: NSDictionary) {
        guard let notificationData = data["notificationData"] as? NSDictionary else {
            print("No notificationData")
            return
        }
        let ongoing = notificationData["ongoing"] as? Bool ?? true
        if ongoing {
            print("iOS does not process ongoing")
            return
        }
        guard let metadata = notificationData["notificationMetadata"] as? String, let id = notificationData["notificationId"] as? Int else {
            print("No notificationMetadata")
            return
        }
        
        guard let jsonData = metadata.data(using: .utf8) else {
            print("Metadata wrongly coded")
            return
        }
        
        do {
            var json = try JSONSerialization.jsonObject(with: jsonData, options: []) as! [String: Any]
            
            let title = json["title"] as! String
            let message = json["message"] as! String
            
            let content = UNMutableNotificationContent()
            content.title = title
            content.body = message
            content.sound = UNNotificationSound.default

            let request = UNNotificationRequest(identifier: "\(id)", content: content, trigger: nil)

            UNUserNotificationCenter.current().add(request) { error in
                if let error = error {
                    print("Error scheduling notification: \(error.localizedDescription)")
                } else {
                    print("Notification scheduled successfully")
                }
            }
        } catch {
            print(error)
        }
    }
}
