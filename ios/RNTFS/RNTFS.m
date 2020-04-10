#import "RNTFS.h"
#include <CommonCrypto/CommonDigest.h>

BOOL checkFileExisted(NSString *path, RCTPromiseRejectBlock reject) {
    BOOL existed = [NSFileManager.defaultManager fileExistsAtPath:path];
    if (!existed) {
        reject(@"1", @"file is not found.", nil);
        return false;
    }
    return true;
}

@implementation RNTStatusBar

- (dispatch_queue_t)methodQueue {
  return dispatch_queue_create("com.github.ReactNativeHero.fs", DISPATCH_QUEUE_SERIAL);
}

RCT_EXPORT_MODULE(RNTFS);

RCT_EXPORT_METHOD(exists:(NSString *)path resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {

    BOOL result = [NSFileManager.defaultManager fileExistsAtPath:path];
    
    resolve(@[
        @(result)
    ]);
    
}

RCT_EXPORT_METHOD(stat:(NSString *)path resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {

    if (!checkFileExisted(path, reject)) {
        return;
    }
    
    NSDictionary *attrs = [NSFileManager.defaultManager attributesOfItemAtPath:path error:nil];

    resolve(@{
        @"size": @(attrs.fileSize),
        @"mtime": @(attrs.fileModificationDate.timeIntervalSince1970 * 1000),
    });
    
}

RCT_EXPORT_METHOD(unlink:(NSString *)path resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {

    if (!checkFileExisted(path, reject)) {
        return;
    }
    
    BOOL result = [NSFileManager.defaultManager removeItemAtPath:path error:nil];
    
    resolve(@[
        @(result)
    ]);
    
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
    
    resolve(@[
        result
    ]);
    
}

@end
