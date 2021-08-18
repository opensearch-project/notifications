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
  EuiHorizontalRule,
  EuiModal,
  EuiModalBody,
  EuiModalFooter,
  EuiModalHeader,
  EuiModalHeaderTitle,
  EuiOverlayMask,
  EuiTitle,
} from '@elastic/eui';
import React from 'react';
import { ModalRootProps } from '../../../../components/Modal/ModalRoot';

interface DetailsListModalProps extends ModalRootProps {
  header: string;
  title: string;
  items: string[];
  onClose: () => void;
}

export function DetailsListModal(props: DetailsListModalProps) {
  return (
    <>
      <EuiOverlayMask>
        <EuiModal onClose={props.onClose} maxWidth={800}>
          <EuiModalHeader>
            <EuiModalHeaderTitle>{props.header}</EuiModalHeaderTitle>
          </EuiModalHeader>
          <EuiModalBody>
            <EuiTitle size="xxs">
              <h1>{props.title}</h1>
            </EuiTitle>
            {props.items.map((item, i) => {
              return (
                <div key={`details-list-modal-item-${i}`}>
                  <EuiHorizontalRule margin="s" />
                  <p>{item}</p>
                </div>
              );
            })}
          </EuiModalBody>
          <EuiModalFooter>
            <EuiButton fill onClick={props.onClose}>
              Close
            </EuiButton>
          </EuiModalFooter>
        </EuiModal>
      </EuiOverlayMask>
    </>
  );
}
