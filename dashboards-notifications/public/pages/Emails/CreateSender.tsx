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
  EuiFlexGroup,
  EuiFlexItem,
  EuiSpacer,
  EuiTitle,
} from '@elastic/eui';
import React, { useContext, useEffect, useState } from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { SERVER_DELAY } from '../../../common';
import { ContentPanel } from '../../components/ContentPanel';
import { CoreServicesContext } from '../../components/coreServices';
import { ServicesContext } from '../../services';
import { BREADCRUMBS, ENCRYPTION_TYPE, ROUTES } from '../../utils/constants';
import { getErrorMessage } from '../../utils/helpers';
import { CreateSenderForm } from './components/forms/CreateSenderForm';
import { createSenderConfigObject } from './utils/helper';
import {
  validateEmail,
  validateHost,
  validatePort,
  validateSenderName,
} from './utils/validationHelper';

interface CreateSenderProps extends RouteComponentProps<{ id?: string }> {
  edit?: boolean;
}

export function CreateSender(props: CreateSenderProps) {
  const coreContext = useContext(CoreServicesContext)!;
  const servicesContext = useContext(ServicesContext)!;

  const [loading, setLoading] = useState(false);
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

  useEffect(() => {
    coreContext.chrome.setBreadcrumbs([
      BREADCRUMBS.NOTIFICATIONS,
      BREADCRUMBS.EMAIL_GROUPS,
      props.edit ? BREADCRUMBS.EDIT_SENDER : BREADCRUMBS.CREATE_SENDER,
    ]);
    window.scrollTo(0, 0);

    if (props.edit) {
      getSender();
    }
  }, []);

  const getSender = async () => {
    const id = props.match.params?.id;
    if (typeof id !== 'string') return;

    try {
      const response = await servicesContext.notificationService.getSender(id);
      setSenderName(response.name);
      setEmail(response.smtp_account.from_address);
      setHost(response.smtp_account.host);
      setPort(response.smtp_account.port);
      setEncryption(response.smtp_account.method);
    } catch (error) {
      coreContext.notifications.toasts.addDanger(
        getErrorMessage(error, 'There was a problem loading sender.')
      );
    }
  };

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
    <>
      <EuiTitle size="l">
        <h1>{`${props.edit ? 'Edit' : 'Create'} sender`}</h1>
      </EuiTitle>

      <EuiSpacer />
      <ContentPanel
        bodyStyles={{ padding: 'initial' }}
        title="Configure sender"
        titleSize="s"
        panelStyles={{ maxWidth: 1000 }}
      >
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
      </ContentPanel>

      <EuiSpacer />
      <EuiFlexGroup justifyContent="flexEnd" style={{ maxWidth: 1024 }}>
        <EuiFlexItem grow={false}>
          <EuiButtonEmpty href={`#${ROUTES.EMAIL_GROUPS}`}>
            Cancel
          </EuiButtonEmpty>
        </EuiFlexItem>
        <EuiFlexItem grow={false}>
          <EuiButton
            fill
            isLoading={loading}
            onClick={async () => {
              if (!isInputValid()) {
                coreContext.notifications.toasts.addDanger(
                  'Some fields are invalid. Fix all highlighted error(s) before continuing.'
                );
                return;
              }
              setLoading(true);
              const config = createSenderConfigObject(
                senderName,
                host,
                port,
                encryption,
                email
              );
              const request = props.edit
                ? servicesContext.notificationService.updateConfig(
                    props.match.params.id!,
                    config
                  )
                : servicesContext.notificationService.createConfig(config);
              await request
                .then((response) => {
                  coreContext.notifications.toasts.addSuccess(
                    `Sender ${senderName} successfully ${
                      props.edit ? 'updated' : 'created'
                    }.`
                  );
                  setTimeout(
                    () => location.assign(`#${ROUTES.EMAIL_GROUPS}`),
                    SERVER_DELAY
                  );
                })
                .catch((error) => {
                  setLoading(false);
                  coreContext.notifications.toasts.addError(error, {
                    title: `Failed to ${
                      props.edit ? 'update' : 'create'
                    } sender.`,
                  });
                });
            }}
          >
            {props.edit ? 'Save' : 'Create'}
          </EuiButton>
        </EuiFlexItem>
      </EuiFlexGroup>
    </>
  );
}
