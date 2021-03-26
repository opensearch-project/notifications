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
} from '@elastic/eui';
import React from 'react';
import { CUSTOM_WEBHOOK_ENDPOINT_TYPE } from '../../../utils/constants';
import { HeaderType } from '../CreateChannel';
import { WebhookHeaders } from './WebhookHeaders';

interface CustomWebhookSettingsProps {
  webhookTypeIdSelected: keyof typeof CUSTOM_WEBHOOK_ENDPOINT_TYPE;
  setWebhookTypeIdSelected: (
    id: keyof typeof CUSTOM_WEBHOOK_ENDPOINT_TYPE
  ) => void;
  webhookURL: string;
  setWebhookURL: (webhookURL: string) => void;
  customURLHost: string;
  setCustomURLHost: (customURLHost: string) => void;
  customURLPort: string;
  setCustomURLPort: (customURLPort: string) => void;
  customURLPath: string;
  setCustomURLPath: (customURLPath: string) => void;
  webhookParams: HeaderType[];
  setWebhookParams: (webhookParams: HeaderType[]) => void;
  webhookHeaders: HeaderType[];
  setWebhookHeaders: (webhookHeaders: HeaderType[]) => void;
}

export function CustomWebhookSettings(props: CustomWebhookSettingsProps) {
  const webhookTypeOptions: EuiRadioGroupOption[] = Object.entries(
    CUSTOM_WEBHOOK_ENDPOINT_TYPE
  ).map(([key, value]) => ({
    id: key,
    label: value,
  }));

  const renderWebhook = () => {
    return (
      <EuiFormRow label="Webhook URL">
        <EuiFieldText
          placeholder=""
          value={props.webhookURL}
          onChange={(e) => props.setWebhookURL(e.target.value)}
        />
      </EuiFormRow>
    );
  };

  const renderCustomURL = () => {
    return (
      <>
        <EuiFormRow label="Host">
          <EuiFieldText
            placeholder=""
            value={props.customURLHost}
            onChange={(e) => props.setCustomURLHost(e.target.value)}
          />
        </EuiFormRow>
        <EuiFormRow label="Port">
          <EuiFieldNumber
            placeholder=""
            value={props.customURLPort}
            onChange={(e) => props.setCustomURLPort(e.target.value)}
          />
        </EuiFormRow>
        <EuiFormRow label="Path">
          <EuiFieldText
            placeholder=""
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
