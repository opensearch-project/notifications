/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import {
  EuiFieldSearch,
  EuiFilterButton,
  EuiFilterGroup,
  EuiFilterSelectItem,
  EuiFlexGroup,
  EuiFlexItem,
  EuiPopover,
} from '@elastic/eui';
import _ from 'lodash';
import React, { useState } from 'react';
import { ENCRYPTION_TYPE } from '../../../../utils/constants';

interface SendersTableControlsProps {
  onSearchChange: (search: string) => void;
  filters: SendersTableControlsFilterType;
  onFiltersChange: (filters: SendersTableControlsFilterType) => void;
}

export interface SendersTableControlsFilterType {
  encryptionMethod: string[];
}

export const SendersTableControls = (props: SendersTableControlsProps) => {
  const [isEncryptionPopoverOpen, setIsEncryptionPopoverOpen] = useState(false);
  const [encryptionItems, setEncryptionItems] = useState(
    Object.entries(ENCRYPTION_TYPE).map(([key, value]) => ({
      field: key,
      display: value,
      checked: 'off',
    }))
  );

  function updateItem(
    items: Array<{ field: string; display: string; checked: string }>,
    index: number
  ) {
    if (!items[index]) return;
    const newItems = [...items];
    newItems[index].checked = newItems[index].checked === 'off' ? 'on' : 'off';

    const newFilters = _.clone(props.filters);
    const checkedItems = newItems
      .filter((item) => item.checked === 'on')
      .map((item) => item.field);

    setEncryptionItems(newItems);
    newFilters.encryptionMethod = checkedItems;
    props.onFiltersChange(newFilters);
  }

  function isItemSelected(
    items: Array<{ field: string; display: string; checked: string }>
  ) {
    return items
      .map((item) => item.checked === 'on')
      .reduce((flag, curr) => flag || curr, false);
  }

  return (
    <EuiFlexGroup>
      <EuiFlexItem>
        <EuiFieldSearch
          data-test-subj="senders-table-search-input"
          fullWidth={true}
          placeholder="Search"
          onSearch={props.onSearchChange}
        />
      </EuiFlexItem>

      <EuiFlexItem grow={false}>
        <EuiFilterGroup>
          <EuiPopover
            button={
              <EuiFilterButton
                iconType="arrowDown"
                grow={false}
                onClick={() =>
                  setIsEncryptionPopoverOpen(!isEncryptionPopoverOpen)
                }
              >
                {isItemSelected(encryptionItems) ? (
                  <b>Encryption method</b>
                ) : (
                  'Encryption method'
                )}
              </EuiFilterButton>
            }
            isOpen={isEncryptionPopoverOpen}
            closePopover={() => setIsEncryptionPopoverOpen(false)}
            panelPaddingSize="none"
          >
            {encryptionItems.map((item, index) => {
              return (
                <EuiFilterSelectItem
                  key={`smtp-sender-encryption-method-filter-${index}`}
                  checked={item.checked === 'on' ? 'on' : undefined}
                  onClick={() => updateItem(encryptionItems, index)}
                >
                  {item.display}
                </EuiFilterSelectItem>
              );
            })}
          </EuiPopover>
        </EuiFilterGroup>
      </EuiFlexItem>
    </EuiFlexGroup>
  );
};
