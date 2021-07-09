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

import { fireEvent, render } from '@testing-library/react';
import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import React from 'react';
import { RouteComponentProps } from 'react-router-dom';
import Notifications from '..';
import {
  coreServicesMock,
  mainStateMock,
  notificationServiceMock,
} from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { ServicesContext } from '../../../services';
import { MainContext } from '../../Main/Main';

describe('<Notifications /> spec', () => {
  configure({ adapter: new Adapter() });

  it('renders the component', () => {
    const props = {
      location: { search: '' },
      match: { params: { id: 'test' } },
    };
    const utils = render(
      <MainContext.Provider value={mainStateMock}>
        <ServicesContext.Provider value={notificationServiceMock}>
          <CoreServicesContext.Provider value={coreServicesMock}>
            <Notifications
              {...(props as RouteComponentProps<{ id: string }>)}
              services={notificationServiceMock}
              mainProps={mainStateMock}
            />
          </CoreServicesContext.Provider>
        </ServicesContext.Provider>
      </MainContext.Provider>
    );

    fireEvent.change(utils.getByTestId('notifications-search-bar-input'), {
      target: { value: 'test' },
    });
    utils.getByText('Refresh').click();
    expect(utils.container.firstChild).toMatchSnapshot();
  });
});
