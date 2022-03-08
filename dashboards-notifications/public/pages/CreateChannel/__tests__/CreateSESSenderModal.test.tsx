/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { fireEvent, render, waitFor } from '@testing-library/react';
import React from 'react';
import {
  coreServicesMock,
  mainStateMock,
  notificationServiceMock,
} from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { MainContext } from '../../Main/Main';
import { CreateSESSenderModal } from '../components/modals/CreateSESSenderModal';

describe('<CreateSESSenderModal/> spec', () => {
  it('renders the component', () => {
    const utils = render(
      <MainContext.Provider value={mainStateMock}>
        <CreateSESSenderModal
          addSenderOptionAndSelect={() => {}}
          onClose={() => {}}
          services={notificationServiceMock}
        />
      </MainContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('validates input', async () => {
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <MainContext.Provider value={mainStateMock}>
          <CreateSESSenderModal
            addSenderOptionAndSelect={() => {}}
            onClose={() => {}}
            services={notificationServiceMock}
          />
        </MainContext.Provider>
      </CoreServicesContext.Provider>
    );
    utils.getByText('Create').click();
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('creates senders', async () => {
    const notificationServiceMock = jest.fn() as any;
    const createConfig = jest.fn(async () => Promise.resolve());
    notificationServiceMock.notificationService = {
      createConfig,
    };
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <MainContext.Provider value={mainStateMock}>
          <CreateSESSenderModal
            addSenderOptionAndSelect={() => {}}
            onClose={() => {}}
            services={notificationServiceMock}
          />
        </MainContext.Provider>
      </CoreServicesContext.Provider>
    );

    const nameInput = utils.getByTestId('create-ses-sender-form-name-input');
    fireEvent.change(nameInput, { target: { value: 'test-name' } });
    const emailInput = utils.getByTestId('create-ses-sender-form-email-input');
    fireEvent.change(emailInput, { target: { value: 'test@email.com' } });
    const roleArnInput = utils.getByTestId(
      'create-ses-sender-form-role-arn-input'
    );
    fireEvent.change(roleArnInput, { target: { value: 'test-role' } });
    const awsRegionInput = utils.getByTestId(
      'create-ses-sender-form-aws-region-input'
    );
    fireEvent.change(awsRegionInput, { target: { value: 'us-east-2' } });

    utils.getByText('Create').click();
    await waitFor(() => {
      expect(createConfig).toBeCalled();
    });
  });

  // TODO: Skipping test due to timeout
  it.skip('handles failures', async () => {
    const notificationServiceMock = jest.fn() as any;
    const createConfig = jest.fn(async () => Promise.reject());
    notificationServiceMock.notificationService = {
      createConfig,
    };
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <MainContext.Provider value={mainStateMock}>
          <CreateSESSenderModal
            addSenderOptionAndSelect={() => {}}
            onClose={() => {}}
            services={notificationServiceMock}
          />
        </MainContext.Provider>
      </CoreServicesContext.Provider>
    );

    const nameInput = utils.getByTestId('create-ses-sender-form-name-input');
    fireEvent.change(nameInput, { target: { value: 'test-name' } });
    const emailInput = utils.getByTestId('create-ses-sender-form-email-input');
    fireEvent.change(emailInput, { target: { value: 'test@email.com' } });
    const roleArnInput = utils.getByTestId(
      'create-ses-sender-form-role-arn-input'
    );
    fireEvent.change(roleArnInput, { target: { value: 'test-role' } });
    const awsRegionInput = utils.getByTestId(
      'create-ses-sender-form-aws-region-input'
    );
    fireEvent.change(awsRegionInput, { target: { value: 'us-east-2' } });

    utils.getByText('Create').click();
    await waitFor(() => {
      expect(createConfig).toBeCalled();
    });
  });
});
