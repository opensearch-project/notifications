/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import {
  EuiButton,
  EuiButtonEmpty,
  EuiComboBox,
  EuiComboBoxOptionOption,
  EuiFlexGroup,
  EuiFlexItem,
  EuiFormRow,
  EuiSpacer,
} from '@elastic/eui';
import React, { useContext, useMemo, useState } from 'react';
import { MainContext } from '../../../../Main/Main';
import {
  FilterOperatorType,
  FilterFieldType,
  getFilterOperatorOptions,
  getOperatorString,
  getFilterFieldOptions,
  getValueComponent,
} from '../utils/filterHelpers';
import { FilterType } from './Filters';

interface FilterEditPopoverProps {
  filter?: FilterType;
  closePopover: () => void;
  index?: number;
  setFilter: (newFilter: FilterType, index?: number) => void;
}

export function FilterEditPopover(props: FilterEditPopoverProps) {
  const mainStateContext = useContext(MainContext)!;
  const [selectedFieldOptions, setSelectedFieldOptions] = useState<
    Array<EuiComboBoxOptionOption<string>>
  >(props.filter ? [{ label: props.filter.field }] : []);
  const [selectedOperatorOptions, setSelectedOperatorOptions] = useState<
    Array<EuiComboBoxOptionOption<string>>
  >(
    props.filter
      ? [
          {
            label: getOperatorString(
              props.filter.operator,
              props.filter.inverted
            ),
          },
        ]
      : []
  );
  const [filterValue, setFilterValue] = useState<FilterType['value']>(
    props.filter?.value || []
  );

  const filterFieldOptions = useMemo(getFilterFieldOptions, []);

  return (
    <div style={{ width: 400 }}>
      {/* invisible button workaround to prevent auto focus on context menu panel switch */}
      <button
        style={{
          width: 0,
          height: 0,
          position: 'fixed',
          marginLeft: -10000,
          bottom: 0,
        }}
      />
      <EuiFlexGroup gutterSize="s">
        <EuiFlexItem grow={6}>
          <EuiFormRow label="Field">
            <EuiComboBox
              placeholder="Select a field first"
              isClearable={false}
              options={filterFieldOptions}
              selectedOptions={selectedFieldOptions}
              onChange={(e) => {
                setSelectedFieldOptions(e);
                setSelectedOperatorOptions([]);
                setFilterValue(null);
              }}
              singleSelection={{ asPlainText: true }}
            />
          </EuiFormRow>
        </EuiFlexItem>
        <EuiFlexItem grow={5}>
          <EuiFormRow label="Operator">
            <EuiComboBox
              placeholder={
                selectedFieldOptions.length === 0 ? 'Waiting' : 'Select'
              }
              isClearable={false}
              isDisabled={selectedFieldOptions.length === 0}
              options={
                selectedFieldOptions.length === 0
                  ? []
                  : getFilterOperatorOptions(
                      selectedFieldOptions[0].label as FilterFieldType
                    )
              }
              selectedOptions={selectedOperatorOptions}
              onChange={(e) => {
                setSelectedOperatorOptions(e);
                setFilterValue(null);
              }}
              singleSelection={{ asPlainText: true }}
            />
          </EuiFormRow>
        </EuiFlexItem>
      </EuiFlexGroup>
      {selectedOperatorOptions.length > 0 &&
        getValueComponent(
          selectedFieldOptions[0].label as FilterFieldType,
          selectedOperatorOptions[0].label as FilterOperatorType,
          filterValue,
          setFilterValue,
          mainStateContext.availableChannels
        )}
      <EuiSpacer size="m" />
      <EuiFlexGroup gutterSize="s" justifyContent="flexEnd">
        <EuiFlexItem grow={false}>
          <EuiButtonEmpty
            data-test-subj="filter-popover-cancel-button"
            onClick={props.closePopover}
          >
            Cancel
          </EuiButtonEmpty>
        </EuiFlexItem>
        <EuiFlexItem grow={false}>
          <EuiButton
            fill
            disabled={
              selectedFieldOptions.length === 0 ||
              selectedOperatorOptions.length === 0 ||
              !(filterValue && filterValue.length > 0)
            }
            onClick={() => {
              props.closePopover();
              props.setFilter(
                {
                  field: selectedFieldOptions[0].label as FilterFieldType,
                  operator: selectedOperatorOptions[0]
                    .label as FilterOperatorType,
                  value: filterValue,
                  inverted: selectedOperatorOptions[0].label.includes('not'),
                  disabled: props.filter ? props.filter.disabled : false,
                },
                props.index
              );
            }}
          >
            Save
          </EuiButton>
        </EuiFlexItem>
      </EuiFlexGroup>
    </div>
  );
}
