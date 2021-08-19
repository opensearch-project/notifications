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
import { ChannelSettingsDetails } from '../components/details/ChannelSettingsDetails';

describe('<ChannelSettingsDetails /> spec', () => {
  configure({ adapter: new Adapter() });

  it('renders the empty component', () => {
    const utils = render(<ChannelSettingsDetails channel={undefined} />);
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('renders Chime channel', () => {
    const utils = render(
      <ChannelSettingsDetails channel={MOCK_DATA.chime} />
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('renders Slack channel', () => {
    const utils = render(
      <ChannelSettingsDetails channel={MOCK_DATA.slack} />
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('renders Email channel', () => {
    const utils = render(
      <ChannelSettingsDetails channel={MOCK_DATA.email} />
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('renders Webhook channel', () => {
    const utils = render(
      <ChannelSettingsDetails channel={MOCK_DATA.webhook} />
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });
});
