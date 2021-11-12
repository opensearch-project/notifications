/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
