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
import React, { useContext, useState } from 'react';
import { CoreServicesContext } from '../../../../components/coreServices';
import { ModalRootProps } from '../../../../components/Modal/ModalRoot';
import { CreateRecipientGroupForm } from '../../../Emails/components/forms/CreateRecipientGroupForm';
import {
  validateRecipientGroupEmails,
  validateRecipientGroupName,
} from '../../../Emails/utils/validationHelper';

interface CreateRecipientGroupModalProps extends ModalRootProps {
  addRecipientGroupOptionAndSelect: (
    recipientGroupOption: EuiComboBoxOptionOption<string>
  ) => void;
  onClose: () => void;
}

export function CreateRecipientGroupModal(
  props: CreateRecipientGroupModalProps
) {
  const coreContext = useContext(CoreServicesContext)!;
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
  const [inputErrors, setInputErrors] = useState<{ [key: string]: string[] }>({
    name: [],
    emailOptions: [],
  });

  const isInputValid = (): boolean => {
    const errors: { [key: string]: string[] } = {
      name: validateRecipientGroupName(name),
      emailOptions: validateRecipientGroupEmails(emailOptions),
    };
    setInputErrors(errors);
    return !Object.values(errors).reduce(
      (errorFlag, error) => errorFlag || error.length > 0,
      false
    );
  };

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
            inputErrors={inputErrors}
            setInputErrors={setInputErrors}
          />
        </EuiModalBody>

        <EuiModalFooter>
          <EuiButtonEmpty onClick={props.onClose} size="s">
            Cancel
          </EuiButtonEmpty>
          <EuiButton
            fill
            onClick={() => {
              if (!isInputValid()) {
                coreContext.notifications.toasts.addDanger(
                  'Some fields are invalid, please check your inputs.'
                );
                return;
              }
              coreContext.notifications.toasts.addSuccess(
                `${name} successfully created. You can select ${name} from the list of recipient groups.`
              );
              props.addRecipientGroupOptionAndSelect({ label: name });
              props.onClose();
            }}
            size="s"
          >
            Create
          </EuiButton>
        </EuiModalFooter>
      </EuiModal>
    </EuiOverlayMask>
  );
}
