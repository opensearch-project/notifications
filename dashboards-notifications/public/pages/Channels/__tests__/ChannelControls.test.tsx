/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { fireEvent, render } from '@testing-library/react';
import React from 'react';
import { mainStateMock } from '../../../../test/mocks/serviceMock';
import { MainContext } from '../../Main/Main';
import { ChannelControls } from '../components/ChannelControls';

describe('<ChannelControls /> spec', () => {
  it('renders the component', () => {
    const onSearchChange = jest.fn();
    const onFiltersChange = jest.fn();
    const { container } = render(
      <MainContext.Provider value={mainStateMock}>
        <ChannelControls
          onSearchChange={onSearchChange}
          filters={{}}
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
        <ChannelControls
          onSearchChange={onSearchChange}
          filters={{}}
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
        <ChannelControls
          onSearchChange={onSearchChange}
          filters={{}}
          onFiltersChange={onFiltersChange}
        />
      </MainContext.Provider>
    );
    fireEvent.click(utils.getByText('Status'));
    fireEvent.click(utils.getByText('Active'));
    expect(onFiltersChange).toBeCalledWith({ state: 'true' });

    fireEvent.click(utils.getByText('Type'));
    fireEvent.click(utils.getByText('Email'));
    fireEvent.click(utils.getByText('Chime'));
    expect(onFiltersChange).toBeCalledWith({ type: ['email', 'chime'] });

    fireEvent.click(utils.getByText('Source'));
    fireEvent.click(utils.getByText('Alerting'));
    fireEvent.click(utils.getByText('Reporting'));
    expect(onFiltersChange).toBeCalledWith({ source: ['alerting', 'reports'] });

    expect(onFiltersChange).toBeCalledTimes(5);
  });
});
