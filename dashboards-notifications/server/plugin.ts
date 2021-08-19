import {
  PluginInitializerContext,
  CoreSetup,
  CoreStart,
  Plugin,
  Logger,
  ILegacyClusterClient,
} from "../../../src/core/server";

import {
  notificationsDashboardsPluginSetup,
  notificationsDashboardsPluginStart,
} from "./types";
import { defineRoutes } from "./routes";
import { NotificationsPlugin } from "./clusters/notificationsPlugin";

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

    const notificationsClient: ILegacyClusterClient = core.opensearch.legacy.createClient(
      'opensearch_notifications',
      {
        plugins: [NotificationsPlugin],
      }
    );

    core.http.registerRouteHandlerContext('notificationsContext', (context, request) => {
      return {
        logger: this.logger,
        notificationsClient,
      };
    });

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
