/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { fireEvent, render } from '@testing-library/react';
import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import React from 'react';
import { ChimeSettings } from '../components/ChimeSettings';
import { CreateChannelContext } from '../CreateChannel';

describe('<ChimeSettings /> spec', () => {
  configure({ adapter: new Adapter() });

  it('renders the component', () => {
    const setChimeWebhook = jest.fn();
    const utils = render(
      <CreateChannelContext.Provider
        value={{
          edit: false,
          inputErrors: { chimeWebhook: [] },
          setInputErrors: jest.fn(),
        }}
      >
        <ChimeSettings
          chimeWebhook="test webhook"
          setChimeWebhook={setChimeWebhook}
        />
      </CreateChannelContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('renders the component with error', () => {
    const setChimeWebhook = jest.fn();
    const utils = render(
      <CreateChannelContext.Provider
        value={{
          edit: false,
          inputErrors: { chimeWebhook: ['test error'] },
          setInputErrors: jest.fn(),
        }}
      >
        <ChimeSettings
          chimeWebhook="test webhook"
          setChimeWebhook={setChimeWebhook}
        />
      </CreateChannelContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('changes input', () => {
    const setChimeWebhook = jest.fn();
    const setInputErrors = jest.fn();
    const utils = render(
      <CreateChannelContext.Provider
        value={{
          edit: false,
          inputErrors: { chimeWebhook: [] },
          setInputErrors,
        }}
      >
        <ChimeSettings
          chimeWebhook="test webhook"
          setChimeWebhook={setChimeWebhook}
        />
      </CreateChannelContext.Provider>
    );
    const input = utils.getByLabelText('Webhook URL');
    fireEvent.change(input, { target: { value: 'https://test-chime-url' } });
    fireEvent.blur(input);
    expect(setChimeWebhook).toBeCalledWith('https://test-chime-url');
    expect(setInputErrors).toBeCalled();
  });
});
