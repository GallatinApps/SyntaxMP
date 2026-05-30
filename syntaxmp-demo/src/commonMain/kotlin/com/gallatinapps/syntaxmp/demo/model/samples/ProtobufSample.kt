package com.gallatinapps.syntaxmp.demo.model.samples

internal val ProtobufSample = """
    syntax = "proto3";

    package hashjot.browser.v1;

    option java_multiple_files = true;
    option swift_prefix = "HJ";

    message LibraryFile {
      string id = 1;
      string path = 2;
      string title = 3;
      FileKind kind = 4;
      repeated string tags = 5;
      map<string, string> metadata = 6;

      oneof preview {
        string text_preview = 7;
        bytes thumbnail = 8;
      }
    }

    enum FileKind {
      FILE_KIND_UNSPECIFIED = 0;
      FILE_KIND_MARKDOWN = 1;
      FILE_KIND_IMAGE = 2;
    }

    service LibraryIndex {
      rpc ListFiles(ListFilesRequest) returns (ListFilesResponse);
      rpc WatchChanges(WatchChangesRequest) returns (stream LibraryFile);
    }
""".trimIndent()
