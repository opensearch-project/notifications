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
import { BREADCRUMBS, ROUTES } from '../../utils/constants';
import { getErrorMessage } from '../../utils/helpers';
import { CreateRecipientGroupForm } from './components/forms/CreateRecipientGroupForm';
import { createRecipientGroupConfigObject } from './utils/helper';
import {
  validateRecipientGroupEmails,
  validateRecipientGroupName,
} from './utils/validationHelper';

interface CreateRecipientGroupProps
  extends RouteComponentProps<{ id?: string }> {
  edit?: boolean;
}

export function CreateRecipientGroup(props: CreateRecipientGroupProps) {
  const coreContext = useContext(CoreServicesContext)!;
  const servicesContext = useContext(ServicesContext)!;

  const [loading, setLoading] = useState(false);
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
      emailOptions: validateRecipientGroupEmails(selectedEmailOptions),
    };
    setInputErrors(errors);
    return !Object.values(errors).reduce(
      (errorFlag, error) => errorFlag || error.length > 0,
      false
    );
  };

  useEffect(() => {
    coreContext.chrome.setBreadcrumbs([
      BREADCRUMBS.NOTIFICATIONS,
      BREADCRUMBS.EMAIL_GROUPS,
      props.edit
        ? BREADCRUMBS.EDIT_RECIPIENT_GROUP
        : BREADCRUMBS.CREATE_RECIPIENT_GROUP,
    ]);
    window.scrollTo(0, 0);

    if (props.edit) {
      getRecipientGroup();
    }
  }, []);

  const getRecipientGroup = async () => {
    const id = props.match.params?.id;
    if (typeof id !== 'string') return;

    try {
      const response =
        await servicesContext.notificationService.getRecipientGroup(id);
      setName(response.name);
      setDescription(response.description || '');
      setSelectedEmailOptions(
        response.email_group.recipient_list.map((email) => ({ label: email }))
      );
    } catch (error) {
      coreContext.notifications.toasts.addDanger(
        getErrorMessage(error, 'There was a problem loading recipient group.')
      );
    }
  };

  return (
    <>
      <EuiTitle size="l">
        <h1>{`${props.edit ? 'Edit' : 'Create'} recipient group`}</h1>
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
              const config = createRecipientGroupConfigObject(
                name,
                description,
                selectedEmailOptions
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
                    `Recipient group ${name} successfully ${
                      props.edit ? 'updated' : 'created'
                    }.`
                  );
                  setTimeout(
                    () => location.hash = `#${ROUTES.EMAIL_GROUPS}`,
                    SERVER_DELAY
                  );
                })
                .catch((error) => {
                  setLoading(false);
                  coreContext.notifications.toasts.addError(error?.body || error, {
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
