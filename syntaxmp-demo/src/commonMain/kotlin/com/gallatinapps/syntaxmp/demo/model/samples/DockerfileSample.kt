package com.gallatinapps.syntaxmp.demo.model.samples

internal val DockerfileSample = """
    # Local image for serving a static Wasm distribution.
    ARG JRE_IMAGE=eclipse-temurin:21-jre-alpine
    FROM ${'$'}JRE_IMAGE AS runtime

    ENV APP_HOME=/opt/syntaxmp-demo
    WORKDIR ${'$'}APP_HOME

    RUN addgroup -S app && adduser -S app -G app
    COPY --chown=app:app build/distributions/syntaxmp-demo ./public

    RUN <<'EOF'
    set -eu
    find ./public -maxdepth 1 -type f -name '*.wasm' -print
    EOF

    USER app
    EXPOSE 8080
    HEALTHCHECK --interval=30s CMD wget -qO- http://127.0.0.1:8080/ || exit 1
    CMD ["java", "-jar", "demo-server.jar"]
""".trimIndent()
