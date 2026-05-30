package com.gallatinapps.syntaxmp.languages.terraform

import com.gallatinapps.syntaxmp.engine.role.SyntaxRole
import com.gallatinapps.syntaxmp.languages.hcl.HclConstants

internal val TerraformConstants = HclConstants

internal val TerraformDeclarationKeywords = setOf(
    "data", "locals", "module", "output", "provider", "resource", "terraform", "variable",
)

internal val TerraformDeclarationKeywordRoles = TerraformDeclarationKeywords.associateWith { name ->
    SyntaxRole.Keyword
}
