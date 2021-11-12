/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { HISTOGRAM_TYPE } from '../../../../utils/constants';
import { EuiSuperSelect, EuiSuperSelectOption } from '@elastic/eui';
import React from 'react';

interface HistogramControlsProps {
  histogramType: keyof typeof HISTOGRAM_TYPE;
  setHistogramType: (histogramType: keyof typeof HISTOGRAM_TYPE) => void;
}

export function HistogramControls(props: HistogramControlsProps) {
  const histogramTypeOptions: Array<EuiSuperSelectOption<
    keyof typeof HISTOGRAM_TYPE
  >> = Object.entries(HISTOGRAM_TYPE).map(([value, inputDisplay]) => ({
    value: value as keyof typeof HISTOGRAM_TYPE,
    inputDisplay,
  }));

  return (
    <>
      <EuiSuperSelect
        style={{ width: 300 }}
        prepend="Slice by"
        fullWidth
        options={histogramTypeOptions}
        valueOfSelected={props.histogramType}
        onChange={props.setHistogramType}
      />
    </>
  );
}
