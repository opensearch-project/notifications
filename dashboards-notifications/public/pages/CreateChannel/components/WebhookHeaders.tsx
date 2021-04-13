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
  EuiFieldText,
  EuiFlexGroup,
  EuiFlexItem,
  EuiFormRow,
  EuiSpacer,
  EuiText,
  EuiTitle,
} from '@elastic/eui';
import React from 'react';
import { HeaderType } from '../CreateChannel';

interface WebhookHeadersProps {
  headers: HeaderType[];
  setHeaders: (headers: HeaderType[]) => void;
  type: 'header' | 'parameter';
}

export function WebhookHeaders(props: WebhookHeadersProps) {
  const setHeader = (
    key: string | null,
    value: string | null,
    index: number
  ) => {
    const header = props.headers[index];
    const newHeaders = [...props.headers];
    if (key !== null) header.key = key;
    else if (value !== null) header.value = value;
    newHeaders.splice(index, 1, header);
    props.setHeaders(newHeaders);
  };

  return (
    <>
      <EuiTitle size="xs">
        <h4>
          {props.type === 'parameter' ? 'Query parameters' : 'Webhook headers'}
        </h4>
      </EuiTitle>

      {props.headers.length === 0 && (
        <>
          <EuiSpacer size="m" />
          <EuiText size="s">{`No ${props.type}s defined.`}</EuiText>
        </>
      )}

      {props.headers.map((header, i) => {
        return (
          <div key={`webhook-${props.type}-${i}`}>
            <EuiSpacer size="s" />
            <EuiFlexGroup style={{ maxWidth: 639 }}>
              <EuiFlexItem>
                <EuiFormRow label="Key">
                  <EuiFieldText
                    placeholder=""
                    value={header.key}
                    onChange={(e) => setHeader(e.target.value, null, i)}
                    disabled={props.type === 'header' && i === 0} // first header needs to be Content-Type
                  />
                </EuiFormRow>
              </EuiFlexItem>
              <EuiFlexItem>
                <EuiFormRow label="Value">
                  <EuiFieldText
                    placeholder=""
                    value={header.value}
                    onChange={(e) => setHeader(null, e.target.value, i)}
                  />
                </EuiFormRow>
              </EuiFlexItem>
              <EuiFlexItem>
                <EuiFormRow hasEmptyLabelSpace>
                  <EuiButton
                    size="s"
                    onClick={() => {
                      const newHeaders = [...props.headers];
                      newHeaders.splice(i, 1);
                      props.setHeaders(newHeaders);
                    }}
                    disabled={props.type === 'header' && i === 0}
                  >
                    {`Remove ${props.type}`}
                  </EuiButton>
                </EuiFormRow>
              </EuiFlexItem>
            </EuiFlexGroup>
          </div>
        );
      })}

      <EuiSpacer size="m" />
      <EuiButton
        size="s"
        onClick={() => {
          props.setHeaders([...props.headers, { key: '', value: '' }]);
        }}
      >
        {`Add ${props.type}`}
      </EuiButton>
    </>
  );
}
