package com.gallatinapps.syntaxmp.builtins

import com.gallatinapps.syntaxmp.role.SyntaxRole
import com.gallatinapps.syntaxmp.builtins.csharp.CSharpBuiltinRoles
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import com.gallatinapps.syntaxmp.builtins.java.JavaBuiltinRoles
import com.gallatinapps.syntaxmp.builtins.javascript.JavaScriptBuiltinRoles
import com.gallatinapps.syntaxmp.builtins.python.PythonBuiltinRoles
import com.gallatinapps.syntaxmp.builtins.sql.SqlBuiltinRoles
import com.gallatinapps.syntaxmp.builtins.typescript.TypeScriptBuiltinRoles
import kotlin.test.Test
import kotlin.test.assertEquals

class BuiltinRoleMapTest {
    @Test
    fun builtinRoleMapsPreserveRepresentativeSemanticBuckets() {
        assertBuiltinRole(JavaScriptBuiltinRoles, "console", SyntaxRole.Variable.Namespace)
        assertBuiltinRole(JavaScriptBuiltinRoles, "JSON", SyntaxRole.Variable.Namespace)
        assertBuiltinRole(JavaScriptBuiltinRoles, "Array", SyntaxRole.Type)
        assertBuiltinRole(TypeScriptBuiltinRoles, "console", SyntaxRole.Variable.Namespace)
        assertBuiltinRole(TypeScriptBuiltinRoles, "Promise", SyntaxRole.Type)

        assertBuiltinRole(CSharpBuiltinRoles, "Math", SyntaxRole.Variable.Namespace)
        assertBuiltinRole(JavaBuiltinRoles, "Math", SyntaxRole.Variable.Namespace)
        assertBuiltinRole(JavaBuiltinRoles, "String", SyntaxRole.Type)

        assertBuiltinRole(PythonBuiltinRoles, "dict", SyntaxRole.Type)
        assertBuiltinRole(PythonBuiltinRoles, "len", SyntaxRole.Function.Builtin)
        assertBuiltinRole(SqlBuiltinRoles, "count", SyntaxRole.Function.Builtin)
    }

    @Test
    fun tokenizerEmitsRepresentativeBuiltinRoles() {
        val javascript = "javascript"
        val python = "python"

        assertTokenAt(javascript, "console.log(JSON.stringify(Array.of(1)))", "console", "Variable", "variable.namespace")
        assertTokenAt(javascript, "console.log(JSON.stringify(Array.of(1)))", "JSON", "Variable", "variable.namespace")
        assertTokenAt(javascript, "console.log(JSON.stringify(Array.of(1)))", "Array", "Type")
        assertTokenAt(python, "value = dict(name=str(len(items)))", "dict", "Type")
        assertTokenAt(python, "value = dict(name=str(len(items)))", "len", "Function", "function.builtin")
        assertTokenAt("sql", "select count(*) from notes", "count", "Function", "function.builtin")
    }
}

private fun assertBuiltinRole(
    roles: Map<String, SyntaxRole>,
    lexeme: String,
    role: SyntaxRole,
) {
    assertEquals(role, roles[lexeme], lexeme)
}
