/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
import { createRecipientGroupConfigObject } from '../../../Emails/utils/helper';
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
          <EuiButtonEmpty onClick={props.onClose}>Cancel</EuiButtonEmpty>
          <EuiButton
            data-test-subj="create-recipient-group-modal-create-button"
            fill
            onClick={async () => {
              if (!isInputValid()) {
                coreContext.notifications.toasts.addDanger(
                  'Some fields are invalid. Fix all highlighted error(s) before continuing.'
                );
                return;
              }
              const config = createRecipientGroupConfigObject(
                name,
                description,
                selectedEmailOptions
              );
              await props.services.notificationService
                .createConfig(config)
                .then((response) => {
                  coreContext.notifications.toasts.addSuccess(
                    `Recipient group ${name} successfully created. You can select ${name} from the list of recipient groups.`
                  );
                  props.addRecipientGroupOptionAndSelect({
                    label: name,
                    value: response.config_id,
                  });
                  props.onClose();
                })
                .catch((error) => {
                  coreContext.notifications.toasts.addError(error?.body || error, {
                    title: 'Failed to create sender.',
                  });
                });
            }}
          >
            Create
          </EuiButton>
        </EuiModalFooter>
      </EuiModal>
    </EuiOverlayMask>
  );
}
