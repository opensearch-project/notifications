/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
