package com.ubudy.voiceaikit.ui

import android.Manifest
import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ubudy.voiceaikit.models.*
import com.ubudy.voiceaikit.viewmodels.VoiceAgentViewModel
import com.ubudy.voiceaikit.viewmodels.VoiceAgentViewModelFactory
import kotlinx.coroutines.delay

private val FieldBg = Color.White.copy(alpha = 0.06f)
private val FieldBorder = Color.White.copy(alpha = 0.1f)

/**
 * Drop-in voice agent composable with form, animated orb, and session management.
 *
 * Usage:
 * ```kotlin
 * VoiceAgentView()
 * VoiceAgentView(config = VoiceAgentConfig.Default, initialAgentType = "legalAdviser")
 * ```
 */
@Composable
fun VoiceAgentView(
    config: VoiceAgentConfig = VoiceAgentConfig.Default,
    initialAgentType: String = ""
) {
    val app = LocalContext.current.applicationContext as Application
    val viewModel: VoiceAgentViewModel = viewModel(
        factory = VoiceAgentViewModelFactory(app, config)
    )

    val state by viewModel.state.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()
    val elapsed by viewModel.elapsedSeconds.collectAsState()

    var hasPermission by remember { mutableStateOf(false) }
    var showForm by remember { mutableStateOf(true) }

    var nameField by remember { mutableStateOf("") }
    var subjectField by remember { mutableStateOf("") }
    var languageField by remember { mutableStateOf("English") }

    val resolvedType = initialAgentType.ifEmpty { config.resolvedDefaultType }
    var typeField by remember { mutableStateOf(resolvedType) }

    val activeConfig = config.agentTypes.firstOrNull { it.id == typeField } ?: config.agentTypes.first()
    val theme = activeConfig.theme
    val languages = listOf("English", "Hindi", "Hinglish")

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) viewModel.toggle()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
            .systemBarsPadding()
    ) {
        AnimatedContent(
            targetState = showForm && (state == ConnectionState.IDLE || state == ConnectionState.DISCONNECTED),
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "screen"
        ) { isFormVisible ->
            if (isFormVisible) {
                FormView(
                    nameField = nameField, onNameChange = { nameField = it },
                    subjectField = subjectField, onSubjectChange = { subjectField = it },
                    languageField = languageField, onLanguageChange = { languageField = it },
                    languages = languages,
                    typeField = typeField, onTypeChange = { typeField = it },
                    config = config, activeConfig = activeConfig, theme = theme,
                    onStart = {
                        viewModel.userInfo = UserInfo(
                            name = nameField, subject = subjectField,
                            language = languageField, type = typeField
                        )
                        showForm = false
                        if (!hasPermission) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        else viewModel.toggle()
                    }
                )
            } else {
                SessionView(
                    state = state, audioLevel = audioLevel, elapsed = elapsed,
                    nameField = nameField, activeConfig = activeConfig, theme = theme,
                    onTap = {
                        if (state == ConnectionState.IDLE || state == ConnectionState.DISCONNECTED) {
                            if (!hasPermission) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            else viewModel.toggle()
                        } else {
                            viewModel.toggle()
                            if (state == ConnectionState.LISTENING || state == ConnectionState.SPEAKING) {
                                showForm = true
                            }
                        }
                    }
                )
            }
        }
    }
}

// ============================================================
// FORM VIEW
// ============================================================

