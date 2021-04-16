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
  EuiFlexGroup,
  EuiFlexItem,
  EuiSpacer,
  EuiTitle,
} from '@elastic/eui';
import React, { useContext, useEffect, useState } from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { ContentPanel } from '../../components/ContentPanel';
import { CoreServicesContext } from '../../components/coreServices';
import { BREADCRUMBS, ROUTES } from '../../utils/constants';
import { CreateRecipientGroupForm } from './components/forms/CreateRecipientGroupForm';
import {
  validateRecipientGroupEmails,
  validateRecipientGroupName,
} from './utils/validationHelper';

interface CreateRecipientGroupProps extends RouteComponentProps {
  edit?: boolean;
}

export function CreateRecipientGroup(props: CreateRecipientGroupProps) {
  const context = useContext(CoreServicesContext)!;
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

  useEffect(() => {
    context.chrome.setBreadcrumbs([
      BREADCRUMBS.NOTIFICATIONS,
      BREADCRUMBS.EMAIL_GROUPS,
      props.edit
        ? BREADCRUMBS.EDIT_RECIPIENT_GROUP
        : BREADCRUMBS.CREATE_RECIPIENT_GROUP,
    ]);
    window.scrollTo(0, 0);

    if (props.edit) {
      setName('test');
      setDescription('test desc');
    }
  }, []);

  return (
    <>
      <EuiTitle size="l">
        <h1>Create recipient group</h1>
      </EuiTitle>

      <EuiSpacer />
      <ContentPanel
        bodyStyles={{ padding: 'initial' }}
        title="Configure recipient group"
        titleSize="s"
        panelStyles={{ maxWidth: 1000 }}
      >
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
            fill
            size="s"
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
