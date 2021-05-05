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

import { EuiLink } from '@elastic/eui';
import React from 'react';
import { ChannelItemType } from '../../../../../models/interfaces';
import { ModalConsumer } from '../../../../components/Modal';
import { CHANNEL_TYPE } from '../../../../utils/constants';
import { HeaderItemType, ListItemType } from '../../types';
import { DetailsListModal } from '../modals/DetailsListModal';
import { DetailsTableModal } from '../modals/DetailsTableModal';
import { ChannelDetailItems } from './ChannelDetailItems';

interface ChannelSettingsDetailsProps {
  channel: ChannelItemType | undefined;
}

export function ChannelSettingsDetails(props: ChannelSettingsDetailsProps) {
  if (!props.channel) return null;

  const settingsList: Array<ListItemType> = [];
  const getModalComponent = (
    items: string[] | HeaderItemType[],
    header: string,
    title?: string,
    separator = ', ',
    isParameters?: boolean
  ) => {
    return (
      <>
        <div style={{ whiteSpace: 'pre-line' }}>
          {items
            .slice(0, 5)
            .map((item: string | HeaderItemType) =>
              typeof item === 'string' ? item : `${item.key}: ${item.value}`
            )
            .join(separator)}
        </div>
        {items.length > 5 && (
          <>
            {' '}
            <ModalConsumer>
              {({ onShow }) => (
                <EuiLink
                  onClick={
                    typeof items[0] === 'string'
                      ? () =>
                          onShow(DetailsListModal, {
                            header: `${header} (${items.length})`,
                            title: title,
                            items: items,
                          })
                      : () =>
                          onShow(DetailsTableModal, {
                            header: `${header} (${items.length})`,
                            isParameters,
                            items: items,
                          })
                  }
                >
                  {items.length - 5} more
                </EuiLink>
              )}
            </ModalConsumer>
          </>
        )}
      </>
    );
  };

  const type = props.channel.type as keyof typeof CHANNEL_TYPE;
  if (type === 'SLACK') {
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
  } else if (type === 'CHIME') {
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
  } else if (type === 'SNS') {
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
  } else if (type === 'EMAIL') {
    const recipientsDescription = getModalComponent(
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
  } else if (type === 'CUSTOM_WEBHOOK') {
    const parametersDescription = getModalComponent(
      Object.entries(props.channel.destination.custom_webhook.parameters).map(
        ([key, value]) => ({ key, value } as HeaderItemType)
      ),
      'Query parameters',
      undefined,
      '\n',
      true
    );
    const headersDescription = getModalComponent(
      Object.entries(props.channel.destination.custom_webhook.headers).map(
        ([key, value]) => ({ key, value } as HeaderItemType)
      ),
      'Webhook headers',
      undefined,
      '\n',
      false
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
  } else if (type === 'SES') {
    const recipientsDescription = getModalComponent(
      props.channel.destination.ses.recipients,
      'Default recipients',
      'Recipients'
    );
    settingsList.push(
      ...[
        {
          title: 'Channel type',
          description: CHANNEL_TYPE.SES,
        },
        {
          title: 'Sender',
          description: props.channel.destination.ses.email_account_id || '-',
        },
        {
          title: 'Default recipients',
          description: recipientsDescription,
        },
        {
          title: 'Email header',
          description: props.channel.destination.ses.header
            ? 'Enabled'
            : 'Disabled',
        },
        {
          title: 'Email footer',
          description: props.channel.destination.ses.footer
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
