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
import { Filters, FilterType } from '../components/SearchBar/Filter/Filters';

describe('<Filters /> spec', () => {
  it('renders the component', () => {
    const setFilters = jest.fn();
    const filters: FilterType[] = [
      {
        field: 'Channel',
        operator: 'is',
        value: 'test',
        inverted: false,
        disabled: true,
      },
      {
        field: 'Source',
        operator: 'is one of',
        value: [{ label: 'Alerting' }, { label: 'ISM' }],
        inverted: true,
        disabled: false,
      },
    ];
    const utils = render(<Filters filters={filters} setFilters={setFilters} />);
    expect(utils.container.firstChild).toMatchSnapshot();

    utils.getByText('Channel: test').click();
    utils.getByText('Re-enable').click();
    utils.getByText('Channel: test').click();
    utils.getByText('Exclude results').click();
    utils.getByText('Channel: test').click();
    utils.getByText('Delete').click();
  });
});
