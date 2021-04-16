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
  EuiSpacer,
  EuiSuperSelect,
  EuiSuperSelectOption,
} from '@elastic/eui';
import React, { useContext, useState } from 'react';
import ReactMde from 'react-mde';
import { CreateChannelContext } from '../CreateChannel';
import { CreateRecipientGroupModal } from './modals/CreateRecipientGroupModal';
import { CreateSenderModal } from './modals/CreateSenderModal';
import { ModalConsumer } from '../../../components/Modal';
import { converter } from '../utils';
import {
  validateEmailSender,
  validateRecipients,
} from '../utils/validationHelper';
import 'react-mde/lib/styles/css/react-mde-all.css';

interface EmailSettingsProps {
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

  const [selectedTabFooter, setSelectedTabFooter] = React.useState<
    'write' | 'preview'
  >('write');
  const [selectedTabHeader, setSelectedTabHeader] = React.useState<
    'write' | 'preview'
  >('write');
  const senderOptions: Array<EuiSuperSelectOption<string>> = [
    {
      value: 'Admin',
      inputDisplay: 'Admin',
    },
  ];
  const [recipientGroupOptions, setRecipientGroupOptions] = useState([
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
      <EuiSpacer size="m" />
      <EuiFlexGroup>
        <EuiFlexItem style={{ maxWidth: 400 }}>
          <EuiFormRow
            label="Sender"
            helpText={`A destination only allows one sender. Use "Create sender" to create a sender with its email address, host, port, encryption method.`}
            error="Sender is required."
            isInvalid={context.inputErrors.sender}
          >
            <EuiSuperSelect
              fullWidth
              options={senderOptions}
              valueOfSelected={props.sender}
              onChange={props.setSender}
              onBlur={() => {
                const error = validateEmailSender(props.sender);
                if (error !== context.inputErrors.sender) {
                  context.setInputErrors({
                    ...context.inputErrors,
                    sender: error,
                  });
                }
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
                  onClick={() => onShow(CreateSenderModal, { test: 123 })}
                >
                  Create sender
                </EuiButton>
              )}
            </ModalConsumer>
          </EuiFormRow>
        </EuiFlexItem>
      </EuiFlexGroup>

      <EuiFlexGroup>
        <EuiFlexItem style={{ maxWidth: 400 }}>
          <EuiFormRow
            label="Default recipients"
            helpText={`Add recipient(s) using an email address or pre-created email group. Use "Create email group" to create an email group.`}
            error="Recipient is required."
            isInvalid={context.inputErrors.recipients}
          >
            <EuiComboBox
              placeholder=""
              fullWidth
              options={recipientGroupOptions}
              selectedOptions={props.selectedRecipientGroupOptions}
              onChange={props.setSelectedRecipientGroupOptions}
              onCreateOption={onCreateEmailOption}
              isClearable={true}
              onBlur={() => {
                const error = validateRecipients(
                  props.selectedRecipientGroupOptions
                );
                if (error !== context.inputErrors.recipients) {
                  context.setInputErrors({
                    ...context.inputErrors,
                    recipients: error,
                  });
                }
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
                    onShow(CreateRecipientGroupModal, { test: 123 })
                  }
                >
                  Create email group
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
          <ReactMde
            value={props.emailHeader}
            onChange={props.setEmailHeader}
            selectedTab={selectedTabHeader}
            onTabChange={setSelectedTabHeader}
            toolbarCommands={[
              ['header', 'bold', 'italic', 'strikethrough'],
              ['unordered-list', 'ordered-list', 'checked-list'],
            ]}
            generateMarkdownPreview={(markdown) =>
              Promise.resolve(converter.makeHtml(markdown))
            }
          />
        </EuiFormRow>
      )}

      {props.headerFooterCheckboxIdToSelectedMap.footer && (
        <EuiFormRow label="Footer" fullWidth={true}>
          <ReactMde
            value={props.emailFooter}
            onChange={props.setEmailFooter}
            selectedTab={selectedTabFooter}
            onTabChange={setSelectedTabFooter}
            toolbarCommands={[
              ['header', 'bold', 'italic', 'strikethrough'],
              ['unordered-list', 'ordered-list', 'checked-list'],
            ]}
            generateMarkdownPreview={(markdown) =>
              Promise.resolve(converter.makeHtml(markdown))
            }
          />
        </EuiFormRow>
      )}
    </>
  );
}
