/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { render } from '@testing-library/react';
import React from 'react';
import { HistogramControls } from '../components/NotificationsHistogram/HistogramControls';

describe('<HistogramControls /> spec', () => {
  it('renders the component', () => {
    const setHistogramType = jest.fn();
    const utils = render(
      <HistogramControls
        histogramType="CHANNEL_TYPE"
        setHistogramType={setHistogramType}
      />
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });
});
