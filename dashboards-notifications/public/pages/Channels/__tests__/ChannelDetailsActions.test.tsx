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

import { render } from '@testing-library/react';
import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import React from 'react';
import { MOCK_DATA } from '../../../../test/mocks/mockData';
import {
  coreServicesMock,
  notificationServiceMock,
} from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { ServicesContext } from '../../../services';
import { ChannelDetailsActions } from '../components/details/ChannelDetailsActions';

describe('<ChannelDetailsActions /> spec', () => {
  configure({ adapter: new Adapter() });

  it('renders the component', () => {
    const channel = MOCK_DATA.chime;
    const utils = render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <ChannelDetailsActions channel={channel} />
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('opens popover', () => {
    const channel = MOCK_DATA.chime;
    const utils = render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <ChannelDetailsActions channel={channel} />
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );
    utils.getByText('Actions').click();
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('clicks buttons in popover', () => {
    const channel = MOCK_DATA.chime;
    const utils = render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <ChannelDetailsActions channel={channel} />
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );
    utils.getByText('Actions').click();
    utils.getByText('Edit').click();
    utils.getByText('Actions').click();
    utils.getByText('Send test message').click();
    utils.getByText('Actions').click();
    utils.getByText('Delete').click();
    expect(utils.container.firstChild).toMatchSnapshot();
  });
});
