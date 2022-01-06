/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { fireEvent, render } from '@testing-library/react';
import React from 'react';
import { CreateSenderForm } from '../components/forms/CreateSenderForm';

describe('<CreateSenderForm/> spec', () => {
  it('renders the component', () => {
    const setSenderName = jest.fn();
    const setEmail = jest.fn();
    const setHost = jest.fn();
    const setPort = jest.fn();
    const setEncryption = jest.fn();
    const setInputErrors = jest.fn();
    const inputErrors = { senderName: [], email: [], host: [], port: [] };
    const utils = render(
      <CreateSenderForm
        senderName="test"
        setSenderName={setSenderName}
        email="test"
        setEmail={setEmail}
        host="test"
        setHost={setHost}
        port="test"
        setPort={setPort}
        encryption="ssl"
        setEncryption={setEncryption}
        inputErrors={inputErrors}
        setInputErrors={setInputErrors}
      />
    );
    expect(utils.container.firstChild).toMatchSnapshot();

    const nameInput = utils.getByTestId('create-sender-form-name-input');
    fireEvent.change(nameInput, { target: { value: 'test name' } });
    fireEvent.blur(nameInput);
    expect(setSenderName).toBeCalledWith('test name');

    const emailInput = utils.getByTestId('create-sender-form-email-input');
    fireEvent.change(emailInput, { target: { value: 'test@email.com' } });
    fireEvent.blur(emailInput);
    expect(setEmail).toBeCalledWith('test@email.com');

    const hostInput = utils.getByTestId('create-sender-form-host-input');
    fireEvent.change(hostInput, { target: { value: 'host.com' } });
    fireEvent.blur(hostInput);
    expect(setHost).toBeCalledWith('host.com');

    const portInput = utils.getByTestId('create-sender-form-port-input');
    fireEvent.change(portInput, { target: { value: '23' } });
    fireEvent.blur(portInput);
    expect(setPort).toBeCalledWith('23');
  });

  it('renders errors', () => {
    const setSenderName = jest.fn();
    const setEmail = jest.fn();
    const setHost = jest.fn();
    const setPort = jest.fn();
    const setEncryption = jest.fn();
    const setInputErrors = jest.fn();
    const inputErrors = {
      senderName: ['test error'],
      email: ['test error'],
      host: ['test error'],
      port: ['test error'],
    };
    const utils = render(
      <CreateSenderForm
        senderName="test"
        setSenderName={setSenderName}
        email="test"
        setEmail={setEmail}
        host="test"
        setHost={setHost}
        port="test"
        setPort={setPort}
        encryption="ssl"
        setEncryption={setEncryption}
        inputErrors={inputErrors}
        setInputErrors={setInputErrors}
      />
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });
});
