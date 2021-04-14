import { PluginInitializerContext } from "../../../src/core/server";
import { notificationsDashboardsPlugin } from "./plugin";

//  This exports static code and TypeScript types,
//  as well as, OpenSearch Dashboards Platform `plugin()` initializer.

export function plugin(initializerContext: PluginInitializerContext) {
  return new notificationsDashboardsPlugin(initializerContext);
}

export {
  notificationsDashboardsPluginSetup,
  notificationsDashboardsPluginStart,
} from "./types";
