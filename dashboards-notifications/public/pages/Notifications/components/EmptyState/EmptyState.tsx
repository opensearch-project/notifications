/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { EuiButton, EuiEmptyPrompt, EuiLink, EuiText } from '@elastic/eui';
import React from 'react';
import { DOCUMENTATION_LINK, ROUTES } from '../../../../utils/constants';

interface EmptyStateProps {
  // false if user has no channels, true if no matching search results
  channels: boolean;
}

export function EmptyState(props: EmptyStateProps) {
  return (
    <>
      {props.channels ? (
        <EuiEmptyPrompt
          title={<h2>No notifications insights to display</h2>}
          body={
            <EuiText>
              Adjust your filter or create a channel.{' '}
              <EuiLink href={DOCUMENTATION_LINK} target="_blank" external>
                Learn more
              </EuiLink>
            </EuiText>
          }
        />
      ) : (
        <EuiEmptyPrompt
          title={<h2>You have no channels set up</h2>}
          body={
            <EuiText>
              <div>
                Create a channel, and then configure actions to get
                notifications from features like Alerting and Index Management.{' '}
                <EuiLink href={DOCUMENTATION_LINK} target="_blank" external>
                  Learn more
                </EuiLink>
              </div>
            </EuiText>
          }
          actions={
            <EuiButton href={`#${ROUTES.CREATE_CHANNEL}`} fill>
              Create channel
            </EuiButton>
          }
        />
      )}
    </>
  );
}
