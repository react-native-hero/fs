#import <React/RCTBridgeModule.h>
#import <QuickLook/QuickLook.h>

@interface RNTFS : NSObject <RCTBridgeModule, QLPreviewControllerDataSource, QLPreviewControllerDelegate>

@property (nonatomic, strong) QLPreviewController *previewController;
@property (nonatomic, strong) NSURL *previewFileURL;

@end
