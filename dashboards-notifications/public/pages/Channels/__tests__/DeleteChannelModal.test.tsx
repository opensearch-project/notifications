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

import { fireEvent, render } from '@testing-library/react';
import React from 'react';
import {
  coreServicesMock,
  notificationServiceMock,
} from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { DeleteChannelModal } from '../components/modals/DeleteChannelModal';

describe('<DeleteChannelModal /> spec', () => {
  it('returns if no channels', () => {
    const { container } = render(
      <DeleteChannelModal
        selected={[]}
        onClose={() => {}}
        services={notificationServiceMock}
      />
    );
    expect(container.firstChild).toBeNull();
  });

  it('renders the component', () => {
    const channels = [jest.fn() as any];
    const { container } = render(
      <DeleteChannelModal
        selected={channels}
        onClose={() => {}}
        services={notificationServiceMock}
      />
    );
    expect(container.firstChild).toMatchSnapshot();
  });

  it('renders multiple channels', () => {
    const channels = [jest.fn() as any, jest.fn() as any];
    const { container } = render(
      <DeleteChannelModal
        selected={channels}
        onClose={() => {}}
        services={notificationServiceMock}
      />
    );
    expect(container.firstChild).toMatchSnapshot();
  });

  it('deletes channels', () => {
    const channels = [jest.fn() as any, jest.fn() as any];
    const onClose = jest.fn();
    const notificationServiceMock = jest.fn() as any;
    notificationServiceMock.notificationService = {
      deleteConfigs: async (ids: string[]) => Promise.resolve(),
    };
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <DeleteChannelModal
          selected={channels}
          onClose={onClose}
          services={notificationServiceMock}
        />
      </CoreServicesContext.Provider>
    );
    const input = utils.getByPlaceholderText('delete');
    fireEvent.change(input, { target: { value: 'delete' } });
    const deleteButton = utils.getByText('Delete');
    fireEvent.click(deleteButton);
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('handles failures when deleting channels', () => {
    const channels = [jest.fn() as any, jest.fn() as any];
    const onClose = jest.fn();
    const notificationServiceMock = jest.fn() as any;
    notificationServiceMock.notificationService = {
      deleteConfigs: async (ids: string[]) => Promise.reject(),
    };
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <DeleteChannelModal
          selected={channels}
          onClose={onClose}
          services={notificationServiceMock}
        />
      </CoreServicesContext.Provider>
    );
    const input = utils.getByPlaceholderText('delete');
    fireEvent.change(input, { target: { value: 'delete' } });
    const deleteButton = utils.getByText('Delete');
    fireEvent.click(deleteButton);
    expect(utils.container.firstChild).toMatchSnapshot();
  });
});
