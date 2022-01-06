/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
