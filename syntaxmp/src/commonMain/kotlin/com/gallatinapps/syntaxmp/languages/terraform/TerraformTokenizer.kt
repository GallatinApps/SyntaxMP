package com.gallatinapps.syntaxmp.languages.terraform

import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeRequest
import com.gallatinapps.syntaxmp.engine.tokenizer.SyntaxTokenizeResult
import com.gallatinapps.syntaxmp.languages.hcl.HclScanner

internal object TerraformTokenizer {
    fun tokenize(request: SyntaxTokenizeRequest): SyntaxTokenizeResult =
        SyntaxTokenizeResult(
            HclScanner(
                code = request.code,
                language = request.languageId,
                constants = TerraformConstants,
                declarationKeywordRoles = TerraformDeclarationKeywordRoles,
            ).scan(),
        )
}
