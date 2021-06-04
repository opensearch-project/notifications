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

import {
  EuiButton,
  EuiInMemoryTable,
  EuiModal,
  EuiModalBody,
  EuiModalFooter,
  EuiModalHeader,
  EuiModalHeaderTitle,
  EuiOverlayMask,
  EuiTableFieldDataColumnType,
} from '@elastic/eui';
import React from 'react';
import { ModalRootProps } from '../../../../components/Modal/ModalRoot';
import { HeaderItemType } from '../../types';

interface DetailsTableModalProps extends ModalRootProps {
  header: string;
  isParameters: boolean; // headers or parameters
  items: HeaderItemType[];
  onClose: () => void;
}

export function DetailsTableModal(props: DetailsTableModalProps) {
  const keyColumn = props.isParameters ? 'Parameter' : 'Header';
  const columns = [
    {
      field: 'key',
      name: keyColumn,
      align: 'left',
      truncateText: false,
      render: (item) => (item ? item : '-'),
    },
    {
      field: 'value',
      name: 'Value',
      align: 'left',
      truncateText: false,
      render: (item) => (item ? item : '-'),
    },
  ] as Array<EuiTableFieldDataColumnType<HeaderItemType>>;

  return (
    <>
      <EuiOverlayMask>
        <EuiModal onClose={props.onClose} maxWidth={800}>
          <EuiModalHeader>
            <EuiModalHeaderTitle>{props.header}</EuiModalHeaderTitle>
          </EuiModalHeader>
          <EuiModalBody>
            <EuiInMemoryTable items={props.items} columns={columns} />
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
