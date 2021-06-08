/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

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
