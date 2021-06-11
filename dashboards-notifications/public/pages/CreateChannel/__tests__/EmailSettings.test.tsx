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
import { coreServicesMock } from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { ServicesContext } from '../../../services';
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
    const setSesSender = jest.fn();
    const setInputErrors = jest.fn();
    const utils = render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <CreateChannelContext.Provider
            value={{
              edit: false,
              inputErrors: { sesSender: [], sender: [], recipients: [] },
              setInputErrors,
            }}
          >
            <EmailSettings
              isAmazonSES={false}
              selectedSenderOptions={[]}
              setSelectedSenderOptions={setSelectedSenderOptions}
              selectedRecipientGroupOptions={[]}
              setSelectedRecipientGroupOptions={
                setSelectedRecipientGroupOptions
              }
              sesSender={''}
              setSesSender={setSesSender}
            />
          </CreateChannelContext.Provider>
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );

    utils.getByText('Create sender').click();
    utils.getByText('Create recipient group').click();

    await waitFor(() => {
      expect(utils.container.firstChild).toMatchSnapshot();
      expect(getSenders).toBeCalled();
      expect(getRecipientGroups).toBeCalled();
    });
  });
});
