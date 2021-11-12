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

import {
  EuiButtonEmpty,
  EuiDescriptionList,
  EuiFlexGroup,
  EuiFlexItem,
  EuiFlyout,
  EuiFlyoutBody,
  EuiFlyoutFooter,
  EuiFlyoutHeader,
  EuiLink,
  EuiSpacer,
  EuiTitle,
} from '@elastic/eui';
import _ from 'lodash';
import React from 'react';
import { NotificationItem } from '../../../../../../models/interfaces';
import { ModalRootProps } from '../../../../../components/Modal/ModalRoot';
import { NOTIFICATION_SOURCE } from '../../../../../utils/constants';
import { renderTime } from '../../../../../utils/helpers';
import { getReferenceText, getReferenceURL } from '../../../utils/helpers';
import { ChannelCard } from './ChannelCard';

interface TableFlyoutProps extends ModalRootProps {
  notificationItem: NotificationItem;
  onClose: () => void;
}

export function TableFlyout(props: TableFlyoutProps) {
  return (
    <>
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
                description: props.notificationItem.event_source.title,
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
                    description: (
                      <EuiLink
                        href={getReferenceURL(props.notificationItem)}
                        target="_blank"
                        external
                      >
                        {getReferenceText(props.notificationItem)}
                      </EuiLink>
                    ),
                  },
                ]}
              />
            </EuiFlexItem>
            <EuiFlexItem>
              <EuiDescriptionList
                listItems={[
                  {
                    title: 'Source type',
                    description: _.get(
                      NOTIFICATION_SOURCE,
                      props.notificationItem.event_source.feature,
                      '-'
                    ),
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
                  props.notificationItem.last_updated_time_ms
                ),
              },
            ]}
          />
          <EuiSpacer />
          <EuiTitle size="xs">
            <h4>Channels sent</h4>
          </EuiTitle>
          {props.notificationItem.status_list.map((channelStatus) => (
            <div key={`channel-card-${channelStatus.config_id}`}>
              <EuiSpacer size="s" />
              <ChannelCard channel={channelStatus} onClose={props.onClose} />
            </div>
          ))}
        </EuiFlyoutBody>
        <EuiFlyoutFooter>
          <EuiButtonEmpty iconType="cross" onClick={props.onClose} flush="left">
            Close
          </EuiButtonEmpty>
        </EuiFlyoutFooter>
      </EuiFlyout>
    </>
  );
}
