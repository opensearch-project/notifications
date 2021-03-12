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
import { Channels } from '../Channels/Channels';
import { CreateChannel } from '../CreateChannel/CreateChannel';
import { ChannelDetails } from '../Channels/ChannelDetails';
import { EmailGroups } from '../Emails/EmailGroups';
import { CreateSender } from '../Emails/CreateSender';
import { CreateRecipientGroup } from '../Emails/CreateRecipientGroup';

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
                  <ModalProvider>
                    <ModalRoot services={services} />
                    <EuiPage>
                      {/*Hide side navigation bar when creating or editing rollup job*/}
                      {pathname !== ROUTES.CREATE_CHANNEL &&
                        !pathname.startsWith(ROUTES.EDIT_CHANNEL) &&
                        !pathname.startsWith(ROUTES.CHANNEL_DETAILS) &&
                        pathname !== ROUTES.CREATE_SENDER &&
                        !pathname.startsWith(ROUTES.EDIT_SENDER) &&
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
                            render={(props: RouteComponentProps<{id: string}>) => (
                              <CreateChannel {...props} edit={true} />
                            )}
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
                              <Channels {...props} />
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
                            path={ROUTES.CREATE_RECIPIENT_GROUP}
                            render={(props: RouteComponentProps) => (
                              <CreateRecipientGroup {...props} />
                            )}
                          />
                          <Route
                            path={`${ROUTES.EDIT_RECIPIENT_GROUP}/:name`}
                            render={(props: RouteComponentProps) => (
                              <CreateRecipientGroup {...props} edit={true} />
                            )}
                          />
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
