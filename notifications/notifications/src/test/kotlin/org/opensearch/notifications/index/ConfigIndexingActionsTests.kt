/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.index

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.opensearch.commons.authuser.User
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.commons.notifications.model.MicrosoftTeams
import org.opensearch.commons.notifications.model.Slack
import java.lang.reflect.Method
import kotlin.test.assertFails

class ConfigIndexingActionsTests {

    @Test
    fun `test validate microsoft teams`() {
        val user = User()
        var microsoftTeams =
            MicrosoftTeams("https://abcdefg.webhook.office.com/webhookb2/12345567abcdefg")
        validateMicrosoftTeamsConfig.invoke(ConfigIndexingActions, microsoftTeams, user)
        microsoftTeams =
            MicrosoftTeams("https://abcde.efg.webhook.office.com/webhookb2/12345567abcdefg")
        validateMicrosoftTeamsConfig.invoke(ConfigIndexingActions, microsoftTeams, user)

        // New allowed domains
        microsoftTeams = MicrosoftTeams("https://test.powerplatform.com/webhook")
        validateMicrosoftTeamsConfig.invoke(ConfigIndexingActions, microsoftTeams, user)
        microsoftTeams = MicrosoftTeams("https://test.logic.azure.com/webhook")
        validateMicrosoftTeamsConfig.invoke(ConfigIndexingActions, microsoftTeams, user)

        // Simple contains check now allows http if domain is present
        microsoftTeams =
            MicrosoftTeams("http://abcdefg.webhook.office.com/webhookb2/12345567abcdefg")
        validateMicrosoftTeamsConfig.invoke(ConfigIndexingActions, microsoftTeams, user)

        // Invalid domains
        microsoftTeams = MicrosoftTeams("https://abcdefg.webhook.abc.com/webhookb2/12345567abcdefg")
        assertFails {
            validateMicrosoftTeamsConfig.invoke(ConfigIndexingActions, microsoftTeams, user)
        }
        microsoftTeams = MicrosoftTeams("https://abcdefg.abc.com")
        assertFails {
            validateMicrosoftTeamsConfig.invoke(ConfigIndexingActions, microsoftTeams, user)
        }
    }

    @Test
    fun `test validate slack`() {
        val user = User()
        var slack = Slack("https://hooks.slack.com/services/123456789/123456789/123456789")
        validateSlackConfig.invoke(ConfigIndexingActions, slack, user)
        slack = Slack("https://hooks.gov-slack.com/services/123456789/123456789/123456789")
        validateSlackConfig.invoke(ConfigIndexingActions, slack, user)
        slack =
            Slack(
                "https://hooks.slack.com/services/samplesamplesamplesamplesamplesamplesamplesamplesample"
            )
        validateSlackConfig.invoke(ConfigIndexingActions, slack, user)
        slack =
            Slack(
                "https://hooks.gov-slack.com/services/samplesamplesamplesamplesamplesamplesamplesamplesample"
            )
        validateSlackConfig.invoke(ConfigIndexingActions, slack, user)
        slack = Slack("http://hooks.slack.com/services/123456789/123456789/123456789/123456789")
        assertFails { validateSlackConfig.invoke(ConfigIndexingActions, slack, user) }
        slack = Slack("http://hooks.gov-slack.com/services/123456789/123456789/123456789/123456789")
        assertFails { validateSlackConfig.invoke(ConfigIndexingActions, slack, user) }
        slack = Slack("https://slack.com/services/123456789/123456789/123456789/123456789")
        assertFails { validateSlackConfig.invoke(ConfigIndexingActions, slack, user) }
        slack = Slack("https://gov-slack.com/services/123456789/123456789/123456789/123456789")
        assertFails { validateSlackConfig.invoke(ConfigIndexingActions, slack, user) }
        slack = Slack("https://hooks.slack.com/123456789/123456789/123456789/123456789/123456789")
        assertFails { validateSlackConfig.invoke(ConfigIndexingActions, slack, user) }
        slack =
            Slack(
                "https://hooks.gov-slack.com/123456789/123456789/123456789/123456789/123456789"
            )
        assertFails { validateSlackConfig.invoke(ConfigIndexingActions, slack, user) }
        slack =
            Slack(
                "https://hook.slack.com/services/123456789/123456789/123456789/123456789/123456789"
            )
        assertFails { validateSlackConfig.invoke(ConfigIndexingActions, slack, user) }
        slack =
            Slack(
                "https://hook.gov-slack.com/services/123456789/123456789/123456789/123456789/123456789"
            )
        assertFails { validateSlackConfig.invoke(ConfigIndexingActions, slack, user) }
        slack = Slack("https://hooks.slack.com/")
        assertFails { validateSlackConfig.invoke(ConfigIndexingActions, slack, user) }
        slack = Slack("https://hooks.gov-slack.com/")
        assertFails { validateSlackConfig.invoke(ConfigIndexingActions, slack, user) }
    }

    @Test
    fun `test validate chime`() {
        val user = User()
        var chime = Chime("https://hooks.chime.aws/incomingwebhooks/sample_chime_url?token=123456")
        validateChimeConfig.invoke(ConfigIndexingActions, chime, user)
        chime =
            Chime(
                "https://hooks.chime.aws/incomingwebhooks/sample_chime_url?token=123456&test=123"
            )
        validateChimeConfig.invoke(ConfigIndexingActions, chime, user)
        chime = Chime("https://hooks.chime.aws/incomingwebhooks/sample_chime_url")
        assertFails { validateChimeConfig.invoke(ConfigIndexingActions, chime, user) }
        chime = Chime("https://hooks.chime.aws/incomingwebhooks?token=123456")
        assertFails { validateChimeConfig.invoke(ConfigIndexingActions, chime, user) }
        chime = Chime("http://hooks.chime.aws/incomingwebhooks/sample_chime_url?token=123456")
        assertFails { validateChimeConfig.invoke(ConfigIndexingActions, chime, user) }
        chime = Chime("https://sample.chime.aws/incomingwebhooks/sample_chime_url?token=123456")
        assertFails { validateChimeConfig.invoke(ConfigIndexingActions, chime, user) }
        chime = Chime("https://hooks.chime.aws/sample_chime_url?token=123456")
        assertFails { validateChimeConfig.invoke(ConfigIndexingActions, chime, user) }
    }

    companion object {
        private lateinit var validateMicrosoftTeamsConfig: Method
        private lateinit var validateSlackConfig: Method
        private lateinit var validateChimeConfig: Method

        @BeforeAll
        @JvmStatic
        fun initialize() {
            /* use reflection to get private method */
            validateMicrosoftTeamsConfig =
                ConfigIndexingActions::class.java.getDeclaredMethod(
                    "validateMicrosoftTeamsConfig",
                    MicrosoftTeams::class.java,
                    User::class.java
                )
            validateSlackConfig =
                ConfigIndexingActions::class.java.getDeclaredMethod(
                    "validateSlackConfig",
                    Slack::class.java,
                    User::class.java
                )
            validateChimeConfig =
                ConfigIndexingActions::class.java.getDeclaredMethod(
                    "validateChimeConfig",
                    Chime::class.java,
                    User::class.java
                )

            validateMicrosoftTeamsConfig.isAccessible = true
            validateSlackConfig.isAccessible = true
            validateChimeConfig.isAccessible = true
        }
    }
}
