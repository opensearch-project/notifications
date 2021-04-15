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

import { EuiLink } from '@elastic/eui';
import React from 'react';
import { ChannelItemType } from '../../../../../models/interfaces';
import { ModalConsumer } from '../../../../components/Modal';
import { CHANNEL_TYPE } from '../../../../utils/constants';
import { ListItemType } from '../../types';
import { DetailsListModal } from '../modals/DetailsListModal';
import { ChannelDetailItems } from './ChannelDetailItems';

interface ChannelSettingsDetailsProps {
  channel: ChannelItemType | undefined;
}

export function ChannelSettingsDetails(props: ChannelSettingsDetailsProps) {
  if (!props.channel) return null;

  const settingsList: Array<ListItemType> = [
    {
      title: 'Channel type',
      description: props.channel.type,
    },
  ];

  if (props.channel.type === CHANNEL_TYPE.SLACK) {
    settingsList.push(
      ...[
        {
          title: 'Webhook URL',
          description: props.channel.destination.slack.url || '-',
        },
      ]
    );
  } else if (props.channel.type === CHANNEL_TYPE.CHIME) {
    settingsList.push(
      ...[
        {
          title: 'Webhook URL',
          description: props.channel.destination.chime.url || '-',
        },
      ]
    );
  } else if (props.channel.type === CHANNEL_TYPE.EMAIL) {
    const recipients = props.channel.destination.email.recipients;
    const recipientsDescription = (
      <>
        {recipients.slice(0, 5).join(', ')}
        {recipients.length > 5 && (
          <>
            {' '}
            <ModalConsumer>
              {({ onShow }) => (
                <EuiLink
                  onClick={() =>
                    onShow(DetailsListModal, {
                      header: `Default recipients (${recipients.length})`,
                      title: 'Recipients',
                      items: recipients,
                    })
                  }
                >
                  {recipients.length - 5} more
                </EuiLink>
              )}
            </ModalConsumer>
          </>
        )}
      </>
    );
    settingsList.push(
      ...[
        {
          title: 'Sender',
          description: props.channel.destination.email.email_account_id || '-',
        },
        {
          title: 'Default recipients',
          description: recipientsDescription,
        },
        {
          title: 'Email header',
          description: props.channel.destination.email.header
            ? 'Enabled'
            : 'Disabled',
        },
        {
          title: 'Email footer',
          description: props.channel.destination.email.footer
            ? 'Enabled'
            : 'Disabled',
        },
      ]
    );
  }

  return (
    <>
      <ChannelDetailItems listItems={settingsList} />
    </>
  );
}
