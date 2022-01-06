/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { fireEvent, render } from '@testing-library/react';
import { CHANNEL_TYPE } from '../../../utils/constants';
import { FilterType } from '../components/SearchBar/Filter/Filters';
import {
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
    expect(channelOperators).toEqual([
      { label: 'is' },
      // { label: 'is not' },
    ]);

    const sourceOperators = getFilterOperatorOptions('Source');
    expect(sourceOperators).toEqual([
      { label: 'is' },
      // { label: 'is not' },
      { label: 'is one of' },
      // { label: 'is not one of' },
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
    const utils = render(
      getValueComponent('Channel', 'is', 0, setValue, CHANNEL_TYPE)
    );
    expect(utils.container.firstChild).toMatchSnapshot();

    const input = utils.container.querySelector('input')!;
    fireEvent.change(input, { target: { value: '100' } });
    expect(setValue).toBeCalledWith('100');
  });

  it('renders combobox', () => {
    const setValue = jest.fn();
    expect(
      render(getValueComponent('Channel type', 'is', 0, setValue, CHANNEL_TYPE))
        .container.firstChild
    ).toMatchSnapshot();
    expect(
      render(getValueComponent('Severity', 'is', 0, setValue, CHANNEL_TYPE))
        .container.firstChild
    ).toMatchSnapshot();
    expect(
      render(getValueComponent('Source', 'is', 0, setValue, CHANNEL_TYPE))
        .container.firstChild
    ).toMatchSnapshot();
    expect(
      render(getValueComponent('Status', 'is', 0, setValue, CHANNEL_TYPE))
        .container.firstChild
    ).toMatchSnapshot();
  });

  it('checks for single selection', () => {
    expect(isSingleSelection('is one of')).toBeFalsy;
    expect(isSingleSelection('is not')).toBeFalsy;
    expect(isSingleSelection({ operator: 'is not one of' } as FilterType))
      .toBeFalsy;
  });

  // not implemented
  it.skip('returns query string from filters', () => {
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
