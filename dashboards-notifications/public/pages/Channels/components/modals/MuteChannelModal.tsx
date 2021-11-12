/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
  EuiButtonEmpty,
  EuiFlexGroup,
  EuiFlexItem,
  EuiModal,
  EuiModalBody,
  EuiModalFooter,
  EuiModalHeader,
  EuiModalHeaderTitle,
  EuiOverlayMask,
  EuiText,
} from '@elastic/eui';
import React, { useContext } from 'react';
import { SERVER_DELAY } from '../../../../../common';
import { ChannelItemType } from '../../../../../models/interfaces';
import { CoreServicesContext } from '../../../../components/coreServices';
import { ModalRootProps } from '../../../../components/Modal/ModalRoot';

interface MuteChannelModalProps extends ModalRootProps {
  selected: ChannelItemType[];
  setSelected: (items: ChannelItemType[]) => void;
  refresh?: () => void;
  onClose: () => void;
}

export const MuteChannelModal = (props: MuteChannelModalProps) => {
  if (props.selected.length !== 1) return null;

  const coreContext = useContext(CoreServicesContext)!;
  return (
    <EuiOverlayMask>
      <EuiModal onClose={props.onClose} maxWidth={500}>
        <EuiModalHeader>
          <EuiModalHeaderTitle>{`Mute ${props.selected[0].name}?`}</EuiModalHeaderTitle>
        </EuiModalHeader>
        <EuiModalBody>
          <EuiText>
            This channel will stop sending notifications to its recipients.
            However, the channel will remain available for selection.
          </EuiText>
        </EuiModalBody>
        <EuiModalFooter>
          <EuiFlexGroup justifyContent="flexEnd">
            <EuiFlexItem grow={false}>
              <EuiButtonEmpty onClick={props.onClose}>Cancel</EuiButtonEmpty>
            </EuiFlexItem>
            <EuiFlexItem grow={false}>
              <EuiButton
                fill
                data-test-subj="mute-channel-modal-mute-button"
                onClick={async () => {
                  const channel = { ...props.selected[0], is_enabled: false };
                  await props.services.notificationService
                    .updateConfig(channel.config_id, channel)
                    .then((resp) => {
                      coreContext.notifications.toasts.addSuccess(
                        `Channel ${channel.name} successfully muted.`
                      );
                      props.setSelected([channel]);
                      if (props.refresh)
                        setTimeout(() => props.refresh!(), SERVER_DELAY);
                    })
                    .catch((error) => {
                      coreContext.notifications.toasts.addError(error?.body || error, {
                        title: 'Failed to mute channel',
                      });
                    });
                  props.onClose();
                }}
              >
                Mute
              </EuiButton>
            </EuiFlexItem>
          </EuiFlexGroup>
        </EuiModalFooter>
      </EuiModal>
    </EuiOverlayMask>
  );
};
