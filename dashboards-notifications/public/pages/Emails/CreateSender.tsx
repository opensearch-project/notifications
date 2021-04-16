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
import { ENCRYPTION_METHOD } from '../../../models/interfaces';
import { ContentPanel } from '../../components/ContentPanel';
import { CoreServicesContext } from '../../components/coreServices';
import { BREADCRUMBS, ROUTES } from '../../utils/constants';
import { CreateSenderForm } from './components/forms/CreateSenderForm';
import {
  validateEmail,
  validateHost,
  validatePort,
  validateSenderName,
} from './utils/validationHelper';

interface CreateSenderProps extends RouteComponentProps {
  edit?: boolean;
}

export function CreateSender(props: CreateSenderProps) {
  const context = useContext(CoreServicesContext)!;
  const [senderName, setSenderName] = useState('');
  const [email, setEmail] = useState('');
  const [host, setHost] = useState('');
  const [port, setPort] = useState('465');
  const [encryption, setEncryption] = useState<ENCRYPTION_METHOD>('SSL');
  const [inputErrors, setInputErrors] = useState<{ [key: string]: string[] }>(
    {
      senderName: [],
      email: [],
      host: [],
      port: [],
    }
  );

  useEffect(() => {
    context.chrome.setBreadcrumbs([
      BREADCRUMBS.NOTIFICATIONS,
      BREADCRUMBS.EMAIL_GROUPS,
      props.edit ? BREADCRUMBS.EDIT_SENDER : BREADCRUMBS.CREATE_SENDER,
    ]);
    window.scrollTo(0, 0);

    if (props.edit) {
      setSenderName('test');
      setEmail('test mail');
    }
  }, []);

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
          <EuiButtonEmpty size="s" href={`#${ROUTES.EMAIL_GROUPS}`}>
            Cancel
          </EuiButtonEmpty>
        </EuiFlexItem>
        <EuiFlexItem grow={false}>
          <EuiButton
            size="s"
            fill
            onClick={() => {
              if (!isInputValid()) return;
              location.assign(`#${ROUTES.EMAIL_GROUPS}`);
            }}
          >
            {props.edit ? 'Save' : 'Create'}
          </EuiButton>
        </EuiFlexItem>
      </EuiFlexGroup>
    </>
  );
}
