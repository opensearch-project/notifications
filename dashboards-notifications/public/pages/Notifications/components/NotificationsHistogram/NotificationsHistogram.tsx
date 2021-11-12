/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
  Datum,
  niceTimeFormatByDay,
  ScaleType,
  Settings,
  timeFormatter,
} from '@elastic/charts';
import { euiPaletteColorBlind, EuiSpacer } from '@elastic/eui';
import _ from 'lodash';
import React from 'react';
import {
  ContentPanel,
  ContentPanelActions,
} from '../../../../components/ContentPanel';
import { HISTOGRAM_TYPE } from '../../../../utils/constants';
import { HistogramControls } from './HistogramControls';

interface NotificationsHistogramProps {
  histogramType: keyof typeof HISTOGRAM_TYPE;
  setHistogramType: (histogramType: keyof typeof HISTOGRAM_TYPE) => void;
  histogramData: Array<Datum>;
}

export function NotificationsHistogram(props: NotificationsHistogramProps) {
  const formatter = timeFormatter(niceTimeFormatByDay(1));
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
            data={props.histogramData}
            xAccessor={'x'}
            yAccessors={['y']}
            splitSeriesAccessors={['g']}
            stackAccessors={['g']}
            xScaleType={ScaleType.Time}
          />
          <Axis id="bottom-axis" position="bottom" tickFormat={formatter} />
          <Axis id="left-axis" position="left" showGridLines />
        </Chart>
      </ContentPanel>
    </>
  );
}
