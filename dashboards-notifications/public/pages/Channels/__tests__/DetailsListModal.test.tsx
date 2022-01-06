/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { render } from '@testing-library/react';
import { configure, mount } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import React from 'react';
import { notificationServiceMock } from '../../../../test/mocks/serviceMock';
import { DetailsListModal } from '../components/modals/DetailsListModal';

describe('<DetailsListModal /> spec', () => {
  configure({ adapter: new Adapter() });

  it('renders the component', () => {
    const items = [
      'test item 1',
      'test item 2',
      'test item 3',
      'test item 4',
      'test item 5',
      'test item 6',
      'test item 7',
      'test item 8',
    ];
    const onClose = jest.fn();
    const wrap = mount(
      <DetailsListModal
        header="test header"
        title="Email addresses"
        items={items}
        onClose={onClose}
        services={notificationServiceMock}
      />
    );
    expect(wrap).toMatchSnapshot();
  });
});
