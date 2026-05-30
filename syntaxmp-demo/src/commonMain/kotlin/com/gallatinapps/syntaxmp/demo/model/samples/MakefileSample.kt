package com.gallatinapps.syntaxmp.demo.model.samples

internal val MakefileSample = """
    |GRADLE := ./gradlew -p SyntaxMP
    |WEB_TASK := :syntaxmp-demo:wasmJsBrowserDevelopmentRun
    |BROWSER ?= open
    |
    |ifeq (${'$'}(CI),true)
    |  GRADLE_FLAGS += --no-daemon
    |endif
    |
    |.PHONY: demo test dist clean
    |
    |demo:
    |${'\t'}${'$'}(GRADLE) ${'$'}(GRADLE_FLAGS) ${'$'}(WEB_TASK)
    |${'\t'}${'$'}(BROWSER) http://localhost:8080/
    |
    |test:
    |${'\t'}${'$'}(GRADLE) ${'$'}(GRADLE_FLAGS) :syntaxmp:jvmTest :syntaxmp-demo:wasmJsBrowserTest
    |
    |dist:
    |${'\t'}${'$'}(GRADLE) :syntaxmp-demo:wasmJsBrowserDistribution
    |
    |clean:
    |${'\t'}rm -rf build SyntaxMP/build SyntaxMP/syntaxmp-demo/build
""".trimMargin()
