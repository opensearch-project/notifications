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
  EuiDescriptionList,
  EuiFlexGroup,
  EuiFlexItem,
  EuiSpacer,
} from '@elastic/eui';
import React from 'react';
import { ListItemType } from '../../types';

interface ChannelDetailItemsProps {
  listItems: Array<ListItemType>;
}

export function ChannelDetailItems(props: ChannelDetailItemsProps) {
  const getPaddedDescriptionList = (list: Array<ListItemType>) => {
    return list
      .concat(
        new Array(Math.ceil(list.length / 3) * 3 - list.length).fill(null)
      )
      .reduce(
        (rows: Array<Array<ListItemType>>, item: ListItemType, i: number) => {
          if (i % 3 === 0) rows.push([item]);
          else rows[rows.length - 1].push(item);
          return rows;
        },
        []
      );
  };

  return (
    <>
      {getPaddedDescriptionList(props.listItems).map((row, rowIndex) => (
        <div key={`channel-description-row-${rowIndex}`}>
          <EuiSpacer size="s" />
          <EuiFlexGroup>
            {row.map((item, itemIndex) => (
              <EuiFlexItem key={`channel-description-item-${itemIndex}`}>
                {item && (
                  <EuiDescriptionList
                    style={{ wordBreak: 'break-word', whiteSpace: 'pre-line' }}
                    listItems={[item]}
                  />
                )}
              </EuiFlexItem>
            ))}
          </EuiFlexGroup>
          <EuiSpacer size="m" />
        </div>
      ))}
    </>
  );
}
