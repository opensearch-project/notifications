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
  EuiFieldText,
  EuiFlexGroup,
  EuiFlexItem,
  EuiFormRow,
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

interface EmailSettingsProps {
  isAmazonSES: boolean;
  selectedSenderOptions: Array<EuiComboBoxOptionOption<string>>;
  setSelectedSenderOptions: (
    options: Array<EuiComboBoxOptionOption<string>>
  ) => void;
  selectedRecipientGroupOptions: Array<EuiComboBoxOptionOption<string>>;
  setSelectedRecipientGroupOptions: (
    options: Array<EuiComboBoxOptionOption<string>>
  ) => void;
  sesSender: string;
  setSesSender: (sesSender: string) => void;
}

export function EmailSettings(props: EmailSettingsProps) {
  const context = useContext(CreateChannelContext)!;
  const coreContext = useContext(CoreServicesContext)!;
  const servicesContext = useContext(ServicesContext)!;

  const [senderOptions, setSenderOptions] = useState<
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
      const senders = await servicesContext.notificationService.getSenders(
        getQueryObject('smtp_account', query)
      );
      setSenderOptions(
        senders.items.map((sender) => ({
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
      {props.isAmazonSES ? (
        <>
          <EuiFormRow
            label="Sender"
            helpText="Enter a sender email address that has been verified by Amazon SES."
            error={context.inputErrors.sesSender.join(' ')}
            isInvalid={context.inputErrors.sesSender.length > 0}
          >
            <EuiFieldText
              fullWidth
              placeholder="Enter a sender email address"
              value={props.sesSender}
              onChange={(e) => props.setSesSender(e.target.value)}
              isInvalid={context.inputErrors.sesSender.length > 0}
              onBlur={() => {
                context.setInputErrors({
                  ...context.inputErrors,
                  sesSender: validateEmailSender(props.sesSender),
                });
              }}
            />
          </EuiFormRow>
          <EuiSpacer size="m" />
        </>
      ) : (
        <>
          <EuiSpacer size="m" />
          <EuiFlexGroup>
            <EuiFlexItem style={{ maxWidth: 400 }}>
              <EuiFormRow
                label="Sender"
                helpText={`A destination only allows one sender. Use "Create sender" to create a sender with its email address, host, port, encryption method.`}
                error={context.inputErrors.sender.join(' ')}
                isInvalid={context.inputErrors.sender.length > 0}
              >
                <EuiComboBox
                  placeholder="Sender name"
                  fullWidth
                  singleSelection
                  options={senderOptions}
                  selectedOptions={props.selectedSenderOptions}
                  onChange={props.setSelectedSenderOptions}
                  isClearable={true}
                  isInvalid={context.inputErrors.sender.length > 0}
                  onBlur={() => {
                    context.setInputErrors({
                      ...context.inputErrors,
                      sender: validateEmailSender(props.selectedSenderOptions),
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
                            setSenderOptions([...senderOptions, newOption]);
                            props.setSelectedSenderOptions([newOption]);
                          },
                        })
                      }
                    >
                      Create sender
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
