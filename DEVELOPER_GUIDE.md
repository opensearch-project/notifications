# Developer Guide

- [Developer Guide](#developer-guide)
  - [Forking and Cloning](#forking-and-cloning)
  - [Install Prerequisites](#install-prerequisites)
    - [JDK 21](#jdk-21)
  - [Setup](#setup)
  - [Build](#build)
    - [Building from the command line](#building-from-the-command-line)
    - [Building from the IDE](#building-from-the-ide)

### Forking and Cloning

Fork this repository on GitHub and clone it locally using `git clone`.

### Install Prerequisites

#### JDK 21

Wazuh Indexer and its plugins are built using **Java 21** at a minimum. You must have a JDK 21 installed with the environment variable `JAVA_HOME` referencing the path to your JDK installation.

Example for Linux/WSL:
`export JAVA_HOME=/usr/lib/jvm/java-21-openjdk`

You can download Java 21 from [Adoptium](https://adoptium.net/releases.html?variant=openjdk21).

### Setup

1. Clone the repository (see [Forking and Cloning](#forking-and-cloning)).
2. Ensure `JAVA_HOME` is pointing to a Java 21 JDK.
3. Launch IntelliJ IDEA, choose **Import Project**, and select the `notifications/settings.gradle` file.

### Build

This package uses the [Gradle](https://docs.gradle.org/current/userguide/userguide.html) build system. We also leverage the OpenSearch build tools for Gradle. These tools follow specific conventions for building Wazuh Indexer components. For advanced troubleshooting, refer to the [OpenSearch build tools source code](https://github.com/opensearch-project/OpenSearch/tree/main/buildSrc/src/main/groovy/org/opensearch/gradle).

#### Building from the command line

Run these commands from the `notifications/` directory.

1. `./gradlew build`: Builds and tests the plugin.
2. `./gradlew run`: Launches a single-node cluster with the `notifications` plugin installed.
3. `./gradlew integTest`: Launches a single-node cluster and runs all integration tests.
4. `./gradlew notificationsBwcCluster#mixedClusterTask`: Runs backwards compatibility tests to ensure stability across versions.

When launching a cluster, logs are stored in `notifications/build/testclusters/integTest-0/logs/` (from repository root).

#### Building from the IDE

Currently, the officially supported IDE is **IntelliJ IDEA**. Gradle tasks can be executed directly from the IntelliJ Gradle toolbar, and additional parameters can be configured via Launch Configurations.

## Code of Conduct
See [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) for more information.

## Security
See [SECURITY.md](SECURITY.md) for more information.

## License
This project is licensed under the Apache-2.0 License. See [LICENSE.txt](LICENSE.txt) for more information.

## Copyright
Copyright Wazuh, Inc. See [NOTICE.txt](NOTICE.txt) for details.