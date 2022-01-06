/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { render } from '@testing-library/react';
import React from 'react';
import { NotificationsHistogram } from '../components/NotificationsHistogram/NotificationsHistogram';

describe('<NotificationsHistogram /> spec', () => {
  it('renders the component', () => {
    const utils = render(<NotificationsHistogram />);
    expect(utils.container.firstChild).toMatchSnapshot();
  });
});
