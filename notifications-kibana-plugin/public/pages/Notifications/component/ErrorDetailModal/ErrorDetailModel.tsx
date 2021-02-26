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

import React from 'react';
import {
  EuiButton,
  EuiModal,
  EuiModalBody,
  EuiModalFooter,
  EuiModalHeader,
  EuiModalHeaderTitle,
  EuiOverlayMask,
  EuiTitle,
  EuiHorizontalRule,
} from '@elastic/eui';
import { NotificationStatusDetail } from '../../../../../models/interfaces';

interface ErrorDetailModalProps {
  detail: NotificationStatusDetail[];
  onClose: () => void;
}

const ErrorDetailModal = ({ detail, onClose }: ErrorDetailModalProps) => {
  return (
    <EuiOverlayMask>
      {/*
      // @ts-ignore */}
      <EuiModal onCancel={onClose} onClose={onClose} maxWidth={1000}>
        <EuiModalHeader>
          <EuiModalHeaderTitle>Notification Errors</EuiModalHeaderTitle>
        </EuiModalHeader>
        {/*TODO: render the errors as UXDR */}
        <EuiModalBody>
          <EuiTitle size="xxs">
            <h1>Error message</h1>
          </EuiTitle>
          {detail.map((i) => {
            return (
              <div>
                <EuiHorizontalRule margin="s" />
                <p>{i.statusText}</p>
              </div>
            );
          })}
        </EuiModalBody>
        <EuiModalFooter>
          <EuiButton
            fill
            onClick={onClose}
            data-test-subj="ErrorDetailModalCloseButton"
          >
            Close
          </EuiButton>
        </EuiModalFooter>
      </EuiModal>
    </EuiOverlayMask>
  );
};

export default ErrorDetailModal;
