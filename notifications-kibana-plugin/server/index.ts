import { PluginInitializerContext } from "../../../src/core/server";
import { opendistroNotificationsKibanaPlugin } from "./plugin";

//  This exports static code and TypeScript types,
//  as well as, Kibana Platform `plugin()` initializer.

export function plugin(initializerContext: PluginInitializerContext) {
  return new opendistroNotificationsKibanaPlugin(initializerContext);
}

export {
  opendistroNotificationsKibanaPluginSetup,
  opendistroNotificationsKibanaPluginStart,
} from "./types";
