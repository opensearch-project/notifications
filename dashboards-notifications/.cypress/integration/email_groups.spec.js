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

/// <reference types="cypress" />

import { delay } from '../utils/constants';

describe('Test create email sender and recipients', () => {
  beforeEach(() => {
    cy.visit(
      `${Cypress.env(
        'opensearchDashboards'
      )}/app/notifications-dashboards#email-groups`
    );
    cy.wait(delay * 3);
  });

  it('creates ssl sender', () => {
    cy.get('.euiButton__text').contains('Create sender').click({ force: true });
    cy.get('[data-test-subj="create-sender-form-name-input"]').type(
      'Test ssl sender'
    );
    cy.get('.euiButton__text').contains('Create').click({ force: true });
    cy.contains('Some fields are invalid.').should('exist');

    cy.get('[data-test-subj="create-sender-form-email-input"]').type(
      'test@email.com'
    );
    cy.get('[data-test-subj="create-sender-form-host-input"]').type(
      'test-host.com'
    );
    cy.get('[data-test-subj="create-sender-form-port-input"]').type('123');
    cy.get('.euiButton__text').contains('Create').click({ force: true });
    cy.contains('successfully created.').should('exist');
    cy.contains('Test ssl sender').should('exist')
  });

  it('creates tls sender', () => {
    cy.get('.euiButton__text').contains('Create sender').click({ force: true });
    cy.get('[data-test-subj="create-sender-form-name-input"]').type(
      'Test tls sender'
    );
    cy.get('[data-test-subj="create-sender-form-email-input"]').type(
      'test@email.com'
    );
    cy.get('[data-test-subj="create-sender-form-host-input"]').type(
      'test-host.com'
    );
    cy.get('[data-test-subj="create-sender-form-port-input"]').type('123');
    cy.get('[data-test-subj="create-sender-form-encryption-input"]').click({force: true})
    cy.wait(delay);
    cy.get('.euiContextMenuItem__text').contains('TLS').click({ force: true });
    cy.wait(delay);

    cy.get('.euiButton__text').contains('Create').click({ force: true });
    cy.contains('successfully created.').should('exist');
    cy.contains('Test ssl sender').should('exist')
  });
  
  it('creates recipient group', () => {
    cy.get('.euiButton__text').contains('Create recipient group').click({ force: true });
    cy.get('[data-test-subj="create-recipient-group-form-name-input"]').type('Test recipient group')
    cy.get('.euiButton__text').contains('Create').click({ force: true });
    cy.contains('Some fields are invalid.').should('exist');

    cy.get('[data-test-subj="create-recipient-group-form-description-input"]').type('Test group description')
    cy.get('[data-test-subj="comboBoxInput"]').type("custom.email.1@test.com{enter}")
    cy.get('[data-test-subj="comboBoxInput"]').type("custom.email.2@test.com{enter}")
    cy.get('[data-test-subj="comboBoxInput"]').type("custom.email.3@test.com{enter}")
    cy.get('[data-test-subj="comboBoxInput"]').type("custom.email.4@test.com{enter}")
    cy.get('[data-test-subj="comboBoxInput"]').type("custom.email.5@test.com{enter}")
    cy.get('[data-test-subj="comboBoxInput"]').type("custom.email.6@test.com{enter}")
    cy.wait(delay);

    cy.get('.euiButton__text').contains('Create').click({ force: true });
    cy.contains('successfully created.').should('exist');
    cy.contains('Test recipient group').should('exist')
  });
});


describe('Test edit sender and recipients', () => {
  beforeEach(() => {
    cy.visit(
      `${Cypress.env(
        'opensearchDashboards'
      )}/app/notifications-dashboards#email-groups`
    );
    cy.wait(delay * 3);
  });
  
  it('edits sender email address', () => {
    cy.get('.euiCheckbox__input[aria-label="Select this row"]').eq(0).click(); // ssl sender
    cy.get('[data-test-subj="senders-table-edit-button"]').click();
    cy.get('[data-test-subj="create-sender-form-email-input"]').type(
      '{selectall}{backspace}edited.test@email.com'
    );
    cy.wait(delay);

    cy.get('.euiButton__text').contains('Save').click({ force: true });
    cy.contains('successfully updated.').should('exist');
  });
  
  it('edits recipient group description', () => {
    cy.get('.euiCheckbox__input[aria-label="Select this row"]').last().click(); // recipient group
    cy.get('[data-test-subj="recipient-groups-table-edit-button"]').click();
    cy.get('[data-test-subj="create-recipient-group-form-description-input"]').type(
      '{selectall}{backspace}Updated group description'
    );
    cy.wait(delay);

    cy.get('.euiButton__text').contains('Save').click({ force: true });
    cy.contains('successfully updated.').should('exist');
  });
})


describe('Test display popup and delete', () => {
  beforeEach(() => {
    cy.visit(
      `${Cypress.env(
        'opensearchDashboards'
      )}/app/notifications-dashboards#email-groups`
    );
    cy.wait(delay * 3);
  });

  it('opens email addresses popup', () => {
    cy.get('.euiLink').contains('1 more').click();
    cy.contains('custom.email.6@test.com').should('exist')
  });
  
  it('deletes sender and recipient group', () => {
    cy.get('.euiCheckbox__input[aria-label="Select this row"]').eq(0).click(); // ssl sender
    cy.get('[data-test-subj="senders-table-delete-button"]').click()
    cy.get('input[placeholder="delete"]').type('delete');
    cy.wait(delay);
    cy.get('[data-test-subj="delete-sender-modal-delete-button"]').click()
    cy.contains('successfully deleted.').should('exist');

    cy.get('[data-test-subj="checkboxSelectAll"]').last().click()
    cy.get('[data-test-subj="recipient-groups-table-delete-button"]').click()
    cy.get('input[placeholder="delete"]').type('delete');
    cy.wait(delay);
    cy.get('[data-test-subj="delete-recipient-group-modal-delete-button"]').click()
    cy.contains('successfully deleted.').should('exist');

    cy.contains('No recipient groups to display').should('exist');
  });
});
