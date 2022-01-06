/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { render } from '@testing-library/react';
import React from 'react';
import { MOCK_DATA } from '../../../../test/mocks/mockData';
import { notificationServiceMock } from '../../../../test/mocks/serviceMock';
import { ChannelCard } from '../components/NotificationsTable/Flyout/ChannelCard';
import { TableFlyout } from '../components/NotificationsTable/Flyout/TableFlyout';

describe('<TableFlyout /> spec', () => {

  it('renders the component', () => {
    const utils = render(
      <TableFlyout
        notificationItem={MOCK_DATA.notifications.items[0]}
        onClose={() => {}}
        services={notificationServiceMock}
      />
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('clicks card link', () => {
    const channel = MOCK_DATA.notifications.items[0].status_list[0]
    const onClose = jest.fn();
    const utils = render(
      <ChannelCard channel={channel} onClose={onClose} />
    );
    utils.getByText(channel.config_name).click();
    expect(onClose).toBeCalled();
  });
});
