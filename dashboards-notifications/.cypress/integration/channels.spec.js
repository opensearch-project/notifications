/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

/// <reference types="cypress" />

import { delay } from '../utils/constants';

describe('Test create channels', () => {
  beforeEach(() => {
    cy.visit(
      `${Cypress.env(
        'opensearchDashboards'
      )}/app/notifications-dashboards#create-channel`
    );
    cy.wait(delay * 3);
  });

  it('creates a slack channel and send test message', () => {
    cy.get('[data-test-subj="create-channel-create-button"]').click();
    cy.contains('Some fields are invalid.').should('exist');

    cy.get('[placeholder="Enter channel name"]').type('Test slack channel');
    cy.get('[data-test-subj="create-channel-slack-webhook-input"]').type(
      'https://sample-slack-webhook'
    );
    cy.wait(delay);
    cy.get('[data-test-subj="create-channel-send-test-message-button"]').click({
      force: true,
    });
    cy.wait(delay);
    // This needs some time to appear as it will wait for backend call to timeout
    cy.contains('test message.').should('exist');

    cy.get('[data-test-subj="create-channel-create-button"]').click({
      force: true,
    });
    cy.contains('successfully created.').should('exist');
  });

  it('creates a chime channel', () => {
    cy.get('[placeholder="Enter channel name"]').type('Test chime channel');

    cy.get('.euiSuperSelectControl').contains('Slack').click({ force: true });
    cy.wait(delay);
    cy.get('.euiContextMenuItem__text')
      .contains('Chime')
      .click({ force: true });
    cy.wait(delay);

    cy.get('[data-test-subj="create-channel-chime-webhook-input"]').type(
      'https://sample-chime-webhook'
    );
    cy.wait(delay);

    cy.get('[data-test-subj="create-channel-create-button"]').click();
    cy.contains('successfully created.').should('exist');
  });

  it('creates a email channel', () => {
    cy.get('[placeholder="Enter channel name"]').type('Test email channel');

    cy.get('.euiSuperSelectControl').contains('Slack').click({ force: true });
    cy.wait(delay);
    cy.get('.euiContextMenuItem__text')
      .contains('Email')
      .click({ force: true });
    cy.wait(delay);

    // custom data-test-subj does not work on combo box
    cy.get('[data-test-subj="comboBoxInput"]').eq(0).click({ force: true });
    cy.contains('test-tls-sender').click();

    cy.get('.euiButton__text')
      .contains('Create recipient group')
      .click({ force: true });
    cy.get('[data-test-subj="create-recipient-group-form-name-input"]').type(
      'Test recipient group'
    );
    cy.get(
      '[data-test-subj="create-recipient-group-form-description-input"]'
    ).type('Recipient group created while creating email channel.');
    cy.get('[data-test-subj="comboBoxInput"]')
      .last()
      .type('custom.email@test.com{enter}');
    cy.wait(delay);
    cy.get(
      '[data-test-subj="create-recipient-group-modal-create-button"]'
    ).click();
    cy.contains('successfully created.').should('exist');

    cy.get('[data-test-subj="create-channel-create-button"]').click();
    cy.contains('successfully created.').should('exist');
  });

  it('creates a email channel with ses sender', () => {
    cy.get('[placeholder="Enter channel name"]').type('Test email channel with ses');

    cy.get('.euiSuperSelectControl').contains('Slack').click({ force: true });
    cy.wait(delay);
    cy.get('.euiContextMenuItem__text')
      .contains('Email')
      .click({ force: true });
    cy.wait(delay);
    
    cy.get('input.euiRadio__input#ses_account').click({ force: true });
    cy.wait(delay);

    cy.get('.euiButton__text')
      .contains('Create SES sender')
      .click({ force: true });
    cy.get('[data-test-subj="create-ses-sender-form-name-input"]').type(
      'test-ses-sender'
    );
    cy.get('[data-test-subj="create-ses-sender-form-email-input"]').type(
      'test@email.com'
    );
    cy.get('[data-test-subj="create-ses-sender-form-role-arn-input"]').type(
      'arn:aws:iam::012345678912:role/NotificationsSESRole'
    );
    cy.get('[data-test-subj="create-ses-sender-form-aws-region-input"]').type(
      'us-east-1'
    );
    cy.get(
      '[data-test-subj="create-ses-sender-modal-create-button"]'
    ).click();
    cy.contains('successfully created.').should('exist');

    // custom data-test-subj does not work on combo box
    cy.get('[data-test-subj="comboBoxInput"]').eq(1).click({ force: true });
    cy.contains('Test recipient group').click();
    cy.wait(delay);

    cy.get('[data-test-subj="create-channel-create-button"]').click();
    cy.contains('successfully created.').should('exist');
  });

  it('creates a webhook channel', () => {
    cy.get('[placeholder="Enter channel name"]').type('Test webhook channel');

    cy.get('.euiSuperSelectControl').contains('Slack').click({ force: true });
    cy.wait(delay);
    cy.get('.euiContextMenuItem__text')
      .contains('Custom webhook')
      .click({ force: true });
    cy.wait(delay);

    cy.get('[data-test-subj="custom-webhook-url-input"]').type(
      'https://custom-webhook-test-url.com:8888/test-path?params1=value1&params2=value2&params3=value3&params4=value4&params5=values5&params6=values6&params7=values7'
    );

    cy.get('[data-test-subj="create-channel-create-button"]').click();
    cy.contains('successfully created.').should('exist');
  });

  it('creates a sns channel', () => {
    cy.get('[placeholder="Enter channel name"]').type('test-sns-channel');

    cy.get('.euiSuperSelectControl').contains('Slack').click({ force: true });
    cy.wait(delay);
    cy.get('.euiContextMenuItem__text')
      .contains('Amazon SNS')
      .click({ force: true });
    cy.wait(delay);

    cy.get('[data-test-subj="sns-settings-topic-arn-input"]').type(
      'arn:aws:sns:us-west-2:123456789012:notifications-test'
    );
    cy.get('[data-test-subj="sns-settings-role-arn-input"]').type(
      'arn:aws:iam::012345678901:role/NotificationsSNSRole'
    );

    cy.get('[data-test-subj="create-channel-create-button"]').click();
    cy.contains('successfully created.').should('exist');
  });
});

