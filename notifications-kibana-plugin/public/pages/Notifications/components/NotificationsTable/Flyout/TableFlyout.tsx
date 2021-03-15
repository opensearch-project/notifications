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
  EuiButtonEmpty,
  EuiDescriptionList,
  EuiFlexGroup,
  EuiFlexItem,
  EuiFlyout,
  EuiFlyoutBody,
  EuiFlyoutFooter,
  EuiFlyoutHeader,
  EuiSpacer,
  EuiTitle,
} from '@elastic/eui';
import React from 'react';
import { NotificationItem } from '../../../../../../models/interfaces';
import { ModalRootProps } from '../../../../../components/Modal/ModalRoot';
import { renderTime } from '../../../../../utils/helpers';
import { ChannelCard } from './ChannelCard';

interface TableFlyoutProps extends ModalRootProps {
  notificationItem: NotificationItem;
  onClose: () => void;
}

export function TableFlyout(props: TableFlyoutProps) {
  return (
    <>
      {props.notificationItem !== null ? (
        <EuiFlyout size="s" onClose={props.onClose}>
          <EuiFlyoutHeader hasBorder>
            <EuiTitle>
              <h2>Notification details</h2>
            </EuiTitle>
          </EuiFlyoutHeader>
          <EuiFlyoutBody>
            <EuiDescriptionList
              listItems={[
                {
                  title: 'Notification',
                  description: props.notificationItem.title,
                },
              ]}
            />
            <EuiSpacer />
            <EuiFlexGroup>
              <EuiFlexItem>
                <EuiDescriptionList
                  listItems={[
                    {
                      title: 'Source',
                      description: 'TODO',
                    },
                  ]}
                />
              </EuiFlexItem>
              <EuiFlexItem>
                <EuiDescriptionList
                  listItems={[
                    {
                      title: 'Source type',
                      description: props.notificationItem.source,
                    },
                  ]}
                />
              </EuiFlexItem>
            </EuiFlexGroup>
            <EuiSpacer />
            <EuiDescriptionList
              listItems={[
                {
                  title: 'Time sent',
                  description: renderTime(
                    props.notificationItem.lastUpdatedTime
                  ),
                },
              ]}
            />
            <EuiSpacer />
            <EuiTitle size="xs">
              <h4>Channels sent</h4>
            </EuiTitle>
            {props.notificationItem.statusList.map((channelStatus) => (
              <div key={`channel-card-${channelStatus.configId}`}>
                <EuiSpacer size="s" />
                <ChannelCard channel={channelStatus} onClose={props.onClose} />
              </div>
            ))}
          </EuiFlyoutBody>
          <EuiFlyoutFooter>
            <EuiButtonEmpty
              iconType="cross"
              onClick={props.onClose}
              flush="left"
            >
              Close
            </EuiButtonEmpty>
          </EuiFlyoutFooter>
        </EuiFlyout>
      ) : null}
    </>
  );
}
