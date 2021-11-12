/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { ComponentType } from 'react';
import { BrowserServices } from '../../models/interfaces';
import { ModalConsumer } from './Modal';

export interface ModalRootProps {
  services: BrowserServices;
}

// All modals will have access to the BrowserServices if they need it
const ModalRoot: React.FunctionComponent<ModalRootProps> = ({ services }) => (
  <ModalConsumer>
    {({
      component: Komponent,
      props,
      onClose,
    }: {
      component: ComponentType<{
        onClose: () => void;
        services: BrowserServices;
      }> | null;
      props: object;
      onClose: () => void;
    }) =>
      Komponent ? (
        <Komponent {...props} onClose={onClose} services={services} />
      ) : null
    }
  </ModalConsumer>
);

export default ModalRoot;
