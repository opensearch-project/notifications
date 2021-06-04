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
import _ from 'lodash';
import React, { useContext, useEffect, useState } from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { ChannelItemType } from '../../../../../models/interfaces';
import { ContentPanel } from '../../../../components/ContentPanel';
import { CoreServicesContext } from '../../../../components/coreServices';
import { ModalConsumer } from '../../../../components/Modal';
import { ServicesContext } from '../../../../services';
import { BREADCRUMBS, NOTIFICATION_SOURCE } from '../../../../utils/constants';
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
    ]);
    refresh();
  }, []);

  const refresh = async () => {
    servicesContext.notificationService
      .getChannel(id)
      .then((response) => {
        if (response.config_type === 'email') {
          return servicesContext.notificationService.getEmailConfigDetails(
            response
          );
        }
        return response;
      })
      .then((response) => {
        setChannel(response);
        coreContext.chrome.setBreadcrumbs([
          BREADCRUMBS.NOTIFICATIONS,
          BREADCRUMBS.CHANNELS,
          {
            text: response?.name || '',
            href: `${BREADCRUMBS.CHANNEL_DETAILS.href}/${id}`,
          },
        ]);
      })
      .catch((error) =>
        coreContext.notifications.toasts.addError(error, {
          title: 'There was a problem loading channel.',
        })
      );
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
      description: renderTime(channel?.last_updated_time_ms || -1),
    },
  ];

  const sources: Array<ListItemType> = [
    {
      title: 'Notification sources',
      description:
        channel?.feature_list
          .map((source) => _.get(NOTIFICATION_SOURCE, source, '-'))
          .join(', ') || '-',
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
          <EuiFlexGroup gutterSize="m" alignItems="flexEnd">
            <EuiFlexItem grow={false}>
              <EuiTitle size="l">
                <h1>{channel?.name || '-'}</h1>
              </EuiTitle>
            </EuiFlexItem>
            <EuiFlexItem grow={false} style={{ paddingBottom: 5 }}>
              {channel?.is_enabled === undefined ? null : channel.is_enabled ? (
                <EuiHealth color="success">Active</EuiHealth>
              ) : (
                <EuiHealth color="subdued">Muted</EuiHealth>
              )}
            </EuiFlexItem>
          </EuiFlexGroup>
        </EuiFlexItem>
        <EuiFlexItem />
        <EuiFlexItem grow={false}>
          {channel && <ChannelDetailsActions channel={channel} />}
        </EuiFlexItem>
        <EuiFlexItem grow={false}>
          <ModalConsumer>
            {({ onShow }) => (
              <EuiButton
                data-test-subj="channel-details-mute-button"
                iconType={channel?.is_enabled ? 'bellSlash' : 'bell'}
                onClick={() => {
                  if (!channel) return;
                  if (channel.is_enabled) {
                    onShow(MuteChannelModal, {
                      selected: [channel],
                      setSelected: (selected: ChannelItemType[]) =>
                        setChannel(selected[0]),
                    });
                  } else {
                    const newChannel = { ...channel, is_enabled: true };
                    servicesContext.notificationService
                      .updateConfig(channel.config_id, newChannel)
                      .then((resp) => {
                        coreContext.notifications.toasts.addSuccess(
                          `Channel ${channel.name} successfully unmuted.`
                        );
                        setChannel(newChannel);
                      });
                  }
                }}
              >
                {channel?.is_enabled ? 'Mute channel' : 'Unmute channel'}
              </EuiButton>
            )}
          </ModalConsumer>
        </EuiFlexItem>
      </EuiFlexGroup>

      <EuiSpacer />
      <ContentPanel
        bodyStyles={{ padding: 'initial' }}
        title="Name and description"
        titleSize="s"
        panelStyles={{ maxWidth: 1300 }}
      >
        <ChannelDetailItems listItems={nameList} />
      </ContentPanel>

      <EuiSpacer />

      <ContentPanel
        bodyStyles={{ padding: 'initial' }}
        title="Configurations"
        titleSize="s"
        panelStyles={{ maxWidth: 1300 }}
      >
        <ChannelSettingsDetails channel={channel} />
      </ContentPanel>

      <EuiSpacer />

      <ContentPanel
        bodyStyles={{ padding: 'initial' }}
        title="Availability"
        titleSize="s"
        panelStyles={{ maxWidth: 1300 }}
      >
        <ChannelDetailItems listItems={sources} />
      </ContentPanel>
    </>
  );
}
