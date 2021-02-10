import { NavigationPublicPluginStart } from "../../../src/plugins/navigation/public";

export interface opendistroNotificationsKibanaPluginSetup {
  getGreeting: () => string;
}
// eslint-disable-next-line @typescript-eslint/no-empty-interface
export interface opendistroNotificationsKibanaPluginStart {}

export interface AppPluginStartDependencies {
  navigation: NavigationPublicPluginStart;
}