describe('Test channels table', () => {
  beforeEach(() => {
    cy.visit(
      `${Cypress.env(
        'opensearchDashboards'
      )}/app/notifications-dashboards#channels`
    );
    cy.wait(delay * 3);
  });

  it('displays channels', async () => {
    cy.contains('Test slack channel').should('exist');
    cy.contains('Test email channel').should('exist');
    cy.contains('Test chime channel').should('exist');
    cy.contains('Test webhook channel').should('exist');
  });

  it('mutes channels', async () => {
    cy.get('.euiCheckbox__input[aria-label="Select this row"]').eq(0).click(); // chime channel
    cy.get('.euiButton__text').contains('Actions').click({ force: true });
    cy.wait(delay);
    cy.get('.euiContextMenuItem__text').contains('Mute').click({ force: true });
    cy.wait(delay);
    cy.get('[data-test-subj="mute-channel-modal-mute-button"]').click({
      force: true,
    });
    cy.wait(delay);
    cy.contains('successfully muted.').should('exist');
    cy.contains('Muted').should('exist');
  });

  it('filters channels', async () => {
    cy.get('input[placeholder="Search"]').type('chime{enter}');
    cy.wait(delay);
    cy.contains('Test chime channel').should('exist');
    cy.contains('Test slack channel').should('not.exist');
    cy.contains('Test email channel').should('not.exist');
    cy.contains('Test webhook channel').should('not.exist');

    cy.get('.euiButtonEmpty__text').contains('Source').click({ force: true });
    cy.get('.euiFilterSelectItem__content')
      .contains('ISM')
      .click({ force: true });
    cy.wait(delay);
    cy.contains('No channels to display').should('exist');
  });
});

describe('Test channel details', () => {
  beforeEach(() => {
    cy.visit(
      `${Cypress.env(
        'opensearchDashboards'
      )}/app/notifications-dashboards#channels`
    );
    cy.contains('Test webhook channel').click();
  });

  it('displays channel details', async () => {
    cy.contains('custom-webhook-test-url.com').should('exist');
    cy.contains('test-path').should('exist');
    cy.contains('8888').should('exist');
    cy.contains('2 more').click();
    cy.contains('Query parameters (7)').should('exist');
    cy.contains('params7').should('exist');
  });

  it('mutes and unmutes channels', async () => {
    cy.contains('Mute channel').click({ force: true });
    cy.get('[data-test-subj="mute-channel-modal-mute-button"]').click({
      force: true,
    });
    cy.contains('successfully muted.').should('exist');
    cy.contains('Muted').should('exist');

    cy.contains('Unmute channel').click({ force: true });
    cy.contains('successfully unmuted.').should('exist');
    cy.contains('Active').should('exist');
  });
  
  it('edits channels', () => {
    cy.contains('Actions').click({ force: true });
    cy.contains('Edit').click({ force: true });
    cy.contains('Edit channel').should('exist');
    cy.get('.euiText').contains('Custom webhook').should('exist');
    cy.get(
      '[data-test-subj="create-channel-description-input"]'
    ).type('{selectall}{backspace}Updated custom webhook description');
    cy.wait(delay);
    cy.contains('Save').click({ force: true });

    cy.contains('successfully updated.').should('exist');
    cy.contains('Updated custom webhook description').should('exist');
  })

  it('deletes channels', async () => {
    cy.contains('Actions').click({ force: true });
    cy.contains('Delete').click({ force: true });
    cy.get('input[placeholder="delete"]').type('delete');
    cy.get('[data-test-subj="delete-channel-modal-delete-button"]').click({force: true})
    cy.contains('successfully deleted.').should('exist');
    cy.contains('Test slack channel').should('exist');
  })
});
