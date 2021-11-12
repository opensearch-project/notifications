/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import React from 'react';
import { EuiButton, EuiOverlayMask, EuiModal } from '@elastic/eui';
import { render, fireEvent } from '@testing-library/react';
import ModalRoot from '../Modal/ModalRoot';
import { ModalConsumer, ModalProvider } from '../Modal/Modal';
import { ServicesConsumer, ServicesContext } from '../../services/services';
import { notificationServiceMock } from '../../../test/mocks/serviceMock';

describe('<ModalRoot /> spec', () => {
  it('renders nothing when not used', () => {
    const { container } = render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <ModalProvider>
          <ServicesConsumer>
            {(services) => services && <ModalRoot services={services} />}
          </ServicesConsumer>
        </ModalProvider>
      </ServicesContext.Provider>
    );

    expect(container.firstChild).toBeNull();
  });

  it('renders a modal that can close and open', () => {
    const Modal = ({ onClose, text }: { onClose: () => {}; text: string }) => (
      <EuiOverlayMask>
        <EuiModal onClose={onClose}>A modal that has {text}</EuiModal>
      </EuiOverlayMask>
    );
    const { queryByText, getByTestId, getByLabelText } = render(
      <div>
        <ServicesContext.Provider value={notificationServiceMock}>
          <ModalProvider>
            <ServicesConsumer>
              {(services) => services && <ModalRoot services={services} />}
            </ServicesConsumer>
            <ModalConsumer>
              {({ onShow }) => (
                <EuiButton
                  data-test-subj="showModal"
                  onClick={() => onShow(Modal, { text: 'interesting text' })}
                >
                  Show Modal
                </EuiButton>
              )}
            </ModalConsumer>
          </ModalProvider>
        </ServicesContext.Provider>
      </div>
    );

    expect(queryByText('A modal that has interesting text')).toBeNull();

    fireEvent.click(getByTestId('showModal'));

    expect(queryByText('A modal that has interesting text')).not.toBeNull();

    fireEvent.click(getByLabelText('Closes this modal window'));

    expect(queryByText('A modal that has interesting text')).toBeNull();
  });
});
