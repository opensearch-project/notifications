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

  const settingsList: Array<ListItemType> = [];
  const getListModalComponent = (
    list: string[],
    header: string,
    title: string,
    separator = ', '
  ) => {
    return (
      <>
        <div style={{ whiteSpace: 'pre-line' }}>
          {list.slice(0, 5).join(separator)}
        </div>
        {list.length > 5 && (
          <>
            {' '}
            <ModalConsumer>
              {({ onShow }) => (
                <EuiLink
                  onClick={() =>
                    onShow(DetailsListModal, {
                      header: `${header} (${list.length})`,
                      title: title,
                      items: list,
                    })
                  }
                >
                  {list.length - 5} more
                </EuiLink>
              )}
            </ModalConsumer>
          </>
        )}
      </>
    );
  };

  if (props.channel.type === 'SLACK') {
    settingsList.push(
      ...[
        {
          title: 'Channel type',
          description: CHANNEL_TYPE.SLACK,
        },
        {
          title: 'Webhook URL',
          description: props.channel.destination.slack.url || '-',
        },
      ]
    );
  } else if (props.channel.type === 'CHIME') {
    settingsList.push(
      ...[
        {
          title: 'Channel type',
          description: CHANNEL_TYPE.CHIME,
        },
        {
          title: 'Webhook URL',
          description: props.channel.destination.chime.url || '-',
        },
      ]
    );
  } else if (props.channel.type === 'SNS') {
    settingsList.push(
      ...[
        {
          title: 'Channel type',
          description: CHANNEL_TYPE.SNS,
        },
        {
          title: 'SNS topic ARN',
          description: props.channel.destination.sns.topic_arn || '-',
        },
        {
          title: 'IAM role ARN',
          description: props.channel.destination.sns.role_arn || '-',
        },
      ]
    );
  } else if (props.channel.type === 'EMAIL') {
    const recipientsDescription = getListModalComponent(
      props.channel.destination.email.recipients,
      'Default recipients',
      'Recipients'
    );
    settingsList.push(
      ...[
        {
          title: 'Channel type',
          description: CHANNEL_TYPE.EMAIL,
        },
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
  } else if (props.channel.type === 'CUSTOM_WEBHOOK') {
    const parametersDescription = getListModalComponent(
      Object.entries(props.channel.destination.custom_webhook.parameters).map(
        ([key, value]) => `${key}: ${value}`
      ),
      'Query parameters',
      'Parameters',
      '\n'
    );
    const headersDescription = getListModalComponent(
      Object.entries(props.channel.destination.custom_webhook.headers).map(
        ([key, value]) => `${key}: ${value}`
      ),
      'Webhook headers',
      'Headers',
      '\n'
    );
    settingsList.push(
      ...[
        {
          title: 'Channel type',
          description: CHANNEL_TYPE.CUSTOM_WEBHOOK,
        },
        {
          title: 'Host',
          description: props.channel.destination.custom_webhook.host || '-',
        },
        {
          title: 'Port',
          description: props.channel.destination.custom_webhook.port || '-',
        },
        {
          title: 'Path',
          description: props.channel.destination.custom_webhook.path || '-',
        },
        {
          title: 'Query parameters',
          description: parametersDescription,
        },
        {
          title: 'Webhook headers',
          description: headersDescription,
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
