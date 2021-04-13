import {
  PluginInitializerContext,
  CoreSetup,
  CoreStart,
  Plugin,
  Logger,
} from "../../../src/core/server";

import {
  opendistroNotificationsOpenSearchDashboardsPluginSetup,
  opendistroNotificationsOpenSearchDashboardsPluginStart,
} from "./types";
import { defineRoutes } from "./routes";

export class opendistroNotificationsOpenSearchDashboardsPlugin
  implements
    Plugin<
      opendistroNotificationsOpenSearchDashboardsPluginSetup,
      opendistroNotificationsOpenSearchDashboardsPluginStart
    > {
  private readonly logger: Logger;

  constructor(initializerContext: PluginInitializerContext) {
    this.logger = initializerContext.logger.get();
  }

  public setup(core: CoreSetup) {
    this.logger.debug("opendistroNotificationsOpenSearchDashboards: Setup");
    const router = core.http.createRouter();

    // Register server side APIs
    defineRoutes(router);

    return {};
  }

  public start(core: CoreStart) {
    this.logger.debug("opendistroNotificationsOpenSearchDashboards: Started");
    return {};
  }

  public stop() {}
}
