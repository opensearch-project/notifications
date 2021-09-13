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

import { render, waitFor } from '@testing-library/react';
import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import React from 'react';
import { MOCK_DATA } from '../../../../test/mocks/mockData';
import {
  coreServicesMock,
  mainStateMock,
} from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { ServicesContext } from '../../../services';
import { MainContext } from '../../Main/Main';
import { EmailSettings } from '../components/EmailSettings';
import { CreateChannelContext } from '../CreateChannel';

describe('<EmailSettings /> spec', () => {
  configure({ adapter: new Adapter() });

  it('renders the component', async () => {
    const notificationServiceMock = jest.fn() as any;
    const getSenders = jest.fn(
      async (queryObject: object) => MOCK_DATA.senders
    );
    const getRecipientGroups = jest.fn(
      async (queryObject: object) => MOCK_DATA.recipientGroups
    );
    notificationServiceMock.notificationService = {
      getSenders,
      getRecipientGroups,
    };

    const setSelectedSenderOptions = jest.fn();
    const setSelectedRecipientGroupOptions = jest.fn();
    const setInputErrors = jest.fn();
    const setSenderType = jest.fn();
    const utils = render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <MainContext.Provider value={mainStateMock}>
            <CreateChannelContext.Provider
              value={{
                edit: false,
                inputErrors: { sesSender: [], smtpSender: [], recipients: [] },
                setInputErrors,
              }}
            >
              <EmailSettings
                senderType={'smtp_account'}
                setSenderType={setSenderType}
                selectedSmtpSenderOptions={[]}
                setSelectedSmtpSenderOptions={setSelectedSenderOptions}
                selectedSesSenderOptions={[]}
                setSelectedSesSenderOptions={setSelectedSenderOptions}
                selectedRecipientGroupOptions={[]}
                setSelectedRecipientGroupOptions={
                  setSelectedRecipientGroupOptions
                }
              />
            </CreateChannelContext.Provider>
          </MainContext.Provider>
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );

    utils.getByText('Create SMTP sender').click();
    utils.getByText('Create recipient group').click();

    await waitFor(() => {
      expect(getSenders).toBeCalled();
      expect(getRecipientGroups).toBeCalled();
    });
  });
});
