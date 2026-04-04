## Version 3.6.0 Release Notes

Compatible with OpenSearch and OpenSearch Dashboards version 3.6.0

### Enhancements

* Define mavenLocal ordering properly for both jars and zips ([#1152](https://github.com/opensearch-project/notifications/pull/1152))

### Bug Fixes

* Exclude transitive Bouncy Castle dependencies to resolve jar hell issue ([#1141](https://github.com/opensearch-project/notifications/pull/1141))
* Fix build failure due to Jackson version conflict ([#1151](https://github.com/opensearch-project/notifications/pull/1151))

### Infrastructure

* Allow publishing plugin zip to Maven local by removing exclusion of publishPluginZipPublicationToMavenLocal task ([#1063](https://github.com/opensearch-project/notifications/pull/1063))
* Update shadow plugin usage to replace deprecated API ([#1138](https://github.com/opensearch-project/notifications/pull/1138))

### Maintenance

* Add multi_tenancy_enabled setting and update settings prefix ([#1148](https://github.com/opensearch-project/notifications/pull/1148))
