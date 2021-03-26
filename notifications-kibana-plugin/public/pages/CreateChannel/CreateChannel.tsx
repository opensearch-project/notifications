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
  EuiFormRow,
  EuiSpacer,
  EuiSuperSelect,
  EuiSuperSelectOption,
  EuiTitle,
} from '@elastic/eui';
import queryString from 'query-string';
import React, { createContext, useContext, useEffect, useState } from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { ContentPanel } from '../../components/ContentPanel';
import { CoreServicesContext } from '../../components/coreServices';
import {
  BREADCRUMBS,
  CHANNEL_TYPE,
  CUSTOM_WEBHOOK_ENDPOINT_TYPE,
  ROUTES,
} from '../../utils/constants';
import { ChannelAvailabilityPanel } from './components/ChannelAvailabilityPanel';
import { ChannelNamePanel } from './components/ChannelNamePanel';
import { CustomWebhookSettings } from './components/CustomWebhookSettings';
import { EmailSettings } from './components/EmailSettings';
import { SlackSettings } from './components/SlackSettings';
import {
  validateChannelName,
  validateEmailSender,
  validateRecipients,
  validateSlackWebhook,
} from './utils/validationHelper';

interface CreateChannelsProps extends RouteComponentProps<{ id?: string }> {
  edit?: boolean;
}

export type CreateChannelInputErrorsType = {
  name: boolean;
  slackWebhook: boolean;
  sender: boolean;
  recipients: boolean;
};

export const CreateChannelContext = createContext<{
  edit?: boolean;
  inputErrors: CreateChannelInputErrorsType;
  setInputErrors: (errors: CreateChannelInputErrorsType) => void;
} | null>(null);

export type HeaderType = { key: string; value: string };

