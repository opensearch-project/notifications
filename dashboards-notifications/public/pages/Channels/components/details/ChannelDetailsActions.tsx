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
import { ROUTES } from '../../../../utils/constants';
import { DeleteChannelModal } from '../modals/DeleteChannelModal';

interface ChannelDetailsActionsParams {
  label: string;
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
  const [isPopoverOpen, setIsPopoverOpen] = useState(false);

  const actions: ChannelDetailsActionsParams[] = [
    {
      label: 'Edit',
      href: `#${ROUTES.EDIT_CHANNEL}/${props.channel.id}`,
    },
    {
      label: 'Send test message',
      action: () => {
        coreContext.notifications.toasts.addSuccess(
          'Successfully sent a test message.'
        );
      },
    },
    {
      label: 'Delete',
      color: 'danger',
      modal: DeleteChannelModal,
      modalParams: {
        href: `#${ROUTES.CHANNELS}`
      }
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
              size="s"
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
              onClick={() => {
                setIsPopoverOpen(false);
                if (params.modal) {
                  onShow(params.modal, {
                    channels: [props.channel],
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
