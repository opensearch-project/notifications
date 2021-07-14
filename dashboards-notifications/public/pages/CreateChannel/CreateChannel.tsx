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
  BACKEND_CHANNEL_TYPE,
  BREADCRUMBS,
  CHANNEL_TYPE,
  CUSTOM_WEBHOOK_ENDPOINT_TYPE,
  ROUTES,
} from '../../utils/constants';
import { getErrorMessage } from '../../utils/helpers';
import { HeaderItemType } from '../Channels/types';
import { MainContext } from '../Main/Main';
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
  const mainStateContext = useContext(MainContext)!;
  const id = props.match.params.id;
  const prevURL =
    props.edit && queryString.parse(props.location.search).from === 'details'
      ? `#${ROUTES.CHANNEL_DETAILS}/${id}`
      : `#${ROUTES.CHANNELS}`;

  const [isEnabled, setIsEnabled] = useState(true); // should be true unless editing muted channel
  const [loading, setLoading] = useState(false);

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');

  const channelTypeOptions: Array<EuiSuperSelectOption<
    keyof typeof CHANNEL_TYPE
  >> = Object.entries(mainStateContext.availableFeatures).map(
    ([key, value]) => ({
      value: key as keyof typeof CHANNEL_TYPE,
      inputDisplay: value,
    })
  );
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

      if (type === BACKEND_CHANNEL_TYPE.SLACK) {
        setSlackWebhook(response.slack?.url || '');
      } else if (type === BACKEND_CHANNEL_TYPE.CHIME) {
        setChimeWebhook(response.chime?.url || '');
      } else if (type === BACKEND_CHANNEL_TYPE.EMAIL) {
        const emailObject = deconstructEmailObject(response.email!);
        setSelectedSenderOptions(emailObject.selectedSenderOptions);
        setSelectedRecipientGroupOptions(
          emailObject.selectedRecipientGroupOptions
        );
      } else if (type === BACKEND_CHANNEL_TYPE.CUSTOM_WEBHOOK) {
        const webhookObject = deconstructWebhookObject(response.webhook!);
        setWebhookURL(webhookObject.webhookURL);
        setCustomURLHost(webhookObject.customURLHost);
        setCustomURLPort(webhookObject.customURLPort);
        setCustomURLPath(webhookObject.customURLPath);
        setWebhookParams(webhookObject.webhookParams);
        setWebhookHeaders(webhookObject.webhookHeaders);
      } else if (type === BACKEND_CHANNEL_TYPE.SES) {
        // TODO
      } else if (type === BACKEND_CHANNEL_TYPE.SNS) {
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
    if (channelType === BACKEND_CHANNEL_TYPE.SLACK) {
      errors.slackWebhook = validateWebhookURL(slackWebhook);
    } else if (channelType === BACKEND_CHANNEL_TYPE.CHIME) {
      errors.chimeWebhook = validateWebhookURL(chimeWebhook);
    } else if (channelType === BACKEND_CHANNEL_TYPE.EMAIL) {
      errors.sender = validateEmailSender(selectedSenderOptions);
      errors.recipients = validateRecipients(selectedRecipientGroupOptions);
    } else if (channelType === BACKEND_CHANNEL_TYPE.CUSTOM_WEBHOOK) {
      if (webhookTypeIdSelected === 'WEBHOOK_URL') {
        errors.webhookURL = validateWebhookURL(webhookURL);
      } else {
        errors.customURLHost = validateCustomURLHost(customURLHost);
        errors.customURLPort = validateCustomURLPort(customURLPort);
      }
    } else if (channelType === BACKEND_CHANNEL_TYPE.SNS) {
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
    if (channelType === BACKEND_CHANNEL_TYPE.SLACK) {
      config.slack = { url: slackWebhook };
    } else if (channelType === BACKEND_CHANNEL_TYPE.CHIME) {
      config.chime = { url: chimeWebhook };
    } else if (channelType === BACKEND_CHANNEL_TYPE.CUSTOM_WEBHOOK) {
      config.webhook = constructWebhookObject(
        webhookTypeIdSelected,
        webhookURL,
        customURLHost,
        customURLPort,
        customURLPath,
        webhookParams,
        webhookHeaders
      );
    } else if (channelType === BACKEND_CHANNEL_TYPE.EMAIL) {
      config.email = constructEmailObject(
        selectedSenderOptions,
        selectedRecipientGroupOptions
      );
    }
    return config;
  };

  const sendTestMessage = async () => {
    const config = createConfigObject();
    config.name = 'temp-' + config.name;
    let tempChannelId;
    try {
      tempChannelId = await servicesContext.notificationService
        .createConfig(config)
        .then((response) => {
          console.info(
            'Created temporary channel to test send message:',
            response
          );
          return response.config_id;
        })
        .catch((error) => {
          error.message =
            'Failed to create temporary channel for test message. ' +
            error.message;
          throw error;
        });

      const eventId = await servicesContext.eventService
        .sendTestMessage(
          tempChannelId,
          config.feature_list[0] // for test message any source works
        )
        .then((response) => response.event_id);

      await servicesContext.eventService
        .getNotification(eventId)
        .then((response) => {
          if (!response.success) {
            const error = new Error('Failed to send the test message.');
            error.stack = JSON.stringify(response.status_list, null, 2);
            throw error;
          }
        });
      coreContext.notifications.toasts.addSuccess(
        'Successfully sent a test message.'
      );
    } catch (error) {
      coreContext.notifications.toasts.addError(error, {
        title: 'Failed to send the test message.',
        toastMessage: 'View error details and adjust the channel settings.',
      });
    } finally {
      if (tempChannelId) {
        servicesContext.notificationService
          .deleteConfigs([tempChannelId])
          .then((response) => {
            console.info('Deleted temporary channel:', response);
          })
          .catch((error) => {
            coreContext.notifications.toasts.addError(error, {
              title: 'Failed to delete temporary channel for test message.',
            });
          });
      }
    }
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
          {channelType === BACKEND_CHANNEL_TYPE.SLACK ? (
            <SlackSettings
              slackWebhook={slackWebhook}
              setSlackWebhook={setSlackWebhook}
            />
          ) : channelType === BACKEND_CHANNEL_TYPE.CHIME ? (
            <ChimeSettings
              chimeWebhook={chimeWebhook}
              setChimeWebhook={setChimeWebhook}
            />
          ) : channelType === BACKEND_CHANNEL_TYPE.EMAIL ||
            channelType === BACKEND_CHANNEL_TYPE.SES ? (
            <EmailSettings
              isAmazonSES={channelType === BACKEND_CHANNEL_TYPE.SES}
              selectedSenderOptions={selectedSenderOptions}
              setSelectedSenderOptions={setSelectedSenderOptions}
              selectedRecipientGroupOptions={selectedRecipientGroupOptions}
              setSelectedRecipientGroupOptions={
                setSelectedRecipientGroupOptions
              }
              sesSender={sesSender}
              setSesSender={setSesSender}
            />
          ) : channelType === BACKEND_CHANNEL_TYPE.CUSTOM_WEBHOOK ? (
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
          ) : channelType === BACKEND_CHANNEL_TYPE.SNS ? (
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
                sendTestMessage();
              }}
            >
              Send test message
            </EuiButton>
          </EuiFlexItem>
          <EuiFlexItem grow={false}>
            <EuiButton
              fill
              data-test-subj="create-channel-create-button"
              isLoading={loading}
              onClick={async () => {
                if (!isInputValid()) {
                  coreContext.notifications.toasts.addDanger(
                    'Some fields are invalid. Fix all highlighted error(s) before continuing.'
                  );
                  return;
                }
                setLoading(true);
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
                    setTimeout(() => (location.hash = prevURL), SERVER_DELAY);
                  })
                  .catch((error) => {
                    setLoading(false);
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
