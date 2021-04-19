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

/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import {
  Axis,
  BarSeries,
  Chart,
  DataGenerator,
  Datum,
  Settings,
} from '@elastic/charts';
import { euiPaletteColorBlind, EuiSpacer } from '@elastic/eui';
import _ from 'lodash';
import React, { useEffect, useState } from 'react';
import {
  ContentPanel,
  ContentPanelActions,
} from '../../../../components/ContentPanel';
import { HISTOGRAM_TYPE } from '../../../../utils/constants';
import { HistogramControls } from './HistogramControls';

interface NotificationsHistogramProps {
  histogramType: keyof typeof HISTOGRAM_TYPE;
  setHistogramType: (histogramType: keyof typeof HISTOGRAM_TYPE) => void;
}

export function NotificationsHistogram(props: NotificationsHistogramProps) {
  const [data, setData] = useState<Datum[]>([]);

  useEffect(() => {
    const dg = new DataGenerator();
    const data = dg.generateGroupedSeries(25, 2, 'Channel-');
    data[18].y = 18;
    setData(data);
  }, []);

  return (
    <>
      <ContentPanel
        actions={
          <ContentPanelActions
            actions={[
              {
                component: (
                  <HistogramControls
                    histogramType={props.histogramType}
                    setHistogramType={props.setHistogramType}
                  />
                ),
              },
            ]}
          />
        }
        bodyStyles={{ padding: 'initial' }}
        title={`Notifications by ${_.get(
          HISTOGRAM_TYPE,
          props.histogramType,
          Object.values(HISTOGRAM_TYPE)[0]
        ).toLowerCase()}`}
        titleSize="m"
      >
        <EuiSpacer />
        <Chart size={{ height: 250 }}>
          <Settings
            theme={{
              colors: {
                vizColors: euiPaletteColorBlind({ sortBy: 'natural' }),
              },
            }}
            showLegend={true}
          />
          <BarSeries
            id="status"
            name="Status"
            data={data}
            xAccessor={'x'}
            yAccessors={['y']}
            splitSeriesAccessors={['g']}
            stackAccessors={['g']}
          />
          <Axis id="bottom-axis" position="bottom" />
          <Axis id="left-axis" position="left" showGridLines />
        </Chart>
      </ContentPanel>
    </>
  );
}
