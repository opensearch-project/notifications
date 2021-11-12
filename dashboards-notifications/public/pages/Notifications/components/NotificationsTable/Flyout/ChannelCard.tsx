/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import {
  EuiCard,
  EuiDescriptionList,
  EuiFlexGroup,
  EuiFlexItem,
  EuiHealth,
  EuiLink,
  EuiText,
} from '@elastic/eui';
import _ from 'lodash';
import React from 'react';
import { ChannelStatus } from '../../../../../../models/interfaces';
import { isStatusCodeSuccess } from '../../../../../services/utils/helper';
import { CHANNEL_TYPE, ROUTES } from '../../../../../utils/constants';

interface ChannelCardProps {
  channel: ChannelStatus;
  onClose: () => void;
}

export function ChannelCard(props: ChannelCardProps) {
  const renderList = (
    title: NonNullable<React.ReactNode>,
    description: NonNullable<React.ReactNode>
  ) => {
    return (
      <EuiDescriptionList
        titleProps={{
          style: {
            color: '#69707D',
            fontSize: '0.6875rem',
            fontWeight: 'normal',
          },
        }}
        listItems={[{ title, description }]}
      />
    );
  };

  const renderStatus = (status: string) => {
    const success = isStatusCodeSuccess(status);
    const color = success ? 'success' : 'danger';
    const label = success ? 'Sent' : 'Error';
    return (
      <EuiHealth color={color}>
        <EuiText size="s">{label}</EuiText>
      </EuiHealth>
    );
  };

  const renderEmailRecipientStatus = () => {
    if (!props.channel.email_recipient_status?.length) return null;
    return (
      <EuiText size="s">
        {props.channel.email_recipient_status.map((status, i) => {
          let statusText = `${status.recipient}: `;
          if (status.delivery_status.status_text.startsWith('Success'))
            statusText += 'Success';
          else if (
            status.delivery_status.status_code === '403' ||
            status.delivery_status.status_code === '404'
          )
            // do not display recipient email if not found or no access
            statusText = status.delivery_status.status_text;
          else statusText += status.delivery_status.status_text;
          return (
            <li key={`${status.recipient}-${i}`}>
              {statusText} ({status.delivery_status.status_code})
            </li>
          );
        })}
      </EuiText>
    );
  };

  return (
    <>
      <EuiCard
        textAlign="left"
        title={
          <EuiLink
            onClick={() => {
              location.assign(
                `#${ROUTES.CHANNEL_DETAILS}/${props.channel.config_id}`
              );
              props.onClose();
            }}
          >
            {props.channel.config_name}
          </EuiLink>
        }
        titleSize="xs"
        description=""
      >
        <EuiFlexGroup>
          <EuiFlexItem>
            {renderList(
              'Channel type',
              _.get(CHANNEL_TYPE, props.channel.config_type, '-')
            )}
          </EuiFlexItem>
          <EuiFlexItem>
            {renderList(
              'Sent status',
              renderStatus(props.channel.delivery_status.status_code)
            )}
          </EuiFlexItem>
        </EuiFlexGroup>
        {!isStatusCodeSuccess(props.channel.delivery_status.status_code) &&
          renderList(
            'Error details',
            <>
              <EuiText style={{ lineBreak: 'anywhere' }} size="s">
                {props.channel.delivery_status.status_text}
              </EuiText>
              {renderEmailRecipientStatus()}
            </>
          )}
      </EuiCard>
    </>
  );
}
