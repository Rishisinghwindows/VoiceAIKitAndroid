package com.ubudy.voiceaikit.models

data class AgentTypeConfig(
    val id: String,
    val displayName: String,
    val title: String,
    val subtitle: String,
    val footerText: String,
    val subjectPlaceholder: String,
    val taglines: List<String>,
    val theme: AgentTheme
) {
    companion object {
        val MentalHealth = AgentTypeConfig(
            id = "MentalHealth",
            displayName = "Mental Health",
            title = "Voice AI Assistant",
            subtitle = "Talk in Hindi or English \u2014 powered by real-time AI",
            footerText = "Ask anything \u2014 available 24/7 in English and Hindi",
            subjectPlaceholder = "Mental health topic to discuss",
            taglines = listOf(
                "I'm Maya, your mental health companion",
                "Share how you feel, I'm here to listen",
                "Breathing exercises & coping tips available",
            ),
            theme = AgentTheme.Default
        )

        val LegalAdviser = AgentTypeConfig(
            id = "legalAdviser",
            displayName = "Legal",
            title = "AI Legal Guru",
            subtitle = "Your AI guide to Indian law",
            footerText = "Ask about BNS, IPC, Constitution & more \u2014 24/7",
            subjectPlaceholder = "Legal topic to discuss",
            taglines = listOf(
                "Ask about IPC, BNS & Indian Constitution",
                "Get instant answers on legal sections",
                "Understand your rights under Indian law",
            ),
            theme = AgentTheme.Legal
        )

        val FinanceGuru = AgentTypeConfig(
            id = "FinanceGuru",
            displayName = "Finance",
            title = "Finance Guru",
            subtitle = "Your AI-powered financial advisor",
            footerText = "Investment, tax & financial planning \u2014 24/7",
            subjectPlaceholder = "Financial topic to discuss",
            taglines = listOf(
                "Ask about investments & portfolio planning",
                "Get answers on tax savings & mutual funds",
                "Understand your financial options",
            ),
            theme = AgentTheme.Default
        )
    }
}
