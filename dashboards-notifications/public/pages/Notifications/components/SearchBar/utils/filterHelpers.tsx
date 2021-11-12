/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { EuiComboBox, EuiFieldText, EuiFormRow, EuiSpacer } from '@elastic/eui';
import React from 'react';
import {
  CHANNEL_TYPE,
  NOTIFICATION_SOURCE,
  SEVERITY_TYPE,
} from '../../../../../utils/constants';
import { FilterType } from '../Filter/Filters';

export interface FilterParamsType {
  'status_list.config_name'?: string[];
  'status_list.config_type'?: Array<keyof typeof CHANNEL_TYPE>;
  'event_source.feature'?: Array<keyof typeof NOTIFICATION_SOURCE>;
  'event_source.severity'?: Array<keyof typeof SEVERITY_TYPE>;
  'status_list.delivery_status.status_code'?: string[];
}

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
    return [
      { label: 'is' },
      // { label: 'is not' },
    ];
  }
  return [
    { label: 'is' },
    // { label: 'is not' },
    { label: 'is one of' },
    // { label: 'is not one of' },
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
  setValue: (v: any) => void,
  availableChannels: { [feature: string]: string }
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

  if (field === 'Channel type')
    options = Object.entries(availableChannels).map(([key, value]) => ({
      label: value,
      value: key,
    }));
  else if (field === 'Severity')
    options = Object.entries(SEVERITY_TYPE).map(([key, value]) => ({
      label: value,
      value: key,
    }));
  else if (field === 'Source')
    options = Object.entries(NOTIFICATION_SOURCE).map(([key, value]) => ({
      label: value,
      value: key,
    }));
  else if (field === 'Status')
    options = [{ label: 'Sent' }, { label: 'Error' }];

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

const toBackendField = (field: FilterFieldType) => {
  switch (field) {
    case 'Channel':
      return 'status_list.config_name';
    case 'Channel type':
      return 'status_list.config_type';
    case 'Severity':
      return 'event_source.severity';
    case 'Source':
      return 'event_source.feature';
    case 'Status':
      return 'status_list.delivery_status.status_code';
    default:
      return '';
  }
};

export const filtersToQueryParams = (filters: FilterType[]) => {
  const filterParams: FilterParamsType = {};
  filters
    .filter((filter) => !filter.disabled && filter.value != null)
    .forEach((filter) => {
      const field = toBackendField(filter.field);
      if (!field) return;
      if (!filterParams[field]) filterParams[field] = [];

      if (field === 'status_list.delivery_status.status_code') {
        const queryForSuccess =
          (filter.value === 'Sent' && !filter.inverted) ||
          (filter.value === 'Error' && filter.inverted);
        filterParams[field]!.push(queryForSuccess ? '200' : '!200');
        return;
      }

      const inverted = filter.inverted ? '!' : '';
      const values =
        typeof filter.value === 'string'
          ? [filter.value]
          : filter.value!.map((option) => option.value || option.label);
      for (const value of values) {
        filterParams[field]!.push(`${inverted}${value}`);
      }
    });
  return filterParams;
};
