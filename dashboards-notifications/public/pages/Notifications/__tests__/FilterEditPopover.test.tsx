/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { fireEvent, render } from '@testing-library/react';
import React from 'react';
import { FilterEditPopover } from '../components/SearchBar/Filter/FilterEditPopover';

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
