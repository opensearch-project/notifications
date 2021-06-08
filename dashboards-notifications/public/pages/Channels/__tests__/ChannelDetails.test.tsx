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

import { fireEvent, render, waitFor } from '@testing-library/react';
import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import React from 'react';
import ReactDOM from 'react-dom';
import { act } from 'react-dom/test-utils';
import { RouteComponentProps } from 'react-router-dom';
import { MOCK_CONFIG } from '../../../../test/mocks/mockData';
import {
  coreServicesMock,
  notificationServiceMock,
} from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { ServicesContext } from '../../../services';
import { ChannelDetails } from '../components/details/ChannelDetails';

describe('<ChannelDetails/> spec', () => {
  configure({ adapter: new Adapter() });

  it('renders the component', () => {
    const props = { match: { params: { id: 'test' } } };
    const utils = render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <ChannelDetails {...(props as RouteComponentProps<{ id: string }>)} />
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('clicks mute or unmute button', () => {
    const props = { match: { params: { id: 'test' } } };
    const utils = render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <ChannelDetails {...(props as RouteComponentProps<{ id: string }>)} />
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );

    const button = utils.getByText('ute channel', { exact: false });
    fireEvent.click(button);
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('renders a specific channel', async () => {
    const props = { match: { params: { id: 'test' } } };
    const notificationServiceMock = jest.fn() as any;
    notificationServiceMock.notificationService = {
      getChannel: async (id: string) => {
        return MOCK_CONFIG.chime;
      },
    };
    let container = document.createElement('div');

    act(() => {
      ReactDOM.render(
        <ServicesContext.Provider value={notificationServiceMock}>
          <CoreServicesContext.Provider value={coreServicesMock}>
            <ChannelDetails
              {...(props as RouteComponentProps<{ id: string }>)}
            />
          </CoreServicesContext.Provider>
        </ServicesContext.Provider>,
        container
      );
    });
    await waitFor(() => {
      expect(container).toMatchSnapshot();
    });
  });

  it('clicks mute button with channel', async () => {
    const props = { match: { params: { id: 'test' } } };
    const notificationServiceMock = jest.fn() as any;
    notificationServiceMock.notificationService = {
      getChannel: async (id: string) => {
        return MOCK_CONFIG.chime;
      },
      updateConfig: jest.fn(),
    };

    const utils = render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <ChannelDetails {...(props as RouteComponentProps<{ id: string }>)} />
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );

    await waitFor(() => {
      utils.getByTestId('channel-details-mute-button').click();
    });
  });

  it('clicks unmute button with channel', async () => {
    const props = { match: { params: { id: 'test' } } };
    const notificationServiceMock = jest.fn() as any;
    const updateConfig = jest.fn(async () => Promise.resolve());
    notificationServiceMock.notificationService = {
      getChannel: async (id: string) => {
        return MOCK_CONFIG.slack;
      },
      updateConfig,
    };

    const utils = render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <ChannelDetails {...(props as RouteComponentProps<{ id: string }>)} />
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );

    await waitFor(() => {
      utils.getByTestId('channel-details-mute-button').click();
    });
  });
});
