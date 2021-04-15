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

import React from 'react';
import { ChannelItemType } from '../../../../../models/interfaces';
import { CHANNEL_TYPE } from '../../../../utils/constants';
import { ListItemType } from '../../types';
import { ChannelDetailItems } from './ChannelDetailItems';

interface ChannelSettingsDetailsProps {
  channel: ChannelItemType | undefined;
}

export function ChannelSettingsDetails(props: ChannelSettingsDetailsProps) {
  if (!props.channel) return null;
  let settingsList: Array<ListItemType>;
  if (props.channel.type === CHANNEL_TYPE.SLACK) {
    settingsList = [
      {
        title: 'Channel type',
        description: CHANNEL_TYPE.SLACK,
      },
      {
        title: 'Webhook URL',
        description: props.channel.destination.slack.url || '-',
      },
    ];
  } else if (props.channel.type === CHANNEL_TYPE.CHIME) {
    settingsList = [
      {
        title: 'Channel type',
        description: CHANNEL_TYPE.CHIME,
      },
      {
        title: 'Webhook URL',
        description: props.channel.destination.chime.url || '-',
      },
    ];
  } else {
    const recipients = props.channel.destination.email.email_account_id;
    settingsList = [
      {
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
        description: 
      },
      }
    ]
  }

  return (
    <>
      <ChannelDetailItems listItems={settingsList} />
    </>
  );
}
