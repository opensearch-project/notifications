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

import {
  validateEmail,
  validateHost,
  validatePort,
  validateRecipientGroupEmails,
  validateRecipientGroupName,
  validateSenderName,
} from '../utils/validationHelper';

describe('test sender and recipient group input validations', () => {
  it('validates sender name', () => {
    const pass = validateSenderName('test-name');
    const fail = validateSenderName('');
    const failWithInvalidCharacters = validateSenderName('invalid name');
    const failWithUpperCaseCharacters = validateSenderName('INVALID_NAME');
    const failWithMultipleErrors = validateSenderName(new Array(52).join('@'))
    expect(pass).toEqual([]);
    expect(fail).toHaveLength(1);
    expect(failWithInvalidCharacters).toEqual(['Sender name contains invalid characters.'])
    expect(failWithUpperCaseCharacters).toEqual(['Sender name contains invalid characters.'])
    expect(failWithMultipleErrors).toHaveLength(2)
  });

  it('validates email', () => {
    const pass = validateEmail('test@email.com');
    const fail = validateEmail('');
    expect(pass).toEqual([]);
    expect(fail).toHaveLength(1);
  });

  it('validates host', () => {
    const pass = validateHost('test.com');
    const fail = validateHost('');
    expect(pass).toEqual([]);
    expect(fail).toHaveLength(1);
  });

  it('validates port', () => {
    const pass = validatePort('23');
    const emptyPort = validatePort('');
    const invalidPort = validatePort('abc');
    expect(pass).toEqual([]);
    expect(emptyPort).toHaveLength(1);
    expect(invalidPort).toHaveLength(1);
  });

  it('validates recipient group name', () => {
    const pass = validateRecipientGroupName('test.com');
    const fail = validateRecipientGroupName('');
    const failWithTooManyCharacters = validateRecipientGroupName(new Array(52).join('a'))
    expect(pass).toEqual([]);
    expect(fail).toHaveLength(1);
    expect(failWithTooManyCharacters).toHaveLength(1);
  });

  it('validates recipient group emails', () => {
    const pass = validateRecipientGroupEmails([{ label: 'test@email.com' }]);
    const fail = validateRecipientGroupEmails([]);
    expect(pass).toEqual([]);
    expect(fail).toHaveLength(1);
  });
});
