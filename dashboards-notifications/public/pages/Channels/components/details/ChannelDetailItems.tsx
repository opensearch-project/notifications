/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
