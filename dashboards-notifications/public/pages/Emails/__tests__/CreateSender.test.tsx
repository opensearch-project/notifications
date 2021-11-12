/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
import { routerComponentPropsMock } from '../../../../test/mocks/routerPropsMock';
import { coreServicesMock } from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { ServicesContext } from '../../../services';
import { CreateSender } from '../CreateSender';

describe('<CreateSender/> spec', () => {
  it('renders the component', () => {
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <CreateSender {...routerComponentPropsMock} />
      </CoreServicesContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  function editAndSendRequest(updateConfig: jest.Mock) {
    const notificationServiceMock = jest.fn() as any;
    notificationServiceMock.notificationService = {
      getSender: async (id: string) => MOCK_DATA.sender,
      updateConfig,
    };
    const props = { match: { params: { id: 'test' } } };
    return render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <CreateSender
            {...(props as RouteComponentProps<{ id: string }>)}
            edit={true}
          />
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );
  }

  it('renders the component for editing', async () => {
    const updateConfig = jest.fn(async () => Promise.resolve());
    const utils = editAndSendRequest(updateConfig)
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
    const utils = editAndSendRequest(updateConfig)
    await waitFor(() => {
      expect(utils.container.firstChild).toMatchSnapshot();
    });

    utils.getByText('Save').click();
    await waitFor(() => {
      expect(updateConfig).toBeCalled();
    });
  });
});
