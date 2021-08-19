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

import { MOCK_DATA } from '../../../../test/mocks/mockData';
import { act, render, waitFor } from '@testing-library/react';
import React from 'react';
import {
  coreServicesMock,
  notificationServiceMock,
} from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { MuteChannelModal } from '../components/modals/MuteChannelModal';

describe('<MuteChannelModal /> spec', () => {
  it('returns if no channels', () => {
    const { container } = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <MuteChannelModal
          selected={[]}
          setSelected={() => {}}
          onClose={() => {}}
          services={notificationServiceMock}
        />
      </CoreServicesContext.Provider>
    );
    expect(container.firstChild).toBeNull();
  });

  it('renders the component', () => {
    const channels = [jest.fn() as any];
    const setSelected = jest.fn();
    const { container } = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <MuteChannelModal
          selected={channels}
          setSelected={setSelected}
          onClose={() => {}}
          services={notificationServiceMock}
        />
      </CoreServicesContext.Provider>
    );
    expect(container.firstChild).toMatchSnapshot();
  });

  it('clicks mute button', async () => {
    const setSelected = jest.fn();
    const notificationServiceMock = jest.fn() as any;
    notificationServiceMock.notificationService = {
      updateConfig: async (id: string, config: any) => {
        return Promise.resolve();
      },
    };
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <MuteChannelModal
          selected={[MOCK_DATA.chime]}
          setSelected={setSelected}
          onClose={() => {}}
          refresh={jest.fn()}
          services={notificationServiceMock}
        />
      </CoreServicesContext.Provider>
    );
    utils.getByText('Mute').click();
    await waitFor(() => expect(setSelected).toBeCalled())
  });

  it('handles failures', async () => {
    const setSelected = jest.fn();
    const notificationServiceMock = jest.fn() as any;
    notificationServiceMock.notificationService = {
      updateConfig: async (id: string, config: any) => {
        return Promise.reject();
      },
    };
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <MuteChannelModal
          selected={[MOCK_DATA.chime]}
          setSelected={setSelected}
          onClose={() => {}}
          services={notificationServiceMock}
        />
      </CoreServicesContext.Provider>
    );
    utils.getByText('Mute').click();
    expect(utils.container.firstChild).toMatchSnapshot();
  });
});
