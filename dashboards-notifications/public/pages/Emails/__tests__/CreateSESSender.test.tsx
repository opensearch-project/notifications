/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { render, waitFor } from '@testing-library/react';
import React from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { MOCK_DATA } from '../../../../test/mocks/mockData';
import { routerComponentPropsMock } from '../../../../test/mocks/routerPropsMock';
import {
  coreServicesMock,
  mainStateMock,
} from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { ServicesContext } from '../../../services';
import { MainContext } from '../../Main/Main';
import { CreateSESSender } from '../CreateSESSender';

describe('<CreateSESSender/> spec', () => {
  it('renders the component', () => {
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <MainContext.Provider value={mainStateMock}>
          <CreateSESSender {...routerComponentPropsMock} />
        </MainContext.Provider>
      </CoreServicesContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  function editAndSendRequest(updateConfig: jest.Mock) {
    const notificationServiceMock = jest.fn() as any;
    notificationServiceMock.notificationService = {
      getSESSender: async (id: string) => MOCK_DATA.sesSender,
      updateConfig,
    };
    const props = { match: { params: { id: 'test' } } };
    return render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <MainContext.Provider value={mainStateMock}>
            <CreateSESSender
              {...(props as RouteComponentProps<{ id: string }>)}
              edit={true}
            />
          </MainContext.Provider>
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );
  }

  it('renders the component for editing', async () => {
    const updateConfig = jest.fn(async () => Promise.resolve());
    const utils = editAndSendRequest(updateConfig);
    await waitFor(() => {
      expect(utils.container.firstChild).toMatchSnapshot();
    });

    utils.getByText('Save').click();
    await waitFor(() => {
      expect(updateConfig).toBeCalled();
    });
  });

  it('handles failed requests', async () => {
    const updateConfig = jest.fn(async () => Promise.reject());
    const utils = editAndSendRequest(updateConfig);
    await waitFor(() => {
      expect(utils.container.firstChild).toMatchSnapshot();
    });

    utils.getByText('Save').click();
    await waitFor(() => {
      expect(updateConfig).toBeCalled();
    });
  });
});
