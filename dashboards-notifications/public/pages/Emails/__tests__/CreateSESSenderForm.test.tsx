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
import { mainStateMock } from '../../../../test/mocks/serviceMock';
import { MainContext } from '../../Main/Main';
import { CreateSESSenderForm } from '../components/forms/CreateSESSenderForm';

describe('<CreateSESSenderForm/> spec', () => {
  it('renders the component', () => {
    const setSenderName = jest.fn();
    const setEmail = jest.fn();
    const setRoleArn = jest.fn();
    const setAwsRegion = jest.fn();
    const setInputErrors = jest.fn();
    const inputErrors = {
      senderName: [],
      email: [],
      awsRegion: [],
      roleArn: [],
    };
    const utils = render(
      <MainContext.Provider value={mainStateMock}>
        <CreateSESSenderForm
          senderName="test"
          setSenderName={setSenderName}
          email=""
          setEmail={setEmail}
          roleArn=""
          setRoleArn={setRoleArn}
          awsRegion=""
          setAwsRegion={setAwsRegion}
          inputErrors={inputErrors}
          setInputErrors={setInputErrors}
        />
      </MainContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();

    const nameInput = utils.getByTestId('create-ses-sender-form-name-input');
    fireEvent.change(nameInput, { target: { value: 'test name' } });
    fireEvent.blur(nameInput);
    expect(setSenderName).toBeCalledWith('test name');

    const emailInput = utils.getByTestId('create-ses-sender-form-email-input');
    fireEvent.change(emailInput, { target: { value: 'test@email.com' } });
    fireEvent.blur(emailInput);
    expect(setEmail).toBeCalledWith('test@email.com');

    const roleArnInput = utils.getByTestId(
      'create-ses-sender-form-role-arn-input'
    );
    fireEvent.change(roleArnInput, { target: { value: 'test-role' } });
    fireEvent.blur(roleArnInput);
    expect(setRoleArn).toBeCalledWith('test-role');

    const awsRegionInput = utils.getByTestId(
      'create-ses-sender-form-aws-region-input'
    );
    fireEvent.change(awsRegionInput, { target: { value: 'us-east-2' } });
    fireEvent.blur(awsRegionInput);
    expect(setAwsRegion).toBeCalledWith('us-east-2');
  });

  it('renders errors', () => {
    const setSenderName = jest.fn();
    const setEmail = jest.fn();
    const setRoleArn = jest.fn();
    const setAwsRegion = jest.fn();
    const setInputErrors = jest.fn();
    const inputErrors = {
      senderName: ['test error'],
      email: ['test error'],
      awsRegion: ['test error'],
      roleArn: ['test error'],
    };
    const utils = render(
      <MainContext.Provider value={{ ...mainStateMock, tooltipSupport: false}}>
        <CreateSESSenderForm
          senderName="test"
          setSenderName={setSenderName}
          email=""
          setEmail={setEmail}
          roleArn=""
          setRoleArn={setRoleArn}
          awsRegion=""
          setAwsRegion={setAwsRegion}
          inputErrors={inputErrors}
          setInputErrors={setInputErrors}
        />
      </MainContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });
});
