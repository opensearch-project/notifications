/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { fireEvent, render } from '@testing-library/react';
import React from 'react';
import { AddFilterButton } from '../components/SearchBar/Filter/AddFilterButton';

describe('<AddFilterButton /> spec', () => {
  it('renders the component', () => {
    const setFilter = jest.fn();
    const utils = render(<AddFilterButton setFilter={setFilter} />);
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('renders popover', () => {
    const setFilter = jest.fn();
    const utils = render(<AddFilterButton setFilter={setFilter} />);
    const button = utils.getByText('+ Add filter');
    fireEvent.click(button);
    expect(utils.container.firstChild).toMatchSnapshot();
  });
});
