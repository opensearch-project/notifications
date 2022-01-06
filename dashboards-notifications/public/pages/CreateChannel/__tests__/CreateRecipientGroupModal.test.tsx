/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { render } from '@testing-library/react';
import React from 'react';
import {
  coreServicesMock,
  notificationServiceMock,
} from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { CreateRecipientGroupModal } from '../components/modals/CreateRecipientGroupModal';

describe('<CreateRecipientGroupModal/> spec', () => {
  it('renders the component', () => {
    const utils = render(
      <CreateRecipientGroupModal
        addRecipientGroupOptionAndSelect={() => {}}
        onClose={() => {}}
        services={notificationServiceMock}
      />
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('validates input', async () => {
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <CreateRecipientGroupModal
          addRecipientGroupOptionAndSelect={() => {}}
          onClose={() => {}}
          services={notificationServiceMock}
        />
      </CoreServicesContext.Provider>
    );
    utils.getByText('Create').click();
    expect(utils.container.firstChild).toMatchSnapshot();
  });
});
