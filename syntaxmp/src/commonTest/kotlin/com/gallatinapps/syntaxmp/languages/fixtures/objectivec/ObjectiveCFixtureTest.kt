package com.gallatinapps.syntaxmp.languages.fixtures.objectivec

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class ObjectiveCFixtureTest {
    private val code = """
        #import <Foundation/Foundation.h>
        @interface Job : NSObject
        @property(nonatomic, copy) NSString *name;
        - (NSInteger)scoreForName:(NSString *)name;
        - (void)resetWithCount:(int)count;
        @end

        NSInteger score(Job *job) {
            NSLog(@"%@", job.name);
            return job ? 10 : 0;
        }
    """.trimIndent()

    @Test
    fun `objective c preprocessor annotations types functions properties and numbers`() {
        assertTokenAt("objective-c", code, "#import <Foundation/Foundation.h>", "Annotation")
        assertTokenAt("objective-c", code, "@interface", "Annotation")
        assertTokenAt("objective-c", code, "Job", "Type")
        assertTokenAt("objective-c", code, "@property", "Annotation")
        assertTokenAt("objective-c", code, "copy", "Variable")
        assertTokenAt("objective-c", code, "NSString", "Type")
        assertTokenAt("objective-c", code, "NSInteger", "Type")
        assertTokenAt("objective-c", code, "void", "Type")
        assertTokenAt("objective-c", code, "int", "Type", occurrence = 1)
        assertTokenAt("objective-c", code, "scoreForName", "Variable")
        assertTokenAt("objective-c", code, "NSLog", "Function")
        assertTokenAt("objective-c", code, "name", "Property", occurrence = 2)
        assertTokenAt("objective-c", code, "return", "Keyword")
        assertTokenAt("objective-c", code, "10", "Number")
    }

    @Test
    fun `keywords inside strings are not highlighted`() = assertNoTokenAt(
        language = "objective-c",
        code = "NSString *label = @\"return\";",
        substring = "return",
        category = "Keyword",
    )

    @Test
    fun `objective c comments prefixed strings protocols categories and selectors are covered`() {
        val code = """
            // @interface should stay comment text
            NSString *label = @"return %@";
            @protocol JobScoring
            - (void)scoreWithName:(NSString *)name count:(NSInteger)count;
            @end
            SEL selector = @selector(scoreWithName:count:);
            @interface Job (Scoring)
            @end
        """.trimIndent()

        assertTokenAt("objective-c", code, "// @interface should stay comment text", "Comment")
        assertTokenAt("objective-c", code, "NSString", "Type")
        assertTokenAt("objective-c", code, "@\"return ", "String")
        assertTokenAt("objective-c", code, "%@", "Escape")
        assertTokenAt("objective-c", code, "@protocol", "Annotation")
        assertTokenAt("objective-c", code, "JobScoring", "Type")
        assertTokenAt("objective-c", code, "scoreWithName", "Variable")
        assertTokenAt("objective-c", code, "NSInteger", "Type")
        assertTokenAt("objective-c", code, "@selector", "Annotation")
        assertTokenAt("objective-c", code, "@interface", "Annotation", occurrence = 1)
        assertTokenAt("objective-c", code, "Scoring", "Type", occurrence = 1)
        assertNoTokenAt("objective-c", code, "@interface", "Annotation")
        assertNoTokenAt("objective-c", code, "return", "Keyword")
    }
}
