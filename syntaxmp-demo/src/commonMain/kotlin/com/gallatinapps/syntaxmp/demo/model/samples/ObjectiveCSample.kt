package com.gallatinapps.syntaxmp.demo.model.samples

internal val ObjectiveCSample = """
    #import <Foundation/Foundation.h>

    typedef NS_ENUM(NSInteger, HJNoteStatus) {
        HJNoteStatusDraft,
        HJNoteStatusReview,
        HJNoteStatusPublished,
    };

    @interface HJNoteSummary : NSObject
    @property (nonatomic, copy) NSString *title;
    @property (nonatomic, assign, getter=isPinned) BOOL pinned;
    @property (nonatomic, assign) NSUInteger words;
    - (instancetype)initWithTitle:(NSString *)title words:(NSUInteger)words pinned:(BOOL)pinned;
    - (NSString *)labelWithStatus:(HJNoteStatus)status;
    @end

    @implementation HJNoteSummary
    - (instancetype)initWithTitle:(NSString *)title words:(NSUInteger)words pinned:(BOOL)pinned {
        self = [super init];
        if (self) {
            _title = [title copy];
            _words = words;
            _pinned = pinned;
        }
        return self;
    }

    - (NSString *)labelWithStatus:(HJNoteStatus)status {
        NSUInteger minutes = MAX(1, (self.words + 219) / 220);
        NSDictionary *names = @{ @0: @"draft", @1: @"review", @2: @"published" };
        return [NSString stringWithFormat:@"%@ • %@ • %lu min",
                self.title, names[@(status)], (unsigned long)minutes];
    }
    @end
""".trimIndent()
