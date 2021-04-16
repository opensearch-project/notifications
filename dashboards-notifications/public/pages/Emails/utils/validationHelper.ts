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

export const validateSenderName = (name: string) => {
  const errors = [];
  if (name.length === 0) errors.push('Invalid sender name.');
  return errors;
};

export const validateEmail = (email: string) => {
  const errors = [];
  if (email.length === 0) errors.push('Invalid email address.');
  return errors;
};

export const validateHost = (host: string) => {
  const errors = [];
  if (host.length === 0) errors.push('Host cannot be empty.');
  return errors;
};

export const validatePort = (port: string) => {
  const errors = [];
  const portNum = parseInt(port);
  if (isNaN(portNum) || portNum < 0 || portNum > 65535)
    errors.push('Invalid port.');
  return errors;
};
