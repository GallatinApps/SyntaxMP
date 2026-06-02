package com.gallatinapps.syntaxmp.primitives

import com.gallatinapps.syntaxmp.builtins.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import kotlin.test.Test

class QualifiedNameScanningTest {
    @Test
    fun `c like qualified declarations and imports use namespace scopes`() {
        assertTokenAt(
            language = "kotlin",
            code = """
                package com.gallatinapps.notekit
                import org.koin.compose.viewmodel.koinViewModel
            """.trimIndent(),
            substring = "com.gallatinapps.notekit",
            category = "Variable",
            scope = "variable.namespace",
        )
        assertTokenAt(
            language = "kotlin",
            code = "import org.koin.compose.viewmodel.koinViewModel",
            substring = "org.koin.compose.viewmodel.koinViewModel",
            category = "Variable",
            scope = "variable.namespace",
        )
        assertTokenAt(
            language = "java",
            code = "import static java.lang.Math.max;",
            substring = "java.lang.Math.max",
            category = "Variable",
            scope = "variable.namespace",
        )
    }

    @Test
    fun `namespace style qualified names use namespace scopes`() {
        assertTokenAt(
            language = "csharp",
            code = """
                using static System.Math;
                namespace NoteKit.Core;
            """.trimIndent(),
            substring = "System.Math",
            category = "Variable",
            scope = "variable.namespace",
        )
        assertTokenAt(
            language = "csharp",
            code = "namespace NoteKit.Core;",
            substring = "NoteKit.Core",
            category = "Variable",
            scope = "variable.namespace",
        )
        assertTokenAt(
            language = "cpp",
            code = "using namespace std; namespace app::core {}",
            substring = "app::core",
            category = "Variable",
            scope = "variable.namespace",
        )
        assertTokenAt(
            language = "swift",
            code = "@testable import struct Foundation.Date",
            substring = "Foundation.Date",
            category = "Variable",
            scope = "variable.namespace",
        )
        assertTokenAt(
            language = "rust",
            code = "pub use crate::collections::HashMap;",
            substring = "crate::collections::HashMap",
            category = "Variable",
            scope = "variable.namespace",
        )
    }

    @Test
    fun `script like qualified imports use namespace scopes`() {
        assertTokenAt(
            language = "python",
            code = "from collections.abc import Iterable",
            substring = "collections.abc",
            category = "Variable",
            scope = "variable.namespace",
        )
        assertTokenAt(
            language = "python",
            code = "import importlib.metadata as metadata",
            substring = "importlib.metadata",
            category = "Variable",
            scope = "variable.namespace",
        )
        assertTokenAt(
            language = "php",
            code = """use Vendor\Package\Thing as Alias;""",
            substring = """Vendor\Package\Thing""",
            category = "Variable",
            scope = "variable.namespace",
        )
    }

    @Test
    fun `single declaration name forms use namespace scopes`() {
        assertTokenAt(
            language = "dart",
            code = "library notekit.core;",
            substring = "notekit.core",
            category = "Variable",
            scope = "variable.namespace",
        )
        assertTokenAt(
            language = "go",
            code = "package main",
            substring = "main",
            category = "Variable",
            scope = "variable.namespace",
        )
        assertTokenAt(
            language = "protobuf",
            code = "package notekit.notes.v1;",
            substring = "notekit.notes.v1",
            category = "Variable",
            scope = "variable.namespace",
        )
    }

    @Test
    fun `qualified-name contexts stop at aliases and semicolon boundaries`() {
        assertTokenAt(
            language = "kotlin",
            code = "import bench.generated.Widget as WidgetAlias; import bench.generated.Next",
            substring = "bench.generated.Widget",
            category = "Variable",
            scope = "variable.namespace",
        )
        assertNoTokenAt(
            language = "kotlin",
            code = "import bench.generated.Widget as WidgetAlias; import bench.generated.Next",
            substring = "WidgetAlias",
            category = "Variable",
            scope = "variable.namespace",
        )
        assertTokenAt(
            language = "kotlin",
            code = "import bench.generated.Widget as WidgetAlias; import bench.generated.Next",
            substring = "bench.generated.Next",
            category = "Variable",
            scope = "variable.namespace",
        )
        assertTokenAt(
            language = "python",
            code = "from collections.abc import Iterable as IterableAlias",
            substring = "collections.abc",
            category = "Variable",
            scope = "variable.namespace",
        )
        assertNoTokenAt(
            language = "python",
            code = "from collections.abc import Iterable as IterableAlias",
            substring = "IterableAlias",
            category = "Variable",
            scope = "variable.namespace",
        )
    }
}
