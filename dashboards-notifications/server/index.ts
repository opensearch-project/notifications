import { PluginInitializerContext } from "../../../src/core/server";
import { opendistroNotificationsOpenSearchDashboardsPlugin } from "./plugin";

//  This exports static code and TypeScript types,
//  as well as, OpenSearch Dashboards Platform `plugin()` initializer.

export function plugin(initializerContext: PluginInitializerContext) {
  return new opendistroNotificationsOpenSearchDashboardsPlugin(initializerContext);
}

export {
  opendistroNotificationsOpenSearchDashboardsPluginSetup,
  opendistroNotificationsOpenSearchDashboardsPluginStart,
} from "./types";
