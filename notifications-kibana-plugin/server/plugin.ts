import {
  PluginInitializerContext,
  CoreSetup,
  CoreStart,
  Plugin,
  Logger,
} from "../../../src/core/server";

import {
  opendistroNotificationsKibanaPluginSetup,
  opendistroNotificationsKibanaPluginStart,
} from "./types";
import { defineRoutes } from "./routes";

export class opendistroNotificationsKibanaPlugin
  implements
    Plugin<
      opendistroNotificationsKibanaPluginSetup,
      opendistroNotificationsKibanaPluginStart
    > {
  private readonly logger: Logger;

  constructor(initializerContext: PluginInitializerContext) {
    this.logger = initializerContext.logger.get();
  }

  public setup(core: CoreSetup) {
    this.logger.debug("opendistroNotificationsKibana: Setup");
    const router = core.http.createRouter();

    // Register server side APIs
    defineRoutes(router);

    return {};
  }

  public start(core: CoreStart) {
    this.logger.debug("opendistroNotificationsKibana: Started");
    return {};
  }

  public stop() {}
}
