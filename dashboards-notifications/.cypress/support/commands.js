/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import testTlsSmtpSender from '../fixtures/test_tls_smtp_sender';
import testSmtpEmailChannel from '../fixtures/test_smtp_email_channel';

const { API, ADMIN_AUTH } = require('./constants');

// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add("login", (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })

Cypress.Commands.overwrite('visit', (originalFn, url, options) => {
  // Add the basic auth header when security enabled in the OpenSearch cluster
  // https://github.com/cypress-io/cypress/issues/1288
  if (Cypress.env('security_enabled')) {
    if (options) {
      options.auth = ADMIN_AUTH;
    } else {
      options = { auth: ADMIN_AUTH };
    }
    // Add query parameters - select the default OpenSearch Dashboards tenant
    options.qs = { security_tenant: 'private' };
    return originalFn(url, options);
  } else {
    return originalFn(url, options);
  }
});

  // Be able to add default options to cy.request(), https://github.com/cypress-io/cypress/issues/726
Cypress.Commands.overwrite('request', (originalFn, ...args) => {
  let defaults = {};
  // Add the basic authentication header when security enabled in the OpenSearch cluster
  if (Cypress.env('security_enabled')) {
    defaults.auth = ADMIN_AUTH;
  }

  let options = {};
  if (typeof args[0] === 'object' && args[0] !== null) {
    options = Object.assign({}, args[0]);
  } else if (args.length === 1) {
    [options.url] = args;
  } else if (args.length === 2) {
    [options.method, options.url] = args;
  } else if (args.length === 3) {
    [options.method, options.url, options.body] = args;
  }

  return originalFn(Object.assign({}, defaults, options));
});

Cypress.Commands.add('createConfig', (notificationConfigJSON) => {
  cy.request('POST', `${Cypress.env('opensearch')}${API.CONFIGS_BASE}`, notificationConfigJSON);
});

Cypress.Commands.add('createTestEmailChannel', () => {
  cy.createConfig(testTlsSmtpSender);
  cy.createConfig(testSmtpEmailChannel);
});

Cypress.Commands.add('deleteAllConfigs', () => {
  cy.request({
    method: 'GET',
    url: `${Cypress.env('opensearch')}${API.CONFIGS_BASE}`,
  }).then((response) => {
    if (response.status === 200) {
      for (let i = 0; i < response.body.total_hits; i++) {
        cy.request(
          'DELETE',
          `${Cypress.env('opensearch')}${API.CONFIGS_BASE}/${response.body.config_list[i].config_id}`
        );
      }
    } else {
      cy.log('Failed to get configs.', response);
    }
  });
});
