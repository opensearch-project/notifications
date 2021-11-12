/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
import { SenderType } from '../../../models/interfaces';
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
import { HeaderItemType, WebhookHttpType, WebhookMethodType } from '../Channels/types';
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
  >> = Object.entries(mainStateContext.availableChannels).map(
    ([key, value]) => ({
      value: key as keyof typeof CHANNEL_TYPE,
      inputDisplay: value,
    })
  );
  const [channelType, setChannelType] = useState(channelTypeOptions[0].value);

  const [slackWebhook, setSlackWebhook] = useState('');
  const [chimeWebhook, setChimeWebhook] = useState('');

  const [senderType, setSenderType] = useState<SenderType>('smtp_account');
  const [selectedSmtpSenderOptions, setSelectedSmtpSenderOptions] = useState<
    Array<EuiComboBoxOptionOption<string>>
  >([]);
  const [selectedSesSenderOptions, setSelectedSesSenderOptions] = useState<
    Array<EuiComboBoxOptionOption<string>>
  >([]);
  // "value" field is the config_id of recipient groups, if it doesn't exist means it's a custom email address
  const [
    selectedRecipientGroupOptions,
    setSelectedRecipientGroupOptions,
  ] = useState<Array<EuiComboBoxOptionOption<string>>>([]);

  const [webhookTypeIdSelected, setWebhookTypeIdSelected] = useState<
    keyof typeof CUSTOM_WEBHOOK_ENDPOINT_TYPE
  >('WEBHOOK_URL');
  const [webhookURL, setWebhookURL] = useState('');
  const [customURLType, setCustomURLType] = useState<WebhookHttpType>('HTTPS');
  const [customURLHost, setCustomURLHost] = useState('');
  const [customURLPort, setCustomURLPort] = useState('');
  const [customURLPath, setCustomURLPath] = useState('');
  const [webhookMethod, setWebhookMethod] = useState<WebhookMethodType>('POST');
  const [webhookParams, setWebhookParams] = useState<HeaderItemType[]>([]);
  const [webhookHeaders, setWebhookHeaders] = useState<HeaderItemType[]>([
    { key: 'Content-Type', value: 'application/json' },
  ]);
  const [topicArn, setTopicArn] = useState(''); // SNS topic ARN
  const [roleArn, setRoleArn] = useState(''); // IAM role ARN (optional for open source distribution)

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
    smtpSender: [],
    sesSender: [],
    recipients: [],
    webhookURL: [],
    customURLHost: [],
    customURLPort: [],
    topicArn: [],
    roleArn: [],
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
        .then(async (response) => {
          if (response.config_type === 'email') {
            const channel = await servicesContext.notificationService.getEmailConfigDetails(
              response
            );
            if (channel.email?.invalid_ids?.length) {
              coreContext.notifications.toasts.addDanger(
                'The sender and/or some recipient groups might have been deleted.'
              );
            }
            return channel;
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
        setSenderType(emailObject.senderType);
        if (emailObject.senderType === 'smtp_account') {
          setSelectedSmtpSenderOptions(emailObject.selectedSenderOptions);
        } else {
          setSelectedSesSenderOptions(emailObject.selectedSenderOptions);
        }
        setSelectedRecipientGroupOptions(
          emailObject.selectedRecipientGroupOptions
        );
      } else if (type === BACKEND_CHANNEL_TYPE.CUSTOM_WEBHOOK) {
        const webhookObject = deconstructWebhookObject(response.webhook!);
        setWebhookURL(webhookObject.webhookURL);
        setCustomURLHost(webhookObject.customURLHost);
        setCustomURLPort(webhookObject.customURLPort);
        setCustomURLPath(webhookObject.customURLPath);
        setWebhookMethod(webhookObject.webhookMethod);
        setWebhookParams(webhookObject.webhookParams);
        setWebhookHeaders(webhookObject.webhookHeaders);
      } else if (type === BACKEND_CHANNEL_TYPE.SNS) {
        setTopicArn(response.sns?.topic_arn || '');
        setRoleArn(response.sns?.role_arn || '');
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
      smtpSender: [],
      sesSender: [],
      recipients: [],
      webhookURL: [],
      customURLHost: [],
      customURLPort: [],
      topicArn: [],
      roleArn: [],
    };
    if (channelType === BACKEND_CHANNEL_TYPE.SLACK) {
      errors.slackWebhook = validateWebhookURL(slackWebhook);
    } else if (channelType === BACKEND_CHANNEL_TYPE.CHIME) {
      errors.chimeWebhook = validateWebhookURL(chimeWebhook);
    } else if (channelType === BACKEND_CHANNEL_TYPE.EMAIL) {
      if (senderType === 'smtp_account') {
        errors.smtpSender = validateEmailSender(selectedSmtpSenderOptions);
      } else {
        errors.sesSender = validateEmailSender(selectedSesSenderOptions);
      }
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
      if (!mainStateContext.tooltipSupport)
        errors.roleArn = validateArn(roleArn);
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
        customURLType,
        customURLHost,
        customURLPort,
        customURLPath,
        webhookMethod,
        webhookParams,
        webhookHeaders
      );
    } else if (channelType === BACKEND_CHANNEL_TYPE.EMAIL) {
      if (senderType === 'smtp_account') {
        config.email = constructEmailObject(
          selectedSmtpSenderOptions,
          selectedRecipientGroupOptions
        );
      } else {
        config.email = constructEmailObject(
          selectedSesSenderOptions,
          selectedRecipientGroupOptions
        );
      }
    } else if (channelType === BACKEND_CHANNEL_TYPE.SNS) {
      config.sns = {
        topic_arn: topicArn,
        ...(roleArn && { role_arn: roleArn }),
      };
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

      await servicesContext.eventService.sendTestMessage(
        tempChannelId,
        config.feature_list[0] // for test message any source works
      );
      coreContext.notifications.toasts.addSuccess(
        'Successfully sent a test message.'
      );
    } catch (error: any) {
      coreContext.notifications.toasts.addError(error?.body || error, {
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
            coreContext.notifications.toasts.addError(error?.body || error, {
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
          ) : channelType === BACKEND_CHANNEL_TYPE.EMAIL ? (
            <EmailSettings
              senderType={senderType}
              setSenderType={setSenderType}
              selectedSmtpSenderOptions={selectedSmtpSenderOptions}
              setSelectedSmtpSenderOptions={setSelectedSmtpSenderOptions}
              selectedSesSenderOptions={selectedSesSenderOptions}
              setSelectedSesSenderOptions={setSelectedSesSenderOptions}
              selectedRecipientGroupOptions={selectedRecipientGroupOptions}
              setSelectedRecipientGroupOptions={
                setSelectedRecipientGroupOptions
              }
            />
          ) : channelType === BACKEND_CHANNEL_TYPE.CUSTOM_WEBHOOK ? (
            <CustomWebhookSettings
              webhookTypeIdSelected={webhookTypeIdSelected}
              setWebhookTypeIdSelected={setWebhookTypeIdSelected}
              webhookURL={webhookURL}
              setWebhookURL={setWebhookURL}
              customURLType={customURLType}
              setCustomURLType={setCustomURLType}
              customURLHost={customURLHost}
              setCustomURLHost={setCustomURLHost}
              customURLPort={customURLPort}
              setCustomURLPort={setCustomURLPort}
              customURLPath={customURLPath}
              setCustomURLPath={setCustomURLPath}
              webhookMethod={webhookMethod}
              setWebhookMethod={setWebhookMethod}
              webhookParams={webhookParams}
              setWebhookParams={setWebhookParams}
              webhookHeaders={webhookHeaders}
              setWebhookHeaders={setWebhookHeaders}
            />
          ) : channelType === BACKEND_CHANNEL_TYPE.SNS ? (
            <SNSSettings
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
                    coreContext.notifications.toasts.addError(
                      error?.body || error,
                      {
                        title: `Failed to ${
                          props.edit ? 'update' : 'create'
                        } channel.`,
                      }
                    );
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
