/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import {
  EuiButtonIcon,
  EuiContextMenu,
  EuiIcon,
  EuiPopover,
} from '@elastic/eui';
import React, { useState } from 'react';
import { FilterType } from './Filters';

interface GlobalFilterButtonProps {
  filters: FilterType[];
  setFilters: (filters: FilterType[]) => void;
}

export function GlobalFilterButton(props: GlobalFilterButtonProps) {
  const [isPopoverOpen, setIsPopoverOpen] = useState(false);
  const globalPopoverPanels = [
    {
      id: 0,
      title: 'Change all filters',
      items: [
        {
          name: 'Enable all',
          icon: <EuiIcon type="eye" size="m" />,
          onClick: () => {
            props.setFilters(
              props.filters.map((filter) => ({ ...filter, disabled: false }))
            );
            setIsPopoverOpen(false);
          },
        },
        {
          name: 'Disable all',
          icon: <EuiIcon type="eyeClosed" size="m" />,
          onClick: () => {
            props.setFilters(
              props.filters.map((filter) => ({ ...filter, disabled: true }))
            );
            setIsPopoverOpen(false);
          },
        },
        // {
        //   name: 'Invert inclusion',
        //   icon: <EuiIcon type="invert" size="m" />,
        //   onClick: () => {
        //     props.setFilters(
        //       props.filters.map((filter) => ({
        //         ...filter,
        //         inverted: !filter.inverted,
        //       }))
        //     );
        //     setIsPopoverOpen(false);
        //   },
        // },
        {
          name: 'Invert enabled/disabled',
          icon: <EuiIcon type="eye" size="m" />,
          onClick: () => {
            props.setFilters(
              props.filters.map((filter) => ({
                ...filter,
                disabled: !filter.disabled,
              }))
            );
            setIsPopoverOpen(false);
          },
        },
        {
          name: 'Remove all',
          icon: <EuiIcon type="trash" size="m" />,
          onClick: () => {
            props.setFilters([]);
            setIsPopoverOpen(false);
          },
        },
      ],
    },
  ];

  return (
    <EuiPopover
      isOpen={isPopoverOpen}
      closePopover={() => setIsPopoverOpen(false)}
      button={
        <EuiButtonIcon
          onClick={() => setIsPopoverOpen(true)}
          iconType="filter"
          title="Change all filters"
          aria-label="Change all filters"
        />
      }
      anchorPosition="rightUp"
      panelPaddingSize="none"
      withTitle
    >
      <EuiContextMenu initialPanelId={0} panels={globalPopoverPanels} />
    </EuiPopover>
  );
}
