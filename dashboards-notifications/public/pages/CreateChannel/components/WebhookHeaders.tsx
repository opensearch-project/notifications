/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
import { HeaderItemType } from '../../Channels/types';

interface WebhookHeadersProps {
  headers: HeaderItemType[];
  setHeaders: (headers: HeaderItemType[]) => void;
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
                    disabled={
                      props.type === 'header' &&
                      i === 0 &&
                      header.key === 'Content-Type'
                    } // first header needs to be Content-Type
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
                    onClick={() => {
                      const newHeaders = [...props.headers];
                      newHeaders.splice(i, 1);
                      props.setHeaders(newHeaders);
                    }}
                    disabled={
                      props.type === 'header' &&
                      i === 0 &&
                      header.key === 'Content-Type'
                    }
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
        onClick={() => {
          props.setHeaders([...props.headers, { key: '', value: '' }]);
        }}
      >
        {`Add ${props.type}`}
      </EuiButton>
    </>
  );
}
