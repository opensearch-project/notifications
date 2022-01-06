/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { fireEvent, render } from '@testing-library/react';
import { configure, mount } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import React from 'react';
import {
  coreServicesMock,
  notificationServiceMock,
} from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { DeleteSenderModal } from '../components/modals/DeleteSenderModal';

describe('<DeleteSenderModal /> spec', () => {
  configure({ adapter: new Adapter() });

  it('returns if no senders', () => {
    const utils = render(
      <DeleteSenderModal
        senders={[]}
        refresh={() => {}}
        onClose={() => {}}
        services={notificationServiceMock}
      />
    );
    expect(utils.container.firstChild).toBeNull();
  });

  it('renders the component', () => {
    const senders = [jest.fn() as any];
    const utils = render(
      <DeleteSenderModal
        senders={senders}
        refresh={() => {}}
        onClose={() => {}}
        services={notificationServiceMock}
      />
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('renders multiple senders', () => {
    const senders = [jest.fn() as any, jest.fn() as any];
    const utils = render(
      <DeleteSenderModal
        senders={senders}
        refresh={() => {}}
        onClose={() => {}}
        services={notificationServiceMock}
      />
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('deletes senders', () => {
    const senders = [jest.fn() as any, jest.fn() as any];
    const notificationServiceMock = jest.fn() as any;
    notificationServiceMock.notificationService = {
      deleteConfigs: async (ids: string[]) => Promise.resolve(),
    };
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <DeleteSenderModal
          senders={senders}
          refresh={() => {}}
          onClose={() => {}}
          services={notificationServiceMock}
        />
      </CoreServicesContext.Provider>
    );
    const input = utils.getAllByPlaceholderText('delete')[0];
    fireEvent.change(input, { target: { value: 'delete' } });
    const deleteButton = utils.getByText('Delete');
    fireEvent.click(deleteButton);
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('handles failures when deleting senders', () => {
    const senders = [jest.fn() as any, jest.fn() as any];
    const notificationServiceMock = jest.fn() as any;
    notificationServiceMock.notificationService = {
      deleteConfigs: async (ids: string[]) => Promise.reject(),
    };
    const utils = render(
      <CoreServicesContext.Provider value={coreServicesMock}>
        <DeleteSenderModal
          senders={senders}
          refresh={() => {}}
          onClose={() => {}}
          services={notificationServiceMock}
        />
      </CoreServicesContext.Provider>
    );
    const input = utils.getAllByPlaceholderText('delete')[0];
    fireEvent.change(input, { target: { value: 'delete' } });
    const deleteButton = utils.getByText('Delete');
    fireEvent.click(deleteButton);
    expect(utils.container.firstChild).toMatchSnapshot();
  });
});
