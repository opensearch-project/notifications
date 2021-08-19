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
import { ServicesContext } from '../../../services';
import { ChannelActions } from '../components/ChannelActions';

describe('<ChannelActions/> spec', () => {
  it('renders the action button disabled by default', () => {
    const channels = [jest.fn() as any];
    const utils = render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <ChannelActions
            selected={channels}
            setSelected={() => {}}
            items={[]}
            setItems={() => {}}
            refresh={() => {}}
          />
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();

    const button = utils.getByText('Actions');
    expect(button).toBeDisabled;
  });

  it('renders the popover', () => {
    const channels = [jest.fn() as any];
    const utils = render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <ChannelActions
            selected={channels}
            setSelected={() => {}}
            items={[]}
            setItems={() => {}}
            refresh={() => {}}
          />
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );
    const button = utils.getByText('Actions');
    fireEvent.click(button);
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('renders the popover with multiple selected channels', () => {
    const channels = [jest.fn() as any, jest.fn() as any];
    const utils = render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <ChannelActions
            selected={channels}
            setSelected={() => {}}
            items={[]}
            setItems={() => {}}
            refresh={() => {}}
          />
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );
    const button = utils.getByText('Actions');
    fireEvent.click(button);
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('clicks in the popover', () => {
    const channel = jest.fn() as any;
    channel.is_enabled = false;
    const channels = [channel];
    const utils = render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <ChannelActions
            selected={channels}
            setSelected={() => {}}
            items={[]}
            setItems={() => {}}
            refresh={() => {}}
          />
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );
    const button = utils.getByText('Actions');
    fireEvent.click(button);

    const muteButton = utils.getByText('Mute');
    fireEvent.click(muteButton);
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('clicks unmute', () => {
    const notificationServiceMock = jest.fn() as any;
    const updateConfig = jest.fn(async () => Promise.resolve());
    notificationServiceMock.notificationService = {
      updateConfig,
    };
    const channel = jest.fn() as any;
    channel.enabled = true;
    const channels = [channel];
    const utils = render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <ChannelActions
            selected={channels}
            setSelected={() => {}}
            items={[]}
            setItems={() => {}}
            refresh={() => {}}
          />
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );
    const button = utils.getByText('Actions');
    fireEvent.click(button);

    const muteButton = utils.getByText('Unmute');
    fireEvent.click(muteButton);
    expect(updateConfig).toBeCalled();
  });
});
