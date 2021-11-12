/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { EuiButtonEmpty, EuiPopover, EuiPopoverTitle } from '@elastic/eui';
import React, { useState } from 'react';
import { FilterEditPopover } from './FilterEditPopover';
import { FilterType } from './Filters';

interface AddFilterButtonProps {
  setFilter: (newFilter: FilterType, index?: number) => void;
}

export function AddFilterButton(props: AddFilterButtonProps) {
  const [isPopoverOpen, setIsPopoverOpen] = useState(false);
  const button = (
    <EuiButtonEmpty
      size="xs"
      onClick={() => {
        setIsPopoverOpen(true);
      }}
    >
      + Add filter
    </EuiButtonEmpty>
  );

  return (
    <>
      <EuiPopover
        button={button}
        isOpen={isPopoverOpen}
        closePopover={() => setIsPopoverOpen(false)}
        anchorPosition="downLeft"
        withTitle
      >
        <EuiPopoverTitle>{'Add filter'}</EuiPopoverTitle>
        <FilterEditPopover
          setFilter={props.setFilter}
          closePopover={() => setIsPopoverOpen(false)}
        />
      </EuiPopover>
    </>
  );
}
