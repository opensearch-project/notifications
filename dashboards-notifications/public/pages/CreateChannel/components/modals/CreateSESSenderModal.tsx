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
import { CreateSESSenderForm } from '../../../Emails/components/forms/CreateSESSenderForm';
import { createSesSenderConfigObject } from '../../../Emails/utils/helper';
import {
  validateAwsRegion,
  validateEmail,
  validateRoleArn,
  validateSenderName,
} from '../../../Emails/utils/validationHelper';
import { MainContext } from '../../../Main/Main';

interface CreateSESSenderModalProps extends ModalRootProps {
  addSenderOptionAndSelect: (
    senderOption: EuiComboBoxOptionOption<string>
  ) => void;
  onClose: () => void;
}

export function CreateSESSenderModal(props: CreateSESSenderModalProps) {
  const coreContext = useContext(CoreServicesContext)!;
  const mainStateContext = useContext(MainContext)!;

  const [senderName, setSenderName] = useState('');
  const [email, setEmail] = useState('');
  const [roleArn, setRoleArn] = useState('');
  const [awsRegion, setAwsRegion] = useState('');
  const [inputErrors, setInputErrors] = useState<{ [key: string]: string[] }>({
    senderName: [],
    email: [],
    roleArn: [],
    awsRegion: [],
  });

  const isInputValid = (): boolean => {
    const errors: { [key: string]: string[] } = {
      senderName: validateSenderName(senderName),
      email: validateEmail(email),
      awsRegion: validateAwsRegion(awsRegion),
      roleArn: [],
    };
    if (!mainStateContext.tooltipSupport) {
      errors.roleArn = validateRoleArn(roleArn);
    }
    setInputErrors(errors);
    return !Object.values(errors).reduce(
      (errorFlag, error) => errorFlag || error.length > 0,
      false
    );
  };

  return (
    <EuiOverlayMask>
      <EuiModal onClose={props.onClose} style={{ width: 750 }}>
        <EuiModalHeader>
          <EuiModalHeaderTitle>Create SES sender</EuiModalHeaderTitle>
        </EuiModalHeader>

        <EuiModalBody>
          <CreateSESSenderForm
            senderName={senderName}
            setSenderName={setSenderName}
            email={email}
            setEmail={setEmail}
            roleArn={roleArn}
            setRoleArn={setRoleArn}
            awsRegion={awsRegion}
            setAwsRegion={setAwsRegion}
            inputErrors={inputErrors}
            setInputErrors={setInputErrors}
          />
        </EuiModalBody>

        <EuiModalFooter>
          <EuiButtonEmpty onClick={props.onClose}>Cancel</EuiButtonEmpty>
          <EuiButton
            data-test-subj="create-ses-sender-modal-create-button"
            fill
            onClick={async () => {
              if (!isInputValid()) {
                coreContext.notifications.toasts.addDanger(
                  'Some fields are invalid. Fix all highlighted error(s) before continuing.'
                );
                return;
              }
              const config = createSesSenderConfigObject(
                senderName,
                email,
                awsRegion,
                roleArn
              );
              await props.services.notificationService
                .createConfig(config)
                .then((response) => {
                  coreContext.notifications.toasts.addSuccess(
                    `Sender ${senderName} successfully created. You can select ${senderName} from the list of senders.`
                  );
                  props.addSenderOptionAndSelect({
                    label: senderName,
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
