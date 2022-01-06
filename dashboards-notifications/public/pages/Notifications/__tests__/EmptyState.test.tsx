/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { fireEvent, render } from '@testing-library/react';
import { configure, mount, shallow } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import React from 'react';
import { EmptyState } from '../components/EmptyState/EmptyState';

describe('<EmptyState /> spec', () => {
  configure({ adapter: new Adapter() });

  it('renders the component with channels configured', () => {
    const utils = render(
      <EmptyState channels />
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('renders the component without channels configured', () => {
    const utils = render(
      <EmptyState channels={false} />
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

});
