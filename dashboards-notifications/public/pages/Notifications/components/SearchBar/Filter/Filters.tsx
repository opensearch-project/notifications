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
  EuiBadge,
  EuiContextMenu,
  EuiContextMenuPanelDescriptor,
  EuiFlexGroup,
  EuiFlexItem,
  EuiIcon,
  EuiPopover,
  EuiTextColor,
} from '@elastic/eui';
import { EuiComboBoxOptionOption } from '@elastic/eui/src/components/combo_box/types';
import React, { useMemo, useState } from 'react';
import {
  FilterFieldType,
  FilterOperatorType,
  isSingleSelection,
} from '../utils/filterHelpers';
import { AddFilterButton } from './AddFilterButton';
import { FilterEditPopover } from './FilterEditPopover';
import { GlobalFilterButton } from './GlobalFilterButton';

export interface FilterType {
  field: FilterFieldType;
  operator: FilterOperatorType;
  value: string | Array<EuiComboBoxOptionOption<string>> | null;
  inverted: boolean;
  disabled: boolean;
}

interface FiltersProps {
  filters: FilterType[];
  setFilters: (filters: FilterType[]) => void;
}

export function Filters(props: FiltersProps) {
  // set a filter at an index. if newFilter doesn't exist, remove filter at the index
  // if index doesn't exist, append newFilter to the end
  const setFilter = (newFilter: FilterType | null, index?: number) => {
    if (index === undefined) index = props.filters.length;
    const newFilters = [...props.filters];
    if (newFilter) newFilters.splice(index, 1, newFilter);
    else newFilters.splice(index, 1);
    props.setFilters(newFilters);
  };

  const getFilterPopoverPanels = (
    filter: FilterType,
    index: number,
    closePopover: () => void
  ): EuiContextMenuPanelDescriptor[] => [
    {
      id: 0,
      items: [
        {
          name: 'Edit filter',
          icon: <EuiIcon type="invert" size="m" />,
          panel: 1,
        },
        {
          name: `${filter.inverted ? 'Include' : 'Exclude'} results`,
          icon: (
            <EuiIcon
              type={filter.inverted ? 'plusInCircle' : 'minusInCircle'}
              size="m"
            />
          ),
          onClick: () => {
            filter.inverted = !filter.inverted;
            setFilter(filter, index);
          },
        },
        {
          name: filter.disabled ? 'Re-enable' : 'Temporarily disable',
          icon: (
            <EuiIcon type={filter.disabled ? 'eye' : 'eyeClosed'} size="m" />
          ),
          onClick: () => {
            filter.disabled = !filter.disabled;
            setFilter(filter, index);
          },
        },
        {
          name: 'Delete',
          icon: <EuiIcon type="trash" size="m" />,
          onClick: () => setFilter(null, index),
        },
      ],
    },
    {
      id: 1,
      width: 430,
      title: 'Edit filter',
      content: (
        <div style={{ margin: 15 }}>
          <FilterEditPopover
            filter={filter}
            index={index}
            setFilter={setFilter}
            closePopover={closePopover}
          />
        </div>
      ),
    },
  ];

  const renderFilters = () => {
    const FilterBadge = ({
      filter,
      index,
    }: {
      filter: FilterType;
      index: number;
    }) => {
      const [isPopoverOpen, setIsPopoverOpen] = useState(false);
      const className =
        'globalFilterItem' +
        (filter.disabled ? ' globalFilterItem-isDisabled' : '') +
        (filter.inverted ? ' globalFilterItem-isExcluded' : '');

      let singleSelection = isSingleSelection(filter);
      let value;
      if (typeof filter.value === 'string') {
        value = filter.value;
      } else if (Array.isArray(filter.value)) {
        // combo box array
        value = filter.value.map((option) => option.label).join(', ');
        if (!singleSelection) value = 'is one of ' + value;
      }

      const filterLabel = filter.inverted ? (
        <>
          <EuiTextColor color={filter.disabled ? 'default' : 'danger'}>
            {'NOT '}
          </EuiTextColor>
          <EuiTextColor color="default">{`${filter.field}: ${value}`}</EuiTextColor>
        </>
      ) : (
        `${filter.field}: ${value}`
      );

      const badge = (
        <EuiBadge
          className={className}
          onClick={() => setIsPopoverOpen(true)}
          onClickAriaLabel="Open filter settings"
          color={filter.disabled ? '#e7e9f0' : 'hollow'}
          iconType="cross"
          iconSide="right"
          iconOnClick={() => setFilter(null, index)}
          iconOnClickAriaLabel="Remove filter"
        >
          {filterLabel}
        </EuiBadge>
      );
      return (
        <EuiFlexItem grow={false} key={`filter-${index}`}>
          <EuiPopover
            isOpen={isPopoverOpen}
            closePopover={() => setIsPopoverOpen(false)}
            panelPaddingSize="none"
            button={badge}
          >
            <EuiContextMenu
              initialPanelId={0}
              panels={getFilterPopoverPanels(filter, index, () =>
                setIsPopoverOpen(false)
              )}
            />
          </EuiPopover>
        </EuiFlexItem>
      );
    };

    return (
      <>
        {props.filters.length > 0
          ? props.filters.map((filter, i) => (
              <FilterBadge filter={filter} index={i} key={i} />
            ))
          : null}
      </>
    );
  };

  const filterComponents = useMemo(() => renderFilters(), [props.filters]);

  return (
    <>
      <EuiFlexGroup
        gutterSize="xs"
        alignItems="center"
        responsive={false}
        style={{ minHeight: 32 }}
      >
        <EuiFlexItem grow={false}>
          <GlobalFilterButton
            filters={props.filters}
            setFilters={props.setFilters}
          />
        </EuiFlexItem>
        {filterComponents}
        <EuiFlexItem grow={false}>
          <AddFilterButton setFilter={setFilter} />
        </EuiFlexItem>
      </EuiFlexGroup>
    </>
  );
}
