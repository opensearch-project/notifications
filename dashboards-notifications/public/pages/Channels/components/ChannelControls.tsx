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
import React, { useContext, useEffect, useState } from 'react';
import {
  CHANNEL_TYPE,
  NOTIFICATION_SOURCE,
} from '../../../../public/utils/constants';
import { MainContext } from '../../Main/Main';
import { ChannelFiltersType } from '../types';

interface ChannelControlsProps {
  onSearchChange: (search: string) => void;
  filters: ChannelFiltersType;
  onFiltersChange: (filters: ChannelFiltersType) => void;
}

export const ChannelControls = (props: ChannelControlsProps) => {
  const mainStateContext = useContext(MainContext)!;
  const [isStatePopoverOpen, setIsStatePopoverOpen] = useState(false);
  const [stateItems, setStateItems] = useState([
    { field: 'true', display: 'Active', checked: 'off' },
    { field: 'false', display: 'Muted', checked: 'off' },
  ]);
  const [isTypePopoverOpen, setIsTypePopoverOpen] = useState(false);
  const [typeItems, setTypeItems] = useState(
    Object.entries(mainStateContext.availableChannels).map(([key, value]) => ({
      field: key,
      display: value,
      checked: 'off',
    }))
  );

  useEffect(() => {
    const newItems = typeItems.filter(
      ({ field }) =>
        !!mainStateContext.availableChannels[field as keyof typeof CHANNEL_TYPE]
    );
    if (newItems.length !== typeItems.length) setTypeItems(newItems);
  }, [mainStateContext.availableChannels]);

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
      case 'type':
        setTypeItems(newItems);
        newFilters.type = checkedItems;
        break;
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
                  onClick={() => updateItem(typeItems, index, 'type')}
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
