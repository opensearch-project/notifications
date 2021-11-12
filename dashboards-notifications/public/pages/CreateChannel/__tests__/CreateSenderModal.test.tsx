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

import { fireEvent, render, waitFor } from '@testing-library/react';
import React from 'react';
import {
  coreServicesMock,
  notificationServiceMock,
} from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { CreateSenderModal } from '../components/modals/CreateSenderModal';

describe('<CreateSenderModal/> spec', () => {
  it('renders the component', () => {
    const utils = render(
      <CreateSenderModal
        addSenderOptionAndSelect={() => {}}
        onClose={() => {}}
        services={notificationServiceMock}
      />
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('validates input', async () => {
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <CreateSenderModal
          addSenderOptionAndSelect={() => {}}
          onClose={() => {}}
          services={notificationServiceMock}
        />
      </CoreServicesContext.Provider>
    );
    utils.getByText('Create').click();
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('creates senders', async () => {
    const notificationServiceMock = jest.fn() as any;
    const createConfig = jest.fn(async () => Promise.resolve());
    notificationServiceMock.notificationService = {
      createConfig,
    };
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <CreateSenderModal
          addSenderOptionAndSelect={() => {}}
          onClose={() => {}}
          services={notificationServiceMock}
        />
      </CoreServicesContext.Provider>
    );

    const nameInput = utils.getByTestId('create-sender-form-name-input');
    fireEvent.change(nameInput, { target: { value: 'test-name' } });
    const emailInput = utils.getByTestId('create-sender-form-email-input');
    fireEvent.change(emailInput, { target: { value: 'test@email.com' } });
    const hostInput = utils.getByTestId('create-sender-form-host-input');
    fireEvent.change(hostInput, { target: { value: 'host.com' } });
    const portInput = utils.getByTestId('create-sender-form-port-input');
    fireEvent.change(portInput, { target: { value: '23' } });

    utils.getByText('Create').click();
    await waitFor(() => {
      expect(createConfig).toBeCalled();
    });
  });

  it('handles failures', async () => {
    const notificationServiceMock = jest.fn() as any;
    const createConfig = jest.fn(async () => Promise.reject());
    notificationServiceMock.notificationService = {
      createConfig,
    };
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <CreateSenderModal
          addSenderOptionAndSelect={() => {}}
          onClose={() => {}}
          services={notificationServiceMock}
        />
      </CoreServicesContext.Provider>
    );

    const nameInput = utils.getByTestId('create-sender-form-name-input');
    fireEvent.change(nameInput, { target: { value: 'test-name' } });
    const emailInput = utils.getByTestId('create-sender-form-email-input');
    fireEvent.change(emailInput, { target: { value: 'test@email.com' } });
    const hostInput = utils.getByTestId('create-sender-form-host-input');
    fireEvent.change(hostInput, { target: { value: 'host.com' } });
    const portInput = utils.getByTestId('create-sender-form-port-input');
    fireEvent.change(portInput, { target: { value: '23' } });

    utils.getByText('Create').click();
    await waitFor(() => {
      expect(createConfig).toBeCalled();
    });
  });
});
