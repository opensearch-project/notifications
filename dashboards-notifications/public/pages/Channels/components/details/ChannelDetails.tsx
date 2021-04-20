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
import { ServicesContext } from '../../../../services';
import { BREADCRUMBS, ROUTES } from '../../../../utils/constants';
import { renderTime } from '../../../../utils/helpers';
import { ListItemType } from '../../types';
import { MuteChannelModal } from '../modals/MuteChannelModal';
import { ChannelDetailItems } from './ChannelDetailItems';
import { ChannelDetailsActions } from './ChannelDetailsActions';
import { ChannelSettingsDetails } from './ChannelSettingsDetails';

interface ChannelDetailsProps extends RouteComponentProps<{ id: string }> {}

export function ChannelDetails(props: ChannelDetailsProps) {
  const coreContext = useContext(CoreServicesContext)!;
  const servicesContext = useContext(ServicesContext)!;
  const id = props.match.params.id;
  const [channel, setChannel] = useState<ChannelItemType>();

  useEffect(() => {
    coreContext.chrome.setBreadcrumbs([
      BREADCRUMBS.NOTIFICATIONS,
      BREADCRUMBS.CHANNELS,
      {
        text: channel?.name || 'Ops_channel',
        href: `${BREADCRUMBS.CHANNEL_DETAILS.href}/${id}`,
      },
    ]);
    refresh();
  }, []);

  const refresh = async () => {
    const response = await servicesContext.notificationService.getChannel(id);
    setChannel(response);
  };

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
        <>
          <EuiFlexItem grow={false}>
            {channel && <ChannelDetailsActions channel={channel} />}
          </EuiFlexItem>
          <EuiFlexItem grow={false}>
            <ModalConsumer>
              {({ onShow }) => (
                <EuiButton
                  size="s"
                  iconType={channel?.enabled ? 'bellSlash' : 'bell'}
                  onClick={() => {
                    if (!channel) return;
                    if (channel.enabled) {
                      onShow(MuteChannelModal, { channels: [channel] });
                    } else {
                      coreContext.notifications.toasts.addSuccess(
                        `${channel.name} successfully unmuted.`
                      );
                    }
                  }}
                >
                  {channel?.enabled ? 'Mute channel' : 'Unmute channel'}
                </EuiButton>
              )}
            </ModalConsumer>
          </EuiFlexItem>
        </>
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
