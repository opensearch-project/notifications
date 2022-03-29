# OpenSearch Dashboards Notifications

Dashboards Notifications plugin provides an interface that helps users to manage and view notifications using the OpenSearch Notifications plugin.

## Documentation

Please see our technical [documentation](https://opensearch.org/docs/) to learn more about its features.

## Setup

1. Download OpenSearch for the version that matches the [OpenSearch Dashboards version specified in package.json](./package.json#L7).
1. Download the OpenSearch Dashboards source code for the [version specified in package.json](./package.json#L7) you want to set up.

1. Change your node version to the version specified in `.node-version` inside the OpenSearch Dashboards root directory.
1. Create a `plugins` directory inside the OpenSearch Dashboards source code directory, if `plugins` directory doesn't exist.
1. Check out this package from version control into the `plugins` directory.
   ```
   git clone git@github.com:opensearch-project/notifications.git plugins --no-checkout
   cd plugins
   echo 'dashboards-notifications/*' >> .git/info/sparse-checkout
   git config core.sparseCheckout true
   git checkout dev
   ```
1. Run `yarn osd bootstrap` inside `OpenSearch-Dashboards/plugins/dashboards-notifications`.

Ultimately, your directory structure should look like this:

```md
.
├── OpenSearch Dashboards
│ └── plugins
│ └── dashboards-notifications
```

## Build

To build the plugin's distributable zip simply run `yarn build`.

Example output: `./build/notificationsDashboards*.zip`

## Run

- `yarn start`

  Starts OpenSearch Dashboards and includes this plugin. OpenSearch Dashboards will be available on `localhost:5601`.

- `yarn test`

  Runs the plugin unit tests.

## Contributing to OpenSearch Dashboards Notifications

We welcome you to get involved in development, documentation, testing the Notifications plugin. See our [CONTRIBUTING.md](./../CONTRIBUTING.md) and join in.

## Bugs, Enhancements or Questions

Please file an issue to report any bugs you may find, enhancements you may need or questions you may have [here](https://github.com/opensearch-project/notifications/issues).

## License

This code is licensed under the Apache 2.0 License.

## Copyright

Copyright OpenSearch Contributors. See [NOTICE](../NOTICE.txt) for details.
