## Version 3.9.0 Release Notes

Compatible with OpenSearch and OpenSearch Dashboards version 3.9.0

### Features

* Onboard notifications plugin to centralized resource authorization ([#1237](https://github.com/opensearch-project/notifications/pull/1237))

### Bug Fixes

* Fix Dispatchers.IO starvation deadlock in the notification send path that could permanently hang all Notifications APIs under sustained concurrent sends ([#1248](https://github.com/opensearch-project/notifications/pull/1248))
* Fix SES/SNS NoClassDefFoundError caused by Jackson 3 and HttpClient5 migration by restoring required classic Jackson 2 and HttpClient4 dependencies ([#1257](https://github.com/opensearch-project/notifications/pull/1257))
* Fix Microsoft Teams notifications to use Adaptive Card schema (v1.2) and support Logic Apps and Power Automate webhook domains ([#1107](https://github.com/opensearch-project/notifications/pull/1107))

### Infrastructure

* Fix Notifications CI dependency failures by aligning AWS SDK STS/Netty versions and adding missing Jackson 2 core dependency ([#1268](https://github.com/opensearch-project/notifications/pull/1268))
* Fix code-coverage action to correctly report missing coverage ([#1269](https://github.com/opensearch-project/notifications/pull/1269))

### Maintenance

* Rename resource sharing feature flag to the non-experimental key for security plugin graduation ([#1271](https://github.com/opensearch-project/notifications/pull/1271))
