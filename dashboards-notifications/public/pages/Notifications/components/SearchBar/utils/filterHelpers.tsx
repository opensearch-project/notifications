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

import {
  CHANNEL_TYPE,
  NOTIFICATION_SOURCE,
  NOTIFICATION_STATUS,
} from '../../../../../utils/constants';
import {
  EuiComboBox,
  EuiComboBoxOptionOption,
  EuiFieldText,
  EuiFormRow,
  EuiSpacer,
} from '@elastic/eui';
import React from 'react';
import { FilterType } from '../Filter/Filters';

export type FilterFieldType =
  | 'Channel'
  | 'Channel type'
  | 'Severity'
  | 'Source'
  | 'Status';

export type FilterOperatorType =
  | 'is'
  | 'is not'
  | 'is one of'
  | 'is not one of';

export type FilterValueType = string | number | string[] | number[];

export const getFilterFieldOptions = () => {
  return [
    { label: 'Channel' },
    { label: 'Channel type' },
    { label: 'Severity' },
    { label: 'Source' },
    { label: 'Status' },
  ];
};

export const getFilterOperatorOptions = (field: FilterFieldType) => {
  if (field === 'Channel' || field === 'Status') {
    return [{ label: 'is' }, { label: 'is not' }];
  }
  return [
    { label: 'is' },
    { label: 'is not' },
    { label: 'is one of' },
    { label: 'is not one of' },
  ];
};

export const getOperatorString = (
  operator: FilterOperatorType,
  inverted: boolean
) => {
  if (operator === 'is' || operator === 'is not')
    return inverted ? 'is not' : 'is';
  if (operator === 'is one of' || operator === 'is not one of')
    return inverted ? 'is not one of' : 'is one of';
  return '';
};

export const getValueComponent = (
  field: FilterFieldType,
  operator: FilterOperatorType,
  value: any,
  setValue: (v: any) => void
) => {
  // only Channel filters should be inputbox, others should be combobox
  if (field === 'Channel')
    return (
      <>
        <EuiSpacer size="s" />
        <EuiFormRow label="Value">
          <EuiFieldText
            placeholder="Enter a value"
            value={value || ''}
            onChange={(e) => setValue(e.target.value)}
          />
        </EuiFormRow>
      </>
    );

  let options;

  if (field === 'Channel type') options = Object.values(CHANNEL_TYPE);
  else if (field === 'Severity') options = ['1', '2', '3', '4', '5'];
  else if (field === 'Source') options = Object.values(NOTIFICATION_SOURCE);
  else options = Object.values(NOTIFICATION_STATUS);

  options = options.map((option) => ({ label: option }));

  const singleSelection = isSingleSelection(operator);
  const placeholder = singleSelection ? 'Select a value' : 'Select values';

  return (
    <>
      <EuiSpacer size="s" />
      <EuiFormRow label="Value">
        <EuiComboBox
          fullWidth
          placeholder={placeholder}
          options={options}
          selectedOptions={value || []}
          singleSelection={singleSelection}
          onChange={setValue}
          isClearable={true}
        />
      </EuiFormRow>
    </>
  );
};

export const isSingleSelection = (filterOrOperator: FilterType | string) => {
  const checkSingleSelection = (operator: string) =>
    operator === 'is' || operator === 'is not';

  return checkSingleSelection(
    typeof filterOrOperator === 'string'
      ? filterOrOperator
      : filterOrOperator.operator
  );
};

export const filterToQueryString = (filters: FilterType[]) => {
  // TODO: need to map display names to field names (Channel type => CHANNEL_TYPE ?)
  return filters
    .filter((filter) => !filter.disabled)
    .map((filter) => {
      if (filter.value === null) return '';

      const sign = filter.inverted ? '-' : '+';
      const field = filter.field;
      const value =
        typeof filter.value === 'string'
          ? filter.value
          : filter.value.map((option) => option.label).join(' OR ');

      return `${sign}(${field}:${value})`;
    })
    .join(' ');
};
