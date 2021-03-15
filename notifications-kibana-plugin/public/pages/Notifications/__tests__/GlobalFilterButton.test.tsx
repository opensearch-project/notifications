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
    utils.getByLabelText('Change all filters').click();
    utils.getByText('Invert inclusion').click();
    utils.getByLabelText('Change all filters').click();
    utils.getByText('Invert enabled/disabled').click();
    utils.getByLabelText('Change all filters').click();
    utils.getByText('Remove all').click();

    expect(setFilters).toBeCalledTimes(5);
  });
});
