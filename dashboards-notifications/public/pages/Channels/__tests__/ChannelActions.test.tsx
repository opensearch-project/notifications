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

import { ChannelItemType } from '.notifications/notifications-kibana-plugin/models/interfaces';
import { fireEvent, render } from '@testing-library/react';
import React from 'react';
import { ChannelActions } from '../components/ChannelActions';

describe('<ChannelActions/> spec', () => {
  it('renders the action button', () => {
    const channels = [jest.fn() as any];
    const utils = render(<ChannelActions selectedItems={channels} />);
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('renders the disabled action button', () => {
    const channels: ChannelItemType[] = [];
    const utils = render(<ChannelActions selectedItems={channels} />);
    const button = utils.getByText('Actions');
    expect(button).toBeDisabled;
  });

  it('renders the popover', () => {
    const channels = [jest.fn() as any];
    const utils = render(<ChannelActions selectedItems={channels} />);
    const button = utils.getByText('Actions');
    fireEvent.click(button);
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('clicks in the popover', () => {
    const channel = jest.fn() as any;
    channel.enabled = true;
    const channels = [channel];
    const utils = render(<ChannelActions selectedItems={channels} />);
    const button = utils.getByText('Actions');
    fireEvent.click(button);
    
    const muteButton = utils.getByText('Mute');
    fireEvent.click(muteButton);
    expect(utils.container.firstChild).toMatchSnapshot();
  });
});