@Composable
private fun FormView(
    nameField: String, onNameChange: (String) -> Unit,
    subjectField: String, onSubjectChange: (String) -> Unit,
    languageField: String, onLanguageChange: (String) -> Unit,
    languages: List<String>,
    typeField: String, onTypeChange: (String) -> Unit,
    config: VoiceAgentConfig, activeConfig: AgentTypeConfig, theme: AgentTheme,
    onStart: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 40.dp)
        ) {
            // Title
            Text(
                text = activeConfig.title,
                style = TextStyle(
                    fontSize = 28.sp, fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.5).sp,
                    brush = Brush.linearGradient(theme.titleGradient)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tell us about yourself to get started",
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Light, color = theme.subtleTextColor)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Form fields
            FormField("\uD83D\uDC64", "Your Name", nameField, onNameChange, theme)
            Spacer(modifier = Modifier.height(14.dp))
            FormField("\uD83D\uDCAC", activeConfig.subjectPlaceholder, subjectField, onSubjectChange, theme)
            Spacer(modifier = Modifier.height(14.dp))

            // Language picker
            SegmentedRow("\uD83C\uDF10", languages, languageField, onLanguageChange, theme)
            Spacer(modifier = Modifier.height(14.dp))

            // Type picker (only if multiple types)
            if (config.agentTypes.size > 1) {
                SegmentedRow(
                    icon = "\u2728",
                    items = config.agentTypes.map { it.id },
                    labels = config.agentTypes.associate { it.id to it.displayName },
                    selected = typeField,
                    onSelect = onTypeChange,
                    theme = theme
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Start button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(theme.titleGradient))
                    .clickable { onStart() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("\uD83C\uDFA4", fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                    Text(
                        "Start Conversation",
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = activeConfig.footerText,
                style = TextStyle(
                    fontSize = 12.sp, fontWeight = FontWeight.Light,
                    letterSpacing = 0.3.sp, color = theme.dimmedTextColor, textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Composable
private fun FormField(
    icon: String, placeholder: String, value: String,
    onValueChange: (String) -> Unit, theme: AgentTheme
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FieldBg)
            .border(1.dp, FieldBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 16.sp, modifier = Modifier.padding(end = 10.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(placeholder, style = TextStyle(fontSize = 15.sp, color = theme.subtleTextColor))
            }
            BasicTextField(
                value = value, onValueChange = onValueChange,
                textStyle = TextStyle(fontSize = 15.sp, color = Color.White),
                cursorBrush = SolidColor(theme.accentColor),
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SegmentedRow(
    icon: String, items: List<String>, selected: String,
    onSelect: (String) -> Unit, theme: AgentTheme,
    labels: Map<String, String>? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FieldBg)
            .border(1.dp, FieldBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 16.sp, modifier = Modifier.padding(end = 10.dp))
        items.forEach { item ->
            val isSelected = selected == item
            val label = labels?.get(item) ?: item
            val selectedBg = theme.accentColor.copy(alpha = 0.15f)
            val selectedBorder = theme.accentColor.copy(alpha = 0.3f)
            Box(
                modifier = Modifier.weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) selectedBg else Color.Transparent)
                    .border(if (isSelected) 1.dp else 0.dp, if (isSelected) selectedBorder else Color.Transparent, RoundedCornerShape(8.dp))
                    .clickable { onSelect(item) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label, style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Light,
                        color = if (isSelected) theme.accentColor else theme.subtleTextColor
                    )
                )
            }
        }
    }
}

// ============================================================
// SESSION VIEW
// ============================================================

@Composable
private fun SessionView(
    state: ConnectionState, audioLevel: Float, elapsed: Int,
    nameField: String, activeConfig: AgentTypeConfig, theme: AgentTheme,
    onTap: () -> Unit
) {
    var showTitle by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }
    var showTagline by remember { mutableStateOf(false) }
    var showOrb by remember { mutableStateOf(false) }
    var showStatus by remember { mutableStateOf(false) }
    var showFooter by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showTitle = true
        delay(200); showSubtitle = true
        delay(100); showOrb = true
        delay(100); showTagline = true
        delay(300); showStatus = true
        delay(100); showFooter = true
    }

    val titleAlpha by animateFloatAsState(if (showTitle) 1f else 0f, tween(800), label = "ta")
    val titleOffset by animateFloatAsState(if (showTitle) 0f else 12f, tween(800), label = "to")
    val subtitleAlpha by animateFloatAsState(if (showSubtitle) 1f else 0f, tween(800), label = "sa")
    val subtitleOffset by animateFloatAsState(if (showSubtitle) 0f else 12f, tween(800), label = "so")
    val taglineAlpha by animateFloatAsState(if (showTagline) 1f else 0f, tween(800), label = "tga")
    val taglineOffset by animateFloatAsState(if (showTagline) 0f else 12f, tween(800), label = "tgo")
    val orbScale by animateFloatAsState(if (showOrb) 1f else 0.92f, tween(1000), label = "os")
    val orbAlpha by animateFloatAsState(if (showOrb) 1f else 0f, tween(1000), label = "oa")
    val statusAlpha by animateFloatAsState(if (showStatus) 1f else 0f, tween(800), label = "sta")
    val statusOffset by animateFloatAsState(if (showStatus) 0f else 12f, tween(800), label = "sto")
    val footerAlpha by animateFloatAsState(if (showFooter) 1f else 0f, tween(800), label = "fa")
    val footerOffset by animateFloatAsState(if (showFooter) 0f else 12f, tween(800), label = "fo")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        // Top: title + subtitle + tagline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 60.dp)
        ) {
            Text(
                text = activeConfig.title,
                style = TextStyle(
                    fontSize = 28.sp, fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.5).sp,
                    brush = Brush.linearGradient(theme.titleGradient)
                ),
                modifier = Modifier.graphicsLayer(alpha = titleAlpha, translationY = titleOffset)
            )

            Text(
                text = activeConfig.subtitle,
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Light, letterSpacing = 0.3.sp, color = theme.subtleTextColor),
                modifier = Modifier.padding(top = 6.dp).graphicsLayer(alpha = subtitleAlpha, translationY = subtitleOffset)
            )

            TypewriterText(
                phrases = activeConfig.taglines,
                cursorColor = theme.accentColor.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 8.dp).graphicsLayer(alpha = taglineAlpha, translationY = taglineOffset)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Center: orb + status
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .graphicsLayer(scaleX = orbScale, scaleY = orbScale, alpha = orbAlpha)
                    .clickable(remember { MutableInteractionSource() }, null) { onTap() }
            ) {
                OrbView(state = state, audioLevel = audioLevel, theme = theme)
            }

            Spacer(modifier = Modifier.height(28.dp))

            val statusText = when (state) {
                ConnectionState.IDLE -> "Tap the orb to start talking"
                ConnectionState.CONNECTING -> "Connecting..."
                ConnectionState.LISTENING -> "Listening..."
                ConnectionState.SPEAKING -> "Speaking..."
                ConnectionState.DISCONNECTED -> "Tap to reconnect"
            }
            val statusColor = when (state) {
                ConnectionState.SPEAKING -> theme.speakingAccentColor
                ConnectionState.LISTENING -> theme.accentColor
                ConnectionState.CONNECTING -> Color.White.copy(alpha = 0.4f)
                else -> theme.subtleTextColor
            }
            Text(
                text = statusText,
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp, color = statusColor),
                modifier = Modifier.graphicsLayer(alpha = statusAlpha, translationY = statusOffset)
            )

            if (state == ConnectionState.LISTENING || state == ConnectionState.SPEAKING) {
                Text(
                    text = "%02d:%02d".format(elapsed / 60, elapsed % 60),
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Light, color = theme.dimmedTextColor),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Footer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 30.dp).graphicsLayer(alpha = footerAlpha, translationY = footerOffset)
        ) {
            if (nameField.isNotEmpty()) {
                Text(
                    "Talking as $nameField",
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Light, color = theme.subtleTextColor),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Text(
                text = activeConfig.footerText,
                style = TextStyle(
                    fontSize = 12.sp, fontWeight = FontWeight.Light,
                    letterSpacing = 0.3.sp, color = theme.dimmedTextColor, textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}
