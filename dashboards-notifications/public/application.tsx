/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import React from 'react';
import ReactDOM from 'react-dom';
import { HashRouter as Router, Route } from 'react-router-dom';
import { AppMountParameters, CoreStart } from '../../../src/core/public';
import { CoreServicesContext } from './components/coreServices';
import Main from './pages/Main';
import { EventService, NotificationService } from './services';
import { ServicesContext } from './services/services';

export const renderApp = (coreStart: CoreStart, params: AppMountParameters) => {
  const http = coreStart.http;
  const notificationService = new NotificationService(http);
  const eventService = new EventService(http);
  const services = { notificationService, eventService };

  ReactDOM.render(
    <Router>
      <Route
        render={(props) => (
          <ServicesContext.Provider value={services}>
            <CoreServicesContext.Provider value={coreStart}>
              <Main {...props} />
            </CoreServicesContext.Provider>
          </ServicesContext.Provider>
        )}
      />
    </Router>,
    params.element
  );

  return () => ReactDOM.unmountComponentAtNode(params.element);
};
