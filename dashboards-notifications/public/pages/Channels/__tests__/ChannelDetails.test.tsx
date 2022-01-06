/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { fireEvent, render, waitFor } from '@testing-library/react';
import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import React from 'react';
import ReactDOM from 'react-dom';
import { act } from 'react-dom/test-utils';
import { RouteComponentProps } from 'react-router-dom';
import { MOCK_DATA } from '../../../../test/mocks/mockData';
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

  it('renders a specific channel', async () => {
    const props = { match: { params: { id: 'test' } } };
    const notificationServiceMock = jest.fn() as any;
    notificationServiceMock.notificationService = {
      getChannel: async (id: string) => {
        return MOCK_DATA.chime;
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

  it('handles a non-existing channel', async () => {
    const props = { match: { params: { id: 'test' } } };
    const notificationServiceMock = jest.fn() as any;
    notificationServiceMock.notificationService = {
      getChannel: async (id: string) => {
        throw "non existing channel"
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
        return MOCK_DATA.chime;
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
        return MOCK_DATA.slack;
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
