# Notifications plugin for OpenSearch

## Overview
Notifications plugin for OpenSearch enables other plugins to send notifications via Email, Slack, Amazon Chime, Custom web-hook etc channels

## Highlights

1. Supports sending email with attachment (PDF, PNG, CSV, etc).
1. Supports sending multipart email with Text and HTML body with full Embedded HTML support.
1. Supports cross-plugin calls to send notifications (without re-implementing).
1. Supports tracking the number of email sent from this plugin and throttling based on it.

## Documentation

Please see our [documentation](https://opendistro.github.io/for-elasticsearch-docs/).

## Setup

1. Check out this package from version control.
1. Launch Intellij IDEA, choose **Import Project**, and select the `settings.gradle` file in the root of this package.
1. To build from the command line, set `JAVA_HOME` to point to a JDK >= 14 before running `./gradlew`.

### Setup email notification using localhost email relay/server

1. Run local email server on the machine where OpenSearch is running. e.g. for Mac, run command `sudo postfix start`
1. Verify that local email server does not require any authentication (Make sure server is listening on local port only)
1. Update the `notification.yml` configuration file according to your setup

### Setup Amazon SES and SDK

While using Amazon SES as email channel for sending mail, use below procedure for SES setup and configure environment.

1. [Setup Amazon SES account](https://docs.aws.amazon.com/ses/latest/DeveloperGuide/sign-up-for-aws.html)
1. [Verify Email address](https://docs.aws.amazon.com/ses/latest/DeveloperGuide/verify-email-addresses-procedure.html)
1. [Create IAM role](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_terms-and-concepts.html#iam-term-service-role-ec2) with [Allowing Access to Email-Sending Actions Only](https://docs.aws.amazon.com/ses/latest/DeveloperGuide/control-user-access.html) `Action` required are `SendEmail` and `SendRawEmail`.
1. While using command line [configure AWS credentials](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html) [Refer Best practices](https://docs.aws.amazon.com/general/latest/gr/aws-access-keys-best-practices.html)
1. [Use Amazon EC2 IAM role to grant permissions](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_use_switch-role-ec2.html) while using EC2

## Build

This project uses following tools

1. [Gradle](https://docs.gradle.org/current/userguide/userguide.html) build system. Gradle comes with an excellent documentation that should be your first stop when trying to figure out how to operate or modify the build.
1. OpenSearch build tools for Gradle.  These tools are idiosyncratic and don't always follow the conventions and instructions for building regular Java code using Gradle. If you encounter such a situation, the OpenSearch build tools is your best bet for figuring out what's going on.

### Building from the command line

1. `./gradlew build` builds and tests project.
1. `./gradlew run` launches a single node cluster with the `notifications` plugin installed.
1. `./gradlew run -PnumNodes=3` launches a multi-node cluster (3 nodes) with the `notifications` plugin installed.
1. `./gradlew integTest` launches a single node cluster with the `notifications` plugin installed and runs all integ tests.
1. `./gradlew integTest -PnumNodes=3` launches a multi-node cluster with the `notifications` plugin installed and runs all integ tests.
1. `./gradlew integTest -Dtests.class="*RunnerIT"` runs a single integ test class
1. `./gradlew integTest -Dtests.method="test execute * with dryrun"` runs a single integ test method
   (remember to quote the test method name if it contains spaces).

When launching a cluster using above commands, logs are placed in `notifications/build/testclusters/integTest-0/logs/`.

#### Run integration tests with Security enabled

1. Setup a local OpenSearch cluster with security plugin.
- `./gradlew build`
- `./gradlew integTest -Dtests.rest.cluster=localhost:9200 -Dtests.cluster=localhost:9200 -Dtests.clustername=opensearch-integrationtest -Dhttps=true -Duser=admin -Dpassword=admin`
- `./gradlew integTestRunner -Dtests.rest.cluster=localhost:9200 -Dtests.cluster=localhost:9200 -Dtests.clustername=opensearch-integrationtest -Dhttps=true -Duser=admin -Dpassword=admin --tests "<test name>"`

### Debugging

Sometimes it's useful to attach a debugger to either the OpenSearch cluster, or the integ tests to see what's going on. When running unit tests, hit **Debug** from the IDE's gutter to debug the tests.
You must start your debugger to listen for remote JVM before running the below commands.

To debug code running in an actual server, run:

```
./gradlew integTest -Dopensearch.debug # to start a cluster and run integ tests
```

OR

```
./gradlew run --debug-jvm # to just start a cluster that can be debugged
```

The OpenSearch server JVM will launch suspended and wait for a debugger to attach to `localhost:5005` before starting the OpenSearch server.
The IDE needs to listen for the remote JVM. If using Intellij you must set your debug-configuration to "Listen to remote JVM" and make sure "Auto Restart" is checked.
You must start your debugger to listen for remote JVM before running the commands.

To debug code running in an integ test (which exercises the server from a separate JVM), run:

```
./gradlew -Dtest.debug integTest
```

The test runner JVM will start suspended and wait for a debugger to attach to `localhost:5005` before running the tests.


### Advanced: Launching multi-node clusters locally

Sometimes you need to launch a cluster with more than one OpenSearch server process.

You can do this by running `./gradlew run -PnumNodes=<numberOfNodesYouWant>`

You can also run the integration tests against a multi-node cluster by running `./gradlew integTest -PnumNodes=<numberOfNodesYouWant>`

You can also debug a multi-node cluster, by using a combination of above multi-node and debug steps.
You must set up debugger configurations to listen on each port starting from `5005` and increasing by 1 for each node.

### Backport

See [link to backport documentation](https://github.com/opensearch-project/opensearch-plugins/blob/main/BACKPORT.md)

## Code of Conduct

See [CODE_OF_CONDUCT](CODE_OF_CONDUCT.md) for more information.

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This project is licensed under the Apache-2.0 License. See [LICENSE](LICENSE.txt) for more information.

## Notice

See [NOTICE](NOTICE.txt) for more information.

## Copyright

Copyright OpenSearch Contributors. See [NOTICE](NOTICE.txt) for details.