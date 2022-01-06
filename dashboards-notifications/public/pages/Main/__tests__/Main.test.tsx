/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { render } from '@testing-library/react';
import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import React from 'react';
import {
  HashRouter as Router,
  Route,
  RouteComponentProps,
} from 'react-router-dom';
import Main from '..';
import {
  coreServicesMock,
  notificationServiceMock,
} from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { ServicesContext } from '../../../services';
import { ROUTES } from '../../../utils/constants';

describe('<Main /> spec', () => {
  configure({ adapter: new Adapter() });

  it('renders the component', () => {
    const mockProps = {
      location: { search: '', pathname: ROUTES.NOTIFICATIONS },
      match: { params: { id: 'test' } },
    };
    const utils = render(
      <Router>
        <Route
          render={(props) => (
            <ServicesContext.Provider value={notificationServiceMock}>
              <CoreServicesContext.Provider value={coreServicesMock}>
                <Main {...(mockProps as RouteComponentProps<{ id: string }>)} />
              </CoreServicesContext.Provider>
            </ServicesContext.Provider>
          )}
        />
      </Router>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });
});
