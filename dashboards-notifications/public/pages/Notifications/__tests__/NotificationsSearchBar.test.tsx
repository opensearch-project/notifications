/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { fireEvent, render } from '@testing-library/react';
import React from 'react';
import { FilterType } from '../components/SearchBar/Filter/Filters';
import { NotificationsSearchBar } from '../components/SearchBar/NotificationsSearchBar';

describe('<NotificationsSearchBar /> spec', () => {
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
        operator: 'is not one of',
        value: [{ label: 'Alerting' }, { label: 'ISM' }],
        inverted: true,
        disabled: false,
      },
    ];
    const setStartTime = jest.fn();
    const setEndTime = jest.fn();
    const setSearch = jest.fn();
    const setFilters = jest.fn();
    const refresh = jest.fn();
    const utils = render(
      <NotificationsSearchBar
        startTime="now-1d"
        setStartTime={setStartTime}
        endTime="now"
        setEndTime={setEndTime}
        search="test"
        setSearch={setSearch}
        filters={filters}
        setFilters={setFilters}
        refresh={refresh}
      />
    );
    expect(utils.container.firstChild).toMatchSnapshot();

    const searchBar = utils.getByPlaceholderText('Search');
    fireEvent.change(searchBar, { target: { value: 'new query' } });
    expect(setSearch).toBeCalledWith('new query');

    utils.getByText('Refresh').click();
    expect(refresh).toBeCalled();
  });
});
