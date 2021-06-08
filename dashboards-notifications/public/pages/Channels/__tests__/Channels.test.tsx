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
import React from 'react';
import { MOCK_DATA } from '../../../../test/mocks/mockData';
import { routerComponentPropsMock } from '../../../../test/mocks/routerPropsMock';
import { coreServicesMock } from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { Channels } from '../Channels';

describe('<Channels/> spec', () => {
  it('renders the empty component', () => {
    const notificationServiceMock = jest.fn() as any;
    const getChannels = jest.fn(
      async (queryObject: object) => []
    );
    notificationServiceMock.notificationService = { getChannels };
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <Channels
          {...routerComponentPropsMock}
          notificationService={notificationServiceMock}
        />
      </CoreServicesContext.Provider>
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
      <CoreServicesContext.Provider value={coreServicesMock}>
        <Channels
          {...routerComponentPropsMock}
          notificationService={notificationService}
        />
      </CoreServicesContext.Provider>
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
