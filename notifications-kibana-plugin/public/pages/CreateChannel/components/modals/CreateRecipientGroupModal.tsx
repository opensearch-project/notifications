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
  EuiComboBoxOptionOption,
  EuiModal,
  EuiModalBody,
  EuiModalFooter,
  EuiModalHeader,
  EuiModalHeaderTitle,
  EuiOverlayMask,
} from '@elastic/eui';
import React, { useState } from 'react';
import { ModalRootProps } from '../../../../components/Modal/ModalRoot';
import { CreateRecipientGroupForm } from '../../../Emails/components/forms/CreateRecipientGroupForm';

interface CreateRecipientGroupModalProps extends ModalRootProps {
  onClose: () => void;
}

export function CreateRecipientGroupModal(
  props: CreateRecipientGroupModalProps
) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [selectedEmailOptions, setSelectedEmailOptions] = useState<
    Array<EuiComboBoxOptionOption<string>>
  >([]);
  const [emailOptions, setEmailOptions] = useState([
    {
      label: 'no-reply@company.com',
    },
  ]);

  return (
    <EuiOverlayMask>
      <EuiModal onClose={props.onClose} style={{ width: 650 }}>
        <EuiModalHeader>
          <EuiModalHeaderTitle>Create recipient group</EuiModalHeaderTitle>
        </EuiModalHeader>

        <EuiModalBody>
          <CreateRecipientGroupForm
            name={name}
            setName={setName}
            description={description}
            setDescription={setDescription}
            selectedEmailOptions={selectedEmailOptions}
            setSelectedEmailOptions={setSelectedEmailOptions}
            emailOptions={emailOptions}
            setEmailOptions={setEmailOptions}
          />
        </EuiModalBody>

        <EuiModalFooter>
          <EuiButtonEmpty onClick={props.onClose} size="s">
            Cancel
          </EuiButtonEmpty>
          <EuiButton fill onClick={props.onClose} size="s">
            Create
          </EuiButton>
        </EuiModalFooter>
      </EuiModal>
    </EuiOverlayMask>
  );
}
