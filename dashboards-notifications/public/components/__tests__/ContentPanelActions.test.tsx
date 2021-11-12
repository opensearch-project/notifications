/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import React from 'react';
import { render, fireEvent } from '@testing-library/react';
import ContentPanelActions from '../ContentPanel/ContentPanelActions';

describe('<ContentPanelActions /> spec', () => {
  it('renders the component', () => {
    const actions = [{ component: 'ContentPanelActions' }];
    const { container } = render(<ContentPanelActions actions={actions} />);
    expect(container.firstChild).toMatchSnapshot();
  });

  it('renders a button to click', () => {
    const spy = jest.fn();
    const actions = [
      {
        component: (
          <button data-test-subj="ContentPanelActionsButton" onClick={spy}>
            test
          </button>
        ),
      },
    ];
    const { getByTestId } = render(<ContentPanelActions actions={actions} />);
    fireEvent.click(getByTestId('ContentPanelActionsButton'));
    expect(spy).toHaveBeenCalledTimes(1);
  });
});
