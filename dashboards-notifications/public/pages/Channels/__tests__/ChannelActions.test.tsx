/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
