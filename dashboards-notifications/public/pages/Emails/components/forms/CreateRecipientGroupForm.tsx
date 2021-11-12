/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import {
  EuiComboBox,
  EuiComboBoxOptionOption,
  EuiFieldText,
  EuiFormRow,
  EuiSpacer,
  EuiText,
  EuiTextArea,
} from '@elastic/eui';
import React from 'react';
import { onComboBoxCreateOption } from '../../utils/helper';
import {
  validateRecipientGroupEmails,
  validateRecipientGroupName,
} from '../../utils/validationHelper';

interface CreateRecipientGroupFormProps {
  name: string;
  setName: (name: string) => void;
  description: string;
  setDescription: (name: string) => void;
  selectedEmailOptions: Array<EuiComboBoxOptionOption<string>>;
  setSelectedEmailOptions: (
    options: Array<EuiComboBoxOptionOption<string>>
  ) => void;
  emailOptions: Array<EuiComboBoxOptionOption<string>>;
  setEmailOptions: (options: Array<EuiComboBoxOptionOption<string>>) => void;
  inputErrors: { [key: string]: string[] };
  setInputErrors: (errors: { [key: string]: string[] }) => void;
}

export function CreateRecipientGroupForm(props: CreateRecipientGroupFormProps) {
  return (
    <>
      <EuiFormRow
        label="Name"
        style={{ maxWidth: '650px' }}
        helpText="The name must contain 2 to 50 characters. Valid characters are A-Z, a-z, 0-9, (_) underscore, (-) hyphen and unicode characters."
        error={props.inputErrors.name.join(' ')}
        isInvalid={props.inputErrors.name.length > 0}
      >
        <EuiFieldText
          fullWidth
          data-test-subj="create-recipient-group-form-name-input"
          placeholder="Enter recipient group name"
          value={props.name}
          onChange={(e) => props.setName(e.target.value)}
          isInvalid={props.inputErrors.name.length > 0}
          onBlur={() => {
            props.setInputErrors({
              ...props.inputErrors,
              name: validateRecipientGroupName(props.name),
            });
          }}
        />
      </EuiFormRow>

      <EuiSpacer size="m" />
      <EuiFormRow
        label={
          <span>
            Description - <i style={{ fontWeight: 'normal' }}>optional</i>
          </span>
        }
        style={{ maxWidth: '650px' }}
      >
        <>
          <EuiText size="xs" color="subdued">
            Describe the purpose of the channel.
          </EuiText>
          <EuiTextArea
            fullWidth
            data-test-subj="create-recipient-group-form-description-input"
            placeholder="What is the purpose of this recipient group?"
            style={{ height: '4.1rem' }}
            value={props.description}
            onChange={(e) => props.setDescription(e.target.value)}
          />
        </>
      </EuiFormRow>

      <EuiSpacer size="m" />
      <EuiFormRow
        label="Emails"
        style={{ maxWidth: '650px' }}
        error={props.inputErrors.emailOptions.join(' ')}
        isInvalid={props.inputErrors.emailOptions.length > 0}
      >
        <>
          <EuiText size="xs" color="subdued">
            Select or type in one or more email addresses.
          </EuiText>
          <EuiComboBox
            placeholder="Email addresses"
            data-test-subj="create-recipient-group-form-emails-input"
            fullWidth
            options={props.emailOptions}
            selectedOptions={props.selectedEmailOptions}
            onChange={props.setSelectedEmailOptions}
            onCreateOption={(searchValue, flattenedOptions) =>
              onComboBoxCreateOption(
                searchValue,
                flattenedOptions,
                props.emailOptions,
                props.setEmailOptions,
                props.selectedEmailOptions,
                props.setSelectedEmailOptions,
                (options) =>
                  props.setInputErrors({
                    ...props.inputErrors,
                    emailOptions: validateRecipientGroupEmails(options),
                  })
              )
            }
            customOptionText={'Add {searchValue} as an email address'}
            isClearable={true}
            isInvalid={props.inputErrors.emailOptions.length > 0}
            onBlur={() => {
              props.setInputErrors({
                ...props.inputErrors,
                emailOptions: validateRecipientGroupEmails(
                  props.selectedEmailOptions
                ),
              });
            }}
          />
        </>
      </EuiFormRow>

      <EuiSpacer size="m" />
    </>
  );
}
