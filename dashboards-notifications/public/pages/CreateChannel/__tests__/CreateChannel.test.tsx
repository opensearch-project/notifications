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

import { render, waitFor } from '@testing-library/react';
import React from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { MOCK_DATA } from '../../../../test/mocks/mockData';
import {
  coreServicesMock,
  notificationServiceMock,
} from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { ServicesContext } from '../../../services';
import { CreateChannel } from '../CreateChannel';

describe('<CreateChannel/> spec', () => {
  it('renders the component', () => {
    const props = { match: { params: { id: 'test' } } };
    const utils = render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <CreateChannel {...(props as RouteComponentProps<{ id: string }>)} />
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );
    utils.getByTestId('create-channel-create-button').click();
    utils.getByText('Alerting').click();
    utils.getByTestId('create-channel-send-test-message-button').click();
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('renders the component for editing slack', async () => {
    const notificationServiceMockSlack = jest.fn() as any;
    const getSlackChannel = jest.fn(
      async (queryObject: object) => MOCK_DATA.slack
    );
    const updateConfigSuccess = jest.fn(async (configId: string, config: any) =>
      Promise.resolve()
    );
    notificationServiceMockSlack.notificationService = {
      getChannel: getSlackChannel,
      updateConfig: updateConfigSuccess,
    };
    const props = {
      location: { search: '' },
      match: { params: { id: 'test' } },
    };
    const utilsSlack = render(
      <ServicesContext.Provider value={notificationServiceMockSlack}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <CreateChannel
            {...(props as RouteComponentProps<{ id: string }>)}
            edit={true}
          />
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );

    await waitFor(() => {
      expect(utilsSlack.container.firstChild).toMatchSnapshot();
      expect(getSlackChannel).toBeCalled();
    });

    utilsSlack.getByTestId('create-channel-create-button').click();
    utilsSlack.getByTestId('create-channel-send-test-message-button').click();
    await waitFor(() => {
      expect(updateConfigSuccess).toBeCalled();
    });
  });

  it('renders the component for editing chime', async () => {
    const updateConfigFailure = jest.fn(async (configId: string, config: any) =>
      Promise.reject()
    );
    const notificationServiceMockChime = jest.fn() as any;
    const getChimeChannel = jest.fn(
      async (queryObject: object) => MOCK_DATA.chime
    );
    notificationServiceMockChime.notificationService = {
      getChannel: getChimeChannel,
      updateConfig: updateConfigFailure,
    };
    const props = {
      location: { search: '' },
      match: { params: { id: 'test' } },
    };
    const utilsChime = render(
      <ServicesContext.Provider value={notificationServiceMockChime}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <CreateChannel
            {...(props as RouteComponentProps<{ id: string }>)}
            edit={true}
          />
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );

    await waitFor(() => {
      expect(utilsChime.container.firstChild).toMatchSnapshot();
      expect(getChimeChannel).toBeCalled();
    });

    utilsChime.getByTestId('create-channel-create-button').click();
    await waitFor(() => {
      expect(updateConfigFailure).toBeCalled();
    });
  });

  it('renders the component for editing email', async () => {
    const notificationServiceMockEmail = jest.fn() as any;
    const getEmailChannel = jest.fn(
      async (queryObject: object) => MOCK_DATA.email
    );
    notificationServiceMockEmail.notificationService = {
      getChannel: getEmailChannel,
    };
    const props = {
      location: { search: '' },
      match: { params: { id: 'test' } },
    };
    const utilsEmail = render(
      <ServicesContext.Provider value={notificationServiceMockEmail}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <CreateChannel
            {...(props as RouteComponentProps<{ id: string }>)}
            edit={true}
          />
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );

    await waitFor(() => {
      expect(utilsEmail.container.firstChild).toMatchSnapshot();
      expect(getEmailChannel).toBeCalled();
    });
  });

  it('renders the component for editing webhook', async () => {
    const notificationServiceMockWebhook = jest.fn() as any;
    const getWebhookChannel = jest.fn(
      async (queryObject: object) => MOCK_DATA.webhook
    );
    notificationServiceMockWebhook.notificationService = {
      getChannel: getWebhookChannel,
    };
    const props = {
      location: { search: '' },
      match: { params: { id: 'test' } },
    };
    const utilsWebhook = render(
      <ServicesContext.Provider value={notificationServiceMockWebhook}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <CreateChannel
            {...(props as RouteComponentProps<{ id: string }>)}
            edit={true}
          />
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );

    await waitFor(() => {
      expect(utilsWebhook.container.firstChild).toMatchSnapshot();
      expect(getWebhookChannel).toBeCalled();
    });
  });
});
