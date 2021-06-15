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

import { EuiButton, EuiContextMenuItem, EuiPopover } from '@elastic/eui';
import React, { useContext, useState } from 'react';
import { SERVER_DELAY } from '../../../../common';
import { ChannelItemType } from '../../../../models/interfaces';
import { CoreServicesContext } from '../../../components/coreServices';
import { ModalConsumer } from '../../../components/Modal';
import { ServicesContext } from '../../../services';
import { ROUTES } from '../../../utils/constants';
import { DeleteChannelModal } from './modals/DeleteChannelModal';
import { MuteChannelModal } from './modals/MuteChannelModal';

interface ChannelActionsParams {
  label: string;
  disabled: boolean;
  modal?: React.ReactNode;
  modalParams?: object;
  href?: string;
  action?: () => void;
}

interface ChannelActionsProps {
  selected: ChannelItemType[];
  setSelected: (items: ChannelItemType[]) => void;
  items: ChannelItemType[];
  setItems: (items: ChannelItemType[]) => void;
  refresh: () => void;
}

export function ChannelActions(props: ChannelActionsProps) {
  const coreContext = useContext(CoreServicesContext)!;
  const servicesContext = useContext(ServicesContext)!;
  const [isPopoverOpen, setIsPopoverOpen] = useState(false);

  const actions: ChannelActionsParams[] = [
    {
      label: 'Edit',
      disabled: props.selected.length !== 1,
      href: `#${ROUTES.EDIT_CHANNEL}/${props.selected[0]?.config_id}`,
    },
    {
      label: 'Delete',
      disabled: props.selected.length === 0,
      modal: DeleteChannelModal,
      modalParams: { refresh: props.refresh },
    },
    {
      label: 'Mute',
      disabled: props.selected.length !== 1 || !props.selected[0].is_enabled,
      modal: MuteChannelModal,
      modalParams: { refresh: props.refresh, setSelected: props.setSelected },
    },
    {
      label: 'Unmute',
      disabled: props.selected.length !== 1 || props.selected[0].is_enabled,
      action: async () => {
        const channel = { ...props.selected[0], is_enabled: true };
        servicesContext.notificationService
          .updateConfig(channel.config_id, channel)
          .then((resp) => {
            coreContext.notifications.toasts.addSuccess(
              `Channel ${channel.name} successfully unmuted.`
            );
            props.setSelected([channel]);
            setTimeout(() => props.refresh(), SERVER_DELAY);
          })
          .catch((error) => {
            coreContext.notifications.toasts.addError(error, {
              title: 'Failed to unmute channel',
            });
          });
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
              disabled={props.selected.length === 0}
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
                    selected: props.selected,
                    ...(params.modalParams || {}),
                  });
                }
                if (params.href) location.assign(params.href);
                if (params.action) params.action();
              }}
            >
              {params.label}
            </EuiContextMenuItem>
          ))}
        </EuiPopover>
      )}
    </ModalConsumer>
  );
}
