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
import { BREADCRUMBS, ROUTES } from '../../utils/constants';
import { getErrorMessage } from '../../utils/helpers';
import { CreateSESSenderForm } from './components/forms/CreateSESSenderForm';
import { createSesSenderConfigObject } from './utils/helper';
import {
  validateAwsRegion,
  validateEmail,
  validateSenderName,
} from './utils/validationHelper';

interface CreateSESSenderProps extends RouteComponentProps<{ id?: string }> {
  edit?: boolean;
}

export function CreateSESSender(props: CreateSESSenderProps) {
  const coreContext = useContext(CoreServicesContext)!;
  const servicesContext = useContext(ServicesContext)!;

  const [loading, setLoading] = useState(false);
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

  useEffect(() => {
    coreContext.chrome.setBreadcrumbs([
      BREADCRUMBS.NOTIFICATIONS,
      BREADCRUMBS.EMAIL_GROUPS,
      props.edit ? BREADCRUMBS.EDIT_SES_SENDER : BREADCRUMBS.CREATE_SES_SENDER,
    ]);
    window.scrollTo(0, 0);

    if (props.edit) {
      getSESSender();
    }
  }, []);

  const getSESSender = async () => {
    const id = props.match.params?.id;
    if (typeof id !== 'string') return;

    try {
      const response = await servicesContext.notificationService.getSESSender(
        id
      );
      setSenderName(response.name);
      setEmail(response.ses_account.from_address);
      setRoleArn(response.ses_account.role_arn || '');
      setAwsRegion(response.ses_account.region);
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
      awsRegion: validateAwsRegion(awsRegion),
      roleArn: [],
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
              const config = createSesSenderConfigObject(
                senderName,
                email,
                awsRegion,
                roleArn
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
                    () => (location.hash = `#${ROUTES.EMAIL_GROUPS}`),
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
