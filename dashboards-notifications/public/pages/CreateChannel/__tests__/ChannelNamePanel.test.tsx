/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { fireEvent, render } from '@testing-library/react';
import React from 'react';
import { ChannelNamePanel } from '../components/ChannelNamePanel';
import { CreateChannelContext } from '../CreateChannel';

describe('<ChannelNamePanel/> spec', () => {
  it('renders the component', () => {
    const setName = jest.fn();
    const setDescription = jest.fn();
    const setInputErrors = jest.fn() as any;
    const utils = render(
      <CreateChannelContext.Provider
        value={{ edit: false, inputErrors: { name: [] }, setInputErrors }}
      >
        <ChannelNamePanel
          name="test"
          setName={setName}
          description="test description"
          setDescription={setDescription}
        />
      </CreateChannelContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('changes input fields', () => {
    const setName = jest.fn();
    const setDescription = jest.fn();
    const setInputErrors = jest.fn() as any;
    const utils = render(
      <CreateChannelContext.Provider
        value={{ edit: false, inputErrors: { name: [] }, setInputErrors }}
      >
        <ChannelNamePanel
          name="test"
          setName={setName}
          description="test description"
          setDescription={setDescription}
        />
      </CreateChannelContext.Provider>
    );
    const nameField = utils.getByPlaceholderText('Enter channel name');
    fireEvent.change(nameField, { target: { value: 'test name' } });
    fireEvent.blur(nameField);
    expect(setName).toBeCalledWith('test name');
  });

  it('renders errors', () => {
    const setName = jest.fn();
    const setDescription = jest.fn();
    const setInputErrors = jest.fn() as any;
    const utils = render(
      <CreateChannelContext.Provider
        value={{
          edit: false,
          inputErrors: { name: ['test error'] },
          setInputErrors,
        }}
      >
        <ChannelNamePanel
          name="test"
          setName={setName}
          description="test description"
          setDescription={setDescription}
        />
      </CreateChannelContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });
});
