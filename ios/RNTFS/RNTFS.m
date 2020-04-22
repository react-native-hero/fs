#import "RNTFS.h"
#import <React/RCTConvert.h>
#include <CommonCrypto/CommonDigest.h>

@implementation RNTFS

static NSString *ERROR_CODE_FILE_NOT_FOUND = @"1";

static BOOL checkFileExisted(NSString *path, RCTPromiseRejectBlock reject) {
    BOOL existed = [NSFileManager.defaultManager fileExistsAtPath:path];
    if (!existed) {
        reject(ERROR_CODE_FILE_NOT_FOUND, @"file is not found.", nil);
        return false;
    }
    return true;
}

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

- (dispatch_queue_t)methodQueue {
    return dispatch_queue_create("com.github.reactnativehero.fs", DISPATCH_QUEUE_SERIAL);
}

- (NSDictionary *)constantsToExport {
    return @{
        @"DIRECTORY_CACHE": [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) firstObject],
        @"DIRECTORY_DOCUMENT": [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject],
        
        @"DIRECTORY_DOWNLOAD": [NSSearchPathForDirectoriesInDomains(NSDownloadsDirectory, NSUserDomainMask, YES) firstObject],
        @"DIRECTORY_PICTURE": [NSSearchPathForDirectoriesInDomains(NSPicturesDirectory, NSUserDomainMask, YES) firstObject],
        @"DIRECTORY_MUSIC": [NSSearchPathForDirectoriesInDomains(NSMusicDirectory, NSUserDomainMask, YES) firstObject],
        @"DIRECTORY_MOVIE": [NSSearchPathForDirectoriesInDomains(NSMoviesDirectory, NSUserDomainMask, YES) firstObject],
        
        @"ERROR_CODE_FILE_NOT_FOUND": ERROR_CODE_FILE_NOT_FOUND,
    };
}

RCT_EXPORT_MODULE(RNTFS);

RCT_EXPORT_METHOD(exists:(NSString *)path resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {

    BOOL result = [NSFileManager.defaultManager fileExistsAtPath:path];
    
    resolve(@{
        @"exists": @(result),
    });
    
}

RCT_EXPORT_METHOD(stat:(NSString *)path resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {

    if (!checkFileExisted(path, reject)) {
        return;
    }
    
    NSDictionary *attrs = [NSFileManager.defaultManager attributesOfItemAtPath:path error:nil];

    double ms = floor(attrs.fileModificationDate.timeIntervalSince1970 * 1000);
    
    resolve(@{
        @"name": path.lastPathComponent,
        @"size": @(attrs.fileSize),
        @"mtime": [NSString stringWithFormat:@"%.0f", ms],
    });
    
}

RCT_EXPORT_METHOD(unlink:(NSString *)path resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {

    if (!checkFileExisted(path, reject)) {
        return;
    }
    
    BOOL result = [NSFileManager.defaultManager removeItemAtPath:path error:nil];
    
    resolve(@{
        @"success": @(result),
    });
    
}

RCT_EXPORT_METHOD(md5:(NSString *)path resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {

    if (!checkFileExisted(path, reject)) {
        return;
    }
    
    NSData *data = [NSData dataWithContentsOfFile:path];

    unsigned char digest[CC_MD5_DIGEST_LENGTH];

    CC_MD5(data.bytes, (CC_LONG)data.length, digest);

    NSMutableString *result = [NSMutableString stringWithCapacity:CC_MD5_DIGEST_LENGTH * 2];

    for (int i = 0; i < CC_MD5_DIGEST_LENGTH; i++) {
        [result appendFormat:@"%02x", digest[i]];
    }
    
    resolve(@{
        @"md5": result,
    });
    
}


- (NSInteger)numberOfPreviewItemsInPreviewController:(QLPreviewController *)controller {
    return 1;
}

- (id <QLPreviewItem>)previewController:(QLPreviewController *)controller previewItemAtIndex:(NSInteger)index {
    return self.previewFileURL;
}

- (void)previewControllerDidDismiss:(QLPreviewController *)controller {
    self.previewController = nil;
    self.previewFileURL = nil;
}

RCT_EXPORT_METHOD(preview:(NSDictionary*)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {

    NSString *path = [RCTConvert NSString:options[@"path"]];
    if (!checkFileExisted(path, reject)) {
        return;
    }

    self.previewFileURL = [NSURL fileURLWithPath:path];

    RCTExecuteOnMainQueue(^{
        
        QLPreviewController *controller = [[QLPreviewController alloc] init];
        self.previewController = controller;
        
        controller.dataSource = self;
        controller.delegate = self;

        UIViewController *root = RCTKeyWindow().rootViewController;
        UIViewController *presented = RCTPresentedViewController();
        
        if (presented && root != presented) {
            [root dismissViewControllerAnimated:YES completion:^ {
                [root presentViewController:controller animated:YES completion:nil];
                resolve(@{});
            }];
        }
        else {
            [root presentViewController:controller animated:YES completion:nil];
            resolve(@{});
        }
        
    });

}

@end
