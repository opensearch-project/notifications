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
import { ENCRYPTION_TYPE } from '../../../../utils/constants';
import { CreateSenderForm } from '../../../Emails/components/forms/CreateSenderForm';
import { createSenderConfigObject } from '../../../Emails/utils/helper';
import {
  validateEmail,
  validateHost,
  validatePort,
  validateSenderName,
} from '../../../Emails/utils/validationHelper';

interface CreateSenderModalProps extends ModalRootProps {
  addSenderOptionAndSelect: (
    senderOption: EuiComboBoxOptionOption<string>
  ) => void;
  onClose: () => void;
}

export function CreateSenderModal(props: CreateSenderModalProps) {
  const coreContext = useContext(CoreServicesContext)!;
  const [senderName, setSenderName] = useState('');
  const [email, setEmail] = useState('');
  const [host, setHost] = useState('');
  const [port, setPort] = useState('');
  const [encryption, setEncryption] = useState<keyof typeof ENCRYPTION_TYPE>(
    Object.keys(ENCRYPTION_TYPE)[0] as keyof typeof ENCRYPTION_TYPE
  );
  const [inputErrors, setInputErrors] = useState<{ [key: string]: string[] }>({
    senderName: [],
    email: [],
    host: [],
    port: [],
  });

  const isInputValid = (): boolean => {
    const errors: { [key: string]: string[] } = {
      senderName: validateSenderName(senderName),
      email: validateEmail(email),
      host: validateHost(host),
      port: validatePort(port),
    };
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
          <EuiModalHeaderTitle>Create sender</EuiModalHeaderTitle>
        </EuiModalHeader>

        <EuiModalBody>
          <CreateSenderForm
            senderName={senderName}
            setSenderName={setSenderName}
            email={email}
            setEmail={setEmail}
            host={host}
            setHost={setHost}
            port={port}
            setPort={setPort}
            encryption={encryption}
            setEncryption={setEncryption}
            inputErrors={inputErrors}
            setInputErrors={setInputErrors}
          />
        </EuiModalBody>

        <EuiModalFooter>
          <EuiButtonEmpty onClick={props.onClose}>Cancel</EuiButtonEmpty>
          <EuiButton
            fill
            onClick={async () => {
              if (!isInputValid()) {
                coreContext.notifications.toasts.addDanger(
                  'Some fields are invalid. Fix all highlighted error(s) before continuing.'
                );
                return;
              }
              const config = createSenderConfigObject(
                senderName,
                host,
                port,
                encryption,
                email
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
                  coreContext.notifications.toasts.addError(error, {
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
