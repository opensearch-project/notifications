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
import { ChannelControls } from '../components/ChannelControls';

describe('<ChannelControls /> spec', () => {
  it('renders the component', () => {
    const onSearchChange = jest.fn();
    const onFiltersChange = jest.fn();
    const { container } = render(
      <ChannelControls
        search=""
        onSearchChange={onSearchChange}
        filters={{}}
        onFiltersChange={onFiltersChange}
      />
    );
    expect(container.firstChild).toMatchSnapshot();
  });

  it('searches with input query', () => {
    const onSearchChange = jest.fn();
    const onFiltersChange = jest.fn();
    const utils = render(
      <ChannelControls
        search=""
        onSearchChange={onSearchChange}
        filters={{}}
        onFiltersChange={onFiltersChange}
      />
    );
    const input = utils.getByPlaceholderText('Search');

    fireEvent.change(input, { target: { value: 'test' } });
    expect(onSearchChange).toBeCalledWith('test');
  });

  it('changes filters', () => {
    const onSearchChange = jest.fn();
    const onFiltersChange = jest.fn();
    const utils = render(
      <ChannelControls
        search=""
        onSearchChange={onSearchChange}
        filters={{}}
        onFiltersChange={onFiltersChange}
      />
    );
    fireEvent.click(utils.getByText('Status'));
    fireEvent.click(utils.getByText('Active'));
    expect(onFiltersChange).toBeCalledWith({ state: 'true' });

    fireEvent.click(utils.getByText('Type'));
    fireEvent.click(utils.getByText('Email'));
    fireEvent.click(utils.getByText('Chime'));
    expect(onFiltersChange).toBeCalledWith({ type: ['email', 'chime'] });

    fireEvent.click(utils.getByText('Source'));
    fireEvent.click(utils.getByText('Alerting'));
    fireEvent.click(utils.getByText('Reporting'));
    expect(onFiltersChange).toBeCalledWith({ source: ['alerting', 'reports'] });

    expect(onFiltersChange).toBeCalledTimes(5);
  });
});
