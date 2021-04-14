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
import React from 'react';
import { ChannelItemType } from '../../../../../models/interfaces';
import { ModalRootProps } from '../../../../components/Modal/ModalRoot';

interface MuteChannelModalProps extends ModalRootProps {
  channels: ChannelItemType[];
  onClose: () => void;
  mute: boolean;
}

export const MuteChannelModal = (props: MuteChannelModalProps) => {
  if (props.channels.length !== 1) return null;

  // do not show modal on unmute
  if (!props.mute) {
    return null;
  }

  return (
    <EuiOverlayMask>
      <EuiModal onClose={props.onClose} maxWidth={500}>
        <EuiModalHeader>
          <EuiModalHeaderTitle>{`Mute ${props.channels[0].name}?`}</EuiModalHeaderTitle>
        </EuiModalHeader>
        <EuiModalBody>
          <EuiText>
            This channel will stop sending notifications to its recipients.
            However, the channel remains available for selection.
          </EuiText>
        </EuiModalBody>
        <EuiModalFooter>
          <EuiFlexGroup justifyContent="flexEnd">
            <EuiFlexItem grow={false}>
              <EuiButtonEmpty onClick={props.onClose}>Cancel</EuiButtonEmpty>
            </EuiFlexItem>
            <EuiFlexItem grow={false}>
              <EuiButton fill onClick={props.onClose}>
                Mute
              </EuiButton>
            </EuiFlexItem>
          </EuiFlexGroup>
        </EuiModalFooter>
      </EuiModal>
    </EuiOverlayMask>
  );
};
