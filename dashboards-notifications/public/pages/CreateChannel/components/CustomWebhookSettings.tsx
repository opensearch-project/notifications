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
  EuiFieldNumber,
  EuiFieldText,
  EuiFormRow,
  EuiRadioGroup,
  EuiRadioGroupOption,
  EuiSpacer,
  EuiSuperSelect,
} from '@elastic/eui';
import React, { useContext } from 'react';
import { CUSTOM_WEBHOOK_ENDPOINT_TYPE } from '../../../utils/constants';
import {
  HeaderItemType,
  WebhookHttpType,
  WebhookMethodType,
} from '../../Channels/types';
import { CreateChannelContext } from '../CreateChannel';
import {
  validateCustomURLHost,
  validateCustomURLPort,
  validateWebhookURL,
} from '../utils/validationHelper';
import { WebhookHeaders } from './WebhookHeaders';

interface CustomWebhookSettingsProps {
  webhookTypeIdSelected: keyof typeof CUSTOM_WEBHOOK_ENDPOINT_TYPE;
  setWebhookTypeIdSelected: (
    id: keyof typeof CUSTOM_WEBHOOK_ENDPOINT_TYPE
  ) => void;
  webhookURL: string;
  setWebhookURL: (webhookURL: string) => void;
  customURLType: WebhookHttpType;
  setCustomURLType: (customURLType: WebhookHttpType) => void;
  customURLHost: string;
  setCustomURLHost: (customURLHost: string) => void;
  customURLPort: string;
  setCustomURLPort: (customURLPort: string) => void;
  customURLPath: string;
  setCustomURLPath: (customURLPath: string) => void;
  webhookMethod: WebhookMethodType;
  setWebhookMethod: (webhookMethod: WebhookMethodType) => void;
  webhookParams: HeaderItemType[];
  setWebhookParams: (webhookParams: HeaderItemType[]) => void;
  webhookHeaders: HeaderItemType[];
  setWebhookHeaders: (webhookHeaders: HeaderItemType[]) => void;
}

export function CustomWebhookSettings(props: CustomWebhookSettingsProps) {
  const context = useContext(CreateChannelContext)!;
  const webhookTypeOptions: EuiRadioGroupOption[] = Object.entries(
    CUSTOM_WEBHOOK_ENDPOINT_TYPE
  ).map(([key, value]) => ({
    id: key,
    label: value,
  }));

  const renderWebhook = () => {
    return (
      <EuiFormRow
        label="Webhook URL"
        error={context.inputErrors.webhookURL.join(' ')}
        isInvalid={context.inputErrors.webhookURL.length > 0}
      >
        <EuiFieldText
          placeholder="https://name.example.com/XXXXX..."
          data-test-subj="custom-webhook-url-input"
          value={props.webhookURL}
          onChange={(e) => props.setWebhookURL(e.target.value)}
          isInvalid={context.inputErrors.webhookURL.length > 0}
          onBlur={() => {
            context.setInputErrors({
              ...context.inputErrors,
              webhookURL: validateWebhookURL(props.webhookURL),
            });
          }}
        />
      </EuiFormRow>
    );
  };

  const renderCustomURL = () => {
    return (
      <>
        <EuiFormRow label="Type">
          <EuiSuperSelect
            options={[
              { value: 'HTTPS', inputDisplay: 'HTTPS' },
              { value: 'HTTP', inputDisplay: 'HTTP' },
            ]}
            valueOfSelected={props.customURLType}
            onChange={props.setCustomURLType}
          />
        </EuiFormRow>
        <EuiFormRow
          label="Host"
          error={context.inputErrors.customURLHost.join(' ')}
          isInvalid={context.inputErrors.customURLHost.length > 0}
        >
          <EuiFieldText
            placeholder="name.example.com"
            data-test-subj="custom-webhook-host-input"
            value={props.customURLHost}
            onChange={(e) => props.setCustomURLHost(e.target.value)}
            isInvalid={context.inputErrors.customURLHost.length > 0}
            onBlur={() => {
              context.setInputErrors({
                ...context.inputErrors,
                customURLHost: validateCustomURLHost(props.customURLHost),
              });
            }}
          />
        </EuiFormRow>
        <EuiFormRow
          label={
            <span>
              Port - <i style={{ fontWeight: 'normal' }}>optional</i>
            </span>
          }
          error={context.inputErrors.customURLPort.join(' ')}
          isInvalid={context.inputErrors.customURLPort.length > 0}
        >
          <EuiFieldNumber
            placeholder="Enter port"
            data-test-subj="custom-webhook-port-input"
            value={props.customURLPort}
            onChange={(e) => props.setCustomURLPort(e.target.value)}
            isInvalid={context.inputErrors.customURLPort.length > 0}
            onBlur={() => {
              context.setInputErrors({
                ...context.inputErrors,
                customURLPort: validateCustomURLPort(props.customURLPort),
              });
            }}
          />
        </EuiFormRow>
        <EuiFormRow
          label={
            <span>
              Path - <i style={{ fontWeight: 'normal' }}>optional</i>
            </span>
          }
        >
          <EuiFieldText
            placeholder="Enter path"
            data-test-subj="custom-webhook-path-input"
            value={props.customURLPath}
            onChange={(e) => props.setCustomURLPath(e.target.value)}
          />
        </EuiFormRow>

        <EuiSpacer />
        <WebhookHeaders
          headers={props.webhookParams}
          setHeaders={props.setWebhookParams}
          type="parameter"
        />
      </>
    );
  };

  return (
    <>
      <EuiFormRow label="Method" style={{ maxWidth: '700px' }}>
        <EuiSuperSelect
          options={[
            { value: 'POST', inputDisplay: 'POST' },
            { value: 'PUT', inputDisplay: 'PUT' },
            { value: 'PATCH', inputDisplay: 'PATCH' },
          ]}
          valueOfSelected={props.webhookMethod}
          onChange={props.setWebhookMethod}
        />
      </EuiFormRow>

      <EuiFormRow label="Define endpoints by" style={{ maxWidth: '700px' }}>
        <EuiRadioGroup
          options={webhookTypeOptions}
          idSelected={props.webhookTypeIdSelected}
          onChange={(id: string) =>
            props.setWebhookTypeIdSelected(
              id as keyof typeof CUSTOM_WEBHOOK_ENDPOINT_TYPE
            )
          }
        />
      </EuiFormRow>
      {props.webhookTypeIdSelected === 'WEBHOOK_URL'
        ? renderWebhook()
        : renderCustomURL()}

      <EuiSpacer size="xxl" />
      <WebhookHeaders
        headers={props.webhookHeaders}
        setHeaders={props.setWebhookHeaders}
        type="header"
      />
    </>
  );
}