export function CreateChannel(props: CreateChannelsProps) {
  const coreContext = useContext(CoreServicesContext)!;
  const id = props.match.params.id;
  const prevURL =
    props.edit && queryString.parse(props.location.search).from === 'details'
      ? `#${ROUTES.CHANNEL_DETAILS}/${id}`
      : `#${ROUTES.CHANNELS}`;

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');

  const channelTypeOptions: Array<EuiSuperSelectOption<
    keyof typeof CHANNEL_TYPE
  >> = Object.entries(CHANNEL_TYPE).map(([key, value]) => ({
    value: key as keyof typeof CHANNEL_TYPE,
    inputDisplay: value,
  }));
  const [channelType, setChannelType] = useState(channelTypeOptions[0].value);

  const [slackWebhook, setSlackWebhook] = useState('');

  const [
    headerFooterCheckboxIdToSelectedMap,
    setHeaderFooterCheckboxIdToSelectedMap,
  ] = useState<{
    [x: string]: boolean;
  }>({});
  const [emailHeader, setEmailHeader] = useState('');
  const [emailFooter, setEmailFooter] = useState('');
  const [sender, setSender] = useState('');
  const [
    selectedRecipientGroupOptions,
    setSelectedRecipientGroupOptions,
  ] = useState<Array<EuiComboBoxOptionOption<string>>>([]);

  const [webhookTypeIdSelected, setWebhookTypeIdSelected] = useState<
    keyof typeof CUSTOM_WEBHOOK_ENDPOINT_TYPE
  >('WEBHOOK_URL');
  const [webhookURL, setWebhookURL] = useState('');
  const [customURLHost, setCustomURLHost] = useState('');
  const [customURLPort, setCustomURLPort] = useState('');
  const [customURLPath, setCustomURLPath] = useState('');
  const [webhookParams, setWebhookParams] = useState<HeaderType[]>([]);
  const [webhookHeaders, setWebhookHeaders] = useState<HeaderType[]>([
    { key: 'Content-Type', value: 'application/json' },
  ]);

  const [
    sourceCheckboxIdToSelectedMap,
    setSourceCheckboxIdToSelectedMap,
  ] = useState<{
    [x: string]: boolean;
  }>({});

  const [inputErrors, setInputErrors] = useState<CreateChannelInputErrorsType>({
    name: false,
    slackWebhook: false,
    sender: false,
    recipients: false,
  });

  useEffect(() => {
    coreContext.chrome.setBreadcrumbs([
      BREADCRUMBS.NOTIFICATIONS,
      BREADCRUMBS.CHANNELS,
      props.edit ? BREADCRUMBS.EDIT_CHANNEL : BREADCRUMBS.CREATE_CHANNEL,
    ]);
    window.scrollTo(0, 0);

    if (props.edit) {
      setName('test');
      setDescription('test desc');
      setSlackWebhook('hxxp');
    }
  }, []);

  // returns whether inputs passed validation
  const validateInput = (): boolean => {
    const errors = {
      name: validateChannelName(name),
      slackWebhook:
        channelType === 'SLACK' && validateSlackWebhook(slackWebhook),
      sender: channelType === 'EMAIL' && validateEmailSender(sender),
      recipients:
        channelType === 'EMAIL' || channelType === 'SES' &&
        validateRecipients(selectedRecipientGroupOptions),
    };
    setInputErrors(errors);
    return !Object.values(errors).reduce(
      (errorFlag, curr) => errorFlag || curr,
      false
    );
  };

  return (
    <>
      <CreateChannelContext.Provider
        value={{ edit: props.edit, inputErrors, setInputErrors }}
      >
        <EuiTitle size="l">
          <h1>{`${props.edit ? 'Edit' : 'Create'} channel`}</h1>
        </EuiTitle>

        <EuiSpacer />
        <ChannelNamePanel
          name={name}
          setName={setName}
          description={description}
          setDescription={setDescription}
        />

        <EuiSpacer />
        <ContentPanel
          bodyStyles={{ padding: 'initial' }}
          title="Settings"
          titleSize="s"
        >
          <EuiFormRow label="Channel type">
            <EuiSuperSelect
              options={channelTypeOptions}
              valueOfSelected={channelType}
              onChange={setChannelType}
              disabled={props.edit}
            />
          </EuiFormRow>
          {channelType === 'SLACK' ? (
            <SlackSettings
              slackWebhook={slackWebhook}
              setSlackWebhook={setSlackWebhook}
            />
          ) : channelType === 'EMAIL' || channelType === 'SES' ? (
            <EmailSettings
              isAmazonSES={channelType === 'SES'}
              headerFooterCheckboxIdToSelectedMap={
                headerFooterCheckboxIdToSelectedMap
              }
              setHeaderFooterCheckboxIdToSelectedMap={
                setHeaderFooterCheckboxIdToSelectedMap
              }
              emailHeader={emailHeader}
              setEmailHeader={setEmailHeader}
              emailFooter={emailFooter}
              setEmailFooter={setEmailFooter}
              sender={sender}
              setSender={setSender}
              selectedRecipientGroupOptions={selectedRecipientGroupOptions}
              setSelectedRecipientGroupOptions={
                setSelectedRecipientGroupOptions
              }
            />
          ) : channelType === 'CUSTOM_WEBHOOK' ? (
            <CustomWebhookSettings
              webhookTypeIdSelected={webhookTypeIdSelected}
              setWebhookTypeIdSelected={setWebhookTypeIdSelected}
              webhookURL={webhookURL}
              setWebhookURL={setWebhookURL}
              customURLHost={customURLHost}
              setCustomURLHost={setCustomURLHost}
              customURLPort={customURLPort}
              setCustomURLPort={setCustomURLPort}
              customURLPath={customURLPath}
              setCustomURLPath={setCustomURLPath}
              webhookParams={webhookParams}
              setWebhookParams={setWebhookParams}
              webhookHeaders={webhookHeaders}
              setWebhookHeaders={setWebhookHeaders}
            />
          ) : null}
        </ContentPanel>

        <EuiSpacer />
        <ChannelAvailabilityPanel
          sourceCheckboxIdToSelectedMap={sourceCheckboxIdToSelectedMap}
          setSourceCheckboxIdToSelectedMap={setSourceCheckboxIdToSelectedMap}
        />

        <EuiSpacer />
        <EuiFlexGroup gutterSize="m" justifyContent="flexEnd">
          <EuiFlexItem grow={false}>
            <EuiButtonEmpty size="s" href={prevURL}>
              Cancel
            </EuiButtonEmpty>
          </EuiFlexItem>
          <EuiFlexItem grow={false}>
            <EuiButton size="s">Send test message</EuiButton>
          </EuiFlexItem>
          <EuiFlexItem grow={false}>
            <EuiButton
              size="s"
              fill
              onClick={() => {
                if (!validateInput()) return;
                location.assign(prevURL);
              }}
            >
              {props.edit ? 'Save' : 'Create'}
            </EuiButton>
          </EuiFlexItem>
        </EuiFlexGroup>
      </CreateChannelContext.Provider>
    </>
  );
}
