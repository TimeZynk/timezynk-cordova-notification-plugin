#import "TZNotification.h"
#import "AppDelegate.h"
#import "NSData+Conversion.h"
#import <Cordova/CDV.h>

@implementation TZNotification

- (NSMutableArray*)pendingNotifications {
    if(_pendingNotifications == nil) {
        _pendingNotifications = [[NSMutableArray alloc] init];
    }
    return _pendingNotifications;
}

/** Javascript commands */

- (void)register:(CDVInvokedUrlCommand*)command
{
#ifdef DEBUG
    NSLog(@"register for sandbox");
#else
    NSLog(@"register for production");
#endif

    [registerCallbackId release];
    registerCallbackId = [command.callbackId copy];
    if ([[UIApplication sharedApplication] respondsToSelector:@selector(registerUserNotificationSettings:)]) {
        UIUserNotificationSettings *settings = [UIUserNotificationSettings
                                                settingsForTypes:(UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeSound | UIRemoteNotificationTypeAlert)
                                                categories:nil];
        [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
    } else {
        [[UIApplication sharedApplication] registerForRemoteNotificationTypes:(UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeAlert | UIRemoteNotificationTypeSound)];
    }
}

- (void)unregister:(CDVInvokedUrlCommand*)command
{
    NSLog(@"unregister");
    [[UIApplication sharedApplication] unregisterForRemoteNotifications];
    NSDictionary* result = [[NSDictionary alloc] initWithObjectsAndKeys:@"ios", @"platform", nil];
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    [result release];
}

- (void)listen:(CDVInvokedUrlCommand*)command
{
    NSLog(@"listen");
    [listenCallbackId release];
    listenCallbackId = [command.callbackId copy];
}

- (void)getPendingNotifications:(CDVInvokedUrlCommand*)command {
    NSLog(@"getPendingNotifications: %@", _pendingNotifications);
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:_pendingNotifications];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    [self.pendingNotifications removeAllObjects];
}

- (void)setBadge:(CDVInvokedUrlCommand*)command {
    NSNumber* badge = [command.arguments objectAtIndex:0];
    NSLog(@"Setting badge to %d", [badge intValue]);

    [[UIApplication sharedApplication] setApplicationIconBadgeNumber:[badge intValue]];

    NSDictionary* result = [[NSDictionary alloc] initWithObjectsAndKeys:badge, @"badge", nil];
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    [result release];
}

/** Objective-C callbacks from AppDelegate */

NSMutableDictionary* convertPayload(NSDictionary* payload)
{
    NSMutableDictionary* mutablePayload = [payload mutableCopy];
    [mutablePayload removeObjectForKey:@"aps"];

    NSNumber* typeCode = [payload objectForKey:@"t"];
    NSString* type;

    switch ([typeCode intValue]) {
        case 'M':
            type = @"message";
            break;

        case 'I':
            type = @"inquiry";
            break;

        case 'J':
            type = @"inquiry-not-booked";
            break;

        case 'K':
            type = @"inquiry-reminder";
            break;

        case 'L':
            type = @"timereport-reminder";
            break;

        case 'B':
            type = @"booking";
            break;

        case 'U':
            type = @"unbooking";
            break;

        case 'C':
            type = @"booking-changed";
            break;

        case 'R':
            type = @"repetition-created";
            break;

        case 'S':
            type = @"repetition-split";
            break;

        case 'D':
            type = @"repetition-deleted";
            break;

        case 'O':
            type = @"new-open-shifts";
            break;

        default:
            type = @"unknown";
    }

    [mutablePayload setValue:type forKey:@"type"];
    [mutablePayload removeObjectForKey:@"t"];
    return mutablePayload;
}

- (void)pushPendingNotification:(NSDictionary*)payload {
    NSMutableDictionary* mutablePayload = convertPayload(payload);
    [mutablePayload setValue:(id)kCFBooleanTrue forKey:@"launchNotification"];
    NSLog(@"Add payload to pending: %@", mutablePayload);
    [self.pendingNotifications addObject:mutablePayload];
    NSLog(@"Pushed to pending: %@", self.pendingNotifications);
}

- (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)devToken {
    NSLog(@"Registered");
    if (registerCallbackId) {
        NSString* token = [devToken hexadecimalString];
        NSDictionary* result = [[NSDictionary alloc] initWithObjectsAndKeys:token, @"registrationId", @"ios", @"platform", nil];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:registerCallbackId];
        [result release];
        [registerCallbackId release];
        registerCallbackId = NULL;
    }
}

- (void)didFailToRegisterForRemoteNotificationsWithError:(NSError *)err {
    NSLog(@"Error");
    NSLog(@"%@",[err localizedDescription]);
}

- (void)didReceiveRemoteNotification:(NSDictionary *)userInfo {
    NSLog(@"didReceiveRemoteNotification: %@", userInfo);
    UIApplicationState state = [UIApplication sharedApplication].applicationState;
    NSLog(@"applicationState: %d", state);

    if (state == UIApplicationStateBackground) {
        // app was just brought from background to foreground
        NSLog(@"sending payload to pending queue");
        [self pushPendingNotification:userInfo];
    } else {
        // app was already in the foreground
        if (listenCallbackId) {
            NSLog(@"sending payload to listen callback");
            NSMutableDictionary* result = convertPayload(userInfo);
            if (state == UIApplicationStateInactive) {
                [result setValue:(id)kCFBooleanTrue forKey:@"launchNotification"];
            }
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
            [pluginResult setKeepCallback:[NSNumber numberWithBool:YES]];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:listenCallbackId];
        }
    }
}

- (void) dealloc {
    [_pendingNotifications dealloc];
    [super dealloc];
}

@end
