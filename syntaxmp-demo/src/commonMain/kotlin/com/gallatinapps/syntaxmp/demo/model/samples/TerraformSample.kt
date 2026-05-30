package com.gallatinapps.syntaxmp.demo.model.samples

internal val TerraformSample = """
    variable "environment" {
      type        = string
      description = "Deployment environment label"
      default     = "local"

      validation {
        condition     = contains(["local", "preview", "prod"], var.environment)
        error_message = "Use a known environment."
      }
    }

    locals {
      app_name = "syntaxmp-demo"
      labels = {
        owner = "docs"
        env   = var.environment
      }
      routes = {
        getting_started = "/#/getting-started"
        kotlin          = "/#/lang/kotlin"
      }
    }

    resource "local_file" "demo_manifest" {
      filename = "${'$'}{path.module}/build/${'$'}{local.app_name}.json"
      content = jsonencode({
        labels = local.labels
        routes = [for name, path in local.routes : "${'$'}{name}:${'$'}{path}"]
      })
    }
""".trimIndent()
