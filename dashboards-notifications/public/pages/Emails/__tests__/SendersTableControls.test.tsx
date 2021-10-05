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
import { SendersTableControls } from '../components/tables/SendersTableControls';

describe('<SendersTableControls /> spec', () => {
  it('renders the component', () => {
    const onSearchChange = jest.fn();
    const onFiltersChange = jest.fn();
    const { container } = render(
      <MainContext.Provider value={mainStateMock}>
        <SendersTableControls
          onSearchChange={onSearchChange}
          filters={{encryptionMethod: []}}
          onFiltersChange={onFiltersChange}
        />
      </MainContext.Provider>
    );
    expect(container.firstChild).toMatchSnapshot();
  });

  it('searches with input query', () => {
    const onSearchChange = jest.fn();
    const onFiltersChange = jest.fn();
    const utils = render(
      <MainContext.Provider value={mainStateMock}>
        <SendersTableControls
          onSearchChange={onSearchChange}
          filters={{encryptionMethod: ["start_tls"]}}
          onFiltersChange={onFiltersChange}
        />
      </MainContext.Provider>
    );
    const input = utils.getByPlaceholderText('Search');

    fireEvent.change(input, { target: { value: 'test' } });
    expect(onSearchChange).toBeCalledWith('test');
  });

  it('changes filters', () => {
    const onSearchChange = jest.fn();
    const onFiltersChange = jest.fn();
    const utils = render(
      <MainContext.Provider value={mainStateMock}>
        <SendersTableControls
          onSearchChange={onSearchChange}
          filters={{encryptionMethod: []}}
          onFiltersChange={onFiltersChange}
        />
      </MainContext.Provider>
    );
    fireEvent.click(utils.getByText('Encryption method'));
    fireEvent.click(utils.getByText('TLS'));
    fireEvent.click(utils.getByText('None'));
    expect(onFiltersChange).toBeCalledWith({ encryptionMethod: ["start_tls", "none"] });
  });
});
