package org.opensearch.commons.notifications.model.config

import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.common.io.stream.Writeable.Reader

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
}
