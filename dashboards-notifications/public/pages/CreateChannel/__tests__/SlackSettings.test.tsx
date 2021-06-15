/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

import { fireEvent, render } from '@testing-library/react';
import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import React from 'react';
import { SlackSettings } from '../components/SlackSettings';
import { CreateChannelContext } from '../CreateChannel';

describe('<SlackSettings /> spec', () => {
  configure({ adapter: new Adapter() });

  it('renders the component', () => {
    const setSlackWebhook = jest.fn();
    const utils = render(
      <CreateChannelContext.Provider
        value={{
          edit: false,
          inputErrors: { slackWebhook: [] },
          setInputErrors: jest.fn(),
        }}
      >
        <SlackSettings
          slackWebhook="test webhook"
          setSlackWebhook={setSlackWebhook}
        />
      </CreateChannelContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('renders the component with error', () => {
    const setSlackWebhook = jest.fn();
    const utils = render(
      <CreateChannelContext.Provider
        value={{
          edit: false,
          inputErrors: { slackWebhook: ['test error'] },
          setInputErrors: jest.fn(),
        }}
      >
        <SlackSettings
          slackWebhook="test webhook"
          setSlackWebhook={setSlackWebhook}
        />
      </CreateChannelContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('changes input', () => {
    const setSlackWebhook = jest.fn();
    const setInputErrors = jest.fn();
    const utils = render(
      <CreateChannelContext.Provider
        value={{
          edit: false,
          inputErrors: { slackWebhook: [] },
          setInputErrors,
        }}
      >
        <SlackSettings
          slackWebhook="test webhook"
          setSlackWebhook={setSlackWebhook}
        />
      </CreateChannelContext.Provider>
    );
    const input = utils.getByLabelText('Slack webhook URL');
    fireEvent.change(input, { target: { value: 'https://test-slack-url' } });
    fireEvent.blur(input);
    expect(setSlackWebhook).toBeCalledWith('https://test-slack-url');
    expect(setInputErrors).toBeCalled();
  });
});
