/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
import { EmailSenders } from '../Emails/EmailSenders';
import Notifications from '../Notifications';

enum Navigation {
  Notifications = 'Notifications',
  Dashboard = 'Dashboard',
  Channels = 'Channels',
  EmailSenders = 'Email senders',
  EmailGroups = 'Email recipient groups',
}

enum Pathname {
  Notifications = '/notifications',
  Channels = '/channels',
}

interface MainProps extends RouteComponentProps {}

export interface MainState {
  availableChannels: Partial<typeof CHANNEL_TYPE>;
  availableConfigTypes: string[]; // available backend config types
  tooltipSupport: boolean; // if true, IAM role for SNS is optional and helper text should be available
}

export const MainContext = createContext<MainState | null>(null);

export default class Main extends Component<MainProps, MainState> {
  static contextType = ServicesContext;

  constructor(props: MainProps) {
    super(props);
    this.state = {
      availableChannels: CHANNEL_TYPE,
      availableConfigTypes: [],
      tooltipSupport: false,
    };
  }

  async componentDidMount() {
    const serverFeatures = await this.context.notificationService.getServerFeatures();
    if (serverFeatures != null) {
      this.setState({
        availableChannels: serverFeatures.availableChannels,
        availableConfigTypes: serverFeatures.availableConfigTypes,
        tooltipSupport: serverFeatures.tooltipSupport,
      });
    } else {
      // Feature API call failed, allow all configs to avoid UI breaking.
      // User requests will still be validated by backend.
      this.setState({
        availableChannels: CHANNEL_TYPE,
        availableConfigTypes: [
          'slack',
          'chime',
          'webhook',
          'email',
          'sns',
          'smtp_account',
          'ses_account',
          'email_group',
        ],
        tooltipSupport: serverFeatures.tooltipSupport,
      });
    }
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
            name: Navigation.EmailSenders,
            id: 3,
            href: `#${ROUTES.EMAIL_SENDERS}`,
            isSelected: pathname === ROUTES.EMAIL_SENDERS,
          },
          {
            name: Navigation.EmailGroups,
            id: 4,
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
                            <EuiPageSideBar style={{ minWidth: 155 }}>
                              <EuiSideNav
                                style={{ width: 155 }}
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
                              path={ROUTES.EMAIL_SENDERS}
                              render={(props: RouteComponentProps) => (
                                <EmailSenders {...props} />
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
