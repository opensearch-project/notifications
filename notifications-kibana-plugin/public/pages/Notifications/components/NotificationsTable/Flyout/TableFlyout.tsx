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
  EuiFlyout,
  EuiFlyoutBody,
  EuiFlyoutHeader,
  EuiTitle,
} from '@elastic/eui';
import React from 'react';

interface TableFlyoutProps {
  flyoutOpen: boolean;
  setFlyoutOpen: (flyoutOpen: boolean) => void;
}

export function TableFlyout(props: TableFlyoutProps) {
  return (
    <>
      {props.flyoutOpen ? (
        <EuiFlyout size="s" onClose={() => props.setFlyoutOpen(false)}>
          <EuiFlyoutHeader hasBorder>
            <EuiTitle>
              <h2>Notification details</h2>
            </EuiTitle>
          </EuiFlyoutHeader>
          <EuiFlyoutBody>hello</EuiFlyoutBody>
        </EuiFlyout>
      ) : null}
    </>
  );
}
