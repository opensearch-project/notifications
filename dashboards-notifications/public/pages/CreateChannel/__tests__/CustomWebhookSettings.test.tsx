/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { fireEvent, render } from '@testing-library/react';
import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import React from 'react';
import { CustomWebhookSettings } from '../components/CustomWebhookSettings';
import { CreateChannelContext } from '../CreateChannel';

describe('<CustomWebhookSettings /> spec', () => {
  configure({ adapter: new Adapter() });

  it('renders the component', () => {
    const setWebhookTypeIdSelected = jest.fn();
    const setWebhookURL = jest.fn();
    const setCustomURLHost = jest.fn();
    const setCustomURLPort = jest.fn();
    const setCustomURLPath = jest.fn();
    const setWebhookParams = jest.fn();
    const setWebhookHeaders = jest.fn();
    const setInputErrors = jest.fn();
    const customURLUtils = render(
      <CreateChannelContext.Provider
        value={{
          edit: false,
          inputErrors: { webhookURL: [], customURLHost: [], customURLPort: [] },
          setInputErrors,
        }}
      >
        <CustomWebhookSettings
          webhookTypeIdSelected="CUSTOM_URL"
          setWebhookTypeIdSelected={setWebhookTypeIdSelected}
          webhookURL="https://test-webhook"
          setWebhookURL={setWebhookURL}
          customURLHost="host.com"
          setCustomURLHost={setCustomURLHost}
          customURLPort="123"
          setCustomURLPort={setCustomURLPort}
          customURLPath=""
          setCustomURLPath={setCustomURLPath}
          webhookParams={[]}
          setWebhookParams={setWebhookParams}
          webhookHeaders={[]}
          setWebhookHeaders={setWebhookHeaders}
        />
      </CreateChannelContext.Provider>
    );
    expect(customURLUtils.container.firstChild).toMatchSnapshot();

    const hostInput = customURLUtils.getByTestId('custom-webhook-host-input');
    fireEvent.change(hostInput, { target: { value: 'https://test-url' } });
    fireEvent.blur(hostInput);
    expect(setCustomURLHost).toBeCalledWith('https://test-url');
    expect(setInputErrors).toBeCalled();

    const webhookURLUtils = render(
      <CreateChannelContext.Provider
        value={{
          edit: false,
          inputErrors: { webhookURL: [], customURLHost: [], customURLPort: [] },
          setInputErrors: jest.fn(),
        }}
      >
        <CustomWebhookSettings
          webhookTypeIdSelected="WEBHOOK_URL"
          setWebhookTypeIdSelected={setWebhookTypeIdSelected}
          webhookURL="https://test-webhook"
          setWebhookURL={setWebhookURL}
          customURLHost="host.com"
          setCustomURLHost={setCustomURLHost}
          customURLPort="123"
          setCustomURLPort={setCustomURLPort}
          customURLPath=""
          setCustomURLPath={setCustomURLPath}
          webhookParams={[]}
          setWebhookParams={setWebhookParams}
          webhookHeaders={[]}
          setWebhookHeaders={setWebhookHeaders}
        />
      </CreateChannelContext.Provider>
    );
    expect(webhookURLUtils.container.firstChild).toMatchSnapshot();

    const urlInput = customURLUtils.getByTestId('custom-webhook-url-input');
    fireEvent.change(urlInput, { target: { value: 'https://test-url' } });
    fireEvent.blur(urlInput);
    expect(setWebhookURL).toBeCalledWith('https://test-url');
    expect(setInputErrors).toBeCalled();

    const pathInput = customURLUtils.getByTestId('custom-webhook-path-input');
    fireEvent.change(pathInput, { target: { value: 'https://test-url' } });
    fireEvent.blur(pathInput);
    expect(setCustomURLPath).toBeCalledWith('https://test-url');
  });

  it('renders the component with errors', () => {
    const customURLUtils = render(
      <CreateChannelContext.Provider
        value={{
          edit: false,
          inputErrors: {
            webhookURL: ['test-error'],
            customURLHost: ['test-error'],
            customURLPort: ['test-error'],
          },
          setInputErrors: jest.fn(),
        }}
      >
        <CustomWebhookSettings
          webhookTypeIdSelected="CUSTOM_URL"
          setWebhookTypeIdSelected={() => {}}
          webhookURL="https://test-webhook"
          setWebhookURL={() => {}}
          customURLHost="host.com"
          setCustomURLHost={() => {}}
          customURLPort="123"
          setCustomURLPort={() => {}}
          customURLPath=""
          setCustomURLPath={() => {}}
          webhookParams={[]}
          setWebhookParams={() => {}}
          webhookHeaders={[]}
          setWebhookHeaders={() => {}}
        />
      </CreateChannelContext.Provider>
    );
    expect(customURLUtils.container.firstChild).toMatchSnapshot();

    const webhookURLUtils = render(
      <CreateChannelContext.Provider
        value={{
          edit: false,
          inputErrors: {
            webhookURL: ['test-error'],
            customURLHost: ['test-error'],
            customURLPort: ['test-error'],
          },
          setInputErrors: jest.fn(),
        }}
      >
        <CustomWebhookSettings
          webhookTypeIdSelected="WEBHOOK_URL"
          setWebhookTypeIdSelected={() => {}}
          webhookURL="https://test-webhook"
          setWebhookURL={() => {}}
          customURLHost="host.com"
          setCustomURLHost={() => {}}
          customURLPort="123"
          setCustomURLPort={() => {}}
          customURLPath=""
          setCustomURLPath={() => {}}
          webhookParams={[]}
          setWebhookParams={() => {}}
          webhookHeaders={[]}
          setWebhookHeaders={() => {}}
        />
      </CreateChannelContext.Provider>
    );
    expect(webhookURLUtils.container.firstChild).toMatchSnapshot();
  });
});
