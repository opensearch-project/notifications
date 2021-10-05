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
  EuiContextMenuItem,
  EuiPopover,
  EuiTextColor,
} from '@elastic/eui';
import { TextColor } from '@elastic/eui/src/components/text/text_color';
import React, { useContext, useState } from 'react';
import { ChannelItemType } from '../../../../../models/interfaces';
import { CoreServicesContext } from '../../../../components/coreServices';
import { ModalConsumer } from '../../../../components/Modal';
import { ServicesContext } from '../../../../services';
import { ROUTES } from '../../../../utils/constants';
import { DeleteChannelModal } from '../modals/DeleteChannelModal';

interface ChannelDetailsActionsParams {
  label: string;
  disabled?: boolean;
  color?: TextColor;
  modal?: React.ReactNode;
  modalParams?: object;
  href?: string;
  action?: () => void;
}

interface ChannelDetailsActionsProps {
  channel: ChannelItemType;
}

export function ChannelDetailsActions(props: ChannelDetailsActionsProps) {
  const coreContext = useContext(CoreServicesContext)!;
  const servicesContext = useContext(ServicesContext)!;
  const [isPopoverOpen, setIsPopoverOpen] = useState(false);

  const sendTestMessage = async () => {
    try {
      const eventId = await servicesContext.eventService
        .sendTestMessage(props.channel.config_id, props.channel.feature_list[0])
        .then((response) => response.event_id);

      await servicesContext.eventService
        .getNotification(eventId)
        .then((response) => {
          if (!response.success) {
            const error = new Error('Failed to send the test message.');
            error.stack = JSON.stringify(response.status_list, null, 2);
            throw error;
          }
        });
      coreContext.notifications.toasts.addSuccess(
        'Successfully sent a test message.'
      );
    } catch (error) {
      coreContext.notifications.toasts.addError(error?.body || error, {
        title: 'Failed to send the test message.',
        toastMessage: 'View error details and adjust the channel settings.',
      });
    }
  };

  const actions: ChannelDetailsActionsParams[] = [
    {
      label: 'Edit',
      href: `#${ROUTES.EDIT_CHANNEL}/${props.channel.config_id}?from=details`,
    },
    {
      label: 'Send test message',
      disabled:
        props.channel.feature_list.length === 0 ||
        !props.channel.config_id ||
        !props.channel.is_enabled,
      action: sendTestMessage,
    },
    {
      label: 'Delete',
      color: 'danger',
      modal: DeleteChannelModal,
      modalParams: {
        href: `#${ROUTES.CHANNELS}`,
      },
    },
  ];

  return (
    <ModalConsumer>
      {({ onShow }) => (
        <EuiPopover
          panelPaddingSize="none"
          button={
            <EuiButton
              iconType="arrowDown"
              iconSide="right"
              onClick={() => setIsPopoverOpen(!isPopoverOpen)}
            >
              Actions
            </EuiButton>
          }
          isOpen={isPopoverOpen}
          closePopover={() => setIsPopoverOpen(false)}
        >
          {actions.map((params) => (
            <EuiContextMenuItem
              key={params.label}
              disabled={params.disabled}
              onClick={() => {
                setIsPopoverOpen(false);
                if (params.modal) {
                  onShow(params.modal, {
                    selected: [props.channel],
                    ...(params.modalParams || {}),
                  });
                }
                if (params.href) location.assign(params.href);
                if (params.action) params.action();
              }}
            >
              {params.color ? (
                <EuiTextColor color={params.color}>{params.label}</EuiTextColor>
              ) : (
                params.label
              )}
            </EuiContextMenuItem>
          ))}
        </EuiPopover>
      )}
    </ModalConsumer>
  );
}
