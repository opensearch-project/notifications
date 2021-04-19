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
  EuiComboBox,
  EuiComboBoxOptionOption,
  EuiFieldText,
  EuiFormRow,
  EuiSpacer,
  EuiText,
  EuiTextArea,
} from '@elastic/eui';
import React from 'react';
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
  emailOptions: Array<{ label: string }>;
  setEmailOptions: (options: Array<{ label: string }>) => void;
  inputErrors: { [key: string]: string[] };
  setInputErrors: (errors: { [key: string]: string[] }) => void;
}

export function CreateRecipientGroupForm(props: CreateRecipientGroupFormProps) {
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
      props.setEmailOptions([...props.emailOptions, newOption]);
    }
    props.setSelectedEmailOptions([...props.selectedEmailOptions, newOption]);
  };

  return (
    <>
      <EuiFormRow
        label="Name"
        style={{ maxWidth: '650px' }}
        helpText="The name must contain 2 to 50 characters. Valid characters are lowercase A-Z, a-z, 0-9, (_) underscore, (-) hyphen and unicode characters."
        error={props.inputErrors.name.join(' ')}
        isInvalid={props.inputErrors.name.length > 0}
      >
        <EuiFieldText
          fullWidth
          placeholder="Enter recipient group name"
          value={props.name}
          onChange={(e) => props.setName(e.target.value)}
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
            placeholder="Description"
            style={{ height: '5.5rem' }}
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
        <EuiComboBox
          placeholder="Email addresses"
          fullWidth
          options={props.emailOptions}
          selectedOptions={props.selectedEmailOptions}
          onChange={props.setSelectedEmailOptions}
          onCreateOption={onCreateEmailOption}
          isClearable={true}
          onBlur={() => {
            props.setInputErrors({
              ...props.inputErrors,
              emailOptions: validateRecipientGroupEmails(
                props.selectedEmailOptions
              ),
            });
          }}
        />
      </EuiFormRow>

      <EuiSpacer size="m" />
    </>
  );
}
