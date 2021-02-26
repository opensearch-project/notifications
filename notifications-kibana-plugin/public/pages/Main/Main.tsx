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

import { EuiPage, EuiPageBody, EuiPageSideBar, EuiSideNav } from '@elastic/eui';
import { CoreServicesConsumer } from '../../components/coreServices';
import { ModalProvider, ModalRoot } from '../../components/Modal';
import { BrowserServices } from '../../models/interfaces';
import { ServicesConsumer } from '../../services/services';
import { ROUTES } from '../../utils/constants';
import React from 'react';
import { Component } from 'react';
import { Redirect, Route, RouteComponentProps, Switch } from 'react-router-dom';
import { CoreStart } from '../../../../../src/core/public';
import Notifications from '../Notifications';

enum Navigation {
  Notifications = 'Notifications',
  Dashboard = 'Dashboard',
  Channels = 'Channels',
}

enum Pathname {
  Notifications = '/notifications',
  Channels = '/channels',
}

interface MainProps extends RouteComponentProps {}

export default class Main extends Component<MainProps, object> {
  render() {
    const {
      location: { pathname },
    } = this.props;

    const sideNav = [
      {
        name: Navigation.Notifications,
        id: 0,
        href: `#${Pathname.Notifications}`,
        items: [
          {
            name: Navigation.Dashboard,
            id: 1,
            href: `#${Pathname.Notifications}`,
            isSelected: pathname === Pathname.Notifications,
          },
          {
            name: Navigation.Channels,
            id: 2,
            href: `#${Pathname.Channels}`,
            isSelected: pathname === Pathname.Channels,
          },
        ],
      },
    ];
    return (
      <CoreServicesConsumer>
        {(core: CoreStart | null) =>
          core && (
            <ServicesConsumer>
              {(services: BrowserServices | null) =>
                services && (
                  <ModalProvider>
                    <ModalRoot services={services} />
                    <EuiPage>
                      {/*Hide side navigation bar when creating or editing rollup job*/}
                      {pathname != ROUTES.CREATE_CHANNEL &&
                        pathname != ROUTES.EDIT_CHANNEL && (
                          <EuiPageSideBar style={{ minWidth: 150 }}>
                            <EuiSideNav
                              style={{ width: 150 }}
                              items={sideNav}
                            />
                          </EuiPageSideBar>
                        )}
                      <EuiPageBody>
                        <Switch>
                          <Route
                            path={ROUTES.CREATE_CHANNEL}
                            render={(props: RouteComponentProps) => (
                              <div>create channel page</div>
                            )}
                          />
                          <Route
                            path={ROUTES.EDIT_CHANNEL}
                            render={(props: RouteComponentProps) => (
                              <div>edit channel page</div>
                            )}
                          />
                          <Route
                            path={ROUTES.CHANNELS}
                            render={(props: RouteComponentProps) => (
                              <div>channel page</div>
                            )}
                          />
                          <Route
                            path={ROUTES.NOTIFICATIONS}
                            render={(props: RouteComponentProps) => (
                              <Notifications
                                {...props}
                                notificationService={
                                  services.notificationService
                                }
                              />
                            )}
                          />
                          {/* <Route
                            path={ROUTES.CHANGE_POLICY}
                            render={(props: RouteComponentProps) => (
                              <ChangePolicy
                                {...props}
                                managedIndexService={
                                  services.managedIndexService
                                }
                                indexService={services.indexService}
                              />
                            )}
                          />
                          <Route
                            path={ROUTES.CREATE_CHANNEL}
                            render={(props: RouteComponentProps) => (
                              <CreatePolicy
                                {...props}
                                isEdit={false}
                                policyService={services.policyService}
                              />
                            )}
                          />
                          <Route
                            path={ROUTES.EDIT_POLICY}
                            render={(props: RouteComponentProps) => (
                              <CreatePolicy
                                {...props}
                                isEdit={true}
                                policyService={services.policyService}
                              />
                            )}
                          />
                          <Route
                            path={ROUTES.INDEX_POLICIES}
                            render={(props: RouteComponentProps) => (
                              <div style={{ padding: '25px 25px' }}>
                                <Policies
                                  {...props}
                                  policyService={services.policyService}
                                />
                              </div>
                            )}
                          />
                          <Route
                            path={ROUTES.MANAGED_INDICES}
                            render={(props: RouteComponentProps) => (
                              <div>
                                <ManagedIndices
                                  {...props}
                                  managedIndexService={
                                    services.managedIndexService
                                  }
                                />
                              </div>
                            )}
                          />
                          <Route
                            path={ROUTES.INDICES}
                            render={(props: RouteComponentProps) => (
                              <div style={{ padding: '25px 25px' }}>
                                <Indices
                                  {...props}
                                  indexService={services.indexService}
                                />
                              </div>
                            )}
                          />
                          <Route
                            path={ROUTES.ROLLUPS}
                            render={(props: RouteComponentProps) => (
                              <div style={{ padding: '25px 25px' }}>
                                <Rollups
                                  {...props}
                                  rollupService={services.rollupService}
                                />
                              </div>
                            )}
                          />
                          <Route
                            path={ROUTES.CREATE_ROLLUP}
                            render={(props: RouteComponentProps) => (
                              <div style={{ padding: '25px 25px' }}>
                                <CreateRollupForm
                                  {...props}
                                  rollupService={services.rollupService}
                                  indexService={services.indexService}
                                />
                              </div>
                            )}
                          />
                          <Route
                            path={ROUTES.EDIT_ROLLUP}
                            render={(props: RouteComponentProps) => (
                              <div style={{ padding: '25px 25px' }}>
                                <EditRollup
                                  {...props}
                                  rollupService={services.rollupService}
                                />
                              </div>
                            )}
                          />
                          <Route
                            path={ROUTES.ROLLUP_DETAILS}
                            render={(props: RouteComponentProps) => (
                              <div style={{ padding: '25px 25px' }}>
                                <RollupDetails
                                  {...props}
                                  rollupService={services.rollupService}
                                />
                              </div>
                            )}
                          /> */}
                          <Redirect from="/" to={ROUTES.NOTIFICATIONS} />
                        </Switch>
                      </EuiPageBody>
                    </EuiPage>
                  </ModalProvider>
                )
              }
            </ServicesConsumer>
          )
        }
      </CoreServicesConsumer>
    );
  }
}
