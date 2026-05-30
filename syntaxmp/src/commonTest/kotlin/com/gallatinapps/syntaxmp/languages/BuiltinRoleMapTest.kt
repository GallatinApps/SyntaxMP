package com.gallatinapps.syntaxmp.languages

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.languages.csharp.CSharpBuiltinRoles
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import com.gallatinapps.syntaxmp.languages.java.JavaBuiltinRoles
import com.gallatinapps.syntaxmp.languages.javascript.JavaScriptBuiltinRoles
import com.gallatinapps.syntaxmp.languages.lua.LuaBuiltinRoles
import com.gallatinapps.syntaxmp.languages.python.PythonBuiltinRoles
import com.gallatinapps.syntaxmp.languages.r.RBuiltinRoles
import com.gallatinapps.syntaxmp.languages.sql.SqlBuiltinRoles
import com.gallatinapps.syntaxmp.languages.typescript.TypeScriptBuiltinRoles
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
        assertBuiltinRole(LuaBuiltinRoles, "table", SyntaxRole.Variable.Namespace)
        assertBuiltinRole(LuaBuiltinRoles, "print", SyntaxRole.Function.Builtin)
        assertBuiltinRole(RBuiltinRoles, "sum", SyntaxRole.Function.Builtin)
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
        assertTokenAt("lua", "local text = string.format('%s', name)", "string", "Variable", "variable.namespace")
        assertTokenAt("r", "total <- sum(values)", "sum", "Function", "function.builtin")
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
