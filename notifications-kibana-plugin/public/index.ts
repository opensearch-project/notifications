import "./index.scss";

import { opendistroNotificationsKibanaPlugin } from "./plugin";

// This exports static code and TypeScript types,
// as well as, Kibana Platform `plugin()` initializer.
export function plugin() {
  return new opendistroNotificationsKibanaPlugin();
}
export {
  opendistroNotificationsKibanaPluginSetup,
  opendistroNotificationsKibanaPluginStart,
} from "./types";
