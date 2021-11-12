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
