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

import { render } from '@testing-library/react';
import React from 'react';
import { CreateRecipientGroupForm } from '../components/forms/CreateRecipientGroupForm';

describe('<CreateRecipientGroupForm/> spec', () => {
  it('renders the component', () => {
    const emailOptions = [
      {
        label: 'test@test.com',
      },
    ];
    const setName = jest.fn();
    const setDescription = jest.fn();
    const setSelectedEmailOptions = jest.fn();
    const setEmailOptions = jest.fn();
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
      />
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });
});
