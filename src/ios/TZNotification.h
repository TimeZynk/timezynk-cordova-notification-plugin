#import <Cordova/CDV.h>

@interface TZNotification : CDVPlugin {
    NSString* registerCallbackId;
    NSString* listenCallbackId;
}

@property (nonatomic, retain) NSMutableArray* pendingNotifications;

/** Javascript commands */
- (void)register:(CDVInvokedUrlCommand*)command;
- (void)unregister:(CDVInvokedUrlCommand*)command;
- (void)listen:(CDVInvokedUrlCommand*)command;
- (void)getPendingNotifications:(CDVInvokedUrlCommand*)command;
- (void)setBadge:(CDVInvokedUrlCommand*)command;

/** Objective-C callbacks from AppDelegate */
- (void)pushPendingNotification:(NSDictionary*)payload;
- (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;
- (void)didFailToRegisterForRemoteNotificationsWithError:(NSError *)error;
- (void)didReceiveRemoteNotification:(NSDictionary *)userInfo;

@end
