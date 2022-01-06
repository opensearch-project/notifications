/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { fireEvent, render } from '@testing-library/react';
import React from 'react';
import { mainStateMock } from '../../../../test/mocks/serviceMock';
import { MainContext } from '../../Main/Main';
import { SendersTableControls } from '../components/tables/SendersTableControls';

describe('<SendersTableControls /> spec', () => {
  it('renders the component', () => {
    const onSearchChange = jest.fn();
    const onFiltersChange = jest.fn();
    const { container } = render(
      <MainContext.Provider value={mainStateMock}>
        <SendersTableControls
          onSearchChange={onSearchChange}
          filters={{encryptionMethod: []}}
          onFiltersChange={onFiltersChange}
        />
      </MainContext.Provider>
    );
    expect(container.firstChild).toMatchSnapshot();
  });

  it('searches with input query', () => {
    const onSearchChange = jest.fn();
    const onFiltersChange = jest.fn();
    const utils = render(
      <MainContext.Provider value={mainStateMock}>
        <SendersTableControls
          onSearchChange={onSearchChange}
          filters={{encryptionMethod: ["start_tls"]}}
          onFiltersChange={onFiltersChange}
        />
      </MainContext.Provider>
    );
    const input = utils.getByPlaceholderText('Search');

    fireEvent.change(input, { target: { value: 'test' } });
    expect(onSearchChange).toBeCalledWith('test');
  });

  it('changes filters', () => {
    const onSearchChange = jest.fn();
    const onFiltersChange = jest.fn();
    const utils = render(
      <MainContext.Provider value={mainStateMock}>
        <SendersTableControls
          onSearchChange={onSearchChange}
          filters={{encryptionMethod: []}}
          onFiltersChange={onFiltersChange}
        />
      </MainContext.Provider>
    );
    fireEvent.click(utils.getByText('Encryption method'));
    fireEvent.click(utils.getByText('TLS'));
    fireEvent.click(utils.getByText('None'));
    expect(onFiltersChange).toBeCalledWith({ encryptionMethod: ["start_tls", "none"] });
  });
});
