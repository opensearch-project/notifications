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

import { EuiPage, EuiPageBody, EuiPageSideBar, EuiSideNav } from '@elastic/eui';
import React, { Component, createContext } from 'react';
import { Redirect, Route, RouteComponentProps, Switch } from 'react-router-dom';
import { CoreStart } from '../../../../../src/core/public';
import { CoreServicesConsumer } from '../../components/coreServices';
import { ModalProvider, ModalRoot } from '../../components/Modal';
import { BrowserServices } from '../../models/interfaces';
import { ServicesConsumer, ServicesContext } from '../../services/services';
import { CHANNEL_TYPE, ROUTES } from '../../utils/constants';
import { Channels } from '../Channels/Channels';
import { ChannelDetails } from '../Channels/components/details/ChannelDetails';
import { CreateChannel } from '../CreateChannel/CreateChannel';
import { CreateRecipientGroup } from '../Emails/CreateRecipientGroup';
import { CreateSender } from '../Emails/CreateSender';
import { CreateSESSender } from '../Emails/CreateSESSender';
import { EmailGroups } from '../Emails/EmailGroups';
import Notifications from '../Notifications';

enum Navigation {
  Notifications = 'Notifications',
  Dashboard = 'Dashboard',
  Channels = 'Channels',
  EmailGroups = 'Email groups',
}

enum Pathname {
  Notifications = '/notifications',
  Channels = '/channels',
}

interface MainProps extends RouteComponentProps {}

export interface MainState {
  availableFeatures: Partial<typeof CHANNEL_TYPE>;
  tooltipSupport: boolean; // if true, IAM role for SNS is optional and helper text should be available
}

export const MainContext = createContext<MainState | null>(null);

export default class Main extends Component<MainProps, MainState> {
  static contextType = ServicesContext;

  constructor(props: MainProps) {
    super(props);
    this.state = {
      availableFeatures: CHANNEL_TYPE,
      tooltipSupport: false,
    };
  }

  async componentDidMount() {
    const serverFeatures = await this.context.notificationService.getServerFeatures();
    if (serverFeatures != null)
      this.setState({
        availableFeatures: serverFeatures.availableFeatures,
        tooltipSupport: serverFeatures.tooltipSupport,
      });
  }

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
          {
            name: Navigation.EmailGroups,
            id: 3,
            href: `#${ROUTES.EMAIL_GROUPS}`,
            isSelected: pathname === ROUTES.EMAIL_GROUPS,
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
                  <MainContext.Provider value={this.state}>
                    <ModalProvider>
                      <ModalRoot services={services} />
                      <EuiPage>
                        {pathname !== ROUTES.CREATE_CHANNEL &&
                          !pathname.startsWith(ROUTES.EDIT_CHANNEL) &&
                          !pathname.startsWith(ROUTES.CHANNEL_DETAILS) &&
                          pathname !== ROUTES.CREATE_SENDER &&
                          !pathname.startsWith(ROUTES.EDIT_SENDER) &&
                          pathname !== ROUTES.CREATE_SES_SENDER &&
                          !pathname.startsWith(ROUTES.EDIT_SES_SENDER) &&
                          pathname !== ROUTES.CREATE_RECIPIENT_GROUP &&
                          !pathname.startsWith(ROUTES.EDIT_RECIPIENT_GROUP) && (
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
                                <CreateChannel {...props} />
                              )}
                            />
                            <Route
                              path={`${ROUTES.EDIT_CHANNEL}/:id`}
                              render={(
                                props: RouteComponentProps<{ id: string }>
                              ) => <CreateChannel {...props} edit={true} />}
                            />
                            <Route
                              path={`${ROUTES.CHANNEL_DETAILS}/:id`}
                              render={(
                                props: RouteComponentProps<{ id: string }>
                              ) => <ChannelDetails {...props} />}
                            />
                            <Route
                              path={ROUTES.CHANNELS}
                              render={(props: RouteComponentProps) => (
                                <Channels
                                  {...props}
                                  notificationService={
                                    services.notificationService
                                  }
                                />
                              )}
                            />
                            <Route
                              path={ROUTES.NOTIFICATIONS}
                              render={(props: RouteComponentProps) => (
                                <Notifications
                                  {...props}
                                  services={services}
                                  mainProps={this.state}
                                />
                              )}
                            />
                            <Route
                              path={ROUTES.EMAIL_GROUPS}
                              render={(props: RouteComponentProps) => (
                                <EmailGroups {...props} />
                              )}
                            />
                            <Route
                              path={ROUTES.CREATE_SENDER}
                              render={(props: RouteComponentProps) => (
                                <CreateSender {...props} />
                              )}
                            />
                            <Route
                              path={`${ROUTES.EDIT_SENDER}/:id`}
                              render={(props: RouteComponentProps) => (
                                <CreateSender {...props} edit={true} />
                              )}
                            />
                            <Route
                              path={ROUTES.CREATE_SES_SENDER}
                              render={(props: RouteComponentProps) => (
                                <CreateSESSender {...props} />
                              )}
                            />
                            <Route
                              path={`${ROUTES.EDIT_SES_SENDER}/:id`}
                              render={(props: RouteComponentProps) => (
                                <CreateSESSender {...props} edit={true} />
                              )}
                            />
                            <Route
                              path={ROUTES.CREATE_RECIPIENT_GROUP}
                              render={(props: RouteComponentProps) => (
                                <CreateRecipientGroup {...props} />
                              )}
                            />
                            <Route
                              path={`${ROUTES.EDIT_RECIPIENT_GROUP}/:id`}
                              render={(props: RouteComponentProps) => (
                                <CreateRecipientGroup {...props} edit={true} />
                              )}
                            />
                            <Redirect from="/" to={ROUTES.NOTIFICATIONS} />
                          </Switch>
                        </EuiPageBody>
                      </EuiPage>
                    </ModalProvider>
                  </MainContext.Provider>
                )
              }
            </ServicesConsumer>
          )
        }
      </CoreServicesConsumer>
    );
  }
}
