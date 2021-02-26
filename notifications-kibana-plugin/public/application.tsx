/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import React from 'react';
import ReactDOM from 'react-dom';
import { HashRouter as Router, Route } from 'react-router-dom';
import { AppMountParameters, CoreStart } from '../../../src/core/public';
import { OpendistroNotificationsKibanaApp } from './components/app';
import { CoreServicesContext } from './components/coreServices';
import Main from './pages/Main';
import { NotificationService } from './services';
import { ServicesContext } from './services/services';
import { AppPluginStartDependencies } from './types';

export const renderApp = (coreStart: CoreStart, params: AppMountParameters) => {
  const http = coreStart.http;
  const notificationService = new NotificationService(http);
  const services = { notificationService };

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
