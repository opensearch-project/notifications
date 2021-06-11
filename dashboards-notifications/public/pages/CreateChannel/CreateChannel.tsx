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
  EuiFormRow,
  EuiSpacer,
  EuiSuperSelect,
  EuiSuperSelectOption,
  EuiText,
  EuiTitle,
} from '@elastic/eui';
import queryString from 'query-string';
import React, { createContext, useContext, useEffect, useState } from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { SERVER_DELAY } from '../../../common';
import { ContentPanel } from '../../components/ContentPanel';
import { CoreServicesContext } from '../../components/coreServices';
import { ServicesContext } from '../../services';
import {
  BREADCRUMBS,
  CHANNEL_TYPE,
  CUSTOM_WEBHOOK_ENDPOINT_TYPE,
  ROUTES,
} from '../../utils/constants';
import { getErrorMessage } from '../../utils/helpers';
import { HeaderItemType } from '../Channels/types';
import { ChannelAvailabilityPanel } from './components/ChannelAvailabilityPanel';
import { ChannelNamePanel } from './components/ChannelNamePanel';
import { ChimeSettings } from './components/ChimeSettings';
import { CustomWebhookSettings } from './components/CustomWebhookSettings';
import { EmailSettings } from './components/EmailSettings';
import { SlackSettings } from './components/SlackSettings';
import { SNSSettings } from './components/SNSSettings';
import {
  constructEmailObject,
  constructWebhookObject,
  deconstructEmailObject,
  deconstructWebhookObject,
} from './utils/helper';
import {
  validateArn,
  validateChannelName,
  validateCustomURLHost,
  validateCustomURLPort,
  validateEmailSender,
  validateRecipients,
  validateWebhookURL,
} from './utils/validationHelper';

interface CreateChannelsProps extends RouteComponentProps<{ id?: string }> {
  edit?: boolean;
}

type InputErrorsType = { [key: string]: string[] };

export const CreateChannelContext = createContext<{
  edit?: boolean;
  inputErrors: InputErrorsType;
  setInputErrors: (errors: InputErrorsType) => void;
} | null>(null);

