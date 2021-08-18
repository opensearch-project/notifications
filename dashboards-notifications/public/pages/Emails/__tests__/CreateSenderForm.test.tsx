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
