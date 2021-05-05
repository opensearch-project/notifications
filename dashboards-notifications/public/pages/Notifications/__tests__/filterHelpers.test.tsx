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
import { FilterType } from '../components/SearchBar/Filter/Filters';
import {
  filterToQueryString,
  getFilterFieldOptions,
  getFilterOperatorOptions,
  getOperatorString,
  getValueComponent,
  isSingleSelection,
} from '../components/SearchBar/utils/filterHelpers';

describe('test filter helper functions', () => {
  it('returns filter field options', () => {
    const options = getFilterFieldOptions();
    expect(options).toEqual([
      { label: 'Channel' },
      { label: 'Channel type' },
      { label: 'Severity' },
      { label: 'Source' },
      { label: 'Status' },
    ]);
  });

  it('returns filter operator options', () => {
    const channelOperators = getFilterOperatorOptions('Channel');
    expect(channelOperators).toEqual([{ label: 'is' }, { label: 'is not' }]);

    const sourceOperators = getFilterOperatorOptions('Source');
    expect(sourceOperators).toEqual([
      { label: 'is' },
      { label: 'is not' },
      { label: 'is one of' },
      { label: 'is not one of' },
    ]);
  });

  it('returns operator as string', () => {
    expect(getOperatorString('is one of', true)).toEqual('is not one of');
    expect(getOperatorString('is not one of', false)).toEqual('is one of');
    expect(getOperatorString('is', true)).toEqual('is not');
    expect(getOperatorString('is not', true)).toEqual('is not');
  });

  it('renders textfield', () => {
    const setValue = jest.fn();
    const utils = render(getValueComponent('Channel', 'is', 0, setValue));
    expect(utils.container.firstChild).toMatchSnapshot();

    const input = utils.container.querySelector('input')!;
    fireEvent.change(input, { target: { value: '100' } });
    expect(setValue).toBeCalledWith('100');
  });

  it('renders combobox', () => {
    const setValue = jest.fn();
    expect(
      render(getValueComponent('Channel type', 'is', 0, setValue)).container
        .firstChild
    ).toMatchSnapshot();
    expect(
      render(getValueComponent('Severity', 'is', 0, setValue)).container
        .firstChild
    ).toMatchSnapshot();
    expect(
      render(getValueComponent('Source', 'is', 0, setValue)).container
        .firstChild
    ).toMatchSnapshot();
    expect(
      render(getValueComponent('Status', 'is', 0, setValue)).container
        .firstChild
    ).toMatchSnapshot();
  });

  it('checks for single selection', () => {
    expect(isSingleSelection('is one of')).toBeFalsy;
    expect(isSingleSelection('is not')).toBeFalsy;
    expect(isSingleSelection({ operator: 'is not one of' } as FilterType))
      .toBeFalsy;
  });

  it('returns query string from filters', () => {
    const filters: FilterType[] = [
      {
        field: 'Channel',
        operator: 'is',
        value: 'test',
        inverted: false,
        disabled: true,
      },
      {
        field: 'Source',
        operator: 'is not one of',
        value: [{ label: 'Alerting' }, { label: 'ISM' }],
        inverted: true,
        disabled: false,
      },
      {
        field: 'Status',
        operator: 'is',
        value: [{ label: 'Sent' }],
        inverted: false,
        disabled: false,
      },
    ];
    const queryString = filterToQueryString(filters);
    expect(queryString).toEqual('-(Source:Alerting OR ISM) +(Status:Sent)');
  });
});
