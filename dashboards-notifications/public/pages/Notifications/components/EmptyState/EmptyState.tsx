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
