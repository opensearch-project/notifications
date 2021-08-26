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
import {
  coreServicesMock,
  mainStateMock,
} from '../../../../test/mocks/serviceMock';
import { CoreServicesContext } from '../../../components/coreServices';
import { ServicesContext } from '../../../services';
import { MainContext } from '../../Main/Main';
import { SNSSettings } from '../components/SNSSettings';
import { CreateChannelContext } from '../CreateChannel';

describe('<SNSSettings /> spec', () => {
  configure({ adapter: new Adapter() });

  it('renders the component', async () => {
    const notificationServiceMock = jest.fn() as any;

    const setTopicArn = jest.fn();
    const setRoleArn = jest.fn();
    const setInputErrors = jest.fn();
    const utils = render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <MainContext.Provider value={mainStateMock}>
            <CreateChannelContext.Provider
              value={{
                edit: false,
                inputErrors: { topicArn: [], roleArn: [] },
                setInputErrors,
              }}
            >
              <SNSSettings
                topicArn={'test-topic'}
                setTopicArn={setTopicArn}
                roleArn={'test-role'}
                setRoleArn={setRoleArn}
              />
            </CreateChannelContext.Provider>
          </MainContext.Provider>
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();
  });

  it('renders and edits fields', () => {
    const notificationServiceMock = jest.fn() as any;

    const setTopicArn = jest.fn();
    const setRoleArn = jest.fn();
    const setInputErrors = jest.fn();
    const utils = render(
      <ServicesContext.Provider value={notificationServiceMock}>
        <CoreServicesContext.Provider value={coreServicesMock}>
          <MainContext.Provider
            value={{ ...mainStateMock, tooltipSupport: false }}
          >
            <CreateChannelContext.Provider
              value={{
                edit: false,
                inputErrors: { topicArn: [], roleArn: [] },
                setInputErrors,
              }}
            >
              <SNSSettings
                topicArn={'test-topic'}
                setTopicArn={setTopicArn}
                roleArn={'test-role'}
                setRoleArn={setRoleArn}
              />
            </CreateChannelContext.Provider>
          </MainContext.Provider>
        </CoreServicesContext.Provider>
      </ServicesContext.Provider>
    );
    expect(utils.container.firstChild).toMatchSnapshot();

    const topicArnInput = utils.getByTestId('sns-settings-topic-arn-input');
    fireEvent.change(topicArnInput, { target: { value: 'test-update-topic' } });
    fireEvent.blur(topicArnInput);
    expect(setTopicArn).toBeCalledWith('test-update-topic');
    expect(setInputErrors).toBeCalled();

    const roleArnInput = utils.getByTestId('sns-settings-role-arn-input');
    fireEvent.change(roleArnInput, { target: { value: 'test-update-role' } });
    fireEvent.blur(roleArnInput);
    expect(setRoleArn).toBeCalledWith('test-update-role');
    expect(setInputErrors).toBeCalled();
  });
});
