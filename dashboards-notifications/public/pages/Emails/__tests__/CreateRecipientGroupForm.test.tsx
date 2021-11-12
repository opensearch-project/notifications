/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
import { configure, shallow } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import React from 'react';
import { CreateRecipientGroupForm } from '../components/forms/CreateRecipientGroupForm';

describe('<CreateRecipientGroupForm/> spec', () => {
  configure({ adapter: new Adapter() });

  it('renders the component', () => {
    const emailOptions = [{ label: 'test@test.com' }];
    const setName = jest.fn();
    const setDescription = jest.fn();
    const setSelectedEmailOptions = jest.fn();
    const setEmailOptions = jest.fn();
    const setInputErrors = jest.fn();
    const inputErrors = {
      name: [],
      emailOptions: [],
    };
    const utils = render(
      <CreateRecipientGroupForm
        name="test"
        setName={setName}
        description="test"
        setDescription={setDescription}
        selectedEmailOptions={[]}
        setSelectedEmailOptions={setSelectedEmailOptions}
        emailOptions={emailOptions}
        setEmailOptions={setEmailOptions}
        inputErrors={inputErrors}
        setInputErrors={setInputErrors}
      />
    );
    expect(utils.container.firstChild).toMatchSnapshot();

    const nameInput = utils.getByTestId(
      'create-recipient-group-form-name-input'
    );
    fireEvent.change(nameInput, { target: { value: 'test name' } });
    fireEvent.blur(nameInput);
    expect(setName).toBeCalledWith('test name');

    const descriptionInput = utils.getByTestId(
      'create-recipient-group-form-description-input'
    );
    fireEvent.change(descriptionInput, {
      target: { value: 'test description' },
    });
    fireEvent.blur(descriptionInput);
    expect(setDescription).toBeCalledWith('test description');
  });

  it('changes email options', () => {
    const emailOptions = [{ label: 'test@test.com' }];
    const setName = jest.fn();
    const setDescription = jest.fn();
    const setSelectedEmailOptions = jest.fn();
    const setEmailOptions = jest.fn();
    const setInputErrors = jest.fn();
    const inputErrors = {
      name: [],
      emailOptions: [],
    };
    const wrap = shallow(
      <CreateRecipientGroupForm
        name="test"
        setName={setName}
        description="test"
        setDescription={setDescription}
        selectedEmailOptions={[]}
        setSelectedEmailOptions={setSelectedEmailOptions}
        emailOptions={emailOptions}
        setEmailOptions={setEmailOptions}
        inputErrors={inputErrors}
        setInputErrors={setInputErrors}
      />
    );
    const input = wrap.find(
      '[data-test-subj="create-recipient-group-form-emails-input"]'
    );
    input.simulate('keydown', { keyCode: 65 });
    input.simulate('keydown', { keyCode: 13 });
  });

  it('render errors', () => {
    const emailOptions = [{ label: 'test@test.com' }];
    const setName = jest.fn();
    const setDescription = jest.fn();
    const setSelectedEmailOptions = jest.fn();
    const setEmailOptions = jest.fn();
    const setInputErrors = jest.fn();
    const inputErrors = {
      name: ['test error'],
      emailOptions: ['test error'],
    };
    const utils = render(
      <CreateRecipientGroupForm
        name="test"
        setName={setName}
        description="test"
        setDescription={setDescription}
        selectedEmailOptions={[]}
        setSelectedEmailOptions={setSelectedEmailOptions}
        emailOptions={emailOptions}
        setEmailOptions={setEmailOptions}
        inputErrors={inputErrors}
        setInputErrors={setInputErrors}
      />
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });
});
