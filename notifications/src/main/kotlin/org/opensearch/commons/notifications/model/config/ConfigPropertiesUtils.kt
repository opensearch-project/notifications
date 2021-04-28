package org.opensearch.commons.notifications.model.config

import com.fasterxml.jackson.databind.ser.Serializers
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.common.io.stream.Writeable.Reader
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.commons.notifications.model.Chime
import org.opensearch.commons.notifications.model.Email
import org.opensearch.commons.notifications.model.EmailGroup
import org.opensearch.commons.notifications.model.Slack
import org.opensearch.commons.notifications.model.SmtpAccount
import org.opensearch.commons.notifications.model.Webhook

val CONFIG_PROPERTIES: List<ConfigDataProperties> = listOf(
    SlackChannelProperties,
    ChimeChannelProperties,
    WebhookChannelProperties,
    EmailChannelProperties,
    EmailGroupChannelProperties,
    SmtpAccountChannelProperties,
    NoOpProperties
)

val CONFIG_TYPE_VS_PROPERTIES: Map<ConfigType, ConfigDataProperties> = CONFIG_PROPERTIES
    .filter { c -> c.getConfigType() != ConfigType.None }
    .associate { c ->
      c.getConfigType() to c
    }

/**
 * Internal field for Tags to ChannelProperty mapping
 */
val TAG_VS_PROPERTY = CONFIG_PROPERTIES
    .filter { prop -> prop.getConfigType() != ConfigType.None }
    .associate { prop ->
      prop.getChannelTag() to prop
    }

object ConfigPropertiesUtils {

  /**
   * validates provided tag
   * @param tag field tag from request
   * @return True if tag is valid else false
   */
  fun isValidConfigTag(tag: String): Boolean {
    return TAG_VS_PROPERTY.containsKey(tag)
  }

  /**
   * Get config type for provided tag
   * @param tag field tag from request
   * @return ConfigType corresponding to tag. Null if invalid tag.
   */
  fun getConfigTypeForTag(tag: String): ConfigType? {
    return TAG_VS_PROPERTY[tag]?.getConfigType()
  }

  /**
   * Get tag type for provided config type
   * @param @ConfigType
   * @return tag corresponding to ConfigType. Null if ConfigType is None.
   */
  fun getTagForConfigType(configType: ConfigType): String? {
    return CONFIG_TYPE_VS_PROPERTIES[configType]?.getChannelTag()
  }

  /**
   * Get Reader for provided config type
   * @param @ConfigType
   * @return Reader
   */
  fun getReaderForConfigType(configType: ConfigType): Reader<out BaseConfigData>? {
    return CONFIG_TYPE_VS_PROPERTIES[configType]?.getConfigDataReader()
  }

  /**
   * Validate config data is of ConfigType
   */
  fun validateConfigData(configType: ConfigType, configData: BaseConfigData?): Boolean {
    if (configType == ConfigType.None) {
      return true
    }

    return when (configType) {
      ConfigType.Slack -> configData is Slack
      ConfigType.Webhook -> configData is Webhook
      ConfigType.Email -> configData is Email
      ConfigType.EmailGroup -> configData is EmailGroup
      ConfigType.SmtpAccount -> configData is SmtpAccount
      ConfigType.Chime -> configData is Chime
      ConfigType.None -> true
    }
  }

  /**
   * Creates config data from parser for given configType
   * @param ConfigType
   * @param parser for configType
   * @return BaseConfigData
   *
   */
  fun createConfigData(configType: ConfigType, parser: XContentParser): BaseConfigData? {
    return CONFIG_TYPE_VS_PROPERTIES[configType]?.createConfigData(parser)
  }
}
