/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
    // utils.getByText('Channel: test').click();
    // utils.getByText('Exclude results').click();
    utils.getByText('Channel: test').click();
    utils.getByText('Delete').click();
  });
});
