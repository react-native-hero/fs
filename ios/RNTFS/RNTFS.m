#import "RNTFS.h"
#include <CommonCrypto/CommonDigest.h>

NSString *ERROR_CODE_FILE_NOT_FOUND = @"1";

BOOL checkFileExisted(NSString *path, RCTPromiseRejectBlock reject) {
    BOOL existed = [NSFileManager.defaultManager fileExistsAtPath:path];
    if (!existed) {
        reject(ERROR_CODE_FILE_NOT_FOUND, @"file is not found.", nil);
        return false;
    }
    return true;
}

@implementation RNTFS

- (dispatch_queue_t)methodQueue {
  return dispatch_queue_create("com.github.reactnativehero.fs", DISPATCH_QUEUE_SERIAL);
}

- (NSDictionary *)constantsToExport {
    return @{
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

@end
