/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { fireEvent, render, waitFor } from '@testing-library/react';
import React from 'react';
import { MOCK_DATA } from '../../../../test/mocks/mockData';
import { routerComponentPropsMock } from '../../../../test/mocks/routerPropsMock';
import {
  coreServicesMock,
  mainStateMock,
} from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { MainContext } from '../../Main/Main';
import { Channels } from '../Channels';

describe('<Channels/> spec', () => {
  it('renders the empty component', () => {
    const notificationServiceMock = jest.fn() as any;
    const getChannels = jest.fn(async (queryObject: object) => []);
    notificationServiceMock.notificationService = { getChannels };
    const utils = render(
      <MainContext.Provider value={mainStateMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <Channels
            {...routerComponentPropsMock}
            notificationService={notificationServiceMock}
          />
        </CoreServicesContext.Provider>
      </MainContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('renders the component', async () => {
    const getChannels = jest.fn(
      async (queryObject: object) => MOCK_DATA.channels
    );
    const notificationService = jest.fn() as any;
    notificationService.getChannels = getChannels;
    const utils = render(
      <MainContext.Provider value={mainStateMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <Channels
            {...routerComponentPropsMock}
            notificationService={notificationService}
          />
        </CoreServicesContext.Provider>
      </MainContext.Provider>
    );

    await waitFor(() => expect(getChannels).toBeCalled());

    const input = utils.getByPlaceholderText('Search');
    fireEvent.change(input, { target: { value: 'test-query' } });

    await waitFor(() =>
      expect(getChannels).toBeCalledWith(
        expect.objectContaining({ query: 'test-query' })
      )
    );
  });
});
