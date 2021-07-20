package tigase.halcyon.core

actual class ConfigurationBuilder actual constructor(halcyon: AbstractHalcyon) :
    AbstractConfigurationBuilder(halcyon) {

    fun setServerHost(host: String): ConfigurationBuilder {
        return this
    }
}