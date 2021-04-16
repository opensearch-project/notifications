import {
  PluginInitializerContext,
  CoreSetup,
  CoreStart,
  Plugin,
  Logger,
} from "../../../src/core/server";

import {
  notificationsDashboardsPluginSetup,
  notificationsDashboardsPluginStart,
} from "./types";
import { defineRoutes } from "./routes";

export class notificationsDashboardsPlugin
  implements
    Plugin<
      notificationsDashboardsPluginSetup,
      notificationsDashboardsPluginStart
    > {
  private readonly logger: Logger;

  constructor(initializerContext: PluginInitializerContext) {
    this.logger = initializerContext.logger.get();
  }

  public setup(core: CoreSetup) {
    this.logger.debug("notificationsDashboards: Setup");
    const router = core.http.createRouter();

    // Register server side APIs
    defineRoutes(router);

    return {};
  }

  public start(core: CoreStart) {
    this.logger.debug("notificationsDashboards: Started");
    return {};
  }

  public stop() {}
}
