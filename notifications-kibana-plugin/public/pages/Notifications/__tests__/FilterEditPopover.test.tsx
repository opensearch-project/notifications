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
import { FilterEditPopover } from '../component/SearchBar/Filter/FilterEditPopover';

describe('<FilterEditPopover /> spec', () => {
  it('renders the component', () => {
    const setFilter = jest.fn();
    const utils = render(
      <FilterEditPopover closePopover={() => {}} setFilter={setFilter} />
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('closes popover', () => {
    const setFilter = jest.fn();
    const closePopover = jest.fn();
    const utils = render(
      <FilterEditPopover closePopover={closePopover} setFilter={setFilter} />
    );

    const cancel = utils.getByText('Cancel');
    fireEvent.click(cancel);
    expect(closePopover).toBeCalled();
  });

  it('changes options', () => {
    const setFilter = jest.fn();
    const closePopover = jest.fn();
    const utils = render(
      <FilterEditPopover closePopover={closePopover} setFilter={setFilter} />
    );

    const inputs = utils.container.querySelectorAll('input');

    fireEvent.change(inputs[0], [{ label: 'Status' }]);
    fireEvent.change(inputs[1], [{ label: 'is' }]);
  });
});
