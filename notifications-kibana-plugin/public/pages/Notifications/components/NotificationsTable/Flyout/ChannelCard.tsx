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
  EuiCard,
  EuiDescriptionList,
  EuiFlexGroup,
  EuiFlexItem,
  EuiHealth,
  EuiLink,
  EuiText,
} from '@elastic/eui';
import React from 'react';
import { ROUTES } from '../../../../../utils/constants';

interface ChannelCardProps {}

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

  const renderStatus = (status: 'Success' | 'Error') => {
    const color = status == 'Success' ? 'success' : 'danger';
    const label = status == 'Success' ? 'Sent' : 'Errors';
    return (
      <EuiHealth color={color}>
        <EuiText size="s">{label}</EuiText>
      </EuiHealth>
    );
  };

  return (
    <>
      <EuiCard
        textAlign="left"
        title={
          <EuiLink href={`#${ROUTES.CHANNEL_DETAILS}/${'id'}`}>
            {'Managers'}
          </EuiLink>
        }
        titleSize="xs"
        description=""
      >
        <EuiFlexGroup>
          <EuiFlexItem>{renderList('Channel type', 'Email')}</EuiFlexItem>
          <EuiFlexItem>
            {renderList('Sent status', renderStatus('Success'))}
          </EuiFlexItem>
        </EuiFlexGroup>
        {renderList('Error details', 'unavailable')}
      </EuiCard>
    </>
  );
}
