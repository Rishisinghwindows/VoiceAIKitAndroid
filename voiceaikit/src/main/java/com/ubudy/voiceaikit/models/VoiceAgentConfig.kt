package com.ubudy.voiceaikit.models

data class VoiceAgentConfig(
    val serverURL: String,
    val livekitURL: String,
    val agentTypes: List<AgentTypeConfig>,
    val defaultAgentType: String = "",
    val maxRetries: Int = 3
) {
    val resolvedDefaultType: String
        get() = defaultAgentType.ifEmpty { agentTypes.firstOrNull()?.id ?: "" }

    companion object {
        val Default = VoiceAgentConfig(
            serverURL = "https://advancedvoiceagent.xappy.io",
            livekitURL = "wss://apiadvancedvoiceagent.xappy.io",
            agentTypes = listOf(
                AgentTypeConfig.MentalHealth,
                AgentTypeConfig.LegalAdviser,
                AgentTypeConfig.FinanceGuru,
            ),
            defaultAgentType = "MentalHealth"
        )
    }
}
