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
  EuiCheckboxGroup,
  EuiCheckboxGroupOption,
  EuiComboBox,
  EuiComboBoxOptionOption,
  EuiFlexGroup,
  EuiFlexItem,
  EuiFormRow,
  EuiMarkdownEditor,
  EuiSpacer,
  EuiSuperSelect,
  EuiSuperSelectOption,
  EuiText,
} from '@elastic/eui';
import React, { useContext, useState } from 'react';
import { ModalConsumer } from '../../../components/Modal';
import { CreateChannelContext } from '../CreateChannel';
import {
  validateEmailSender,
  validateRecipients,
} from '../utils/validationHelper';
import { CreateRecipientGroupModal } from './modals/CreateRecipientGroupModal';
import { CreateSenderModal } from './modals/CreateSenderModal';

interface EmailSettingsProps {
  isAmazonSES: boolean;
  headerFooterCheckboxIdToSelectedMap: { [x: string]: boolean };
  setHeaderFooterCheckboxIdToSelectedMap: (map: {
    [x: string]: boolean;
  }) => void;
  emailHeader: string;
  setEmailHeader: (emailHeader: string) => void;
  emailFooter: string;
  setEmailFooter: (emailFooter: string) => void;
  sender: string;
  setSender: (sender: string) => void;
  selectedRecipientGroupOptions: Array<EuiComboBoxOptionOption<string>>;
  setSelectedRecipientGroupOptions: (
    options: Array<EuiComboBoxOptionOption<string>>
  ) => void;
}

export function EmailSettings(props: EmailSettingsProps) {
  const context = useContext(CreateChannelContext)!;
  const checkboxOptions: EuiCheckboxGroupOption[] = [
    {
      id: 'header',
      label: 'Add header',
    },
    {
      id: 'footer',
      label: 'Add footer',
    },
  ];

  const [senderOptions, setSenderOptions] = useState<
    Array<EuiSuperSelectOption<string>>
  >([
    {
      value: 'Admin',
      inputDisplay: 'Admin',
    },
  ]);
  const [recipientGroupOptions, setRecipientGroupOptions] = useState<
    Array<EuiComboBoxOptionOption<string>>
  >([
    {
      label: 'no-reply@company.com',
    },
  ]);

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
          <EuiFormRow label="Sender">
            <EuiText size="s">
              To configure sender account, modify your elasticsearch.yml
            </EuiText>
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
                <EuiSuperSelect
                  fullWidth
                  options={senderOptions}
                  valueOfSelected={props.sender}
                  onChange={props.setSender}
                  onBlur={() => {
                    context.setInputErrors({
                      ...context.inputErrors,
                      sender: validateEmailSender(props.sender),
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
                      size="s"
                      onClick={() =>
                        onShow(CreateSenderModal, {
                          addSenderOptionAndSelect: (
                            newOption: EuiSuperSelectOption<string>
                          ) => {
                            setSenderOptions([...senderOptions, newOption]);
                            props.setSender(newOption.value);
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
              placeholder="Email address, email group name"
              fullWidth
              options={recipientGroupOptions}
              selectedOptions={props.selectedRecipientGroupOptions}
              onChange={props.setSelectedRecipientGroupOptions}
              onCreateOption={onCreateEmailOption}
              isClearable={true}
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
                  size="s"
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

      <EuiSpacer size="m" />
      <EuiFormRow>
        <EuiCheckboxGroup
          options={checkboxOptions}
          idToSelectedMap={props.headerFooterCheckboxIdToSelectedMap}
          onChange={(optionId: string) => {
            props.setHeaderFooterCheckboxIdToSelectedMap({
              ...props.headerFooterCheckboxIdToSelectedMap,
              ...{
                [optionId]: !props.headerFooterCheckboxIdToSelectedMap[
                  optionId
                ],
              },
            });
          }}
          legend={{ children: 'Header and footer' }}
        />
      </EuiFormRow>

      {props.headerFooterCheckboxIdToSelectedMap.header && (
        <EuiFormRow label="Header" fullWidth={true}>
          <EuiMarkdownEditor
            aria-labelledby="email-header-markdown-editor"
            value={props.emailHeader}
            onChange={props.setEmailHeader}
          />
        </EuiFormRow>
      )}

      {props.headerFooterCheckboxIdToSelectedMap.footer && (
        <EuiFormRow label="Footer" fullWidth={true}>
          <EuiMarkdownEditor
            aria-labelledby="email-footer-markdown-editor"
            value={props.emailFooter}
            onChange={props.setEmailFooter}
          />
        </EuiFormRow>
      )}
    </>
  );
}
