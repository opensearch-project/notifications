/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { MOCK_DATA } from '../../../../test/mocks/mockData';
import { render, waitFor } from '@testing-library/react';
import React from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { routerComponentPropsMock } from '../../../../test/mocks/routerPropsMock';
import {
  coreServicesMock,
  notificationServiceMock,
} from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { ServicesContext } from '../../../services';
import { CreateRecipientGroup } from '../CreateRecipientGroup';

describe('<CreateRecipientGroup/> spec', () => {
  it('renders the component', () => {
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <CreateRecipientGroup {...routerComponentPropsMock} />
      </CoreServicesContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('renders the component for editing', async () => {
    const notificationServiceMock = jest.fn() as any;
    const updateConfig = jest.fn(async () => Promise.resolve());
    notificationServiceMock.notificationService = {
      getRecipientGroup: async (id: string) => MOCK_DATA.recipientGroup,
      updateConfig,
    };
    const props = { match: { params: { id: 'test' } } };
    const utils = render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <CreateRecipientGroup
            {...(props as RouteComponentProps<{ id: string }>)}
            edit={true}
          />
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );
    await waitFor(() => {});

    utils.getByText('Save').click();
    await waitFor(() => {
      expect(updateConfig).toBeCalled();
    });
  });
});
