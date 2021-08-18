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

import { fireEvent, render } from '@testing-library/react';
import React from 'react';
import {
  coreServicesMock,
  notificationServiceMock,
} from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { DeleteRecipientGroupModal } from '../components/modals/DeleteRecipientGroupModal';

describe('<DeleteRecipientGroupModal /> spec', () => {
  it('returns if no recipient groups', () => {
    const { container } = render(
      <DeleteRecipientGroupModal
        recipientGroups={[]}
        onClose={() => {}}
        refresh={() => {}}
        services={notificationServiceMock}
      />
    );
    expect(container.firstChild).toBeNull();
  });

  it('renders the component', () => {
    const recipientGroups = [jest.fn() as any];
    const { container } = render(
      <DeleteRecipientGroupModal
        recipientGroups={recipientGroups}
        onClose={() => {}}
        refresh={() => {}}
        services={notificationServiceMock}
      />
    );
    expect(container.firstChild).toMatchSnapshot();
  });

  it('renders multiple recipient groups', () => {
    const recipientGroups = [jest.fn() as any, jest.fn() as any];
    const { container } = render(
      <DeleteRecipientGroupModal
        recipientGroups={recipientGroups}
        onClose={() => {}}
        refresh={() => {}}
        services={notificationServiceMock}
      />
    );
    expect(container.firstChild).toMatchSnapshot();
  });

  it('deletes recipient groups', () => {
    const recipientGroups = [jest.fn() as any, jest.fn() as any];
    const notificationServiceMock = jest.fn() as any;
    notificationServiceMock.notificationService = {
      deleteConfigs: async (ids: string[]) => Promise.resolve(),
    };
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <DeleteRecipientGroupModal
          recipientGroups={recipientGroups}
          onClose={() => {}}
          refresh={() => {}}
          services={notificationServiceMock}
        />
      </CoreServicesContext.Provider>
    );
    const input = utils.getAllByPlaceholderText('delete')[0];
    fireEvent.change(input, { target: { value: 'delete' } });
    const deleteButton = utils.getByText('Delete');
    fireEvent.click(deleteButton);
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('handles failures', () => {
    const recipientGroups = [jest.fn() as any, jest.fn() as any];
    const notificationServiceMock = jest.fn() as any;
    notificationServiceMock.notificationService = {
      deleteConfigs: async (ids: string[]) => Promise.reject(),
    };
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <DeleteRecipientGroupModal
          recipientGroups={recipientGroups}
          onClose={() => {}}
          refresh={() => {}}
          services={notificationServiceMock}
        />
      </CoreServicesContext.Provider>
    );
    const input = utils.getAllByPlaceholderText('delete')[0];
    fireEvent.change(input, { target: { value: 'delete' } });
    const deleteButton = utils.getByText('Delete');
    fireEvent.click(deleteButton);
    expect(utils.container.firstChild).toMatchSnapshot();
  });
});
