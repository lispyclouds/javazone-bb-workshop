locals {
  lambda_filename = "lambda.zip"
  lambda_name     = "no-pii"
  lambda_layer_filename = "runtime_layer.zip"
  runtimes = [
    "provided",
  ]
  architectures = [
    "x86_64",
  ]
}

resource "aws_iam_role" "no_pii" {
  name = "${local.lambda_name}-lambda"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_lambda_layer_version" "bb" {
  layer_name               = "bb"
  source_code_hash         = filebase64sha256(local.lambda_layer_filename)
  compatible_architectures = local.architectures
  compatible_runtimes      = local.runtimes
  filename                 = local.lambda_layer_filename
}

resource "aws_lambda_function" "no_pii" {
  function_name = local.lambda_name
  role          = aws_iam_role.no_pii.arn

  handler          = "dummy"
  memory_size      = "512"
  source_code_hash = filebase64sha256(local.lambda_filename)

  filename = local.lambda_filename

  runtime = local.runtimes[0]
  architectures = local.architectures

  layers = [
    aws_lambda_layer_version.bb.arn,
  ]
}
