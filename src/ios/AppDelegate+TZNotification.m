#import "AppDelegate+TZNotification.h"
#import "TZNotification.h"

@implementation AppDelegate (TZNotificationDelegate)

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    NSLog(@"registration succeeded");
    TZNotification* notificationPlugin = [self.viewController getCommandInstance:@"TZNotification"];
    [notificationPlugin didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
}

- (void)application:(UIApplication *)application didRegisterUserNotificationSettings:(UIUserNotificationSettings *)notificationSettings
{
    [application registerForRemoteNotifications];
}

- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
    NSLog(@"registration error");
    TZNotification* notificationPlugin = [self.viewController getCommandInstance:@"TZNotification"];
    [notificationPlugin didFailToRegisterForRemoteNotificationsWithError:error];
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo
{
    TZNotification* notificationPlugin = [self.viewController getCommandInstance:@"TZNotification"];
    [notificationPlugin didReceiveRemoteNotification:userInfo];
}

@end
