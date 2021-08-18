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
  EuiComboBox,
  EuiComboBoxOptionOption,
  EuiFlexGroup,
  EuiFlexItem,
  EuiFormRow,
  EuiRadioGroup,
  EuiSpacer,
  SortDirection,
} from '@elastic/eui';
import React, { useCallback, useContext, useEffect, useState } from 'react';
import { CoreServicesContext } from '../../../components/coreServices';
import { ModalConsumer } from '../../../components/Modal';
import { ServicesContext } from '../../../services';
import { getErrorMessage } from '../../../utils/helpers';
import { CreateChannelContext } from '../CreateChannel';
import {
  validateEmailSender,
  validateRecipients,
} from '../utils/validationHelper';
import { CreateRecipientGroupModal } from './modals/CreateRecipientGroupModal';
import { CreateSenderModal } from './modals/CreateSenderModal';
import { CreateSESSenderModal } from './modals/CreateSesSenderModal';

interface EmailSettingsProps {
  senderType: 'smtp' | 'ses';
  setSenderType: (senderType: 'smtp' | 'ses') => void;
  selectedSmtpSenderOptions: Array<EuiComboBoxOptionOption<string>>;
  setSelectedSmtpSenderOptions: (
    options: Array<EuiComboBoxOptionOption<string>>
  ) => void;
  selectedSesSenderOptions: Array<EuiComboBoxOptionOption<string>>;
  setSelectedSesSenderOptions: (
    options: Array<EuiComboBoxOptionOption<string>>
  ) => void;
  selectedRecipientGroupOptions: Array<EuiComboBoxOptionOption<string>>;
  setSelectedRecipientGroupOptions: (
    options: Array<EuiComboBoxOptionOption<string>>
  ) => void;
}

