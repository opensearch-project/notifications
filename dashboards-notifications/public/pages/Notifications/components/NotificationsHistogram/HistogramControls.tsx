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
