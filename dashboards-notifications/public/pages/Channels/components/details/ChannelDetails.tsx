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
  EuiButton,
  EuiFlexGroup,
  EuiFlexItem,
  EuiHealth,
  EuiSpacer,
  EuiTitle,
} from '@elastic/eui';
import React, { useContext, useEffect, useState } from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { ChannelItemType } from '../../../../../models/interfaces';
import {
  ContentPanel,
  ContentPanelActions,
} from '../../../../components/ContentPanel';
import { CoreServicesContext } from '../../../../components/coreServices';
import { ModalConsumer } from '../../../../components/Modal';
import { BREADCRUMBS, ROUTES } from '../../../../utils/constants';
import { renderTime } from '../../../../utils/helpers';
import { ChannelDetailItems } from './ChannelDetailItems';
import { ChannelSettingsDetails } from './ChannelSettingsDetails';
import { DeleteChannelModal } from '../modals/DeleteChannelModal';
import { MuteChannelModal } from '../modals/MuteChannelModal';
import { ListItemType } from '../../types';

interface ChannelDetailsProps extends RouteComponentProps<{ id: string }> {}

export function ChannelDetails(props: ChannelDetailsProps) {
  const context = useContext(CoreServicesContext)!;
  const id = props.match.params.id;
  const [channel, setChannel] = useState<ChannelItemType>();

  useEffect(() => {
    context.chrome.setBreadcrumbs([
      BREADCRUMBS.NOTIFICATIONS,
      BREADCRUMBS.CHANNELS,
      {
        text: channel?.name || 'Ops_channel',
        href: `${BREADCRUMBS.CHANNEL_DETAILS.href}/${id}`,
      },
    ]);
    setChannel({
      id,
      name: 'Ops_channel',
      enabled: true,
      type: 'CUSTOM_WEBHOOK',
      allowedFeatures: ['Alerting', 'Reporting'],
      lastUpdatedTime: new Date().getTime(),
      description: 'This group will send to all operational team members.',
      destination: {
        slack: {
          url:
            'https://hooks.slack.com/services/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',
        },
        chime: {
          url: 'https://chime',
        },
        email: {
          email_account_id: 'robot@gmail.com',
          recipients: [
            'Team 2',
            'cyberadmin@company.com',
            'Ops_team_weekly',
            'Team 5',
            'bot@company.com',
            'Team 7',
            'security_pos@company.com',
          ],
        },
        ses: {
          source_arn: '',
          from: 'ses@test.com',
        },
        sns: {
          topic_arn: 'arn:aws:sns:us-east-1:24586493349034:es-alerting-test',
          role_arn: 'arn:aws:sns:us-east-1:24586493349034:es-alerting-test',
        },
        custom_webhook: {
          host: 'https:hooks.myhost.com',
          port: 21,
          path: 'custompath',
          parameters: {
            Parameter1: 'value1',
            Parameter2: 'value2',
            Parameter3: 'value3',
            Parameter4: 'value4',
            Parameter5: 'value5',
            Parameter6: 'value6',
            Parameter7: 'value7',
            Parameter8: 'value8',
          },
          headers: {
            'Content-Type': 'application/JSON',
            Header1: 'value1',
            Header2: 'value2',
            Header3: 'value3',
            Header4: 'value4',
            Header5: 'value5',
            Header6: 'value6',
            Header7: 'value7',
            Header8: 'value8',
          }
        }
      },
    });
  }, []);

  const nameList: Array<ListItemType> = [
    {
      title: 'Channel name',
      description: channel?.name || '-',
    },
    {
      title: 'Description',
      description: channel?.description || '-',
    },
    {
      title: 'Last updated',
      description: renderTime(channel?.lastUpdatedTime || -1),
    },
  ];

  const sources: Array<ListItemType> = [
    {
      title: 'Notification sources',
      description: channel?.allowedFeatures.join(', ') || '-',
    },
  ];

  return (
    <>
      <EuiFlexGroup
        alignItems="center"
        gutterSize="m"
        style={{ maxWidth: 1316 }}
      >
        <EuiFlexItem grow={false}>
          <EuiTitle size="l">
            <h1>{channel?.name || '-'}</h1>
          </EuiTitle>
        </EuiFlexItem>
        <EuiFlexItem grow={false}>
          {channel?.enabled === undefined ? null : channel.enabled ? (
            <EuiHealth color="success">Active</EuiHealth>
          ) : (
            <EuiHealth color="subdued">Muted</EuiHealth>
          )}
        </EuiFlexItem>
        <EuiFlexItem />
        <ModalConsumer>
          {({ onShow }) => (
            <>
              <EuiFlexItem grow={false}>
                <EuiButton
                  size="s"
                  color="danger"
                  onClick={() =>
                    onShow(DeleteChannelModal, {
                      channels: [channel],
                    })
                  }
                >
                  Delete
                </EuiButton>
              </EuiFlexItem>
              <EuiFlexItem grow={false}>
                <EuiButton
                  size="s"
                  iconType={channel?.enabled ? 'bellSlash' : 'bell'}
                  onClick={() =>
                    onShow(MuteChannelModal, {
                      channels: [channel],
                      mute: channel?.enabled,
                    })
                  }
                >
                  {channel?.enabled ? 'Mute channel' : 'Unmute channel'}
                </EuiButton>
              </EuiFlexItem>
            </>
          )}
        </ModalConsumer>
      </EuiFlexGroup>

      <EuiSpacer />
      <ContentPanel
        bodyStyles={{ padding: 'initial' }}
        title="Channel configuration"
        titleSize="s"
        panelStyles={{ maxWidth: 1300 }}
        actions={
          <ContentPanelActions
            actions={[
              {
                component: (
                  <EuiButton
                    size="s"
                    href={`#${ROUTES.EDIT_CHANNEL}/${id}?from=details`}
                  >
                    Edit
                  </EuiButton>
                ),
              },
            ]}
          />
        }
      >
        <EuiSpacer size="m" />
        <EuiTitle size="s">
          <h4>Name and description</h4>
        </EuiTitle>
        <ChannelDetailItems listItems={nameList} />

        <EuiSpacer size="xxl" />
        <EuiTitle size="s">
          <h4>Settings</h4>
        </EuiTitle>
        <ChannelSettingsDetails channel={channel} />

        <EuiSpacer size="xxl" />
        <EuiTitle size="s">
          <h4>Availability</h4>
        </EuiTitle>
        <ChannelDetailItems listItems={sources} />

        <EuiSpacer />
      </ContentPanel>
    </>
  );
}
