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
import {
  CHANNEL_TYPE,
  NOTIFICATION_SOURCE,
} from '../../../../public/utils/constants';
import { ChannelFiltersType } from '../types';

interface ChannelControlsProps {
  search: string;
  onSearchChange: (search: string) => void;
  filters: ChannelFiltersType;
  onFiltersChange: (filters: ChannelFiltersType) => void;
}

export const ChannelControls = (props: ChannelControlsProps) => {
  const [isStatePopoverOpen, setIsStatePopoverOpen] = useState(false);
  const [stateItems, setStateItems] = useState([
    { field: 'true', display: 'Active', checked: 'off' },
    { field: 'false', display: 'Muted', checked: 'off' },
  ]);
  const [isTypePopoverOpen, setIsTypePopoverOpen] = useState(false);
  const [typeItems, setTypeItems] = useState(
    Object.entries(CHANNEL_TYPE).map(([key, value]) => ({
      field: key,
      display: value,
      checked: 'off',
    }))
  );
  const [isSourcePopoverOpen, setIsSourcePopoverOpen] = useState(false);
  const [sourceItems, setSourceItems] = useState(
    Object.entries(NOTIFICATION_SOURCE).map(([key, value]) => ({
      field: key,
      display: value,
      checked: 'off',
    }))
  );

  function updateItem(
    items: Array<{ field: string; display: string; checked: string }>,
    index: number,
    type: 'state' | 'type' | 'source',
    singleSelect?: boolean
  ) {
    if (!items[index]) return;
    const newItems = [...items];
    if (singleSelect) {
      const checked = newItems[index].checked === 'off' ? 'on' : 'off';
      newItems.forEach((item, i) => (item.checked = 'off'));
      newItems[index].checked = checked;
    } else {
      newItems[index].checked =
        newItems[index].checked === 'off' ? 'on' : 'off';
    }

    const newFilters = _.clone(props.filters);
    const checkedItems = newItems
      .filter((item) => item.checked === 'on')
      .map((item) => item.field);

    switch (type) {
      case 'state':
        setStateItems(newItems);
        newFilters.state = checkedItems[0];
        break;
      case 'source':
        setSourceItems(newItems);
        newFilters.source = checkedItems;
        break;
      case 'type':
        setTypeItems(newItems);
        newFilters.type = checkedItems;
        break
      default:
        break;
    }
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
          fullWidth={true}
          placeholder="Search"
          onSearch={(search) => props.onSearchChange(search)}
        />
      </EuiFlexItem>

      <EuiFlexItem grow={false}>
        <EuiFilterGroup>
          <EuiPopover
            button={
              <EuiFilterButton
                iconType="arrowDown"
                grow={false}
                onClick={() => setIsStatePopoverOpen(!isStatePopoverOpen)}
              >
                {isItemSelected(stateItems) ? <b>Status</b> : 'Status'}
              </EuiFilterButton>
            }
            isOpen={isStatePopoverOpen}
            closePopover={() => setIsStatePopoverOpen(false)}
            panelPaddingSize="none"
          >
            {stateItems.map((item, index) => {
              return (
                <EuiFilterSelectItem
                  key={`channel-state-filter-${index}`}
                  checked={item.checked === 'on' ? 'on' : undefined}
                  onClick={() => {
                    updateItem(stateItems, index, 'state', true);
                    setIsStatePopoverOpen(false);
                  }}
                >
                  {item.display}
                </EuiFilterSelectItem>
              );
            })}
          </EuiPopover>
          <EuiPopover
            button={
              <EuiFilterButton
                iconType="arrowDown"
                grow={false}
                onClick={() => setIsTypePopoverOpen(!isTypePopoverOpen)}
              >
                {isItemSelected(typeItems) ? <b>Type</b> : 'Type'}
              </EuiFilterButton>
            }
            isOpen={isTypePopoverOpen}
            closePopover={() => setIsTypePopoverOpen(false)}
            panelPaddingSize="none"
          >
            {typeItems.map((item, index) => {
              return (
                <EuiFilterSelectItem
                  key={`channel-type-filter-${index}`}
                  checked={item.checked === 'on' ? 'on' : undefined}
                  onClick={() =>
                    updateItem(typeItems, index, 'type')
                  }
                >
                  {item.display}
                </EuiFilterSelectItem>
              );
            })}
          </EuiPopover>
          <EuiPopover
            button={
              <EuiFilterButton
                iconType="arrowDown"
                grow={false}
                onClick={() => setIsSourcePopoverOpen(!isSourcePopoverOpen)}
              >
                {isItemSelected(sourceItems) ? <b>Source</b> : 'Source'}
              </EuiFilterButton>
            }
            isOpen={isSourcePopoverOpen}
            closePopover={() => setIsSourcePopoverOpen(false)}
            panelPaddingSize="none"
          >
            {sourceItems.map((item, index) => {
              return (
                <EuiFilterSelectItem
                  key={`channel-source-filter-${index}`}
                  checked={item.checked === 'on' ? 'on' : undefined}
                  onClick={() =>
                    updateItem(sourceItems, index, 'source')
                  }
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
