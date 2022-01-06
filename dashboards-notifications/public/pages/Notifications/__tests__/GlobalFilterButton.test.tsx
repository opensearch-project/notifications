/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { render } from '@testing-library/react';
import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import React from 'react';
import { FilterType } from '../components/SearchBar/Filter/Filters';
import { GlobalFilterButton } from '../components/SearchBar/Filter/GlobalFilterButton';

describe('<GlobalFilterButton /> spec', () => {
  configure({ adapter: new Adapter() });

  it('renders the component', () => {
    const setFilters = jest.fn();
    const utils = render(
      <GlobalFilterButton filters={[]} setFilters={setFilters} />
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('renders the component', () => {
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
    const setFilters = jest.fn();
    const utils = render(
      <GlobalFilterButton filters={filters} setFilters={setFilters} />
    );

    utils.getByLabelText('Change all filters').click();
    utils.getByText('Enable all').click();
    utils.getByLabelText('Change all filters').click();
    utils.getByText('Disable all').click();
    // utils.getByLabelText('Change all filters').click();
    // utils.getByText('Invert inclusion').click();
    utils.getByLabelText('Change all filters').click();
    utils.getByText('Invert enabled/disabled').click();
    utils.getByLabelText('Change all filters').click();
    utils.getByText('Remove all').click();

    expect(setFilters).toBeCalledTimes(4);
  });
});
