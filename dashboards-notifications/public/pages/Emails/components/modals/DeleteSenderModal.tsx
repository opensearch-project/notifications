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
  EuiFieldText,
  EuiFlexGroup,
  EuiFlexItem,
  EuiModal,
  EuiModalBody,
  EuiModalFooter,
  EuiModalHeader,
  EuiModalHeaderTitle,
  EuiOverlayMask,
  EuiSpacer,
  EuiText,
} from '@elastic/eui';
import React, { useState } from 'react';
import { SenderItemType } from '../../../../../models/interfaces';
import { ModalRootProps } from '../../../../components/Modal/ModalRoot';

interface DeleteSenderModalProps extends ModalRootProps {
  senders: SenderItemType[];
  onClose: () => void;
}

export const DeleteSenderModal = (props: DeleteSenderModalProps) => {
  if (!props.senders.length) return null;

  const [input, setInput] = useState('');
  const num = props.senders.length;
  const name = num >= 2 ? `${num} senders` : props.senders[0].name;
  const message = `Delete ${
    num >= 2 ? 'the following senders' : name
  } permanently? Any channels using ${
    num >= 2 ? 'these email senders' : 'this email sender'
  } will not be able to send notifications.`;

  return (
    <EuiOverlayMask>
      <EuiModal onClose={props.onClose} maxWidth={500}>
        <EuiModalHeader>
          <EuiModalHeaderTitle>{`Delete ${name}?`}</EuiModalHeaderTitle>
        </EuiModalHeader>
        <EuiModalBody>
          <EuiText>{message}</EuiText>
          {num >= 2 && (
            <>
              <EuiSpacer />
              {props.senders.map((sender, i) => (
                <EuiText
                  key={`sender-list-item-${i}`}
                  style={{ marginLeft: 20 }}
                >
                  <li>{sender.name}</li>
                </EuiText>
              ))}
            </>
          )}
          <EuiSpacer />
          <EuiText>
            To confirm delete, type <i>delete</i> in the field.
          </EuiText>
          <EuiFieldText
            placeholder="delete"
            value={input}
            onChange={(e) => setInput(e.target.value)}
          />
        </EuiModalBody>
        <EuiModalFooter>
          <EuiFlexGroup justifyContent="flexEnd">
            <EuiFlexItem grow={false}>
              <EuiButtonEmpty onClick={props.onClose}>Cancel</EuiButtonEmpty>
            </EuiFlexItem>
            <EuiFlexItem grow={false}>
              <EuiButton
                fill
                color="danger"
                onClick={props.onClose}
                disabled={input !== 'delete'}
              >
                Delete
              </EuiButton>
            </EuiFlexItem>
          </EuiFlexGroup>
        </EuiModalFooter>
      </EuiModal>
    </EuiOverlayMask>
  );
};