export function EmailSettings(props: EmailSettingsProps) {
  const context = useContext(CreateChannelContext)!;
  const coreContext = useContext(CoreServicesContext)!;
  const servicesContext = useContext(ServicesContext)!;

  const [sesSenderOptions, setSesSenderOptions] = useState<
    Array<EuiComboBoxOptionOption<string>>
  >([]);
  const [smtpSenderOptions, setSmtpSenderOptions] = useState<
    Array<EuiComboBoxOptionOption<string>>
  >([]);
  const [recipientGroupOptions, setRecipientGroupOptions] = useState<
    Array<EuiComboBoxOptionOption<string>>
  >([]);

  const getQueryObject = (config_type: string, query?: string) => ({
    from_index: 0,
    max_items: 10000,
    config_type,
    sort_field: 'name',
    sort_order: SortDirection.ASC,
    ...(query ? { query } : {}),
  });

  const refreshSenders = useCallback(async (query?: string) => {
    try {
      const smtpSenders = await servicesContext.notificationService.getSenders(
        getQueryObject('smtp_account', query)
      );
      const sesSenders = await servicesContext.notificationService.getSenders(
        getQueryObject('ses_account', query)
      );
      setSmtpSenderOptions(
        smtpSenders.items.map((sender) => ({
          label: sender.name,
          value: sender.config_id,
        }))
      );
      setSesSenderOptions(
        sesSenders.items.map((sender) => ({
          label: sender.name,
          value: sender.config_id,
        }))
      );
    } catch (error) {
      coreContext.notifications.toasts.addDanger(
        getErrorMessage(error, 'There was a problem loading senders.')
      );
    }
  }, []);

  const refreshRecipientGroups = useCallback(async (query?: string) => {
    try {
      const recipientGroups = await servicesContext.notificationService.getRecipientGroups(
        getQueryObject('email_group', query)
      );
      setRecipientGroupOptions(
        recipientGroups.items.map((recipientGroup) => ({
          label: recipientGroup.name,
          value: recipientGroup.config_id,
        }))
      );
    } catch (error) {
      coreContext.notifications.toasts.addDanger(
        getErrorMessage(error, 'There was a problem loading recipient groups.')
      );
    }
  }, []);

  useEffect(() => {
    refreshSenders();
    refreshRecipientGroups();
  }, []);

  const onCreateEmailOption = (
    searchValue: string,
    flattenedOptions: Array<EuiComboBoxOptionOption<string>> = []
  ) => {
    const normalizedSearchValue = searchValue.trim().toLowerCase();
    if (!normalizedSearchValue) return;

    const newOption = { label: searchValue };
    if (
      flattenedOptions.findIndex(
        (option) => option.label.trim().toLowerCase() === normalizedSearchValue
      ) === -1
    ) {
      setRecipientGroupOptions([...recipientGroupOptions, newOption]);
    }
    props.setSelectedRecipientGroupOptions([
      ...props.selectedRecipientGroupOptions,
      newOption,
    ]);
  };

  return (
    <>
      <EuiFormRow label="Sender type">
        <EuiRadioGroup
          options={[
            {
              id: 'smtp',
              label: 'SMTP sender',
            },
            {
              id: 'ses',
              label: 'SES sender',
            },
          ]}
          idSelected={props.senderType}
          onChange={(id) => props.setSenderType(id as 'smtp' | 'ses')}
          name="sender type radio group"
        />
      </EuiFormRow>
      {props.senderType === 'ses' ? (
        <>
          <EuiSpacer size="m" />
          <EuiFlexGroup>
            <EuiFlexItem style={{ maxWidth: 400 }}>
              <EuiFormRow
                label="SES sender"
                helpText={`A destination only allows one SMTP or SES sender. Use "Create SES sender" to create a sender with its email address, host, port, encryption method.`}
                error={context.inputErrors.sesSender.join(' ')}
                isInvalid={context.inputErrors.sesSender.length > 0}
              >
                <EuiComboBox
                  placeholder="Sender name"
                  fullWidth
                  singleSelection
                  options={sesSenderOptions}
                  selectedOptions={props.selectedSesSenderOptions}
                  onChange={props.setSelectedSesSenderOptions}
                  isClearable={true}
                  isInvalid={context.inputErrors.sesSender.length > 0}
                  onBlur={() => {
                    context.setInputErrors({
                      ...context.inputErrors,
                      sesSender: validateEmailSender(
                        props.selectedSesSenderOptions
                      ),
                    });
                  }}
                />
              </EuiFormRow>
            </EuiFlexItem>
            <EuiFlexItem grow={false}>
              <EuiFormRow hasEmptyLabelSpace>
                <ModalConsumer>
                  {({ onShow }) => (
                    <EuiButton
                      onClick={() =>
                        onShow(CreateSESSenderModal, {
                          addSenderOptionAndSelect: (
                            newOption: EuiComboBoxOptionOption<string>
                          ) => {
                            setSesSenderOptions([
                              ...sesSenderOptions,
                              newOption,
                            ]);
                            props.setSelectedSesSenderOptions([newOption]);
                            context.setInputErrors({
                              ...context.inputErrors,
                              sesSender: validateEmailSender([newOption]),
                            });
                          },
                        })
                      }
                    >
                      Create SES sender
                    </EuiButton>
                  )}
                </ModalConsumer>
              </EuiFormRow>
            </EuiFlexItem>
          </EuiFlexGroup>
        </>
      ) : (
        <>
          <EuiSpacer size="m" />
          <EuiFlexGroup>
            <EuiFlexItem style={{ maxWidth: 400 }}>
              <EuiFormRow
                label="Smtp sender"
                helpText={`A destination only allows one sender. Use "Create sender" to create a sender with its email address, host, port, encryption method.`}
                error={context.inputErrors.smtpSender.join(' ')}
                isInvalid={context.inputErrors.smtpSender.length > 0}
              >
                <EuiComboBox
                  placeholder="Sender name"
                  fullWidth
                  singleSelection
                  options={smtpSenderOptions}
                  selectedOptions={props.selectedSmtpSenderOptions}
                  onChange={props.setSelectedSmtpSenderOptions}
                  isClearable={true}
                  isInvalid={context.inputErrors.smtpSender.length > 0}
                  onBlur={() => {
                    context.setInputErrors({
                      ...context.inputErrors,
                      smtpSender: validateEmailSender(
                        props.selectedSmtpSenderOptions
                      ),
                    });
                  }}
                />
              </EuiFormRow>
            </EuiFlexItem>
            <EuiFlexItem grow={false}>
              <EuiFormRow hasEmptyLabelSpace>
                <ModalConsumer>
                  {({ onShow }) => (
                    <EuiButton
                      onClick={() =>
                        onShow(CreateSenderModal, {
                          addSenderOptionAndSelect: (
                            newOption: EuiComboBoxOptionOption<string>
                          ) => {
                            setSmtpSenderOptions([
                              ...smtpSenderOptions,
                              newOption,
                            ]);
                            props.setSelectedSmtpSenderOptions([newOption]);
                            context.setInputErrors({
                              ...context.inputErrors,
                              smtpSender: validateEmailSender([newOption]),
                            });
                          },
                        })
                      }
                    >
                      Create SMTP sender
                    </EuiButton>
                  )}
                </ModalConsumer>
              </EuiFormRow>
            </EuiFlexItem>
          </EuiFlexGroup>
        </>
      )}

      <EuiFlexGroup>
        <EuiFlexItem style={{ maxWidth: 400 }}>
          <EuiFormRow
            label="Default recipients"
            helpText={`Add recipient(s) using an email address or pre-created email group. Use "Create email group" to create an email group.`}
            error={context.inputErrors.recipients.join(' ')}
            isInvalid={context.inputErrors.recipients.length > 0}
          >
            <EuiComboBox
              placeholder="Email address, recipient group name"
              fullWidth
              options={recipientGroupOptions}
              selectedOptions={props.selectedRecipientGroupOptions}
              onChange={props.setSelectedRecipientGroupOptions}
              onCreateOption={onCreateEmailOption}
              customOptionText={'Add {searchValue} as a default recipient'}
              isClearable={true}
              isInvalid={context.inputErrors.recipients.length > 0}
              onBlur={() => {
                context.setInputErrors({
                  ...context.inputErrors,
                  recipients: validateRecipients(
                    props.selectedRecipientGroupOptions
                  ),
                });
              }}
            />
          </EuiFormRow>
        </EuiFlexItem>
        <EuiFlexItem grow={false}>
          <EuiFormRow hasEmptyLabelSpace>
            <ModalConsumer>
              {({ onShow }) => (
                <EuiButton
                  onClick={() =>
                    onShow(CreateRecipientGroupModal, {
                      addRecipientGroupOptionAndSelect: (
                        newOption: EuiComboBoxOptionOption<string>
                      ) => {
                        setRecipientGroupOptions([
                          ...recipientGroupOptions,
                          newOption,
                        ]);
                        props.setSelectedRecipientGroupOptions([
                          ...props.selectedRecipientGroupOptions,
                          newOption,
                        ]);
                        context.setInputErrors({
                          ...context.inputErrors,
                          recipients: validateRecipients([newOption]),
                        });
                      },
                    })
                  }
                >
                  Create recipient group
                </EuiButton>
              )}
            </ModalConsumer>
          </EuiFormRow>
        </EuiFlexItem>
      </EuiFlexGroup>
    </>
  );
}
