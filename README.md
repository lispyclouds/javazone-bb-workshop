# JavaZone 2023

This contains the source code for [Elegant and Maintainable scripting with Clojure and Babashka](https://2023.javazone.no/program/19a5cab3-7afd-4dc1-b60a-bea8562d3186).

This builds, tests and deploys a simple AWS Lambda.

## Deck and references

Get the deck used in the workshop having all references from [here](https://github.com/lispyclouds/javazone-bb-workshop/files/12520039/JavaZone.Babashka.Workshop.pdf).

## Requirements

- [Babashka](https://github.com/babashka/babashka#installation), latest recommended
- [Clojure CLI](https://clojure.org/guides/install_clojure), latest recommended
- [Optional] [Terraform](https://developer.hashicorp.com/terraform/tutorials/aws-get-started/install-cli), needed only for deployment, latest recommended

## Usage

- Run unit tests: `bb test`
- Clean and build the Lambda: `bb pack`. Defaults to `--arch=aarch64 --version=latest`.
- [Optional] Deploy to AWS: `bb deploy`. Defaults to `--arch=aarch64 --version=latest`.
- [Optional] Clean up: `cd deploy && terraform destroy`

## License

Copyright © Rahul De, Anupriya Johari

Distributed under the MIT License. See LICENSE.