export function CreateChannel(props: CreateChannelsProps) {
  const isOdfe = true;

  const coreContext = useContext(CoreServicesContext)!;
  const servicesContext = useContext(ServicesContext)!;
  const id = props.match.params.id;
  const prevURL =
    props.edit && queryString.parse(props.location.search).from === 'details'
      ? `#${ROUTES.CHANNEL_DETAILS}/${id}`
      : `#${ROUTES.CHANNELS}`;

  const [isEnabled, setIsEnabled] = useState(true); // should be true unless editing muted channel

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
  const [chimeWebhook, setChimeWebhook] = useState('');

  const [selectedSenderOptions, setSelectedSenderOptions] = useState<
    Array<EuiComboBoxOptionOption<string>>
  >([]);
  // "value" field is the config_id of recipient groups, if it doesn't exist means it's a custom email address
  const [
    selectedRecipientGroupOptions,
    setSelectedRecipientGroupOptions,
  ] = useState<Array<EuiComboBoxOptionOption<string>>>([]);

  const [sesSender, setSesSender] = useState('');

  const [webhookTypeIdSelected, setWebhookTypeIdSelected] = useState<
    keyof typeof CUSTOM_WEBHOOK_ENDPOINT_TYPE
  >('WEBHOOK_URL');
  const [webhookURL, setWebhookURL] = useState('');
  const [customURLHost, setCustomURLHost] = useState('');
  const [customURLPort, setCustomURLPort] = useState('');
  const [customURLPath, setCustomURLPath] = useState('');
  const [webhookParams, setWebhookParams] = useState<HeaderItemType[]>([]);
  const [webhookHeaders, setWebhookHeaders] = useState<HeaderItemType[]>([
    { key: 'Content-Type', value: 'application/json' },
  ]);
  const [topicArn, setTopicArn] = useState(''); // SNS topic ARN
  const [roleArn, setRoleArn] = useState(''); // IAM role ARN (optional for ODFE)

  const [
    sourceCheckboxIdToSelectedMap,
    setSourceCheckboxIdToSelectedMap,
  ] = useState<{
    [x: string]: boolean;
  }>({});

  const [inputErrors, setInputErrors] = useState<InputErrorsType>({
    name: [],
    slackWebhook: [],
    chimeWebhook: [],
    sender: [],
    recipients: [],
    webhookURL: [],
    customURLHost: [],
    customURLPort: [],
    topicArn: [],
    roleArn: [],
    sesSender: [],
  });

  useEffect(() => {
    coreContext.chrome.setBreadcrumbs([
      BREADCRUMBS.NOTIFICATIONS,
      BREADCRUMBS.CHANNELS,
      props.edit ? BREADCRUMBS.EDIT_CHANNEL : BREADCRUMBS.CREATE_CHANNEL,
    ]);
    window.scrollTo(0, 0);

    if (props.edit) {
      getChannel();
    }
  }, []);

  const getChannel = async () => {
    const id = props.match.params.id;
    if (typeof id !== 'string') return;

    try {
      const response = await servicesContext.notificationService
        .getChannel(id)
        .then((response) => {
          if (response.config_type === 'email') {
            return servicesContext.notificationService.getEmailConfigDetails(
              response
            );
          }
          return response;
        });
      const type = response.config_type as keyof typeof CHANNEL_TYPE;
      setIsEnabled(response.is_enabled);
      setName(response.name);
      setDescription(response.description || '');
      setChannelType(type);
      setSourceCheckboxIdToSelectedMap(
        Object.fromEntries(
          response.feature_list.map((feature) => [feature, true])
        )
      );

      if (type === 'slack') {
        setSlackWebhook(response.slack?.url || '');
      } else if (type === 'chime') {
        setChimeWebhook(response.chime?.url || '');
      } else if (type === 'email') {
        const emailObject = deconstructEmailObject(response.email!);
        setSelectedSenderOptions(emailObject.selectedSenderOptions);
        setSelectedRecipientGroupOptions(
          emailObject.selectedRecipientGroupOptions
        );
      } else if (type === 'webhook') {
        const webhookObject = deconstructWebhookObject(response.webhook!);
        setWebhookURL(webhookObject.webhookURL);
        setCustomURLHost(webhookObject.customURLHost);
        setCustomURLPort(webhookObject.customURLPort);
        setCustomURLPath(webhookObject.customURLPath);
        setWebhookParams(webhookObject.webhookParams);
        setWebhookHeaders(webhookObject.webhookHeaders);
      } else if (type === 'SNS') {
        // TODO
      } else if (type === 'SES') {
        // TODO
      }
    } catch (error) {
      coreContext.notifications.toasts.addDanger(
        getErrorMessage(error, 'There was a problem loading channel.')
      );
    }
  };

  const isInputValid = (): boolean => {
    const errors: InputErrorsType = {
      name: validateChannelName(name),
      slackWebhook: [],
      chimeWebhook: [],
      sender: [],
      recipients: [],
      webhookURL: [],
      customURLHost: [],
      customURLPort: [],
      topicArn: [],
      roleArn: [],
      sesSender: [],
    };
    if (channelType === 'slack') {
      errors.slackWebhook = validateWebhookURL(slackWebhook);
    } else if (channelType === 'chime') {
      errors.chimeWebhook = validateWebhookURL(chimeWebhook);
    } else if (channelType === 'email') {
      errors.sender = validateEmailSender(selectedSenderOptions);
      errors.recipients = validateRecipients(selectedRecipientGroupOptions);
    } else if (channelType === 'webhook') {
      if (webhookTypeIdSelected === 'WEBHOOK_URL') {
        errors.webhookURL = validateWebhookURL(webhookURL);
      } else {
        errors.customURLHost = validateCustomURLHost(customURLHost);
        errors.customURLPort = validateCustomURLPort(customURLPort);
      }
    } else if (channelType === 'SNS') {
      errors.topicArn = validateArn(topicArn);
      if (!isOdfe) errors.roleArn = validateArn(roleArn);
    }
    setInputErrors(errors);
    return !Object.values(errors).reduce(
      (errorFlag, error) => errorFlag || error.length > 0,
      false
    );
  };

  const createConfigObject = () => {
    const config: any = {
      name,
      description,
      config_type: channelType,
      feature_list: Object.entries(sourceCheckboxIdToSelectedMap)
        .filter(([key, value]) => value)
        .map(([key, value]) => key),
      is_enabled: isEnabled,
    };
    if (channelType === 'slack') {
      config.slack = { url: slackWebhook };
    } else if (channelType === 'chime') {
      config.chime = { url: chimeWebhook };
    } else if (channelType === 'webhook') {
      config.webhook = constructWebhookObject(
        webhookTypeIdSelected,
        webhookURL,
        customURLHost,
        customURLPort,
        customURLPath,
        webhookParams,
        webhookHeaders
      );
    } else if (channelType === 'email') {
      config.email = constructEmailObject(
        selectedSenderOptions,
        selectedRecipientGroupOptions
      );
    }
    return config;
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
          title="Configurations"
          titleSize="s"
        >
          <EuiFormRow label="Channel type">
            {props.edit ? (
              <EuiText size="s">{CHANNEL_TYPE[channelType]}</EuiText>
            ) : (
              <>
                <EuiText size="xs" color="subdued">
                  Channel type cannot be changed after the channel is created.
                </EuiText>
                <EuiSuperSelect
                  options={channelTypeOptions}
                  valueOfSelected={channelType}
                  onChange={setChannelType}
                  disabled={props.edit}
                />
              </>
            )}
          </EuiFormRow>
          {channelType === 'slack' ? (
            <SlackSettings
              slackWebhook={slackWebhook}
              setSlackWebhook={setSlackWebhook}
            />
          ) : channelType === 'chime' ? (
            <ChimeSettings
              chimeWebhook={chimeWebhook}
              setChimeWebhook={setChimeWebhook}
            />
          ) : channelType === 'email' || channelType === 'SES' ? (
            <EmailSettings
              isAmazonSES={channelType === 'SES'}
              selectedSenderOptions={selectedSenderOptions}
              setSelectedSenderOptions={setSelectedSenderOptions}
              selectedRecipientGroupOptions={selectedRecipientGroupOptions}
              setSelectedRecipientGroupOptions={
                setSelectedRecipientGroupOptions
              }
              sesSender={sesSender}
              setSesSender={setSesSender}
            />
          ) : channelType === 'webhook' ? (
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
          ) : channelType === 'SNS' ? (
            <SNSSettings
              isOdfe={isOdfe}
              topicArn={topicArn}
              setTopicArn={setTopicArn}
              roleArn={roleArn}
              setRoleArn={setRoleArn}
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
            <EuiButtonEmpty href={prevURL}>Cancel</EuiButtonEmpty>
          </EuiFlexItem>
          <EuiFlexItem grow={false}>
            <EuiButton
              data-test-subj="create-channel-send-test-message-button"
              disabled={Object.values(sourceCheckboxIdToSelectedMap).every(
                (enabled) => !enabled
              )}
              onClick={() => {
                if (!isInputValid()) {
                  coreContext.notifications.toasts.addDanger(
                    'Some fields are invalid. Fix all highlighted error(s) before continuing.'
                  );
                  return;
                }
                if (true) {
                  coreContext.notifications.toasts.addSuccess(
                    'Successfully sent a test message.'
                  );
                } else {
                  const error: Error = {
                    name: 'Error details',
                    message:
                      'Message cannot be sent. Security_team (PagerDuty) webhook is invalid.',
                    stack: `TypeError: Failed to fetch\n\tat Fetch.fetchResponse (http://localhost:5601/9007199254740991/bundles/core/core.entry.js:17006:13)\n\tat async interceptResponse (http://localhost:5601/9007199254740991/bundles/core/core.entry.js:17444:10)\n\tat async http://localhost:5601/9007199254740991/bundles/core/core.entry.js:16930:39`,
                  };
                  coreContext.notifications.toasts.addError(error, {
                    title: 'Failed to send the test message.',
                    toastMessage:
                      'View error details and adjust the channel settings.',
                  });
                }
              }}
            >
              Send test message
            </EuiButton>
          </EuiFlexItem>
          <EuiFlexItem grow={false}>
            <EuiButton
              fill
              data-test-subj="create-channel-create-button"
              onClick={async () => {
                if (!isInputValid()) {
                  coreContext.notifications.toasts.addDanger(
                    'Some fields are invalid. Fix all highlighted error(s) before continuing.'
                  );
                  return;
                }
                const config = createConfigObject();
                const request = props.edit
                  ? servicesContext.notificationService.updateConfig(
                      id!,
                      config
                    )
                  : servicesContext.notificationService.createConfig(config);
                await request
                  .then((response) => {
                    coreContext.notifications.toasts.addSuccess(
                      `Channel ${name} successfully ${
                        props.edit ? 'updated' : 'created'
                      }.`
                    );
                    setTimeout(() => location.assign(prevURL), SERVER_DELAY);
                  })
                  .catch((error) => {
                    coreContext.notifications.toasts.addError(error, {
                      title: `Failed to ${
                        props.edit ? 'update' : 'create'
                      } channel.`,
                    });
                  });
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
