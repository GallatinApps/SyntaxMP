package com.gallatinapps.syntaxmp.builtins.fixtures.protobuf

import com.gallatinapps.syntaxmp.builtins.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import kotlin.test.Test

class ProtobufFixtureTest {
    private val code = """
        // proto scoring
        syntax = "proto3";
        import "common.proto";
        message Note {
          optional string title = 1;
          repeated int32 ids = 2;
          oneof body {
            string markdown = 3;
            bytes raw = 4;
          }
        }
        enum State { ACTIVE = 0; }
        service Notes {
          rpc Get (Note) returns (Note);
        }
    """.trimIndent()

    @Test
    fun `protobuf declarations scalars imports and rpc`() {
        assertTokenAt("protobuf", code, "// proto scoring", "Comment")
        assertTokenAt("protobuf", code, "syntax", "Keyword")
        assertTokenAt("protobuf", code, "\"proto3\"", "String")
        assertTokenAt("protobuf", code, "import", "Keyword")
        assertTokenAt("protobuf", code, "message", "Keyword")
        assertTokenAt("protobuf", code, "Note", "Type")
        assertTokenAt("protobuf", code, "optional", "Keyword")
        assertTokenAt("protobuf", code, "string", "Type")
        assertTokenAt("protobuf", code, "title", "Property")
        assertTokenAt("protobuf", code, "repeated", "Keyword")
        assertTokenAt("protobuf", code, "int32", "Type")
        assertTokenAt("protobuf", code, "ids", "Property")
        assertTokenAt("protobuf", code, "oneof", "Keyword")
        assertTokenAt("protobuf", code, "bytes", "Type")
        assertTokenAt("protobuf", code, "enum", "Keyword")
        assertTokenAt("protobuf", code, "ACTIVE", "Constant")
        assertTokenAt("protobuf", code, "0", "Number")
        assertTokenAt("protobuf", code, "service", "Keyword")
        assertTokenAt("protobuf", code, "rpc", "Keyword")
        assertTokenAt("protobuf", code, "returns", "Keyword")
    }

    @Test
    fun `keywords inside strings are not highlighted`() = assertNoTokenAt(
        language = "protobuf",
        code = "string label = \"message\";",
        substring = "message",
        category = "Keyword",
    )

    @Test
    fun `protobuf map fields options and enum members are categorized`() {
        val code = """
            message Labels {
              map<string, int32> counts = 1;
              option deprecated = true;
            }

            enum FileKind {
              FILE_KIND_UNSPECIFIED = 0;
              FILE_KIND_MARKDOWN = 1;
            }
        """.trimIndent()

        assertTokenAt("protobuf", code, "message", "Keyword")
        assertTokenAt("protobuf", code, "map", "Type")
        assertTokenAt("protobuf", code, "string", "Type")
        assertTokenAt("protobuf", code, "int32", "Type")
        assertTokenAt("protobuf", code, "counts", "Property")
        assertTokenAt("protobuf", code, "option", "Keyword")
        assertTokenAt("protobuf", code, "deprecated", "Property")
        assertTokenAt("protobuf", code, "true", "Constant")
        assertTokenAt("protobuf", code, "FILE_KIND_UNSPECIFIED", "Constant")
        assertTokenAt("protobuf", code, "FILE_KIND_MARKDOWN", "Constant")
    }
}
