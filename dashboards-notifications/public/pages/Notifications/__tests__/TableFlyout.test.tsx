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

import { render } from '@testing-library/react';
import React from 'react';
import { notificationServiceMock } from '../../../../test/mocks/serviceMock';
import { ChannelCard } from '../components/NotificationsTable/Flyout/ChannelCard';
import { TableFlyout } from '../components/NotificationsTable/Flyout/TableFlyout';

describe('<TableFlyout /> spec', () => {
  const item = {
    id: '1',
    title: 'Alert notification on high error rate',
    referenceId: 'alert_id_1',
    source: 'Alerting',
    severity: 'High',
    lastUpdatedTime: 1612229000,
    tags: ['optional string list'],
    status: 'Error',
    statusList: [
      {
        configId: '1',
        configName: 'dev_email_channel',
        configType: 'Email',
        emailRecipientStatus: [
          {
            recipient: 'dd@amazon.com',
            deliveryStatus: { statusCode: '500', statusText: 'Some error' },
          },
          {
            recipient: 'cc@amazon.com',
            deliveryStatus: { statusCode: '404', statusText: 'invalid' },
          },
        ],
        deliveryStatus: { statusCode: '500', statusText: 'Error' },
      },
      {
        configId: '2',
        configName: 'manage_slack_channel',
        configType: 'Slack',
        deliveryStatus: { statusCode: '200', statusText: 'Success' },
      },
    ],
  };

  it('renders the component', () => {
    const utils = render(
      <TableFlyout
        notificationItem={item}
        onClose={() => {}}
        services={notificationServiceMock}
      />
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('clicks card link', () => {
    const channel = item.statusList[0];
    const onClose = jest.fn();
    const utils = render(
      <ChannelCard channel={channel} onClose={onClose} />
    );
    utils.getByText(channel.configName).click();
    expect(onClose).toBeCalled();
  });
});
