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

import { fireEvent, render, waitFor } from '@testing-library/react';
import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import React from 'react';
import { MOCK_CONFIG } from '../../../../test/mocks/mockData';
import {
  coreServicesMock,
  notificationServiceMock,
} from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { ServicesContext } from '../../../services';
import { SendersTable } from '../components/tables/SendersTable';

describe('<SendersTable /> spec', () => {
  configure({ adapter: new Adapter() });

  it('renders empty state', async () => {
    const utils = render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <SendersTable />
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('renders table', async () => {
    const notificationServiceMock = jest.fn() as any;
    const getSenders = jest.fn(
      async (queryObject: object) => MOCK_CONFIG.senders
    );
    notificationServiceMock.notificationService = { getSenders };

    const utils = render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <SendersTable />
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );

    await waitFor(() => expect(getSenders).toBeCalled());

    const input = utils.getByPlaceholderText('Search');
    fireEvent.change(input, { target: { value: 'test-query' } });

    await waitFor(() =>
      expect(getSenders).toBeCalledWith(
        expect.objectContaining({ query: 'test-query' })
      )
    );
  });
});
