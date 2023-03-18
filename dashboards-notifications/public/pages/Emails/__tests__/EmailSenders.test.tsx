/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { render, screen } from '@testing-library/react';
import React from 'react';
import { routerComponentPropsMock } from '../../../../test/mocks/routerPropsMock';
import {
  coreServicesMock,
  mainStateMock,
  notificationServiceMock,
} from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { ServicesContext } from '../../../services';
import { MainContext } from '../../Main/Main';
import { EmailSenders } from '../EmailSenders';

describe('<EmailSenders/> spec', () => {
  it('renders the component with SMTP config type', () => {
    const mainState = { ...mainStateMock,
      availableConfigTypes: [
        'slack',
        'chime',
        'webhook',
        'email',
        'sns',
        'smtp_account',
        'ses_account',
        'email_group',
      ],
    };
    const utils = render(
        <ServicesContext.Provider value={notificationServiceMock}>
          <CoreServicesContext.Provider value={coreServicesMock}>
            <MainContext.Provider value={mainState}>
              <EmailSenders {...routerComponentPropsMock} />
            </MainContext.Provider>
          </CoreServicesContext.Provider>
        </ServicesContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
    expect(screen.queryByText('SMTP senders')).not.toBeNull();
    expect(screen.queryByText('SES senders')).not.toBeNull();
  });

  it('renders the component without SMTP config type', async () => {
    const mainState = { ...mainStateMock,
      availableConfigTypes: [
        'slack',
        'chime',
        'webhook',
        'email',
        'sns',
        'ses_account',
        'email_group',
      ],
    };
    const utils = render(
        <ServicesContext.Provider value={notificationServiceMock}>
          <CoreServicesContext.Provider value={coreServicesMock}>
            <MainContext.Provider value={mainState}>
              <EmailSenders {...routerComponentPropsMock} />
            </MainContext.Provider>
          </CoreServicesContext.Provider>
        </ServicesContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
    expect(screen.queryByText('SMTP senders')).toBeNull();
    expect(screen.queryByText('SES senders')).not.toBeNull();
  });
});
