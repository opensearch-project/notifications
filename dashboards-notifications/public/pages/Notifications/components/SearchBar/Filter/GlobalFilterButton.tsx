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
