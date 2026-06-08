package com.example
import coil.compose.AsyncImage
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.activity.compose.BackHandler

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.ui.theme.*

// -- GENERIC UTILITIES --

@Composable
fun AvatarImage(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 40,
    isSpeaking: Boolean = false,
    wavePower: Float = 0.0f
) {
    val speakBorder = if (isSpeaking && wavePower > 0.1f) {
        val animWidth by animateDpAsState(
            targetValue = (2 + (wavePower * 5)).dp,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "ring"
        )
        Modifier.border(animWidth, MaterialTheme.colorScheme.primary, CircleShape)
    } else {
        Modifier
    }

    ProceduralLinkedInAvatar(
        name = name,
        sizeDp = size,
        modifier = modifier.then(speakBorder)
    )
}

@Composable
fun ExpertCoverPhoto(
    expert: Expert,
    modifier: Modifier = Modifier
) {
    val seed = expert.name.hashCode()
    val colors = listOf(
        listOf(Color(0xFF8E44AD), Color(0xFF3498DB)),
        listOf(Color(0xFFE67E22), Color(0xFFE74C3C)),
        listOf(Color(0xFF1ABC9C), Color(0xFF2ECC71)),
        listOf(Color(0xFF2C3E50), Color(0xFF34495E)),
        listOf(Color(0xFFFFB74D), Color(0xFFEF6C00)),
        listOf(Color(0xFF4DB6AC), Color(0xFF00796B))
    )
    val bgColors = colors[seed.let { if (it < 0) -it else it } % colors.size]

    Box(
        modifier = modifier
            .background(Brush.linearGradient(bgColors))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = h * 0.8f,
                center = androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.5f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = h * 1.3f,
                center = androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.5f)
            )
            val cols = 8
            val rows = 4
            val cellW = w / cols
            val cellH = h / rows
            for (r in 0..rows) {
                for (c in 0..cols) {
                    if ((c + r) % 3 == 0) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.1f),
                            radius = 3f,
                            center = androidx.compose.ui.geometry.Offset(c * cellW, r * cellH)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                .padding(14.dp)
        ) {
            Text(
                text = expert.areaEmoji,
                fontSize = 42.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun ProceduralLinkedInAvatar(
    name: String,
    sizeDp: Int = 40,
    modifier: Modifier = Modifier
) {
    val seed = name.hashCode().let { if (it < 0) -it else it }
    // Determine professional/personable color palette
    val palettes = listOf(
        // Light bg, primary suit, neck/tie color accent
        Triple(Color(0xFFE3F2FD), Color(0xFF1E88E5), Color(0xFF3F51B5)),
        Triple(Color(0xFFEDE7F6), Color(0xFF5E35B1), Color(0xFF9575CD)),
        Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Color(0xFF4CAF50)),
        Triple(Color(0xFFFFF3E0), Color(0xFFE65100), Color(0xFFFFB74D)),
        Triple(Color(0xFFFCE4EC), Color(0xFFD81B60), Color(0xFFEC407A)),
        Triple(Color(0xFFE0F2F1), Color(0xFF00796B), Color(0xFF26A69A))
    )
    val palette = palettes[seed % palettes.size]
    
    val isFemale = name.contains("Gauri") || name.contains("Savita") || name.contains("Sudha") ||
                   name.contains("Devyani") || name.contains("Nirmala") || name.contains("Begum") ||
                   name.contains("Kavitha") || name.contains("Nandini") || name.contains("Gail") ||
                   name.contains("Shashi") || name.contains("Kalyani") || name.contains("Urmila") ||
                   name.contains("Leela") || name.contains("Priya") || name.contains("Nisha") || name.contains("Ma ")

    val sizeModifier = if (sizeDp > 0) Modifier.size(sizeDp.dp) else Modifier

    Box(
        modifier = modifier
            .then(sizeModifier)
            .clip(CircleShape)
            .background(palette.first)
            .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            // 1. Draw Suit shoulders/attire (curved path at the bottom)
            val suitColor = palette.second
            val skinTone = Color(0xFFFCD0B4) // Warm senior skin tone
            val hairColor = if (seed % 3 == 0) Color(0xFF888888) else if (seed % 3 == 1) Color(0xFF4E342E) else Color(0xFFE0E0E0)
            
            val suitPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, h)
                quadraticTo(w * 0.1f, h * 0.65f, w * 0.3f, h * 0.65f)
                lineTo(w * 0.7f, h * 0.65f)
                quadraticTo(w * 0.9f, h * 0.65f, w, h)
                close()
            }
            drawPath(suitPath, color = suitColor)
            
            // 2. Shirt collar V-neck
            val shirtPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.4f, h * 0.65f)
                lineTo(w * 0.5f, h * 0.78f)
                lineTo(w * 0.6f, h * 0.65f)
                close()
            }
            drawPath(shirtPath, color = Color.White)
            
            // Tie accent (for male professional)
            if (!isFemale && seed % 2 == 0) {
                val tiePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.46f, h * 0.66f)
                    lineTo(w * 0.54f, h * 0.66f)
                    lineTo(w * 0.52f, h * 0.88f)
                    lineTo(w * 0.5f, h * 0.94f)
                    lineTo(w * 0.48f, h * 0.88f)
                    close()
                }
                drawPath(tiePath, color = palette.third)
            }
            
            // 3. Neck
            drawRect(
                color = skinTone.copy(alpha = 0.92f),
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.42f, h * 0.45f),
                size = androidx.compose.ui.geometry.Size(w * 0.16f, h * 0.2f)
            )
            
            // 4. Face/Head
            drawCircle(
                color = skinTone,
                radius = w * 0.22f,
                center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.38f)
            )
            
            if (isFemale) {
                // Female professional hair
                val hairPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.25f, h * 0.38f)
                    quadraticTo(w * 0.22f, h * 0.15f, w * 0.5f, h * 0.12f)
                    quadraticTo(w * 0.78f, h * 0.15f, w * 0.75f, h * 0.38f)
                    quadraticTo(w * 0.85f, h * 0.52f, w * 0.8f, h * 0.62f)
                    quadraticTo(w * 0.75f, h * 0.3f, w * 0.5f, h * 0.22f)
                    quadraticTo(w * 0.25f, h * 0.3f, w * 0.2f, h * 0.62f)
                    quadraticTo(w * 0.15f, h * 0.52f, w * 0.25f, h * 0.38f)
                }
                drawPath(hairPath, color = hairColor)
                
                // Traditional bindi for Indian aesthetic warmth
                drawCircle(
                    color = Color(0xFFC62828),
                    radius = w * 0.024f,
                    center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.33f)
                )
            } else {
                // Male professional short hair crop
                val hairPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.25f, h * 0.32f)
                    quadraticTo(w * 0.25f, h * 0.14f, w * 0.5f, h * 0.12f)
                    quadraticTo(w * 0.75f, h * 0.14f, w * 0.75f, h * 0.32f)
                    quadraticTo(w * 0.70f, h * 0.22f, w * 0.5f, h * 0.21f)
                    quadraticTo(w * 0.30f, h * 0.22f, w * 0.25f, h * 0.32f)
                }
                drawPath(hairPath, color = hairColor)
            }
            
            // 5. Stylized spectacles/glasses for wisdom look
            if (seed % 2 == 0) {
                val glassTint = Color(0xFF263238)
                // Left frame circle
                drawCircle(
                    color = glassTint,
                    radius = w * 0.065f,
                    center = androidx.compose.ui.geometry.Offset(w * 0.42f, h * 0.38f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8f)
                )
                // Right frame circle
                drawCircle(
                    color = glassTint,
                    radius = w * 0.065f,
                    center = androidx.compose.ui.geometry.Offset(w * 0.58f, h * 0.38f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8f)
                )
                // Middle bridge
                drawLine(
                    color = glassTint,
                    start = androidx.compose.ui.geometry.Offset(w * 0.485f, h * 0.38f),
                    end = androidx.compose.ui.geometry.Offset(w * 0.515f, h * 0.38f),
                    strokeWidth = 2.2f
                )
            }
            
            // 6. Natural warm friendly smiling mouth
            val mouthPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.44f, h * 0.46f)
                quadraticTo(w * 0.5f, h * 0.51f, w * 0.56f, h * 0.46f)
            }
            drawPath(
                path = mouthPath,
                color = Color(0xFFD32F2F).copy(alpha = 0.85f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.4f)
            )
        }
    }
}

@Composable
fun CustomWaveform(
    modifier: Modifier = Modifier,
    wavePower: Float = 0.0f,
    active: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val maxAmp = if (active) (height * 0.4f) * (wavePower.coerceIn(0.1f, 1.0f)) else 4f

        val path = androidx.compose.ui.graphics.Path()
        val totalSteps = width.toInt()
        if (totalSteps > 0) {
            for (x in 0..totalSteps step 5) {
                val angle = (x / width) * 4f * Math.PI.toFloat() + phase
                val y = centerY + Math.sin(angle.toDouble()).toFloat() * maxAmp
                if (x == 0) {
                    path.moveTo(x.toFloat(), y)
                } else {
                    path.lineTo(x.toFloat(), y)
                }
            }
            drawPath(path, color, style = androidx.compose.ui.graphics.drawscope.Stroke(3f))
        }
    }
}

@Composable
fun SimulatedVibrantVoicePlayer(
    duration: Int,
    transcription: String?,
    onPlayToggle: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (currentSeconds < duration) {
                delay(1000)
                currentSeconds++
            }
            isPlaying = false
            currentSeconds = 0
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        isPlaying = !isPlaying
                        if (!isPlaying) currentSeconds = 0
                        onPlayToggle()
                    },
                    modifier = Modifier.testTag("play_voice_message_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = "Play voice note",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isPlaying) "Playing Audio (00:${currentSeconds.toString().padStart(2, '0')}/00:${duration})" else "Voice Note ($duration seconds)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomWaveform(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp),
                        wavePower = if (isPlaying) 0.6f else 0.05f,
                        active = isPlaying
                    )
                }
            }

            if (transcription != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "AI Transcript",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = transcription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}


// -- BOTTOM NAVIGATION BAR --

@Composable
fun AgeNoBarBottomNavigation(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    isTeacherMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background, // Match soft warm bone background
        tonalElevation = 2.dp,
        modifier = modifier
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .height(84.dp) // Generous height for comfortable tap actions
    ) {
        val items = remember(isTeacherMode) {
            val list = mutableListOf<Triple<AppTab, androidx.compose.ui.graphics.vector.ImageVector, String>>(
                Triple(AppTab.Home, Icons.Outlined.Home, "Home")
            )
            list.add(Triple(AppTab.Communities, Icons.Outlined.Groups, "Circles"))
            list.add(Triple(AppTab.VoiceRooms, Icons.Outlined.Coffee, "Gatherings"))
            list.add(Triple(AppTab.Requests, Icons.Outlined.Assignment, "Requests"))
            list
        }

        items.forEach { (tab, icon, label) ->
            val isSelected = selectedTab::class == tab::class
            NavigationBarItem(
                selected = isSelected,
                alwaysShowLabel = false,
                onClick = { onTabSelected(tab) },
                icon = {
                    Box(modifier = Modifier.padding(2.dp)) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(24.dp) // Sleek size
                        )
                    }
                },
                label = { 
                    Text(
                        text = label, 
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 10.sp,
                        letterSpacing = 0.05.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.testTag("nav_tab_${label.lowercase().replace(" ", "")}")
            )
        }
    }
}

// -- MAIN COMPONENT SCREENS --

data class HomeCategory(
    val emoji: String,
    val name: String,
    val bgGrad: List<Color>,
    val desc: String
)

@Composable
fun CategoryIllustration(categoryName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        when (categoryName) {
            "Education" -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFFC5CAE9).copy(alpha = 0.5f),
                            radius = size.minDimension * 0.45f,
                            center = Offset(size.width * 0.5f, size.height * 0.5f)
                        )
                    }
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .size(54.dp)
                            .align(Alignment.Center)
                            .offset(x = (-16).dp, y = (-12).dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("👨‍🏫", fontSize = 32.sp)
                        }
                    }
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .size(44.dp)
                            .align(Alignment.Center)
                            .offset(x = 20.dp, y = 14.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("🧑‍🎓", fontSize = 24.sp)
                        }
                    }
                    Text(
                        text = "📖",
                        fontSize = 22.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                    )
                }
            }
            "Finance & Banking" -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFFA5D6A7).copy(alpha = 0.4f),
                            radius = size.minDimension * 0.45f,
                            center = Offset(size.width * 0.5f, size.height * 0.5f)
                        )
                    }
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .size(54.dp)
                            .align(Alignment.Center)
                            .offset(x = (-18).dp, y = (-8).dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("👴", fontSize = 32.sp)
                        }
                    }
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .size(46.dp)
                            .align(Alignment.Center)
                            .offset(x = 18.dp, y = 12.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("👪", fontSize = 24.sp)
                        }
                    }
                    Text(
                        text = "🪙",
                        fontSize = 24.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 8.dp)
                    )
                }
            }
            "Legal Guidance" -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFFE1BEE7).copy(alpha = 0.4f),
                            radius = size.minDimension * 0.45f,
                            center = Offset(size.width * 0.5f, size.height * 0.5f)
                        )
                    }
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .size(54.dp)
                            .align(Alignment.Center)
                            .offset(x = 18.dp, y = (-12).dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("👵", fontSize = 32.sp)
                        }
                    }
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .size(46.dp)
                            .align(Alignment.Center)
                            .offset(x = (-16).dp, y = 14.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("👩‍⚖️", fontSize = 24.sp)
                        }
                    }
                    Text(
                        text = "⚖️",
                        fontSize = 24.sp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 4.dp, y = 6.dp)
                    )
                }
            }
            "Science & Technology" -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFFD1C4E9).copy(alpha = 0.4f),
                            radius = size.minDimension * 0.45f,
                            center = Offset(size.width * 0.5f, size.height * 0.5f)
                        )
                    }
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .size(52.dp)
                            .align(Alignment.Center)
                            .offset(x = (-18).dp, y = 14.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("👴", fontSize = 30.sp)
                        }
                    }
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .size(46.dp)
                            .align(Alignment.Center)
                            .offset(x = 16.dp, y = (-12).dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("🙋‍♂️", fontSize = 24.sp)
                        }
                    }
                    Text(
                        text = "📱",
                        fontSize = 28.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = (-4).dp, y = (-8).dp)
                    )
                }
            }
            "Career & Interviews" -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFFFFCC80).copy(alpha = 0.4f),
                            radius = size.minDimension * 0.45f,
                            center = Offset(size.width * 0.5f, size.height * 0.5f)
                        )
                    }
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .size(54.dp)
                            .align(Alignment.Center)
                            .offset(x = (-14).dp, y = (-12).dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("👨‍💼", fontSize = 32.sp)
                        }
                    }
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0B2)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .size(44.dp)
                            .align(Alignment.Center)
                            .offset(x = 20.dp, y = 12.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("👩‍💻", fontSize = 24.sp)
                        }
                    }
                    Text(
                        text = "💼",
                        fontSize = 22.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                    )
                }
            }
            "Gardening" -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFFA5D6A7).copy(alpha = 0.4f),
                            radius = size.minDimension * 0.45f,
                            center = Offset(size.width * 0.5f, size.height * 0.5f)
                        )
                    }
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .size(54.dp)
                            .align(Alignment.Center)
                            .offset(x = (-12).dp, y = (-12).dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("👴", fontSize = 32.sp)
                        }
                    }
                    Text(
                        text = "🌿",
                        fontSize = 32.sp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = (-4).dp)
                    )
                    Text(
                        text = "🏡",
                        fontSize = 20.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                    )
                }
            }
            "Cooking" -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFFFFCC80).copy(alpha = 0.4f),
                            radius = size.minDimension * 0.45f,
                            center = Offset(size.width * 0.5f, size.height * 0.5f)
                        )
                    }
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .size(54.dp)
                            .align(Alignment.Center)
                            .offset(x = (-16).dp, y = (-10).dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("👵", fontSize = 32.sp)
                        }
                    }
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF59D)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .size(44.dp)
                            .align(Alignment.Center)
                            .offset(x = 18.dp, y = 14.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("🧑‍🍳", fontSize = 24.sp)
                        }
                    }
                    Text(
                        text = "🍳",
                        fontSize = 24.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-6).dp, y = 4.dp)
                    )
                }
            }
            "Yoga & Wellness", "Health & Wellness" -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFF80CBC4).copy(alpha = 0.4f),
                            radius = size.minDimension * 0.45f,
                            center = Offset(size.width * 0.5f, size.height * 0.5f)
                        )
                    }
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .size(54.dp)
                            .align(Alignment.Center)
                            .offset(x = (-12).dp, y = (-6).dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("🧘‍♂️", fontSize = 32.sp)
                        }
                    }
                    Text(
                        text = "🌸",
                        fontSize = 26.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 6.dp)
                    )
                    Text(
                        text = "✨",
                        fontSize = 18.sp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = 10.dp, y = (-12).dp)
                    )
                }
            }
            "Astrology & Astronomy", "Astrology" -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFFCE93D8).copy(alpha = 0.4f),
                            radius = size.minDimension * 0.45f,
                            center = Offset(size.width * 0.5f, size.height * 0.5f)
                        )
                    }
                    Text(
                        text = "🌙",
                        fontSize = 36.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = (-12).dp, y = (-6).dp)
                    )
                    Text(
                        text = "✨",
                        fontSize = 20.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 6.dp)
                    )
                    Text(
                        text = "🔮",
                        fontSize = 26.sp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = (-12).dp)
                    )
                }
            }
            "Languages" -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFF90CAF9).copy(alpha = 0.4f),
                            radius = size.minDimension * 0.45f,
                            center = Offset(size.width * 0.5f, size.height * 0.5f)
                        )
                    }
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .size(52.dp)
                            .align(Alignment.Center)
                            .offset(x = (-16).dp, y = (-10).dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("👵", fontSize = 30.sp)
                        }
                    }
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFBBDEFB)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .size(44.dp)
                            .align(Alignment.Center)
                            .offset(x = 18.dp, y = 12.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("🙋‍♀️", fontSize = 24.sp)
                        }
                    }
                    Text(
                        text = "💬",
                        fontSize = 24.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-6).dp, y = 6.dp)
                    )
                }
            }
            "Parenting" -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFFFFCC80).copy(alpha = 0.4f),
                            radius = size.minDimension * 0.45f,
                            center = Offset(size.width * 0.5f, size.height * 0.5f)
                        )
                    }
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .size(52.dp)
                            .align(Alignment.Center)
                            .offset(x = (-14).dp, y = (-10).dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("👵", fontSize = 30.sp)
                        }
                    }
                    Text(
                        text = "👶",
                        fontSize = 28.sp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-6).dp, y = (-12).dp)
                    )
                    Text(
                        text = "❤️",
                        fontSize = 18.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-6).dp, y = 6.dp)
                    )
                }
            }
            "Music" -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFF90CAF9).copy(alpha = 0.4f),
                            radius = size.minDimension * 0.45f,
                            center = Offset(size.width * 0.5f, size.height * 0.5f)
                        )
                    }
                    Text(
                        text = "🎸",
                        fontSize = 32.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = (-14).dp, y = (-4).dp)
                    )
                    Text(
                        text = "🎵",
                        fontSize = 26.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-10).dp, y = 8.dp)
                    )
                    Text(
                        text = "✨",
                        fontSize = 18.sp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-6).dp, y = (-12).dp)
                    )
                }
            }
            "Senior Living" -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFFB0BEC5).copy(alpha = 0.4f),
                            radius = size.minDimension * 0.45f,
                            center = Offset(size.width * 0.5f, size.height * 0.5f)
                        )
                    }
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .size(54.dp)
                            .align(Alignment.Center)
                            .offset(x = (-12).dp, y = (-10).dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("👵", fontSize = 32.sp)
                        }
                    }
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECEFF1)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .size(46.dp)
                            .align(Alignment.Center)
                            .offset(x = 16.dp, y = 10.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("👴", fontSize = 28.sp)
                        }
                    }
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFFEF9A9A).copy(alpha = 0.4f),
                            radius = size.minDimension * 0.45f,
                            center = Offset(size.width * 0.5f, size.height * 0.5f)
                        )
                    }
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .size(52.dp)
                            .align(Alignment.Center)
                            .offset(x = (-16).dp, y = (-12).dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("👩‍⚕️", fontSize = 30.sp)
                        }
                    }
                    Text(
                        text = "❤️",
                        fontSize = 28.sp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-6).dp, y = (-12).dp)
                    )
                    Text(
                        text = "🩺",
                        fontSize = 24.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-6).dp, y = 6.dp)
                    )
                }
            }
        }
    }
}

fun getCategoryImageUrl(categoryName: String): Any {
    return when (categoryName) {
        "Education" -> R.drawable.img_cat_education
        "Finance & Banking", "Finance and Banking" -> R.drawable.img_cat_finance
        "Legal Guidance" -> R.drawable.img_cat_legal
        "Science & Technology", "Science and Technology" -> R.drawable.img_cat_science
        "Career & Interviews", "Careers and Interviews" -> R.drawable.img_cat_careers
        "Gardening" -> R.drawable.img_cat_gardening
        "Cooking" -> R.drawable.img_cat_cooking
        "Recipes & Traditions", "Recipes and traditions", "Recipes and Traditions" -> R.drawable.img_cat_recipes
        "Yoga & Wellness", "Health & Wellness", "health and welness" -> R.drawable.img_cat_wellness
        "Astrology & Astronomy", "Astrology", "astrology and astronomy" -> R.drawable.img_cat_astrology
        "Languages", "languages" -> R.drawable.img_cat_languages
        "Parenting" -> "https://images.unsplash.com/photo-1609234656388-0ff363383899?auto=format&fit=crop&q=80&w=600"
        "Music", "music, shlokas", "Music, Shlokas & Wisdom" -> R.drawable.img_cat_music
        "Senior Living" -> "https://images.unsplash.com/photo-1576765608535-5f04d1e3f289?auto=format&fit=crop&q=80&w=600"
        "Emergency Help & First Aid" -> "https://images.unsplash.com/photo-1581594693702-fbdc51b2763b?auto=format&fit=crop&q=80&w=600"
        "Ayurveda", "ayurveda" -> R.drawable.img_cat_ayurveda
        "Physiotherapy", "physiotherapy" -> R.drawable.img_cat_physio
        "Indian Dance Forms", "indian dance form" -> R.drawable.img_cat_dance
        "Stories", "Stories, Shlokas & Wisdom" -> R.drawable.img_cat_stories
        else -> "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&q=80&w=600"
    }
}

@Composable
fun CategoryDiscoveryCard(
    category: HomeCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(235.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(26.dp),
                clip = false,
                ambientColor = category.bgGrad.last().copy(alpha = 0.25f),
                spotColor = category.bgGrad.last().copy(alpha = 0.5f)
            )
            .clickable(onClick = onClick)
            .testTag("category_card_${category.name.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Underlayer background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                category.bgGrad.first(),
                                category.bgGrad.last()
                            )
                        )
                    )
            )

            // Dynamic background illustration as a placeholder
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .align(Alignment.Center)
            ) {
                CategoryIllustration(categoryName = category.name)
            }

            // Cinematic Unsplash Category Photo fills the card
            AsyncImage(
                model = getCategoryImageUrl(category.name),
                contentDescription = category.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Luxury discovery overlay gradient (top slight shade, bottom deep shadow for text legibility)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.35f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Content Overlay at the bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                // Glassmorph supporting emoji badge (minimal, premium styling)
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(8.dp))
                        .border(
                            width = 0.5.dp,
                            color = Color.White.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = category.emoji,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Short title
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Minimal supporting description
                Text(
                    text = category.desc,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 9.2.sp,
                    lineHeight = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MainCategoryCard(
    category: MainCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(26.dp),
                clip = false,
                ambientColor = category.bgGrad.last().copy(alpha = 0.25f),
                spotColor = category.bgGrad.last().copy(alpha = 0.5f)
            )
            .clickable(onClick = onClick)
            .testTag("main_category_card_${category.name.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Cinematic Photo fills the card
            Image(
                painter = painterResource(id = category.imageRes),
                contentDescription = category.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Luxury discovery overlay gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.35f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Content Overlay at the bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(18.dp)
            ) {
                // Glassmorph supporting emoji badge
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(8.dp))
                        .border(
                            width = 0.5.dp,
                            color = Color.White.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = category.emoji,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Short title
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Minimal supporting description
                Text(
                    text = category.desc,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 11.5.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SubExperienceCard(
    subExperience: SubExperience,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(26.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Full-bleed image
            Image(
                painter = painterResource(id = subExperience.imageRes),
                contentDescription = subExperience.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Glass/gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.1f),
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )

            // Absolute date or pill placeholder on the top left like "Live Now"
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "Live Now",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Bottom elements
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Title and Description
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(subExperience.emoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = subExperience.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subExperience.desc,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Beautiful white button with arrow inside
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Explore",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EngagementChoiceCard(
    title: String,
    description: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Navigate",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// SCREEN 1: HOME DASHBOARD
data class SubExperience(
    val emoji: String,
    val name: String,
    val imageRes: Int,
    val desc: String,
    val originalCategoryName: String,
    val defaultSubPage: String? = null,
    val memberCount: Int = 1200,
    val expertCount: Int = 540
)

data class MainCategory(
    val emoji: String,
    val name: String,
    val imageRes: Int,
    val desc: String,
    val bgGrad: List<Color>,
    val subExperiences: List<SubExperience>
)

sealed class NavigationState {
    object Dashboard : NavigationState()
    data class CategoryDetail(val category: MainCategory) : NavigationState()
    data class SubcategoryDetail(val subExp: SubExperience, val category: MainCategory) : NavigationState()
    data class EventDetail(val event: CommunityEvent) : NavigationState()
    data class PremiumScheduler(val expertId: String, val rescheduleBookingId: String? = null) : NavigationState()
}

data class CalendarDay(
    val key: String,
    val fullName: String,
    val labelNum: String,
    val isToday: Boolean = false,
    val dateString: String = ""
)

fun Modifier.whiteCardShadow(): Modifier = this.drawBehind {
    val cornerRadiusPx = 20.dp.toPx()
    val width = size.width
    val height = size.height

    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.TRANSPARENT
    }

    drawContext.canvas.nativeCanvas.apply {
        // Layer 1: 0 20px 40px rgba(0,0,0,0.08)
        paint.setShadowLayer(
            40.dp.toPx(),
            0f,
            20.dp.toPx(),
            Color.Black.copy(alpha = 0.08f).toArgb()
        )
        drawRoundRect(
            android.graphics.RectF(0f, 0f, width, height),
            cornerRadiusPx,
            cornerRadiusPx,
            paint
        )

        // Layer 2: 0 8px 16px rgba(0,0,0,0.10)
        paint.setShadowLayer(
            16.dp.toPx(),
            0f,
            8.dp.toPx(),
            Color.Black.copy(alpha = 0.10f).toArgb()
        )
        drawRoundRect(
            android.graphics.RectF(0f, 0f, width, height),
            cornerRadiusPx,
            cornerRadiusPx,
            paint
        )

        // Layer 3: 0 2px 4px rgba(0,0,0,0.04)
        paint.setShadowLayer(
            4.dp.toPx(),
            0f,
            2.dp.toPx(),
            Color.Black.copy(alpha = 0.04f).toArgb()
        )
        drawRoundRect(
            android.graphics.RectF(0f, 0f, width, height),
            cornerRadiusPx,
            cornerRadiusPx,
            paint
        )
    }

    drawRoundRect(
        color = Color.White,
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
    )

    drawRoundRect(
        color = Color.White.copy(alpha = 0.9f),
        style = Stroke(width = 1.dp.toPx()),
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
    )
}

fun Modifier.bulletinHaloWrapper(): Modifier = this.drawBehind {
    val brush = Brush.radialGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFFF4B8D4),
            0.4f to Color(0xFFE8C4F0),
            0.75f to Color(0xFFF0E8E0),
            1.0f to Color(0xFFF0E8E0)
        ),
        center = Offset(size.width * 0.65f, size.height * 0.35f),
        radius = maxOf(size.width, size.height) * 1.0f
    )
    drawRoundRect(
        brush = brush,
        cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx())
    )
}.padding(2.5.dp)

fun Modifier.sessionsHaloWrapper(): Modifier = this.drawBehind {
    val brush = Brush.radialGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFFB8D4F4),
            0.4f to Color(0xFFC4E0F0),
            0.75f to Color(0xFFF0E8E0),
            1.0f to Color(0xFFF0E8E0)
        ),
        center = Offset(size.width * 0.35f, size.height * 0.65f),
        radius = maxOf(size.width, size.height) * 1.0f
    )
    drawRoundRect(
        brush = brush,
        cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx())
    )
}.padding(2.5.dp)

@Composable
fun HomeDashboardScreen(
    viewModel: AgeNoBarViewModel,
    onNavigateToCommunity: (String) -> Unit,
    onNavigateToVoiceRooms: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val communities by viewModel.communities.collectAsState()
    val events by viewModel.events.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val previousCalls by viewModel.previousCalls.collectAsState()
    val teacherWalletBalance by viewModel.teacherWalletBalance.collectAsState()
    val experts by viewModel.experts.collectAsState()
    val bookingsList by viewModel.bookingsList.collectAsState()

    val isTeacherMode = user.userRoleType == "Teach"

    var showQuickQuestionDialog by remember { mutableStateOf(false) }
    var selectedCircleCategory by remember { mutableStateOf("All") }
    var selectedExpertCategory by remember { mutableStateOf("All") }
    var showPayoutSuccessAlert by remember { mutableStateOf(false) }
    var playingCallId by remember { mutableStateOf<String?>(null) }

    // Dialog state for quick tutoring booking feedback
    var bookedSuccessExpertName by remember { mutableStateOf<String?>(null) }

    var activeSelectedCategory by remember { mutableStateOf<String?>(null) }
    var activeMainCategory by remember { mutableStateOf<MainCategory?>(null) }
    var activeSubExperience by remember { mutableStateOf<SubExperience?>(null) }

    var activeSubPage by remember { mutableStateOf<String?>(null) }
    var selectedSubCuisine by remember { mutableStateOf<String?>(null) }
    var selectedSubFestival by remember { mutableStateOf<String?>(null) }
    var aiIngredients by remember { mutableStateOf("") }
    var aiRecipeResult by remember { mutableStateOf<String?>(null) }
    var isAiRecipeGenerating by remember { mutableStateOf(false) }
    var selectedLanguageName by remember { mutableStateOf<String?>(null) }
    var selectedMusicCategory by remember { mutableStateOf<String?>(null) }
    var astrologyCurrentTab by remember { mutableStateOf("Astrology") }
    var activeSubFeatureToast by remember { mutableStateOf<String?>(null) }
    var activeTutorialDialogText by remember { mutableStateOf<String?>(null) }
    var isVoiceRecipeRecording by remember { mutableStateOf(false) }

    val mainCategoriesList = listOf(
        MainCategory(
            emoji = "📚",
            name = "LEARN & GROW",
            imageRes = R.drawable.img_cat_education,
            desc = "Tutoring, financial literacy, career growth, legal aid, and scientific innovation.",
            bgGrad = listOf(Color(0xFFEBF5FB), Color(0xFFD4E6F1)),
            subExperiences = listOf(
                SubExperience("👨‍🏫", "Teacher Mentors", R.drawable.img_cat_education, "Personalized learning and tutoring mentors.", "Education", null, 1200, 540),
                SubExperience("💼", "Career Coaching", R.drawable.img_cat_careers, "Professional growth, CV reviews, and path coaching.", "Career & Interviews", null, 890, 310),
                SubExperience("📝", "Interview Preparation", R.drawable.img_cat_careers, "Mock interviews, communication drills, and technical training.", "Career & Interviews", null, 450, 140),
                SubExperience("💰", "Finance Literacy", R.drawable.img_cat_finance, "Retirement plans, savings, banking schemes, and tax awareness.", "Finance & Banking", null, 930, 280),
                SubExperience("📊", "Retirement Planning", R.drawable.img_cat_finance, "Roadmapping secure pensions and senior savings accounts.", "Finance & Banking", null, 640, 180),
                SubExperience("⚖️", "Legal Guidance", R.drawable.img_cat_legal, "Practical legal literacy, property guidelines, and rights.", "Legal Guidance", null, 400, 120),
                SubExperience("🔬", "Science & Technology", R.drawable.img_cat_science, "Explore STEM updates, coding principles, and modern tools.", "Science & Technology", null, 780, 250),
                SubExperience("👥", "Study Groups", R.drawable.img_cat_education, "Intergenerational lifelong learning cohorts and circles.", "Education", null, 512, 90)
            )
        ),
        MainCategory(
            emoji = "❤️",
            name = "HEALTH & WELLNESS",
            imageRes = R.drawable.img_cat_wellness,
            desc = "Holistic physical health, ayurveda, rehabilitation, yoga, and meditation.",
            bgGrad = listOf(Color(0xFFEAF2F8), Color(0xFFA9CCE3)),
            subExperiences = listOf(
                SubExperience("🌿", "Ayurveda", R.drawable.img_cat_ayurveda, "Organic herbs, healing natural oils, and traditional therapies.", "Ayurveda", null, 1400, 210),
                SubExperience("🧘", "Yoga", R.drawable.img_cat_wellness, "Gentle stretches, posture correction, and breath control.", "Health & Wellness", null, 2200, 350),
                SubExperience("🫁", "Meditation", R.drawable.img_cat_wellness, "Tranquil mindfulness sessions for focus and lower stress.", "Health & Wellness", null, 1800, 290),
                SubExperience("🦵", "Physiotherapy", R.drawable.img_cat_physio, "Senior posture recovery and specialized mobility exercises.", "Physiotherapy", null, 980, 160),
                SubExperience("🌸", "Healthy Aging", R.drawable.img_cat_wellness, "Lifelong physical strength, daily habits, and vitality charts.", "Health & Wellness", null, 750, 140),
                SubExperience("🍎", "Nutrition", R.drawable.img_cat_ayurveda, "Balanced senior diet sheets, organic salads, and dietary charts.", "Ayurveda", null, 1100, 220)
            )
        ),
        MainCategory(
            emoji = "🏡",
            name = "RECIPES & TRADITIONS",
            imageRes = R.drawable.img_cat_cooking,
            desc = "Authentic Indian family recipes, festival traditions, and kitchen secrets.",
            bgGrad = listOf(Color(0xFFFCF3CF), Color(0xFFF5CBA7)),
            subExperiences = listOf(
                SubExperience("🍲", "Indian Cuisine", R.drawable.img_cat_cooking, "Step-by-step masterclasses for traditional subcontinental cooking.", "Recipes & Traditions", "recipes", 3400, 420),
                SubExperience("🎉", "Festival Recipes", R.drawable.img_cat_recipes, "Sacred sweets, celebratory savories, and traditional festival menus.", "Recipes & Traditions", "festivals", 2800, 310),
                SubExperience("📜", "Family Traditions", R.drawable.img_cat_recipes, "Warm generation-spanning family diaries, cooking tips, and customs.", "Recipes & Traditions", "blogs", 1500, 190),
                SubExperience("🍛", "Regional Cooking", R.drawable.img_cat_cooking, "South-Indian sambhars, Maharashtrian modaks, and regional spices.", "Recipes & Traditions", "recipes", 1950, 280),
                SubExperience("🤖", "AI Recipe Assistant", R.drawable.img_ai_chachi, "Enter any available kitchen items to instantly design custom recipes.", "Recipes & Traditions", "ai_chef", 5000, 1),
                SubExperience("👥", "Food Communities", R.drawable.img_cat_cooking, "Meet, share, and swap authentic recipes with passionate homecooks.", "Recipes & Traditions", "blogs", 1200, 95)
            )
        ),
        MainCategory(
            emoji = "🎭",
            name = "ARTS, MUSIC & CULTURE",
            imageRes = R.drawable.img_cat_dance,
            desc = "Discover music, classical instruments, classical dance forms, and paintings.",
            bgGrad = listOf(Color(0xFFFFF1F1), Color(0xFFFECACA)),
            subExperiences = listOf(
                SubExperience("🎵", "Carnatic Music", R.drawable.img_cat_music, "Explore composition principles, notes, and classical ragas.", "Music", "vocal", 1100, 180),
                SubExperience("🎶", "Hindustani Music", R.drawable.img_cat_music, "North Indian vocals, khayals, and rhythmic breathing lessons.", "Music", "vocal", 920, 150),
                SubExperience("🙏", "Bhajans & Shlokas", R.drawable.img_cat_music, "Sufi chants, ancient pronunciations, sacred shlokas and community devotional singing.", "Music", "vocal", 2400, 310),
                SubExperience("🎻", "Instruments", R.drawable.img_cat_music, "Learn Indian flute, sitar, harmonium, and tabla basics.", "Music", "instruments", 840, 130),
                SubExperience("💃", "Dance Forms", R.drawable.img_cat_dance, "Traditional classical styles such as Bharatanatyam and Kathak.", "Indian Dance Forms", null, 950, 160),
                SubExperience("🎨", "Traditional Arts", R.drawable.img_cat_recipes, "Folk craft methods: Warli paintings, pottery, and organic arts.", "Indian Dance Forms", null, 720, 110)
            )
        ),
        MainCategory(
            emoji = "📖",
            name = "STORIES & HERITAGE",
            imageRes = R.drawable.img_cat_stories,
            desc = "Ancient scriptures, mythology, regional legends, and Sanskrit learning.",
            bgGrad = listOf(Color(0xFFFEF9E7), Color(0xFFFAD7A0)),
            subExperiences = listOf(
                SubExperience("🦁", "Panchatantra", R.drawable.img_cat_stories, "Legendary animal fables of deep wisdom to teach grandkids.", "Stories, Shlokas & Wisdom", null, 940, 120),
                SubExperience("🗣️", "Sanskrit Stories", R.drawable.img_cat_stories, "Practice traditional Sanskrit words through engaging oral lore.", "Stories, Shlokas & Wisdom", null, 830, 140),
                SubExperience("🏹", "Ramayana", R.drawable.img_cat_stories, "Explore the heroic epic of Lord Rama and righteous values.", "Stories, Shlokas & Wisdom", null, 1450, 180),
                SubExperience("🛡️", "Mahabharata", R.drawable.img_cat_stories, "An in-depth guide through ancient chapters and moral dilemmas.", "Stories, Shlokas & Wisdom", null, 1320, 170),
                SubExperience("🕉️", "Devotional Stories", R.drawable.img_cat_stories, "Inspiring legends of saints, mystics, and traditional gurus.", "Stories, Shlokas & Wisdom", null, 1600, 210),
                SubExperience("🎙️", "Story Time", R.drawable.img_cat_stories, "Live daily group storytelling circles for lifelong narrators.", "Stories, Shlokas & Wisdom", null, 1100, 150),
                SubExperience("🏛️", "Indian Heritage", R.drawable.img_cat_stories, "A digital tour through grand temples and historical ruins.", "Stories, Shlokas & Wisdom", null, 980, 130)
            )
        ),
        MainCategory(
            emoji = "🌿",
            name = "NATURE & LIFESTYLE",
            imageRes = R.drawable.img_cat_gardening,
            desc = "Balcony organic farming, indoor setups, and plant care communities.",
            bgGrad = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9)),
            subExperiences = listOf(
                SubExperience("🌱", "Gardening", R.drawable.img_cat_gardening, "Unearth soil mixes, organic fertilization, and seed potting.", "Gardening", null, 2500, 310),
                SubExperience("🪴", "Terrace Gardening", R.drawable.img_cat_gardening, "Design private rooftop organic retreats and urban vegetables.", "Gardening", null, 1400, 180),
                SubExperience("🌽", "Organic Farming", R.drawable.img_cat_gardening, "Composting, vermiculture, chemical-free sprays, and pest cures.", "Gardening", null, 1100, 150),
                SubExperience("🏡", "Indoor Plants", R.drawable.img_cat_gardening, "Light guides and scheduling for healthy oxygen-rich home decor.", "Gardening", null, 1650, 220),
                SubExperience("🌳", "Bonsai", R.drawable.img_cat_gardening, "The ancient oriental shape discipline of trimming miniaturized trees.", "Gardening", null, 670, 80),
                SubExperience("👥", "Nature Communities", R.drawable.img_cat_gardening, "Meet plant experts, exchange seeds, and document sprouts.", "Gardening", null, 820, 75)
            )
        )
    )

    val categoriesList = listOf(
        HomeCategory("📚", "Education", listOf(Color(0xFFEBF5FB), Color(0xFFD4E6F1)), "Tutoring, exams and lifelong learning."),
        HomeCategory("💰", "Finance & Banking", listOf(Color(0xFFE8F8F5), Color(0xFFA3E4D7)), "Financial literacy and retirement guidance."),
        HomeCategory("⚖️", "Legal Guidance", listOf(Color(0xFFFCF3CF), Color(0xFFF9E79F)), "Legal awareness and practical guidance."),
        HomeCategory("💼", "Career & Interviews", listOf(Color(0xFFF6DDCC), Color(0xFFEDBB99)), "Career growth and interview preparation."),
        HomeCategory("🔬", "Science & Technology", listOf(Color(0xFFF5EEF8), Color(0xFFD7BDE2)), "STEM learning and innovation."),
        HomeCategory("🌿", "Gardening", listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9)), "Plants, food growing and nature."),
        HomeCategory("🍳", "Cooking", listOf(Color(0xFFFEF9E7), Color(0xFFFDEDEC)), "Traditional recipes, family kitchen wisdom, and high-quality Indian dishes."),
        HomeCategory("🏡", "Recipes & Traditions", listOf(Color(0xFFFCF3CF), Color(0xFFF5CBA7)), "Browse North & South Indian cuisines, sacred festival rituals, and folklore."),
        HomeCategory("🧘", "Health & Wellness", listOf(Color(0xFFEAF2F8), Color(0xFFA9CCE3)), "Active seniors learning rehabilitation, yoga, and meditation practices."),
        HomeCategory("🌿", "Ayurveda", listOf(Color(0xFFF5F5DC), Color(0xFFE6CCB2)), "Traditional herbs, healing oils, and luxury natural wellness practices."),
        HomeCategory("🦵", "Physiotherapy", listOf(Color(0xFFEBF5FB), Color(0xFFC2DFFF)), "Professional rehabilitation, posture, and mobility exercises."),
        HomeCategory("🗣", "Languages", listOf(Color(0xFFFDEDEC), Color(0xFFF9D5D3)), "Practice popular Indian languages like Hindi, Tamil, Telugu and global tongues."),
        HomeCategory("👶", "Parenting", listOf(Color(0xFFFEFBF3), Color(0xFFFDF6EC)), "Three generations of warm support: grandparents, parents, and children together."),
        HomeCategory("🎵", "Music", listOf(Color(0xFFEBF5FB), Color(0xFFD6EAF8)), "Discover Indian Classical, Devotional chanting, instrument guides and vocal lessons."),
        HomeCategory("🎭", "Indian Dance Forms", listOf(Color(0xFFFFF1F1), Color(0xFFFECACA)), "Discover Bharatanatyam, Kathak, Kuchipudi and classical performing heritage."),
        HomeCategory("📖", "Stories, Shlokas & Wisdom", listOf(Color(0xFFFEF9E7), Color(0xFFFAD7A0)), "Panchatantra, Devotional stories, Ramayana epic, and grandparent story circles."),
        HomeCategory("⭐", "Astrology & Astronomy", listOf(Color(0xFFF5EEF8), Color(0xFFE8DAEF)), "Separate paths: explore traditional Vedic Astrology/Panchang or astronomy science and stars."),
        HomeCategory("👵", "Senior Living", listOf(Color(0xFFEAEDED), Color(0xFFCCD1D1)), "Retirement, hobbies and companionship."),
        HomeCategory("❤️", "Emergency Help & First Aid", listOf(Color(0xFFFDEDEC), Color(0xFFFADBD8)), "Quick access to emergency resources.")
    )

    val circleCategories = listOf("All", "Education", "Finance", "Family", "Interests", "Music")
    val expertCategories = listOf("All", "Tech Safety", "Gardening", "Wellbeing", "Language")

    val calendarDays = remember {
        val today = java.time.LocalDate.now()
        (-7..7).map { offset ->
            val date = today.plusDays(offset.toLong())
            val isDateToday = offset == 0
            val shortName = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()) // "Mon", "Tue"...
            val fullName = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault()) // "Monday", "Tuesday"...
            val labelNum = date.dayOfMonth.toString()
            CalendarDay(
                key = shortName.take(3), // e.g. "Mon"
                fullName = fullName,
                labelNum = labelNum,
                isToday = isDateToday,
                dateString = date.toString()
            )
        }
    }

    fun getEventsForDay(day: CalendarDay, allEvents: List<CommunityEvent>): List<CommunityEvent> {
        return allEvents.filter { event ->
            val timeString = event.localTime.lowercase()
            val matchToday = day.isToday && (timeString.contains("today") || timeString.contains(day.fullName.lowercase()) || timeString.contains(day.key.lowercase()))
            val matchDayName = timeString.contains(day.fullName.lowercase()) || timeString.contains(day.key.lowercase())
            matchToday || matchDayName
        }
    }

    val initiallySelectedDay = remember(calendarDays) {
        calendarDays.find { it.isToday }?.dateString ?: ""
    }
    var selectedCalendarDay by remember(initiallySelectedDay) { mutableStateOf(initiallySelectedDay) }

    var screenBackstack by remember { mutableStateOf<List<NavigationState>>(listOf(NavigationState.Dashboard)) }
    var selectedExpertForProfileState by remember { mutableStateOf<Expert?>(null) }
    var activeActionExpertForHome by remember { mutableStateOf<Expert?>(null) }
    var activeActionTypeForHome by remember { mutableStateOf<String?>(null) }
    val currentScreen = screenBackstack.lastOrNull() ?: NavigationState.Dashboard

    val homeResetTrigger by viewModel.homeResetTrigger.collectAsState()
    LaunchedEffect(homeResetTrigger) {
        if (homeResetTrigger > 0) {
            screenBackstack = listOf(NavigationState.Dashboard)
            selectedExpertForProfileState = null
            activeActionExpertForHome = null
            activeActionTypeForHome = null
        }
    }

    BackHandler(enabled = screenBackstack.size > 1) {
        screenBackstack = screenBackstack.dropLast(1)
    }

    val homeBgColor = Color(0xFFFDF6EE)

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (currentScreen is NavigationState.Dashboard) {
                    Modifier.background(homeBgColor)
                } else {
                    Modifier.background(MaterialTheme.colorScheme.background)
                }
            )
    ) {
        when (currentScreen) {
            is NavigationState.CategoryDetail -> {
                CategoryDetailScreen(
                    category = currentScreen.category,
                    viewModel = viewModel,
                    onBack = {
                        if (screenBackstack.size > 1) {
                            screenBackstack = screenBackstack.dropLast(1)
                        }
                    },
                    onSubcategoryClick = { sub ->
                        screenBackstack = screenBackstack + NavigationState.SubcategoryDetail(sub, currentScreen.category)
                    },
                    onExpertClick = { expert ->
                        selectedExpertForProfileState = expert
                    },
                    onCommunityClick = { comm ->
                        onNavigateToCommunity(comm.id)
                    },
                    onEventClick = { ev ->
                        screenBackstack = screenBackstack + NavigationState.EventDetail(ev)
                    }
                )
            }
            is NavigationState.SubcategoryDetail -> {
                SubcategoryDetailScreen(
                    subExp = currentScreen.subExp,
                    category = currentScreen.category,
                    viewModel = viewModel,
                    onBack = {
                        if (screenBackstack.size > 1) {
                            screenBackstack = screenBackstack.dropLast(1)
                        }
                    },
                    onExpertClick = { expert ->
                        selectedExpertForProfileState = expert
                    },
                    onCommunityClick = { comm ->
                        onNavigateToCommunity(comm.id)
                    },
                    onEventClick = { ev ->
                        screenBackstack = screenBackstack + NavigationState.EventDetail(ev)
                    }
                )
            }
            is NavigationState.PremiumScheduler -> {
                PremiumBookingCalendarScreen(
                    expertId = currentScreen.expertId,
                    rescheduleBookingId = currentScreen.rescheduleBookingId,
                    viewModel = viewModel,
                    onBack = {
                        if (screenBackstack.size > 1) {
                            screenBackstack = screenBackstack.dropLast(1)
                        }
                    }
                )
            }
            is NavigationState.EventDetail -> {
                val fullHostExpert = experts.firstOrNull { it.name.equals(currentScreen.event.hostName, ignoreCase = true) } ?: (experts.firstOrNull() ?: Expert("1", "Host", "Mentor", "Education", 10, "👨‍🏫", listOf("English"), 4.9, 20, 15, ""))
                EventDetailScreen(
                    event = currentScreen.event,
                    onBack = {
                        if (screenBackstack.size > 1) {
                            screenBackstack = screenBackstack.dropLast(1)
                        }
                    },
                    onHostClick = {
                        selectedExpertForProfileState = fullHostExpert
                    }
                )
            }
            is NavigationState.Dashboard -> {
                val homeLazyListState = rememberLazyListState()
                val homeCoroutineScope = rememberCoroutineScope()
                LazyColumn(
                    state = homeLazyListState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp, start = 16.dp, end = 16.dp)
                ) {
            // ITEM 1: Pristine editorial App Bar with connected arched bridge logo and red bell notification
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Diversity1,  // Friendly icon representing family connection
                                contentDescription = "Wisdom Bridge Logo",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Wisdom Bridge",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 23.sp
                            )
                            Text(
                                text = "Connecting Generations",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Notification & Profile row (styled premium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Profile Avatar (My Haven) button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White, CircleShape)
                                .border(1.dp, BorderLightSystem, CircleShape)
                                .clickable { viewModel.selectTab(AppTab.Profile) }
                                .testTag("top_profile_haven_trigger"),
                            contentAlignment = Alignment.Center
                        ) {
                            AvatarImage(
                                name = user.name,
                                size = 36
                            )
                        }

                        // Notification Bell (styled premium with a subtle active indicator dot)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White, CircleShape)
                                .border(1.dp, BorderLightSystem, CircleShape)
                                .clickable { /* Subtle mock notification interaction */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFFAC2424), CircleShape)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-10).dp, y = (10).dp)
                            )
                        }
                    }
                }
            }

            // ITEM 2: Premium segmented mode selector to switch between Learn and Teach roles
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val modes = listOf("Learn" to "📚 Learner Mode", "Teach" to "🎓 Teacher Mode")
                    modes.forEach { (modeKey, modeTitle) ->
                        val isSelected = user.userRoleType == modeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(9.dp)
                                )
                                .clickable { viewModel.changeUserRoleType(modeKey) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = modeTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // ITEM 3: Elevated Community Bulletin with individual pastel multilayer custom boxes
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notice_board_bulletin")
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    // Notice board Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📌", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "COMMUNITY BULLETIN",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Quick Access",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Three option bulletin card grid with beautiful individual pastel multilayer custom boxes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Tile 1: AI Chachi (Bulletin gradient)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(112.dp)
                                .bulletinHaloWrapper()
                                .clickable {
                                    viewModel.selectDirectConversation(null)
                                    viewModel.selectTab(AppTab.Messages)
                                }
                                .testTag("chachi_banner_card_trigger")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .whiteCardShadow()
                                    .padding(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_ai_chachi),
                                            contentDescription = "Chachi avatar",
                                            modifier = Modifier.size(30.dp).clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "AI Chachi",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Text(
                                        text = "Ask Anything",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Tile 2: Find Expert (Sessions gradient)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(112.dp)
                                .sessionsHaloWrapper()
                                .clickable {
                                    homeCoroutineScope.launch {
                                        homeLazyListState.animateScrollToItem(3)
                                    }
                                }
                                .testTag("notice_find_expert_trigger")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .whiteCardShadow()
                                    .padding(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("👨‍🏫", fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Find Expert",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Text(
                                        text = "Browse Mentors",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Tile 3: Join Circle (Bulletin gradient)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(112.dp)
                                .bulletinHaloWrapper()
                                .clickable { viewModel.selectTab(AppTab.Communities) }
                                .testTag("notice_join_circle_trigger")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .whiteCardShadow()
                                    .padding(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("👥", fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Join Circle",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Text(
                                        text = "Meet Communities",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_upcoming_sessions_section")
                        .whiteCardShadow()
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Upcoming Sessions 📅",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B1A1A),
                                modifier = Modifier.clickable {
                                    viewModel.openScheduler("exp_mock_lakshmi_rao")
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        val upcomingBookings = bookingsList.filter { it.status == "Upcoming" || it.status == "Joined" }
                        if (upcomingBookings.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(Color(0xFFFAFAFA), RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📅", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "No upcoming tutoring sessions booked.",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "Schedule a slot with an expert below!",
                                        fontSize = 9.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        } else {
                            upcomingBookings.take(3).forEach { item ->
                                val matchedExpert = experts.find { it.id == item.expertId }
                                val topicLower = (matchedExpert?.topic ?: "").lowercase()
                                
                                val (borderColor, emoji, displayTopic) = when {
                                    topicLower.contains("math") -> Triple(Color(0xFFAED6F1), "📐", matchedExpert?.topic ?: "Mathematics")
                                    topicLower.contains("garden") || topicLower.contains("plant") -> Triple(Color(0xFFA9DFBF), "🌳", matchedExpert?.topic ?: "Gardening")
                                    topicLower.contains("financ") || topicLower.contains("money") || topicLower.contains("tax") -> Triple(Color(0xFFF9E79F), "💰", matchedExpert?.topic ?: "Finance")
                                    topicLower.contains("well") || topicLower.contains("yoga") || topicLower.contains("health") -> Triple(Color(0xFFD7BDE2), "🧘", matchedExpert?.topic ?: "Wellness")
                                    topicLower.contains("legal") || topicLower.contains("law") -> Triple(Color(0xFFFAD7A0), "⚖️", matchedExpert?.topic ?: "Legal")
                                    else -> Triple(Color(0xFFD5D8DC), "🗓️", matchedExpert?.topic ?: "1:1 Session")
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp)
                                        .shadow(
                                            elevation = 2.dp,
                                            shape = RoundedCornerShape(12.dp),
                                            clip = false
                                        )
                                        .background(
                                            color = Color(0xFFFAFAFA),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .drawBehind {
                                            val strokeWidthPx = 3.dp.toPx()
                                            drawLine(
                                                color = borderColor,
                                                start = Offset(strokeWidthPx / 2f, 0f),
                                                end = Offset(strokeWidthPx / 2f, this.size.height),
                                                strokeWidth = strokeWidthPx
                                            )
                                        }
                                        .clickable { viewModel.setEditingBooking(item) }
                                        .testTag("home_upcoming_session_${item.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(borderColor.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(emoji, fontSize = 18.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = if (displayTopic.contains("Session")) displayTopic else "$displayTopic Refresher Session",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (item.status == "Joined") {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .background(Color(0xFF4CAF50), CircleShape)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = item.timing,
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = "Mentor: ${item.expertName} (${item.durationMinutes}m • ${if (item.isVideo) "Video" else "Voice"})",
                                                fontSize = 11.sp,
                                                color = Color(0xFF8B1A1A),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = { viewModel.setEditingBooking(item) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Modify booking",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            val isJoinedState = item.status == "Joined"
                                            Button(
                                                onClick = { 
                                                    viewModel.joinBooking(item.id)
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isJoinedState) Color(0xFF2E7D32) else Color(0xFF8B1A1A),
                                                    contentColor = Color.White
                                                ),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.height(28.dp).testTag("home_upcoming_join_${item.id}")
                                            ) {
                                                Text(if (isJoinedState) "Joined ✓" else "Join", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TEMPLATE TEACHER MODE SPECIFIC FEATURE: Wallet Cashout
            if (isTeacherMode) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🏦", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Teacher Wallet Account Balance",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Credits awarded by grateful families and junior pupils",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        text = "AVAILABLE FOR UPI CASH-OUT:",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "₹$teacherWalletBalance",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (teacherWalletBalance > 0) {
                                    Button(
                                        onClick = {
                                            viewModel.transferWalletToUpi(teacherWalletBalance)
                                            showPayoutSuccessAlert = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Transfer to UPI", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                } else {
                                    Text(
                                        text = "✓ Debited to UPI",
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ITEM 5: Explore Topics Section title
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    var isSearchExpanded by remember { mutableStateOf(false) }
                    var searchInterestText by remember { mutableStateOf("") }

                    if (!isSearchExpanded) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Explore Topics",
                                style = MaterialTheme.typography.titleLarge,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (isTeacherMode) {
                                Text(
                                    text = "View all",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { /* Informative click */ }
                                )
                            } else {
                                IconButton(
                                    onClick = { isSearchExpanded = true },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                        .testTag("home_explore_search_expand_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = "Search Interest",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = "Search icon",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (searchInterestText.isEmpty()) {
                                        Text(
                                            text = "Search interest...",
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    androidx.compose.foundation.text.BasicTextField(
                                        value = searchInterestText,
                                        onValueChange = { searchInterestText = it },
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 13.sp
                                        ),
                                        modifier = Modifier.fillMaxWidth().testTag("home_explore_search_input")
                                    )
                                }
                                if (searchInterestText.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { searchInterestText = "" }
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    if (searchInterestText.trim().isNotEmpty()) {
                                        viewModel.searchTeachersByInterest(searchInterestText.trim())
                                        viewModel.selectTab(AppTab.SearchRecommend)
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                    .testTag("home_explore_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowForward,
                                    contentDescription = "Search submit",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { isSearchExpanded = false },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Cancel search",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Display our 5 beautiful Main Categories in list of full-width premium cards
            mainCategoriesList.forEach { category ->
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    MainCategoryCard(
                        category = category,
                        onClick = {
                            screenBackstack = screenBackstack + NavigationState.CategoryDetail(category)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // ITEM 6: Beautifully integrated calendar section (My Week Calendar)
            item {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "My Week Calendar 🗓️",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Your personalized week schedule of RSVPed classes and live workshop circles.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    val calendarListState = rememberLazyListState()
                    val todayIndex = remember(calendarDays) { calendarDays.indexOfFirst { it.isToday } }
                    LaunchedEffect(todayIndex) {
                        if (todayIndex != -1) {
                            calendarListState.scrollToItem(maxOf(0, todayIndex - 2))
                        }
                    }

                    LazyRow(
                        state = calendarListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(26.dp))
                            .border(BorderStroke(1.dp, Color.White), RoundedCornerShape(26.dp))
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(calendarDays) { day ->
                            val isSelected = selectedCalendarDay == day.dateString
                            val hasBooking = bookingsList.any { b ->
                                val bTiming = b.timing.lowercase()
                                val dayKey = day.key.lowercase()
                                val dayNameFull = day.fullName.lowercase()
                                bTiming.contains(dayKey) || bTiming.contains(dayNameFull) ||
                                (day.isToday && bTiming.contains("today")) ||
                                (bTiming.contains("tomorrow") && calendarDays.indexOf(day) == (calendarDays.indexOfFirst { it.isToday } + 1) % calendarDays.size)
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(55.dp)
                                    .padding(horizontal = 2.dp)
                                    .shadow(
                                        elevation = if (isSelected) 3.dp else 0.dp,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .background(
                                        brush = if (isSelected) Brush.linearGradient(
                                            colors = listOf(Color(0xFFF8E7EE), Color(0xFFEADCF8)) // Blush Pink to Soft Lavender
                                        ) else if (day.isToday) Brush.linearGradient(
                                            colors = listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.8f))
                                        ) else Brush.linearGradient(
                                            colors = listOf(Color.Transparent, Color.Transparent)
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .border(
                                        width = if (day.isToday && !isSelected) 1.dp else 0.dp,
                                        color = if (day.isToday && !isSelected) Color(0xFFEADCF8) else Color.Transparent,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { selectedCalendarDay = day.dateString }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = day.key,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected || day.isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF2C2625) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                
                                Spacer(modifier = Modifier.height(6.dp))

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            color = if (isSelected) Color.White.copy(alpha = 0.5f)
                                                    else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.labelNum,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFF2C2625)
                                                else if (day.isToday) Color(0xFF705DA3)
                                                else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Event indicator dot (filled for bookings, lighter/empty for no bookings)
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            color = if (hasBooking) Color(0xFF8B1A1A) else Color.Gray.copy(alpha = 0.25f),
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Display events for the selected calendar day
                    val activeDayObject = calendarDays.find { it.dateString == selectedCalendarDay } ?: calendarDays[0]
                    val eventsForDay = getEventsForDay(activeDayObject, events)
                    val bookingsForDay = bookingsList.filter { b ->
                        val bTiming = b.timing.lowercase()
                        val dayKey = activeDayObject.key.lowercase()
                        val dayNameFull = activeDayObject.fullName.lowercase()
                        bTiming.contains(dayKey) || bTiming.contains(dayNameFull) ||
                        (activeDayObject.isToday && bTiming.contains("today")) ||
                        (bTiming.contains("tomorrow") && calendarDays.indexOf(activeDayObject) == (calendarDays.indexOfFirst { it.isToday } + 1) % calendarDays.size)
                    }

                    if (eventsForDay.isEmpty() && bookingsForDay.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📭", fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "No active bookings or events scheduled for ${activeDayObject.fullName}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // First display the 1:1 Booked lessons
                            bookingsForDay.forEach { item ->
                                val matchedExpert = experts.find { it.id == item.expertId }
                                val topicLower = (matchedExpert?.topic ?: "").lowercase()
                                
                                val (borderColor, emoji, displayTopic) = when {
                                    topicLower.contains("math") -> Triple(Color(0xFFAED6F1), "📐", matchedExpert?.topic ?: "Mathematics")
                                    topicLower.contains("garden") || topicLower.contains("plant") -> Triple(Color(0xFFA9DFBF), "🌳", matchedExpert?.topic ?: "Gardening")
                                    topicLower.contains("financ") || topicLower.contains("money") || topicLower.contains("tax") -> Triple(Color(0xFFF9E79F), "💰", matchedExpert?.topic ?: "Finance")
                                    topicLower.contains("well") || topicLower.contains("yoga") || topicLower.contains("health") -> Triple(Color(0xFFD7BDE2), "🧘", matchedExpert?.topic ?: "Wellness")
                                    topicLower.contains("legal") || topicLower.contains("law") -> Triple(Color(0xFFFAD7A0), "⚖️", matchedExpert?.topic ?: "Legal")
                                    else -> Triple(Color(0xFFD5D8DC), "🗓️", matchedExpert?.topic ?: "1:1 Session")
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(14.dp), clip = false)
                                        .clickable { viewModel.setEditingBooking(item) }
                                        .testTag("home_calendar_booking_${item.id}"),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .drawBehind {
                                                val strokeWidthPx = 3.dp.toPx()
                                                drawLine(
                                                    color = borderColor,
                                                    start = Offset(strokeWidthPx / 2f, 0f),
                                                    end = Offset(strokeWidthPx / 2f, this.size.height),
                                                    strokeWidth = strokeWidthPx
                                                )
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(borderColor.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(emoji, fontSize = 20.sp)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = if (displayTopic.contains("Session")) displayTopic else "$displayTopic Refresher Session",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (item.status == "Joined") {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .background(Color(0xFF4CAF50), CircleShape)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = item.timing,
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = "Mentor: ${item.expertName} (${item.durationMinutes}m • ${if (item.isVideo) "Video" else "Voice"})",
                                                fontSize = 12.sp,
                                                color = Color(0xFF8B1A1A),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = { viewModel.setEditingBooking(item) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Modify booking",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            val isJoinedState = item.status == "Joined"
                                            Button(
                                                onClick = { 
                                                    viewModel.joinBooking(item.id)
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isJoinedState) Color(0xFF2E7D32) else Color(0xFF8B1A1A),
                                                    contentColor = Color.White
                                                ),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier.height(36.dp).testTag("calendar_join_${item.id}")
                                            ) {
                                                Text(if (isJoinedState) "Joined ✓" else "Join", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // Followed by general events
                            eventsForDay.forEach { event ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(2.dp, RoundedCornerShape(28.dp))
                                    .clickable {
                                        screenBackstack = screenBackstack + NavigationState.EventDetail(event)
                                    }
                                    .testTag("dashboard_event_card_${event.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (event.isUserRsvped) SoftGreenCard
                                                     else PastelPeachCard
                                ),
                                shape = RoundedCornerShape(28.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (event.isUserRsvped) Color(0xFF88C999).copy(alpha = 0.4f)
                                            else Color(0xFFEAA28A).copy(alpha = 0.25f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            ) {
                                                Text(
                                                    text = event.type.uppercase(),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = event.communityName,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        if (event.isUserRsvped) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.CheckCircle, "RSVP Active", tint = Color(0xFF2ECC71), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Joined", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E8449))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = event.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = event.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            AvatarImage(name = event.hostName, size = 28)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = event.hostName,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Host / Teacher",
                                                    fontSize = 9.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = { viewModel.toggleEventRsvp(event.id) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (event.isUserRsvped) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                                                                 else MaterialTheme.colorScheme.primary
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text(
                                                text = if (event.isUserRsvped) "Leave Class ❌" else "RSVP & Attend 🗓️",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (event.isUserRsvped) MaterialTheme.colorScheme.onErrorContainer else Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
                }
            }
        }

    // Modal dialogue presenting the Sub-Experiences when a Main Category is clicked
    if (activeMainCategory != null && activeSelectedCategory == null) {
        val mainCat = activeMainCategory!!
        AlertDialog(
            onDismissRequest = { activeMainCategory = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(mainCat.bgGrad.last().copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(mainCat.emoji, fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = mainCat.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Select a sub-topic to explore:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(mainCat.subExperiences) { subExp ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        activeSubExperience = subExp
                                        activeSelectedCategory = subExp.originalCategoryName
                                        activeSubPage = subExp.defaultSubPage
                                        selectedSubCuisine = null
                                        selectedSubFestival = null
                                        selectedLanguageName = null
                                        selectedMusicCategory = null
                                        aiIngredients = ""
                                        aiRecipeResult = null
                                        isAiRecipeGenerating = false
                                        isVoiceRecipeRecording = false
                                    }
                                    .testTag("sub_experience_card_${subExp.name.lowercase().replace(" ", "_")}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(subExp.emoji, fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = subExp.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = subExp.desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Explore",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeMainCategory = null }) {
                    Text("Go Back", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 6.dp
        )
    }

    // Modal dialogue presenting exactly 2 Choices when a Category is chosen
    if (activeSelectedCategory != null) {
        val selectedCat = categoriesList.find { it.name == activeSelectedCategory }
        if (selectedCat != null) {
            // Extended custom categories state elements are now declared in outer scope

            if (activeSelectedCategory == "Emergency Help & First Aid") {
                AlertDialog(
                    onDismissRequest = { activeSelectedCategory = null },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedCat.emoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = selectedCat.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    text = {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text(
                                    text = "TAP TO INSTANTLY ACCESS EMERGENCY RESOURCES:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC0392B)
                                )
                            }

                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    val emergencyActions = listOf(
                                        Triple("🚑", "Ambulance Near Me", "Locate and dispatch nearest cardiac/general ambulance responder."),
                                        Triple("🏥", "Hospitals Near Me", "Route guidance to 24/7 emergency critical-care wards."),
                                        Triple("❤️", "First Aid Videos", "No-hands guide to CPR, choking maneuvers, and wound control."),
                                        Triple("📞", "Emergency Numbers", "Call National Help Desk, Ambulance (108), Elder Line (14567)."),
                                        Triple("📍", "Share My Location", "Automatically broadcast live GPS coordinates with family."),
                                        Triple("👨‍👩‍👧", "Notify Family", "Send high-priority urgent SMS safety alerts immediately.")
                                    )

                                    emergencyActions.forEach { action ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    when (action.second) {
                                                        "Ambulance Near Me" -> {
                                                            activeSubFeatureToast = "📢 Contacting Nearest Ambulance Dispatch... Sending your live GPS coordinates."
                                                        }
                                                        "Hospitals Near Me" -> {
                                                            activeSubFeatureToast = "🏥 Mapping route to nearest 24/7 Multi-speciality Hospital..."
                                                        }
                                                        "First Aid Videos" -> {
                                                            activeTutorialDialogText = "🔴 FIRST AID QUICK INSTRUCTION:\n\n1. CARDIO-PULMONARY RESUSCITATION (CPR): Place both hands in the center of the chest. Push hard and fast at 100-120 compressions per minute.\n\n2. CHOKING (HEIMLICH): Stand behind the person, wrap arms around waist, and make quick upward thrusts above the navel.\n\n3. WOUNDS & BLEEDING: Press a clean, sterile cloth firmly over the wound until blood stops."
                                                        }
                                                        "Emergency Numbers" -> {
                                                            activeTutorialDialogText = "📞 WISDOM BRIDGE HELPLINES:\n\n• National Emergency: 112\n• Ambulance Support: 108\n• Senior Citizens Helpline: 14567\n• Wisdom Bridge Core Care Team: 1800-WISDOM"
                                                        }
                                                        "Share My Location" -> {
                                                            activeSubFeatureToast = "📍 Location Shared: 12.9716° N, 77.5946° E. Broadcasted live GPS coordinates."
                                                        }
                                                        "Notify Family" -> {
                                                            activeSubFeatureToast = "📲 Urgent SOS Alert SMS sent to registered family members."
                                                        }
                                                    }
                                                },
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color(0xFFFDEDEC)
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, Color(0xFFFADBD8))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(action.first, fontSize = 28.sp)
                                                Spacer(modifier = Modifier.width(14.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = action.second,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF78281F)
                                                    )
                                                    Text(
                                                        text = action.third,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color(0xFF943126),
                                                        lineHeight = 13.sp
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.ArrowForward,
                                                    contentDescription = "Go",
                                                    tint = Color(0xFFC0392B),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { activeSelectedCategory = null }) {
                            Text("Go Back", fontWeight = FontWeight.Bold)
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    tonalElevation = 6.dp
                )
            } else {
                // Render the breathtaking custom full-screen experience overlay matching Image 2!
                val mainCat = activeMainCategory ?: mainCategoriesList.first()
                val subExp = activeSubExperience ?: mainCat.subExperiences.first()
                val themeColor = selectedCat.bgGrad.last()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .clickable(enabled = true) { /* consume taps */ }
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Header layout
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    activeSelectedCategory = null
                                    activeSubPage = null
                                    selectedLanguageName = null
                                    selectedMusicCategory = null
                                    selectedSubCuisine = null
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = themeColor
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Back to ${mainCat.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = themeColor,
                                modifier = Modifier.clickable {
                                    activeSelectedCategory = null
                                    activeSubPage = null
                                    selectedLanguageName = null
                                    selectedMusicCategory = null
                                    selectedSubCuisine = null
                                }
                            )
                        }

                        // Hero Card Image Banner
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    painter = painterResource(id = subExp.imageRes),
                                    contentDescription = subExp.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Black.copy(alpha = 0.2f),
                                                    Color.Black.copy(alpha = 0.75f)
                                                )
                                            )
                                        )
                                )
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "${subExp.emoji} ${subExp.name.uppercase()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = subExp.desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.9f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val hasCustomViews = listOf("Recipes & Traditions", "Languages", "Music", "Astrology & Astronomy", "Stories, Shlokas & Wisdom").contains(selectedCat.name)

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            if (!hasCustomViews) {
                                item {
                                    Text(
                                        text = "What would you like to explore?",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.DarkGray,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }

                                item {
                                    EngagementChoiceCard(
                                        title = "Learn From People",
                                        description = "Connect with qualified mentors, teachers and peers for personalized individual sessions.",
                                        icon = "👨‍🏫",
                                        onClick = {
                                            viewModel.selectTab(AppTab.Home)
                                            activeMainCategory = null
                                            activeSubExperience = null
                                            activeSelectedCategory = null
                                        }
                                    )
                                }

                                item {
                                    EngagementChoiceCard(
                                        title = "Join Community",
                                        description = "Be part of active discussion forums, share stories, and build lasting companionship.",
                                        icon = "👥",
                                        onClick = {
                                            viewModel.selectTab(AppTab.Communities)
                                            activeMainCategory = null
                                            activeSubExperience = null
                                            activeSelectedCategory = null
                                        }
                                    )
                                }

                                item {
                                    EngagementChoiceCard(
                                        title = "Live Gatherings",
                                        description = "Participate in real-time group conversations, yoga modules, and live vocal circles.",
                                        icon = "🎥",
                                        onClick = {
                                            viewModel.selectTab(AppTab.VoiceRooms)
                                            activeMainCategory = null
                                            activeSubExperience = null
                                            activeSelectedCategory = null
                                        }
                                    )
                                }
                            } else {
                                // Special Case 2: Recipes & Traditions Heritage Center
                                if (selectedCat.name == "Recipes & Traditions") {
                            if (activeSubPage == null) {
                                // Master Hub selection view
                                item {
                                    Text(
                                        text = "EXPLORE TRADITIONS & HERITAGE:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        val hubs = listOf(
                                            listOf("🍲", "Heritage Recipes", "Browse South Indian, North Indian, East, West, Veg & Vegan cuisines.", "recipes"),
                                            listOf("🎉", "Festivals & Sacred Rituals", "Explore Diwali, Onam, Pongal customs, rituals & folklore.", "festivals"),
                                            listOf("🤖", "Chachi's AI Recipe Assistant", "Enter simple kitchen ingredients to instantly design custom healthy dishes.", "ai_chef"),
                                            listOf("📚", "Cultural Food Chronicles", "Read traditional regional cooking histories and lore.", "blogs")
                                        )
                                        hubs.forEach { hub ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { activeSubPage = hub[3] },
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(hub[0], fontSize = 18.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = hub[1],
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = hub[2],
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                                        )
                                                    }
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowForward,
                                                        contentDescription = "Open",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // We also preserve the storytelling recorder!
                                item {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "RECORD CULTURAL STORIES:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                isVoiceRecipeRecording = !isVoiceRecipeRecording
                                                activeSubFeatureToast = if (isVoiceRecipeRecording) "🎙️ Story Recording Begun... Speak about your ancestral memories, food stories, or folklore secret!" else "✅ Voice story recorded successfully and preserved in the community archives!"
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isVoiceRecipeRecording) Color(0xFFFEE2E2) else Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(0.5.dp, if (isVoiceRecipeRecording) Color.Red else MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("🎙️", fontSize = 24.sp)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = if (isVoiceRecipeRecording) "Recording Story... [Tap to Stop]" else "Record Oral Lore Storytelling",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isVoiceRecipeRecording) Color.Red else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Preserve traditional stories for generations to come.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (isVoiceRecipeRecording) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.Red)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.FavoriteBorder,
                                                    contentDescription = "Core action",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Heritage circles preservation
                                item {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "JOIN LOCAL HERITAGE CIRCLES:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        val circles = listOf(
                                            Pair("🍳", "Traditional Cooking Circle"),
                                            Pair("🎉", "Festival Traditions Circle"),
                                            Pair("📖", "Storytelling Circle"),
                                            Pair("🌏", "Regional Culture Circle")
                                        )
                                        circles.forEach { circle ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        viewModel.setHomeSelectedTopic("Recipes & Traditions")
                                                        viewModel.selectTab(AppTab.Communities)
                                                        activeSelectedCategory = null
                                                        activeSubFeatureToast = "Welcome! You have coordinates into ${circle.second} group."
                                                    },
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)),
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(circle.first, fontSize = 20.sp)
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = circle.second,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    Text("Join", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // We are in a specific sub-page
                                when (activeSubPage) {
                                    "recipes" -> {
                                        if (selectedSubCuisine == null) {
                                            item {
                                                Text("Browse authentic heritage recipes by cuisine:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            }
                                            item {
                                                val cuisines = listOf(
                                                    Triple("🍲", "South Indian", "Steamed idli, sambar, rasam, and roasted delights."),
                                                    Triple("🫓", "North Indian", "Ghee parathas, comforting paneer, and rich lentil curries."),
                                                    Triple("🍀", "East Indian", "Traditional regional sweets, stews, and herbal preparations."),
                                                    Triple("🌾", "West Indian", "Dhokla, parathas, and light digestive recipes."),
                                                    Triple("🥦", "Vegetarian", "Healthy seasonal vegetable stews and moong dal classics."),
                                                    Triple("🥑", "Vegan", "Sattvik organic plant-based custom nourishing dishes."),
                                                    Triple("🍝", "Italian", "Whole wheat pastas with fresh garden basil and pestos."),
                                                    Triple("🌮", "Mexican", "Crispy maize tacos, black bean stews, and mild fresh salsa."),
                                                    Triple("🍵", "Continental", "Warm herbal soups, oven baked greens, and simple grain bowls."),
                                                    Triple("🍜", "Asian", "Stir-fried ginger vegetables and clear healthy noodle broths.")
                                                )
                                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    cuisines.forEach { cuisine ->
                                                        Card(
                                                            modifier = Modifier.fillMaxWidth().clickable { selectedSubCuisine = cuisine.second },
                                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                                            shape = RoundedCornerShape(10.dp),
                                                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                                        ) {
                                                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                                                Text(cuisine.first, fontSize = 20.sp)
                                                                Spacer(modifier = Modifier.width(10.dp))
                                                                Column(modifier = Modifier.weight(1f)) {
                                                                    Text(cuisine.second, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                                    Text(cuisine.third, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = Color.Gray)
                                                                }
                                                                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Go", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            // Showing individual cuisine recipes
                                            item {
                                                val recipes = when {
                                                    selectedSubCuisine!!.contains("South") -> listOf(
                                                        Pair("🥘 Chachi's Fluffy Idli & Sambar", "Fluffy steamed rice batter cakes from scratch, coupled with warm seasonal winter gourd lentils.\n\n• Step 1: Ferment natural black urad dal and split raw red skin rice for 8 hours.\n• Step 2: Steam cook for 12 minutes exactly.\n• Step 3: Serve with piping hot drumstick sambar.\n\n💡 Chachi's secret: Add 1/2 teaspoon fenugreek seeds while grinding urad for natural softness!"),
                                                        Pair("🍛 Crispy Urulai Fry Roast", "A sensory delight of small baby potatoes toasted slow with red skin. Easy and pleasant for all age groups.\n\n• Step 1: Boil hand-cut baby potatoes with salt & pinch of organic turmeric.\n• Step 2: Season frying pan with pure cold-pressed mustard oil, whole fenugreek seeds, curry sprigs.\n• Step 3: Roast baby potato quarters slow for 15 minutes.")
                                                    )
                                                    selectedSubCuisine!!.contains("North") -> listOf(
                                                        Pair("🫓 Ghee Aloo Paratha with Curd", "Unleavened soft flatbreads gently stuffed with home spiced soft potatoes.\n\n• Step 1: Stuff wheat dough with spiced mashed potato hash.\n• Step 2: Roll very light to avoid bursting edges.\n• Step 3: Toast with high-quality cow ghee on heavy cast iron tava.")
                                                    )
                                                    selectedSubCuisine!!.contains("East") -> listOf(
                                                        Pair("🍯 Traditional Sandesh Sweets", "A delicate and delightful dessert prepared with fresh chenna (cottage cheese).\n\n• Step 1: Knead absolute fresh chenna with organic date palm jaggery.\n• Step 2: Heat slow on a low flame for 5 minutes.\n• Step 3: Press into beautiful traditional molds.")
                                                    )
                                                    selectedSubCuisine!!.contains("West") -> listOf(
                                                        Pair("🌾 Classic Gujarati Dhokla", "Savory steamed gram flour cakes, fluffy, spongey, and incredibly light on the stomach.\n\n• Step 1: Mix chicksea flour batter with lemon juice & turmeric.\n• Step 2: Steam for 15 minutes and temper with mustard seeds and fresh curry sprigs.")
                                                    )
                                                    selectedSubCuisine!!.contains("Vegan") -> listOf(
                                                        Pair("🥑 Sattvik Sweet Potato & Coconut Stew", "Warm coconut milk based vegetable broth spiced with cumin and fresh ginger.\n\n• Step 1: Boil diced sweet potatoes & beans in thin coconut milk.\n• Step 2: Simmer with grated ginger, curry leaves, and a touch of cold-pressed coconut oil.")
                                                    )
                                                    selectedSubCuisine!!.contains("Italian") -> listOf(
                                                        Pair("🍝 Basil Pesto Whole Wheat Penne", "Wholesome organic pasta tossed in a sensory green basil pesto sauce.\n\n• Step 1: Blend fresh garden basil leaves, garlic cloves, parmigio/walnuts, and cold-pressed olive oil.\n• Step 2: Boil penne pasta with double sea salt and dress combined.")
                                                    )
                                                    selectedSubCuisine!!.contains("Mexican") -> listOf(
                                                        Pair("🌮 Loaded Black Bean Soft Tacos", "Warm maize soft flat shells loaded with slow simmered organic black beans.\n\n• Step 1: Simmer black beans with cumin, garlic and sweet onions.\n• Step 2: Place on soft corn tortillas, add fresh shredded lettuce, home curd cheese.")
                                                    )
                                                    selectedSubCuisine!!.contains("Continental") -> listOf(
                                                        Pair("🍲 Warm Roasted Pumpkin Broth", "Comforting creamy soup seasoned with rosemary and fresh thyme.\n\n• Step 1: Roast sweet pumpkin chunks in the oven with olive oil and garlic.\n• Step 2: Puree with hot vegetable stock, and finish with toasted pumpkin seeds on top.")
                                                    )
                                                    selectedSubCuisine!!.contains("Asian") -> listOf(
                                                        Pair("🍜 Ginger Garlic Veg Noodle Soup", "Very light clear broth with seasonal vegetables and thin glass noodles.\n\n• Step 1: Sauté grated ginger, garlic, and scallions.\n• Step 2: Add water, seasonal bok choy and tofu cubes, simmer with light soy sauce.")
                                                    )
                                                    else -> listOf(
                                                        Pair("🥦 Yellow Moong Dal Tadka", "Simple comforting yellow split-lentils, seasoned natively. Highly soothing for senior digestion!\n\n• Step 1: Cook yellow split moong with green chilies, grated ginger, and salt.\n• Step 2: Heat 1 tbsp pure cow ghee, add whole cumin seeds, asafoetida, dry red chili.\n• Step 3: Pour the smoking tadka over the simmered dal.")
                                                    )
                                                }
                                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                    recipes.forEach { rec ->
                                                        Card(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                                            elevation = CardDefaults.cardElevation(1.dp),
                                                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                                        ) {
                                                            Column(modifier = Modifier.padding(12.dp)) {
                                                                Text(rec.first, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                                Spacer(modifier = Modifier.height(4.dp))
                                                                Text(rec.second, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, lineHeight = 15.sp, color = Color(0xFF334155))
                                                                Spacer(modifier = Modifier.height(10.dp))
                                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                    Button(
                                                                        onClick = { activeSubFeatureToast = "Saved ${rec.first.substring(2)} to your wisdom collection! ❤️" },
                                                                        modifier = Modifier.weight(1f),
                                                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                                                    ) {
                                                                        Text("Save Recipe 💾", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    "festivals" -> {
                                        if (selectedSubFestival == null) {
                                            item {
                                                Text("Choose an Indian festival to learn recipes and traditions:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            }
                                            item {
                                                val festivals = listOf(
                                                    Pair("🪔", "Diwali Festival of Lights"),
                                                    Pair("🌾", "Ugadi New Year customs"),
                                                    Pair("🌸", "Onam Regional Sacred Feast"),
                                                    Pair("🐘", "Ganesh Chaturthi Fest"),
                                                    Pair("💃", "Navratri Divine Devotional"),
                                                    Pair("🥛", "Pongal Harvest Rituals"),
                                                    Pair("🎨", "Holi Festival of Colors")
                                                )
                                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    festivals.forEach { fest ->
                                                        Card(
                                                            modifier = Modifier.fillMaxWidth().clickable { selectedSubFestival = fest.second },
                                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                                            shape = RoundedCornerShape(10.dp),
                                                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                                        ) {
                                                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                                Text(fest.first, fontSize = 20.sp)
                                                                Spacer(modifier = Modifier.width(10.dp))
                                                                Text(fest.second, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                                Spacer(modifier = Modifier.weight(1f))
                                                                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Go", modifier = Modifier.size(14.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            item {
                                                val detailedFest = when {
                                                    selectedSubFestival!!.contains("Diwali") -> listOf(
                                                        Pair("🍲 Recipe", "Ghee Besan Ladoo: Slowly roast chickpea flour in molten ghee, fold with organic date palm jaggery, and roll hand size!"),
                                                        Pair("📿 Rituals", "Oil bathing (Ganga Snanam) at dawn, decorating entrance-ways with fresh colorful blossoms, and lighting clay ghee lamps (diyas)."),
                                                        Pair("🕯️ Traditions", "Decorating homes with colorful floral Rangolis, cleaning copper vessels, and performing family Lakshmi prayers."),
                                                        Pair("📚 Stories", "Honoring the return of Lord Rama to Ayodhya after 14 years. Illuminating paths signifies wisdom triumphing over darkness.")
                                                    )
                                                    selectedSubFestival!!.contains("Ugadi") -> listOf(
                                                        Pair("🍲 Recipe", "Ugadi Pachadi: Blend fresh neem blossoms, jaggery, tamarind, raw mango, chili, and salt representing 6 flavors of life!"),
                                                        Pair("📿 Rituals", "Chanting mango leaf ties on the doorway, reading the traditional Panchang, and sharing sweet puran-polis with neighbours."),
                                                        Pair("🕯️ Traditions", "Starting new ventures, family ledger blessings, and reviewing spiritual horoscopes."),
                                                        Pair("📚 Stories", "Symbolizes looking forward to destiny with equanimity (accepting joy, grief, anger, fear, surprise, and aversion alike).")
                                                    )
                                                    selectedSubFestival!!.contains("Onam") -> listOf(
                                                        Pair("🍲 Recipe", "Classic Ada Pradhaman Payasam: Soft rice flakes boiled with milk, fresh cardamom, raw solid jaggery, and fried coconut chips."),
                                                        Pair("📿 Rituals", "Arranging vibrant flower-carpets (Pookalam) on courtyard floors, clay deity setup, and massive banana leaf lunches."),
                                                        Pair("🕯️ Traditions", "Spectacular snake boat races, traditional Pulikali tiger dances, and intergenerational family reunions."),
                                                        Pair("📚 Stories", "Honoring the annual peaceful return of the loving, non-judgmental Emperor Mahabali to visit his citizens.")
                                                    )
                                                    selectedSubFestival!!.contains("Ganesh") -> listOf(
                                                        Pair("🍲 Recipe", "Steamed Cardamom Modak: Soft rice flour dumplings stuffed with sweet jaggery-coconut stuffing, cooked under slow steam."),
                                                        Pair("📿 Rituals", "Installation of beautiful eco-friendly clay Ganesha idols, daily morning/evening light aratis with incense and bells."),
                                                        Pair("🕯️ Traditions", "Vocal group bhajan singing, hand mridangam playing, and sharing local sweets."),
                                                        Pair("📚 Stories", "Celebrating the birth of Lord Ganesha, the remover of obstacles, who guides intelligence, art, and intergenerational wisdom.")
                                                    )
                                                    selectedSubFestival!!.contains("Navratri") -> listOf(
                                                        Pair("🍲 Recipe", "Nourishing Konda Kadalai Sundal: Hearty steamed brown chickpeas tossed with freshly grated coconut, mustard seeds, and curry leaves."),
                                                        Pair("📿 Rituals", "Arranging multi-tier artistic clay doll displays (Golu/Kolu), offering sacred red vermillion, and executing local folk dances."),
                                                        Pair("🕯️ Traditions", "Placing primary textbooks & tools under musical worship, celebrating Durga, Lakshmi, and Saraswati."),
                                                        Pair("📚 Stories", "The triumph of Goddess Durga over Mahishasura after nine days of strategic battle. Celebrates the power of feminine intelligence and protection.")
                                                    )
                                                    selectedSubFestival!!.contains("Pongal") -> listOf(
                                                        Pair("🍲 Recipe", "Sweet Sakkarai Pongal: Creamy rice, yellow moong lentils, sweetened organic jaggery, spiked with ghee roasted cashews."),
                                                        Pair("📿 Rituals", "Allowing freshly boiled milk and rice to spill over clay pots outdoors while chanting 'Pongalo Pongal!' to welcome harvest abundance."),
                                                        Pair("🕯️ Traditions", "Honoring Surya (the Sun God), and beautifully painting and feeding farm cattle as a token of deep gratitude."),
                                                        Pair("📚 Stories", "Ancient winter solstice festival thanking nature and cattle for constant agricultural nourishment.")
                                                    )
                                                    else -> listOf(
                                                        Pair("🍲 Recipe", "Holi Gujiya: Sweet hand-made flour pockets stuffed with roasted milk solids (khoya), coconut shreds, raisins, and cardamoms."),
                                                        Pair("📿 Rituals", "Chanting devotional songs around the Holika dahan bonfire at night, and scattering color powders."),
                                                        Pair("🕯️ Traditions", "Gathering with neighbors, singing classic folk thumris, and spraying organic herbal color waters."),
                                                        Pair("📚 Stories", "How young devotee Prahlad's faith triumphed over flame, demonstrating the final absolute protection of cosmic righteousness.")
                                                    )
                                                }
                                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                    detailedFest.forEach { section ->
                                                        Card(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEFDFB)),
                                                            border = BorderStroke(0.5.dp, Color(0xFFF5CBA7))
                                                        ) {
                                                            Column(modifier = Modifier.padding(12.dp)) {
                                                                Text(section.first, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFFBA4A00))
                                                                Spacer(modifier = Modifier.height(4.dp))
                                                                Text(section.second, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, lineHeight = 16.sp, color = Color(0xFF5D4037))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    "ai_chef" -> {
                                        item {
                                            Text(
                                                text = "Tell AI Chachi: \"What ingredients do I have?\"",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        item {
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                // Text field with placeholder
                                                OutlinedTextField(
                                                    value = aiIngredients,
                                                    onValueChange = { aiIngredients = it },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    placeholder = { Text("E.g., Rice, Tomato, Onion, Curd...", fontSize = 12.sp) },
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                                    )
                                                )

                                                // Quick Selection Preset Chips
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    val presets = listOf("Tomato", "Curd", "Rice", "Onion")
                                                    presets.forEach { pr ->
                                                        Box(
                                                            modifier = Modifier
                                                                .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                                                .border(0.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                                                                .clickable {
                                                                    aiIngredients = if (aiIngredients.isEmpty()) pr else "$aiIngredients, $pr"
                                                                }
                                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Text(pr, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))

                                                // Glowing consulting button
                                                Button(
                                                    onClick = {
                                                        isAiRecipeGenerating = true
                                                        aiRecipeResult = "GENERATING"
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text("Consult AI Chachi ✨", fontWeight = FontWeight.Bold)
                                                }

                                                // Simulating code generation with a clean loader
                                                if (isAiRecipeGenerating) {
                                                    LaunchedEffect(isAiRecipeGenerating) {
                                                        delay(1200)
                                                        isAiRecipeGenerating = false
                                                        val itemLower = aiIngredients.lowercase()
                                                        aiRecipeResult = if (itemLower.contains("rice") && itemLower.contains("curd")) {
                                                            "SOOTHING CURD RICE"
                                                        } else if (itemLower.contains("tomato") || itemLower.contains("onion")) {
                                                            "QUICK TOMATO ONION RASAM CHUTNEY"
                                                        } else {
                                                            "TRADITIONAL GOURMET KITCHEN KHICHDI"
                                                        }
                                                    }
                                                    Column(
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                                        Text("AI Chachi is thinking... 🧑‍🍳", fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp), color = Color.Gray)
                                                    }
                                                }

                                                if (aiRecipeResult != null && aiRecipeResult != "GENERATING") {
                                                    val parts = when (aiRecipeResult) {
                                                        "SOOTHING CURD RICE" -> listOf(
                                                            Pair("🍲 Recipe Suggestion", "AI Chachi's Soothing Creamy Curd Rice"),
                                                            Pair("📋 Step-by-Step Instructions", "1. Mash cooked soft rice carefully. \n2. Mix with fresh thick curd, grated ginger, and salt.\n3. Temper mustard seeds, curry leaves, and a pinch of asafoetida in hot ghee. Combine well."),
                                                            Pair("🎥 Cooking Video Hint", "Whisk curd with 2 tablespoons of warm milk before folding into rice to prevent curd from souring!"),
                                                            Pair("🍛 Alternative Dishes", "Comfort Lemon Rice, Tomato Pepper Rasam soup.")
                                                        )
                                                        "QUICK TOMATO ONION RASAM CHUTNEY" -> listOf(
                                                            Pair("🍲 Recipe Suggestion", "Sweet & Spicy Tomato Onion Relish"),
                                                            Pair("📋 Step-by-Step Instructions", "1. Coarsely chop tomatoes and onions.\n2. Sauté with mustard seeds, split skinless urad dal, and dry red chilies in cold pressed sesame oil.\n3. Blend with fresh curry leaves and a tiny piece of sweet jaggery."),
                                                            Pair("🎥 Cooking Video Hint", "Add the jaggery *only* at the end of grinding to perfectly balance the spicy sourness!"),
                                                            Pair("🍛 Alternative Dishes", "Thakkali Thokku, Spicy Tomato Rasam.")
                                                        )
                                                        else -> listOf(
                                                            Pair("🍲 Recipe Suggestion", "Healthy Intergenerational Moong Dal Khichdi"),
                                                            Pair("📋 Step-by-Step Instructions", "1. Wash and dry roast split yellow moong dal for 3 minutes.\n2. Pressure-cook with organic white rice and a pinch of hand ground turmeric.\n3. Temper with whole cumin seeds, black pepper, and pure cow ghee."),
                                                            Pair("🎥 Cooking Video Hint", "Drizzle a teaspoon of raw lemon juice over hot plate to instantly double the vitamin C value!"),
                                                            Pair("🍛 Alternative Dishes", "Comfort Yellow Lentil Dal, Plain Steamed Pongal.")
                                                        )
                                                    }

                                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Text("AI CHACHI'S SPECIAL SUGGESTIONS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                                        parts.forEach { part ->
                                                            Card(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                                                border = BorderStroke(0.5.dp, Color(0xFFBBF7D0))
                                                            ) {
                                                                Column(modifier = Modifier.padding(10.dp)) {
                                                                    Text(part.first, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                                                                    Spacer(modifier = Modifier.height(2.dp))
                                                                    Text(part.second, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, lineHeight = 15.sp, color = Color(0xFF1E293B))
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    "blogs" -> {
                                        item {
                                            Text("Region & Cuisine Cultural food blogs:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        }
                                        item {
                                            val blogs = listOf(
                                                Triple("🍲 South Indian", "The Magic of Cast-Iron Ghee Dhal", "History shows heavy iron pans retain healthy trace-elements. Slow wood fire dhal is tempered natively to produce exceptional digestive values for seniors."),
                                                Triple("🌾 Ayurvedic", "Regional Spice Pairings for Gut Health", "Curry leaves, black pepper and cumin combined correctly can improve nutrition absorption up to 40%. Learn historical principles of Indian spice combinations."),
                                                Triple("🎉 Festival", "Sweet Memories: Intergenerational Joy", "Sharing and preparing sweets like Besan Ladoo together can cure senior isolation and create lasting bonds between children and grandchildren."),
                                                Triple("🌏 Heritage", "Lost Rice Grains of Regional India", "Discover the rich nutrient profile and cultural stories of classic heritage grains such as Mapillai Samba, Karuppu Kavuni and Basmati.")
                                            )
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                blogs.forEach { blog ->
                                                    Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                                    ) {
                                                        Column(modifier = Modifier.padding(10.dp)) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                                ) {
                                                                    Text(blog.first, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                                }
                                                            }
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(blog.second, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            Text(blog.third, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, lineHeight = 15.sp, color = Color(0xFF475569))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Special Case 3: Languages Category
                        else if (selectedCat.name == "Languages") {
                            if (selectedLanguageName == null) {
                                item {
                                    Text(text = "SELECT ANY LANGUAGE TO COMMENCE:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                item {
                                    Text(text = "Popular Indian Languages:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                                }
                                item {
                                    val indians = listOf("Hindi 🕉️", "Kannada 🌾", "Tamil 🦚", "Telugu 🌱", "Malayalam 🌴", "Sanskrit 📖")
                                    val chunks = indians.chunked(3)
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        chunks.forEach { chunk ->
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                chunk.forEach { lang ->
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                                            .border(0.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                                            .clickable { selectedLanguageName = lang.substring(0, lang.length - 2).trim() }
                                                            .padding(vertical = 8.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(lang, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                                    }
                                                }
                                                if (chunk.size < 3) {
                                                    repeat(3 - chunk.size) {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                item {
                                    Text(text = "Global World Languages:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                                }
                                item {
                                    val globals = listOf("English 🇬🇧", "Spanish 🇪🇸", "French 🇫🇷", "German 🇩🇪", "Japanese 🇯🇵", "Mandarin Chinese 🇨🇳", "Arabic 🕌")
                                    val chunks = globals.chunked(3)
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        chunks.forEach { chunk ->
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                chunk.forEach { lang ->
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .background(Color(0xFFFDF2F8), RoundedCornerShape(8.dp))
                                                            .border(0.5.dp, Color(0xFFFBCFE8), RoundedCornerShape(8.dp))
                                                            .clickable {
                                                                selectedLanguageName = when {
                                                                    lang.contains("English") -> "English"
                                                                    lang.contains("Spanish") -> "Spanish"
                                                                    lang.contains("French") -> "French"
                                                                    lang.contains("German") -> "German"
                                                                    lang.contains("Japanese") -> "Japanese"
                                                                    lang.contains("Mandarin") -> "Mandarin Chinese"
                                                                    lang.contains("Arabic") -> "Arabic"
                                                                    else -> lang.substring(0, lang.length - 2).trim()
                                                                }
                                                            }
                                                            .padding(vertical = 8.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(lang, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9D174D))
                                                    }
                                                }
                                                if (chunk.size < 3) {
                                                    repeat(3 - chunk.size) {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Detailed language screen
                                item {
                                    Text("How would you like to engage with $selectedLanguageName?", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        val lopts = listOf(
                                            Triple("🗣", "Learn $selectedLanguageName", "Interactive flashcards to learn daily basics and simple greeting terms."),
                                            Triple("🤝", "Teach $selectedLanguageName", "List yourself as a language helper to help global students learn."),
                                            Triple("👥", "Join $selectedLanguageName Community", "Participate in localized text & resource exchange."),
                                            Triple("🎙", "Practice Speaking Live", "Enter live audio conversational voice rooms instantly.")
                                        )
                                        lopts.forEach { lopt ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    when {
                                                        lopt.second.startsWith("Learn") -> {
                                                            val quizText = when(selectedLanguageName) {
                                                                "Kannada" -> "LEARN KANNADA BASIC GREETINGS:\n\n• Hello: Namaskara (ನಮಸ್ಕಾರ)\n• How are you?: Hegiddiri? (ಹೇಗಿದ್ದೀರಿ?)\n• Thank you: Dhanyavadagalu (ಧನ್ಯವಾದಗಳು)\n• Welcome: Susvagata (ಸುಸ್ವಾಗತ)"
                                                                "Hindi" -> "LEARN HINDI BASIC GREETINGS:\n\n• Hello: Namaste (नमस्ते)\n• How are you?: Aap kaise hain? (आप कैसे हैं?)\n• Thank you: Dhanyavaad (धन्यवाद)\n• Welcome: Swagat hain"
                                                                "Spanish" -> "LEARN SPANISH BASIC GREETINGS:\n\n• Hello: Hola\n• How are you?: ¿Cómo estás?\n• Thank you: Gracias\n• Welcome: Bienvenido"
                                                                else -> "LEARN $selectedLanguageName BASIC GREETINGS:\n\n• Hello: Greeting term\n• How are you?: Basic inquiry\n• Thank you: Appreciation term\n• Enjoy learning new tongues!"
                                                            }
                                                            activeTutorialDialogText = quizText
                                                        }
                                                        lopt.second.startsWith("Teach") -> {
                                                            activeSubFeatureToast = "✅ Successfully listed! You are now set as an expert Language Mentor for $selectedLanguageName."
                                                        }
                                                        lopt.second.contains("Community") -> {
                                                            viewModel.setHomeSelectedTopic("Languages")
                                                            viewModel.selectTab(AppTab.Communities)
                                                            activeSelectedCategory = null
                                                            activeSubFeatureToast = "Welcome to the $selectedLanguageName Conversation Circle!"
                                                        }
                                                        else -> {
                                                            viewModel.setHomeSelectedTopic("Languages")
                                                            viewModel.selectTab(AppTab.VoiceRooms)
                                                            activeSelectedCategory = null
                                                            activeSubFeatureToast = "Connecting you to the live $selectedLanguageName vocal room..."
                                                        }
                                                    }
                                                },
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                            ) {
                                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(lopt.first, fontSize = 16.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(lopt.second, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                        Text(lopt.third, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = Color.Gray)
                                                    }
                                                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Go", modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Special Case 4: Music Category
                        else if (selectedCat.name == "Music") {
                            if (selectedMusicCategory == null) {
                                item {
                                    Text("SELECT MUSIC DISCOVERY BRANCH:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                item {
                                    val musics = listOf(
                                        Triple("🎵", "Indian Classical", "Carnatic & Hindustani classical lessons, ragas and talas."),
                                        Triple("📿", "Devotional", "Learn ancient protective Shlokas, Bhajans, Stotras, and Vedic chantings."),
                                        Triple("🎹", "Instrument Learning", "Hands-on instruction for Veena, Violin, Flute, Keyboard, Tabla, or Mridangam."),
                                        Triple("🎤", "Vocal Training", "Vocal exercise, breathing techniques, Classical vocals, and Devotional singing.")
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        musics.forEach { mus ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth().clickable { selectedMusicCategory = mus.second },
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                            ) {
                                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(mus.first, fontSize = 18.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(mus.second, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                        Text(mus.third, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = Color.Gray)
                                                    }
                                                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Go", modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Music details sub-screen
                                item {
                                    Text("Wisdom teachings for $selectedMusicCategory:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                item {
                                    val tracks = when (selectedMusicCategory) {
                                        "Indian Classical" -> listOf(
                                            Pair("🎼 Carnatic Music Lesson 1", "Discover Sarali Varisas scale basis in South Indian music (Raga Mayamalavagowla)."),
                                            Pair("🌸 Hindustani Raga Yaman Tutorial", "Calm and tranquil evening swara movements in Raga Yaman for peaceful focus.")
                                        )
                                        "Devotional" -> listOf(
                                            Pair("🕯️ Ancient Protective Shlokas", "Pronunciation basics of powerful daily shlokas for deep inner peace."),
                                            Pair("📿 Devotional Bhajans & Stotras", "Learn high-vibe classical bhajans, stotras, and protective Vedic chanting.")
                                        )
                                        "Instrument Learning" -> listOf(
                                            Pair("🪕 Veena & Violin Fingering", "Detailed finger placement instructions for Veena and Violin string control."),
                                            Pair("🪈 Flute Blowing & Mridangam Beats", "Blowing exercises on Flute, keyboard scales, Tabla, and Mridangam hand rolls.")
                                        )
                                        else -> listOf(
                                            Pair("🎤 Classial Vocals & Light Riyaz", "Daily vocal riyaz schedules to maintain clean pitch and breath control."),
                                            Pair("🎼 Devotional Singing & Light Music", "Simple breathing patterns and pitch training for light classical vocals.")
                                        )
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        tracks.forEach { t ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Text(t.first, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(t.second, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = Color.Gray)
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Button(
                                                            onClick = { activeSubFeatureToast = "🎙️ Simulating practicing lesson '${t.first}'... Recording voice feed to match perfect notes." },
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Text("Listen & Practice 🎧", fontSize = 10.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Special Case 5: Astrology & Astronomy
                        else if (selectedCat.name == "Astrology & Astronomy") {
                            item {
                                // Dynamic tabs to separate sections cleanly
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                                        .padding(4.dp)
                                ) {
                                    listOf("Astrology", "Astronomy").forEach { tab ->
                                        val active = astrologyCurrentTab == tab
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(
                                                    color = if (active) Color.White else Color.Transparent,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable { astrologyCurrentTab = tab }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (tab == "Astrology") "⭐ Vedic Astrology" else "🌌 Space Science",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (active) MaterialTheme.colorScheme.primary else Color.Gray
                                            )
                                        }
                                    }
                                }
                            }

                            if (astrologyCurrentTab == "Astrology") {
                                item {
                                    val astroItems = listOf(
                                        Triple("🧭", "Vedic Astrology & Panchang Calculations", "Access daily Vedic auspicious planetary alignments, sunrise/sunset, tithi, and nakshatras."),
                                        Triple("🕉️", "Horoscope Learning (Kundali)", "Sanskrit birth chart reading tutorials. Connect with elders to decode stellar positions."),
                                        Triple("📜", "Traditional Knowledge & Star Guidance", "Rooted spiritual principles, ancestral stories, and celestial wisdom guides.")
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        astroItems.forEach { item ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    if (item.second.startsWith("Interactive")) {
                                                        activeTutorialDialogText = "📅 INTEGRATED PANCHANG FOR TODAY:\n\n• Sunrise: 05:54 AM | Sunset: 06:42 PM\n• Tithi: Shukla Dashami\n• Nakshatra: Chitra Nakshatra\n• Auspicious Time (Abhijit): 11:53 AM - 12:44 PM\n• Rahu Kalam (Avoid actions): 03:00 PM - 04:30 PM\n\nPlan your auspicious tasks smoothly!"
                                                    } else {
                                                        activeSubFeatureToast = "✨ Opening ${item.second} module..."
                                                    }
                                                },
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                            ) {
                                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier.size(32.dp).background(Color(0xFFFAF5FF), RoundedCornerShape(6.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(item.first, fontSize = 16.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(item.second, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                        Text(item.third, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = Color.Gray)
                                                    }
                                                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Go", modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Astronomy section
                                item {
                                    val astroSci = listOf(
                                        Triple("🌌", "Space Science & Stars", "Identify Ursa Major, Orion, galactic systems, and Vedic astronomy positions side-by-side."),
                                        Triple("🪐", "Planetary Astronomy", "Science details of Mars rover exploration, Saturn ring alignments, and atmospheric chemistry."),
                                        Triple("🔭", "Telescopes & Sky Observation", "Helpful tools for sky stargazing, remote planetariums, and NASA cosmic gallery.")
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        astroSci.forEach { item ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    if (item.second.startsWith("Constellation")) {
                                                        activeTutorialDialogText = "🌌 DEEP STAR SEEKER GUIDE:\n\n• CURRENTLY VISIBLE Nakshatra: Chitra (located in the constellation Virgo / Spica).\n• ORION NEBULA: Sighted 1,344 light years away. Best viewed near south-western winter horizons at 9:00 PM.\n• MOON PHASE: Gibbous waxing (brightness: 81.4%).\n\nKeep your telescopes ready!"
                                                    } else {
                                                        activeSubFeatureToast = "🚀 Opening ${item.second} astronomical feed..."
                                                    }
                                                },
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                            ) {
                                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier.size(32.dp).background(Color(0xFFECFDF5), RoundedCornerShape(6.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(item.first, fontSize = 16.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(item.second, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                        Text(item.third, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = Color.Gray)
                                                    }
                                                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Go", modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        else if (selectedCat.name == "Health & Wellness" || selectedCat.name == "Yoga & Wellness" || selectedCat.name == "Ayurveda" || selectedCat.name == "Physiotherapy") {
                            item {
                                Text("WELLNESS PATHWAY MODALITIES:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            item {
                                val wellnessItems = listOf(
                                    Triple("🧘", "Yoga & Meditation", "Gentle posture holds, senior-safe stretching, joint flexibility, and breathing exercises."),
                                    Triple("🌿", "Ayurveda & Natural Care", "Natural Sattvik herb lists, body constitution guidance (Vata-Pitta-Kapha) and therapeutic oils."),
                                    Triple("🦵", "Physiotherapy & Physical Recovery", "Active joints pain management, senior safe muscle stretches and mobility enhancement exercises."),
                                    Triple("📘", "Wellness Education", "Learn cardiac care, healthy nutrition choices, light workouts and balance protection guides.")
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    wellnessItems.forEach { item ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth().clickable {
                                                if (item.second.startsWith("Yoga")) {
                                                    activeTutorialDialogText = "🧘 GENTLE YOGA & PATTERNS FOR ELDERS:\n\n1. Sukhasana Pranayama: Simple cross-legged sitting with focused deep belly breathing for 5 minutes.\n2. Tadasana (Palm Tree Pose): Gentle vertical stretch to correct absolute spine postures.\n3. Chair Bhujangasana: Backbend stretch supported safely by a study chair.\n\nKeep postures calm, safe and effortless!"
                                                } else if (item.second.startsWith("Ayurveda")) {
                                                    activeTutorialDialogText = "🌿 AYURVEDIC SATTVIK GUIDE:\n\n• Ginger Lemon Infusion: Boil fresh ginger slices for 10 minutes, cool slightly, add raw forest honey to boost respiratory digestion.\n• Golden Turmeric Milk: Warm cup of cow's milk with half teaspoon of organic turmeric and black pepper before bedtime for skeletal joint integrity."
                                                } else if (item.second.startsWith("Physiotherapy")) {
                                                    activeTutorialDialogText = "🦵 PHYSIOTHERAPY & POSTURE STRETCHES:\n\n• Wall Angels: Stand flat against wall, slide arms up and down 10 times to reinforce shoulder-blade stability.\n• Seated Ankle Pumps: Move feet up & down to trigger cardiovascular blood flow from calf muscles."
                                                } else {
                                                    activeSubFeatureToast = "✨ Opening ${item.second} learning files..."
                                                }
                                            },
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(item.first, fontSize = 18.sp)
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(item.second, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                                    Text(item.third, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = Color.Gray)
                                                }
                                                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Go", modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else if (selectedCat.name == "Stories, Shlokas & Wisdom" || selectedCat.name.contains("Stories") || selectedCat.name.contains("Wisdom")) {
                            item {
                                Text("SELECT STORY ORAL LORE DISCOVERY:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            item {
                                val storyItems = listOf(
                                    Triple("📖", "Sanskrit Stories & Heritage Tales", "Discover deep historical sanskrit moral values and rich classical stories."),
                                    Triple("🦊", "Panchatantra Animal Fables", "Traditional ancient fables teaching intelligence, friendship, and practical wisdom."),
                                    Triple("📿", "Devotional Stories of Saints", "Inspiring journeys of great Indian spiritual guides, saints, and cosmic protectors."),
                                    Triple("👶", "Story Time For Grandkids", "Gentle value-loaded bedtime and active afternoon stories to tell kids."),
                                    Triple("👵", "Grandparent Oral Lore circles", "Vocal group storytelling loops to share old memories and local histories.")
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    storyItems.forEach { item ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth().clickable {
                                                if (item.second.startsWith("Sanskrit")) {
                                                    activeTutorialDialogText = "📖 TIMELESS SANSKRIT MORAL STORY:\n\n• Story of the Golden Swan (Suvarnahamsa):\n\nA poor mother and her daughters were blessed by a golden-feathered swan. It gave them one feather at a time to support them. In her excitement and greed, the mother tried to plow all feathers at once, but they turned to coarse heron feathers! \n\nMoral: Satyameva Jayate — have patience and live in absolute contentment with what life naturally provides."
                                                } else if (item.second.contains("Panchatantra")) {
                                                    activeTutorialDialogText = "🦊 PANCHATANTRA STORY:\n\n• The Clever Monkey and the Crocodile:\n\nA monkey lived on a sweet jamun tree. A crocodile became his friend. In her greed, the crocodile's wife wanted to eat the monkey's heart. The monkey cleverly said he left his heart on the tree, and escaped on reaching the shore!\n\nMoral: Presence of mind and strategic intelligence can defuse any emergency."
                                                } else {
                                                    activeSubFeatureToast = "✨ Entering ${item.second} circle..."
                                                }
                                            },
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(item.first, fontSize = 18.sp)
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(item.second, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                                    Text(item.third, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = Color.Gray)
                                                }
                                                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Go", modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Universal section: How would you like to connect (hidden inside deeper sub-pages)
                        if (activeSubPage == null && selectedLanguageName == null && selectedMusicCategory == null) {
                            item {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "CHOOSE HOW TO ENGAGE",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }

                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val opts = listOf(
                                        Triple("👨‍🏫", "Learn From People", "Connect with friendly mentors, tutors & experts"),
                                        Triple("👥", "Join Community", "Be part of localized conversational groups"),
                                        Triple("🎙️", "Live Gatherings", "Join premium live vocal sessions & classes")
                                    )
                                    opts.forEach { opt ->
                                        val tag = when(opt.second) {
                                            "Learn From People" -> "learn_from_people_option"
                                            "Join Community" -> "join_community_option"
                                            else -> "live_gatherings_option"
                                        }
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.setHomeSelectedTopic(selectedCat.name)
                                                    val tab = when(opt.second) {
                                                        "Learn From People" -> AppTab.Home
                                                        "Join Community" -> AppTab.Communities
                                                        else -> AppTab.VoiceRooms
                                                    }
                                                    viewModel.selectTab(tab)
                                                    activeSelectedCategory = null
                                                }
                                                .testTag(tag),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                            shape = RoundedCornerShape(14.dp),
                                            border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(34.dp)
                                                        .background(Color.White, RoundedCornerShape(10.dp))
                                                        .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(opt.first, fontSize = 16.sp)
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = opt.second,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontSize = 12.5.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF0F172A)
                                                    )
                                                    Spacer(modifier = Modifier.height(1.dp))
                                                    Text(
                                                        text = opt.third,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontSize = 10.sp,
                                                        lineHeight = 13.sp,
                                                        color = Color(0xFF64748B)
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.ArrowForward,
                                                    contentDescription = "Navigate",
                                                    tint = Color(0xFF94A3B8),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                            }
                        }
                    }
                }
            }

            // Sub dialog for instructional Guides/Interactive Toasts
            if (activeSubFeatureToast != null) {
                AlertDialog(
                    onDismissRequest = { activeSubFeatureToast = null },
                    title = { Text("Information Center ℹ️", fontWeight = FontWeight.Bold) },
                    text = { Text(activeSubFeatureToast!!) },
                    confirmButton = {
                        Button(onClick = { activeSubFeatureToast = null }) {
                            Text("OK")
                        }
                    },
                    shape = RoundedCornerShape(20.dp)
                )
            }

            if (activeTutorialDialogText != null) {
                AlertDialog(
                    onDismissRequest = { activeTutorialDialogText = null },
                    title = { Text("Guidance & Instruction 📖", fontWeight = FontWeight.Bold) },
                    text = { Text(activeTutorialDialogText!!) },
                    confirmButton = {
                        Button(onClick = { activeTutorialDialogText = null }) {
                            Text("I Understand")
                        }
                    },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }

    // Payout cash collection alert banner dialog
    if (showPayoutSuccessAlert) {
        AlertDialog(
            onDismissRequest = { showPayoutSuccessAlert = false },
            title = { Text("Transfer Successful 💸", fontWeight = FontWeight.Bold) },
            text = { Text("Your tuition earnings have been securely debited and credited directly to your registered UPI accounts. Please check your bank notifications. Thank you for sharing your wonderful wisdom!") },
            confirmButton = {
                Button(
                    onClick = { showPayoutSuccessAlert = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Tutoring Booking successful alert dialog
    if (bookedSuccessExpertName != null) {
        AlertDialog(
            onDismissRequest = { bookedSuccessExpertName = null },
            title = { Text("Session Scheduled! 🗓️", fontWeight = FontWeight.Bold) },
            text = { Text("You booked a friendly intergenerational call with $bookedSuccessExpertName. They have been notified with love, and are looking forward to our session together! Your notifications/SMS will keep you updated.") },
            confirmButton = {
                Button(
                    onClick = { bookedSuccessExpertName = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Decline & Done", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    var queryText by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("Education") }
    var recordingActive by remember { mutableStateOf(false) }

    if (showQuickQuestionDialog) {
        AlertDialog(
            onDismissRequest = { 
                queryText = ""
                selectedCat = "Education"
                recordingActive = false
                showQuickQuestionDialog = false 
            },
            title = { Text("Ask the Community Circle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Type your question below or record your voice. Seniors find speaking comfortable!", fontSize = 14.sp)

                    TextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        placeholder = { Text("How can I do composting?") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Category: ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        val catList = listOf("Education", "Gardening", "Career", "Finance")
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            catList.forEach { cat ->
                                Button(
                                    onClick = { selectedCat = cat },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedCat == cat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(cat, fontSize = 11.sp, color = if (selectedCat == cat) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (recordingActive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondary.copy(
                                    alpha = 0.08f
                                ), RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = {
                                if (!recordingActive) {
                                    recordingActive = true
                                } else {
                                    recordingActive = false
                                    viewModel.postQuestion("Voice question snippet", selectedCat, 15)
                                    queryText = ""
                                    selectedCat = "Education"
                                    showQuickQuestionDialog = false
                                }
                            },
                            modifier = Modifier.testTag("record_voice_question_button")
                        ) {
                            Icon(
                                imageVector = if (recordingActive) Icons.Filled.StopCircle else Icons.Filled.Mic,
                                contentDescription = "Record question with Voice",
                                tint = if (recordingActive) Color.Red else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (recordingActive) "Stop & Send Voice (AI will transcribe)" else "Tap icon to Ask with Voice note",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            queryText = ""
                            selectedCat = "Education"
                            recordingActive = false
                            showQuickQuestionDialog = false
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (queryText.isNotBlank()) {
                                viewModel.postQuestion(queryText, selectedCat, null)
                                queryText = ""
                                selectedCat = "Education"
                                recordingActive = false
                                showQuickQuestionDialog = false
                            }
                        },
                        enabled = queryText.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.5f).testTag("submit_quick_question_btn")
                    ) {
                        Text("Post Question", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    selectedExpertForProfileState?.let { expert ->
            val followedExpertIds by viewModel.followedExpertIds.collectAsState()
            val isFollowed = followedExpertIds.contains(expert.id)
            val bookedSecondsFlow = viewModel.bookedSessionExpertIds.collectAsState()
            val isBooked = bookedSecondsFlow.value.contains(expert.id)

            WisdomProfileDetailView(
                expert = expert,
                isFollowed = isFollowed,
                bookedTime = if (isBooked) "Booked!" else null,
                privateQuestionsCount = 0,
                onClose = { selectedExpertForProfileState = null },
                onActionType = { type ->
                    if (type == "Follow_Trigger") {
                        viewModel.toggleFollowExpert(expert.id)
                    } else if (type == "Book") {
                        viewModel.openScheduler(expert.id)
                        selectedExpertForProfileState = null
                    } else {
                        activeActionExpertForHome = expert
                        activeActionTypeForHome = type
                    }
                },
                onFollowToggle = { viewModel.toggleFollowExpert(expert.id) }
            )
        }

    activeActionTypeForHome?.let { actionType ->
        activeActionExpertForHome?.let { expert ->
            ConnectActionDialog(
                expert = expert,
                actionType = actionType,
                onDismiss = {
                    activeActionTypeForHome = null
                    activeActionExpertForHome = null
                },
                onConfirmBooking = { chosenTime, duration, isVideo ->
                    viewModel.bookSessionWithExpert(expert.id, chosenTime, duration, isVideo)
                    activeActionTypeForHome = null
                    activeActionExpertForHome = null
                },
                onConfirmPrivateQuestion = { textQuestion ->
                    viewModel.submitPrivateQuestionToExpert(expert.id, textQuestion)
                    viewModel.startDirectChat(expert.id, textQuestion)
                    activeActionTypeForHome = null
                    activeActionExpertForHome = null
                    selectedExpertForProfileState = null
                }
            )
        }
    }
    }
}

// SCREEN 2: COMMUNITIES DIRECTORY

// -- REDESIGNED COMMUNITY FEED EXPERIENCE --

data class PremiumThreadedComment(
    val id: String,
    val authorName: String,
    val authorRole: String = "Member",
    val isVerifiedExpert: Boolean = false,
    val text: String,
    val timestamp: String,
    val childReplies: List<PremiumThreadedComment> = emptyList()
)

data class PremiumCommunityPost(
    val id: String,
    val communityId: String,
    val authorName: String,
    val authorRole: String = "Member",
    val authorTitle: String? = null,
    val isVerifiedExpert: Boolean = false,
    val timestamp: String = "Just now",
    val title: String,
    val description: String,
    val type: String = "Question", // Text, Photo, Question, Poll, Recipe, Story
    val coverGradient: List<Color> = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9)),
    val coverEmoji: String = "🌿",
    val likesCount: Int = 12,
    val commentsCount: Int = 3,
    val bookmarksCount: Int = 2,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val comments: List<PremiumThreadedComment> = emptyList(),
    val pollOptions: List<PremiumPollOption>? = null,
    val recipeDetails: PremiumRecipeDetails? = null
)

data class PremiumPollOption(
    val text: String,
    val votes: Int,
    val votedByMe: Boolean = false
)

data class PremiumRecipeDetails(
    val prepTime: String = "25 mins",
    val difficulty: String = "Easy",
    val ingredients: List<String> = emptyList()
)

object PremiumCirclesFeedStore {
    val postsState = mutableStateListOf<PremiumCommunityPost>()

    fun initIfEmpty() {
        if (postsState.isNotEmpty()) return

        postsState.addAll(listOf(
            PremiumCommunityPost(
                id = "post_gard_1",
                communityId = "comm_gardening",
                authorName = "Ramesh Kumar",
                authorRole = "Verified Mentor",
                authorTitle = "Retired Botany Professor & Host",
                isVerifiedExpert = true,
                timestamp = "2 hours ago",
                title = "How do I protect tomato plants during monsoon in Bangalore? 🍅⛈️",
                description = "My hybrid tomatoes in the balcony are starting to bear heavy fruit, but the early monsoon squalls are bruising the stems. Looking for budget-friendly windbreaks or vertical staking ideas suited for elders!",
                type = "Question",
                coverGradient = listOf(Color(0xFFD4E157), Color(0xFF689F38)),
                coverEmoji = "🌿",
                likesCount = 34,
                commentsCount = 2,
                bookmarksCount = 12,
                comments = listOf(
                    PremiumThreadedComment(
                        id = "c_gard_1_1",
                        authorName = "Mohan Rao",
                        authorRole = "Verified Teacher",
                        isVerifiedExpert = true,
                        text = "Ramesh, I draped transparent wraps over custom bamboo frames. It acts as an affordable mini shield against the heavy squalls!",
                        timestamp = "1 hour ago",
                        childReplies = listOf(
                            PremiumThreadedComment(
                                id = "cr_gard_1_1_reply",
                                authorName = "Ramesh Kumar",
                                authorRole = "Expert",
                                text = "Brilliant suggestion Mohan! I will set up bamboo pegs this afternoon.",
                                timestamp = "45 mins ago"
                            )
                        )
                    ),
                    PremiumThreadedComment(
                        id = "c_gard_1_2",
                        authorName = "Nisha Hegde",
                        authorRole = "Active Member",
                        text = "Also ensure the drainage hole isn't blocked by fine silt. Tomatoes hate standing water.",
                        timestamp = "30 mins ago"
                    )
                )
            ),
            PremiumCommunityPost(
                id = "post_cook_1",
                communityId = "comm_music", 
                authorName = "Saraswathi Bai",
                authorRole = "Verified Mentor",
                authorTitle = "Traditional Culinary Artist",
                isVerifiedExpert = true,
                timestamp = "3 hours ago",
                title = "Traditional Karnataka Recipes: Porous Mysore Pak Secrets! 🧈🍯",
                description = "Sharing my grandmother's heirloom recipe for organic melt-in-mouth Mysore Pak! The secret to getting those perfect porous layers is pouring extremely hot bubbling ghee over roasted besan at the exact sound peak. Ask me any cooking query below!",
                type = "Recipe",
                coverGradient = listOf(Color(0xFFFFF59D), Color(0xFFF57F17)),
                coverEmoji = "🍳",
                likesCount = 56,
                commentsCount = 1,
                bookmarksCount = 26,
                recipeDetails = PremiumRecipeDetails(
                    prepTime = "30 mins",
                    difficulty = "Medium",
                    ingredients = listOf("Gram Flour (Besan) - 1 Cup", "Pure Organic Ghee - 2 Cups", "Raw Honey/Sugar - 1.5 Cups", "Water - 1/2 Cup")
                ),
                comments = listOf(
                    PremiumThreadedComment(
                        id = "c_cook_1_1",
                        authorName = "Grandma Leela",
                        authorRole = "Circle Host",
                        text = "Saraswathi, your proportions are spot on! The sweet aroma carries so much nostalgia.",
                        timestamp = "2 hours ago"
                    )
                )
            ),
            PremiumCommunityPost(
                id = "post_story_1",
                communityId = "comm_grandparents",
                authorName = "Lakshmi Rao",
                authorRole = "Verified Mentor",
                authorTitle = "Verified Mathematics & Ethics Mentor",
                isVerifiedExpert = true,
                timestamp = "Yesterday",
                title = "How grandparents can help children learn core values 👵📖",
                description = "In this fast digital world, children are easily distracted by screens. Instead of dry lectures, I find telling 10-minute bedtime stories where characters solve trust or kind dilemmas works like absolute magic! What do you guys think?",
                type = "Story",
                coverGradient = listOf(Color(0xFFCE93D8), Color(0xFF6A1B9A)),
                coverEmoji = "👵",
                likesCount = 42,
                commentsCount = 1,
                bookmarksCount = 15,
                comments = listOf(
                    PremiumThreadedComment(
                        id = "c_story_1_1",
                        authorName = "Anand Shah",
                        authorRole = "Mentor",
                        text = "Bedtime stories create emotional bonds that no high-tech video game can ever replace, Lakshmi Ji. Highly agree!",
                        timestamp = "Yesterday"
                    )
                )
            ),
            PremiumCommunityPost(
                id = "post_fin_1",
                communityId = "comm_finance",
                authorName = "Priya Mehta",
                authorRole = "Verified Mentor",
                authorTitle = "Verified Pension Consultant",
                isVerifiedExpert = true,
                timestamp = "4 hours ago",
                title = "Retirement income planning tips & safe deposit guides 💰",
                description = "Should elders keep funds in standard Savings or Senior SCSS? Let's discuss high-yield secure post office options where returns are guaranteed! What are your absolute favorite tips?",
                type = "Question",
                coverGradient = listOf(Color(0xFF80CBC4), Color(0xFF00695C)),
                coverEmoji = "💰",
                likesCount = 68,
                commentsCount = 1,
                bookmarksCount = 31,
                comments = listOf(
                    PremiumThreadedComment(
                        id = "c_fin_1_1",
                        authorName = "Anand Shah",
                        authorRole = "Verified Expert",
                        isVerifiedExpert = true,
                        text = "SCSS currently offers 8.2% and is fully backed. I recommend distributing matching sums to bypass tax slab limits.",
                        timestamp = "3 hours ago"
                    )
                )
            )
        ))
    }

    fun toggleLikePost(postId: String) {
        val idx = postsState.indexOfFirst { it.id == postId }
        if (idx != -1) {
            val p = postsState[idx]
            val newLiked = !p.isLiked
            postsState[idx] = p.copy(
                isLiked = newLiked,
                likesCount = p.likesCount + (if (newLiked) 1 else -1)
            )
        }
    }

    fun toggleBookmarkPost(postId: String) {
        val idx = postsState.indexOfFirst { it.id == postId }
        if (idx != -1) {
            val p = postsState[idx]
            val newBookmarked = !p.isBookmarked
            postsState[idx] = p.copy(
                isBookmarked = newBookmarked,
                bookmarksCount = p.bookmarksCount + (if (newBookmarked) 1 else -1)
            )
        }
    }

    fun addCommentToPost(postId: String, text: String, authorName: String) {
        val idx = postsState.indexOfFirst { it.id == postId }
        if (idx != -1) {
            val p = postsState[idx]
            val newComment = PremiumThreadedComment(
                id = java.util.UUID.randomUUID().toString(),
                authorName = authorName,
                authorRole = "Active Member",
                text = text,
                timestamp = "Just now"
            )
            postsState[idx] = p.copy(
                comments = p.comments + newComment,
                commentsCount = p.commentsCount + 1
            )
        }
    }

    fun addReplyToComment(postId: String, commentId: String, text: String, authorName: String) {
        val idx = postsState.indexOfFirst { it.id == postId }
        if (idx != -1) {
            val p = postsState[idx]
            val updatedComments = p.comments.map { comment ->
                if (comment.id == commentId) {
                    val newReply = PremiumThreadedComment(
                        id = java.util.UUID.randomUUID().toString(),
                        authorName = authorName,
                        authorRole = "Active Member",
                        text = text,
                        timestamp = "Just now"
                    )
                    comment.copy(childReplies = comment.childReplies + newReply)
                } else comment
            }
            postsState[idx] = p.copy(
                comments = updatedComments,
                commentsCount = p.commentsCount + 1
            )
        }
    }
}

fun getSortedPosts(communityId: String?, sortBy: String): List<PremiumCommunityPost> {
    val filtered = if (communityId != null) {
        PremiumCirclesFeedStore.postsState.filter { it.communityId == communityId }
    } else {
        PremiumCirclesFeedStore.postsState
    }

    return when (sortBy) {
        "Recent" -> filtered.sortedByDescending { it.timestamp.contains("now") || it.timestamp.contains("min") || it.timestamp.contains("hour") }
        "Trending" -> filtered.sortedByDescending { it.likesCount + it.commentsCount * 3 }
        "Most Helpful" -> filtered.sortedByDescending { it.bookmarksCount }
        "Expert Answers" -> filtered.filter { it.comments.any { comment -> comment.isVerifiedExpert } }
        else -> filtered
    }
}

fun getChachiSummary(postTitle: String): String {
    return when {
        postTitle.contains("tomato", true) -> {
            "Ramesh beta, here is what your AI Chachi understood about this beautiful Gardening discussion:\n\n" +
            "• **The Core Solution**: Mohan suggests draping transparent wraps over custom bamboo frames. It acts as an affordable mini shield against the Bangalore monsoons.\n" +
            "• **Soil Drainage**: Nisha gives prime advice to clear the fine yellow silt blockages. Do not let roots sit in soggy mud!\n\n" +
            "👵 *Chachi says: 'A healthy garden is a healthy home, beta! Keep your hands in the warm soil, it keeps our hearts active and young.'*"
        }
        postTitle.contains("Recipes", true) || postTitle.contains("Mysore Pak", true) -> {
            "Saraswathi beta, your AI Chachi has read through this mouthwatering thread:\n\n" +
            "• **The Ghee Secret**: The porous honeycomb layers in authentic Mysore Pak happen purely when bubbling hot ghee is poured exactly at the peak roasting of besan.\n" +
            "• **Community Love**: Grandma Leela is already excited to try it for Ganesh Chaturthi!\n\n" +
            "👵 *Chachi says: 'Traditional food is the language of love. Bless you for preserving your maternal grandmother's authentic standards!'*"
        }
        postTitle.contains("moral values", true) -> {
            "Lakshmi beta, your AI Chachi is deeply touched by this storytelling discussion:\n\n" +
            "• **Power of Stories**: Bedtime story sessions created with emotional values foster intergenerational warmth, replacing screen-noise.\n" +
            "• **Grandpa's Role**: Anand has already started a 15-minute screen-free bedtime routine, and his grandchildren are loving it!\n\n" +
            "👵 *Chachi says: 'Our life wisdom is the best treasure we can bequeath to our heirs. Continue telling these stories, my child!'*"
        }
        postTitle.contains("Retirement", true) || postTitle.contains("income", true) -> {
            "Anand beta, your AI Chachi has summarized your Pension tips list:\n\n" +
            "• **SCSS is Top**: SCSS offers a secure 8.2% backed by the government, far outperforming standard savings rates.\n" +
            "• **Smart Exemptions**: Priya points out that distributing investments smartly bypasses high quarterly TDS brackets.\n\n" +
            "👵 *Chachi says: 'Money well guarded is peace well earned. Never let financial jargon worry you. Chachi is always here to simplify with pure love.'*"
        }
        else -> {
            "Beta, your AI Chachi has read the active posts in this gorgeous circle:\n\n" +
            "• This community is sharing wonderful advice, asking important questions, and keeping intergenerational wisdom fully alive!\n\n" +
            "👵 *Chachi says: 'A house that meets to talk and share experiences is a house decorated with absolute happiness. Bless you all!'*"
        }
    }
}

@Composable
fun CreatePostDialog(
    currentCommunityId: String?,
    onDismiss: () -> Unit,
    onCreated: (PremiumCommunityPost) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var chosenType by remember { mutableStateOf("Text") } // Text, Photo, Question, Poll, Recipe, Story
    var selectedCommId by remember { mutableStateOf(currentCommunityId ?: "comm_gardening") }

    val postTypes = listOf("Text", "Photo", "Question", "Poll", "Recipe", "Story")
    val communitiesList = listOf(
        "comm_gardening" to "Gardening 🌿",
        "comm_music" to "Traditional Cooking 🎵",
        "comm_grandparents" to "Grandparents 👵",
        "comm_finance" to "Finance 💰",
        "comm_teachers" to "Teachers 🏫"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        val newPost = PremiumCommunityPost(
                            id = java.util.UUID.randomUUID().toString(),
                            communityId = selectedCommId,
                            authorName = "Shreya Iyer",
                            authorRole = "Active Member",
                            isVerifiedExpert = false,
                            timestamp = "Just now",
                            title = title,
                            description = description,
                            type = chosenType,
                            coverGradient = when(chosenType) {
                                "Photo" -> listOf(Color(0xFF80CBC4), Color(0xFF00695C))
                                "Recipe" -> listOf(Color(0xFFFFF59D), Color(0xFFF57F17))
                                "Story" -> listOf(Color(0xFFCE93D8), Color(0xFF6A1B9A))
                                else -> listOf(Color(0xFFECEFF1), Color(0xFF78909C))
                            },
                            coverEmoji = when(chosenType) {
                                "Photo" -> "📸"
                                "Recipe" -> "🍳"
                                "Story" -> "📖"
                                "Question" -> "❓"
                                "Poll" -> "📊"
                                else -> "📝"
                            },
                            likesCount = 0,
                            commentsCount = 0,
                            bookmarksCount = 0,
                            pollOptions = if (chosenType == "Poll") listOf(
                                PremiumPollOption("I agree completely!", 0),
                                PremiumPollOption("Let's discuss further.", 0)
                            ) else null,
                            recipeDetails = if (chosenType == "Recipe") PremiumRecipeDetails("15 mins", "Easy", listOf("Ingredients item 1", "Ingredients item 2")) else null
                        )
                        onCreated(newPost)
                    }
                },
                enabled = title.isNotBlank() && description.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Publish Post", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontWeight = FontWeight.Medium)
            }
        },
        title = {
            Text("✍️ Create a Beautiful Post", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF0F172A))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Share your wisdom, ask a neighborhood question, or post a lovely local story with friends.",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )

                if (currentCommunityId == null) {
                    Column {
                        Text("Select Circle", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            communitiesList.forEach { (id, label) ->
                                val isSelected = selectedCommId == id
                                Button(
                                    onClick = { selectedCommId = id },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF1F5F9)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(label, fontSize = 10.sp, color = if (isSelected) Color.White else Color(0xFF334155), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Column {
                    Text("Select Post Style", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        postTypes.forEach { type ->
                            val isSelected = chosenType == type
                            Button(
                                onClick = { chosenType = type },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF1F5F9)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(type, fontSize = 10.sp, color = if (isSelected) Color.White else Color(0xFF334155), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Post Title (e.g. Best tea compost tips)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("What would you like to share? Speak from your experience.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun AIChachiSummaryDialog(
    postTitle: String,
    onDismiss: () -> Unit
) {
    var loadingFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1400)
        loadingFinished = true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Dhanyavaad Chachi! 🙏", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👵 AI Chachi's Wise Corner", fontWeight = FontWeight.ExtraBold, color = Color(0xFFB45309))
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!loadingFinished) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = Color(0xFFD97706), modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Elders' Chachi is reading this thread with maternal care... 👵✨",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFFB45309)
                    )
                } else {
                    Text(
                        text = getChachiSummary(postTitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF513813)
                    )
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color(0xFFFFFBEB),
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun CommunitiesDirectoryScreen(
    viewModel: AgeNoBarViewModel,
    onCommunitySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val communities by viewModel.communities.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryTab by remember { mutableStateOf("All") }
    var activeSubTab by remember { mutableStateOf("Neighbor Feed") } // "Neighbor Feed", "Discover Circles"
    var sortBy by remember { mutableStateOf("Recent") }

    var showCreatePost by remember { mutableStateOf(false) }
    var summarizingPostTitle by remember { mutableStateOf<String?>(null) }
    var showWhatsAppShareToast by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        PremiumCirclesFeedStore.initIfEmpty()
    }

    val categories = listOf("All", "Education", "Finance", "Family", "Interests", "Music")

    val filteredCommunities = communities.filter {
        (selectedCategoryTab == "All" || it.category == selectedCategoryTab) &&
                (it.name.contains(searchQuery, true) || it.description.contains(searchQuery, true))
    }

    val posts = PremiumCirclesFeedStore.postsState
    val feedToDisplay = getSortedPosts(null, sortBy)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF2F5F9),
                        Color(0xFFE7EDF4),
                        Color(0xFFDEE5EE)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(12.dp))

            // Dual Tab Selector mimicking premium Facebook Groups/Nextdoor Experience!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Neighbor Feed", "Discover Circles").forEach { tab ->
                    val isSelected = activeSubTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { activeSubTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Text(
                                text = if (tab == "Neighbor Feed") "💬 " else "🏫 ",
                                fontSize = 14.sp
                            )
                            Text(
                                text = tab,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF475569)
                            )
                        }
                    }
                }
            }

            if (activeSubTab == "Neighbor Feed") {
                // Header with Sorting Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Neighborhood Discussions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Senior-moderated warm activity stream",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Sort button / dropdown
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.8f))
                            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sort: $sortBy",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                sortBy = when(sortBy) {
                                    "Recent" -> "Trending"
                                    "Trending" -> "Most Helpful"
                                    "Most Helpful" -> "Expert Answers"
                                    else -> "Recent"
                                }
                            }
                        )
                    }
                }

                // Active scrollable feed
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp)
                ) {
                    items(feedToDisplay, key = { it.id }) { post ->
                        var isCommentsExpanded by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 10.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    clip = false,
                                    ambientColor = Color.Black.copy(alpha = 0.04f),
                                    spotColor = Color.Black.copy(alpha = 0.06f)
                                ),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column {
                                // Large header cover image block if specified
                                if (post.type == "Photo" || post.type == "Recipe" || post.type == "Story") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .background(Brush.linearGradient(post.coverGradient)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = post.coverEmoji,
                                            fontSize = 54.sp,
                                            modifier = Modifier.alpha(0.85f)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f))
                                                    )
                                                )
                                        )
                                        Text(
                                            text = post.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(14.dp),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // Author panel with Verified constraints
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    AvatarImage(name = post.authorName, size = 44)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = post.authorName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF1E293B)
                                            )
                                            if (post.isVerifiedExpert) {
                                                Icon(
                                                    imageVector = Icons.Filled.Verified,
                                                    contentDescription = "Verified expert Blue Badge",
                                                    tint = Color(0xFF3B82F6),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Surface(
                                                    color = Color(0xFFEFF6FF),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "EXPERT",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF1D4ED8),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        if (post.authorTitle != null) {
                                            Text(
                                                text = post.authorTitle,
                                                fontSize = 11.sp,
                                                color = Color(0xFF475569),
                                                fontWeight = FontWeight.Medium
                                            )
                                        } else {
                                            Text(
                                                text = post.authorRole,
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                        Text(
                                            text = post.timestamp,
                                            fontSize = 9.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                // Title and description (Not duplicated if photo cover is drawn)
                                if (post.type != "Photo" && post.type != "Recipe" && post.type != "Story") {
                                    Text(
                                        text = post.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }

                                Text(
                                    text = post.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )

                                // Conditional Poll component
                                if (post.pollOptions != null) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        post.pollOptions.forEach { opt ->
                                            val votedByMe = opt.votedByMe
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clickable {
                                                        PremiumCirclesFeedStore.toggleLikePost(post.id) // increments interaction
                                                    },
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (votedByMe) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
                                                ),
                                                border = BorderStroke(1.dp, if (votedByMe) Color(0xFF93C5FD) else Color(0xFFE2E8F0))
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(opt.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Text("${opt.votes} votes", fontSize = 11.sp, color = Color(0xFF64748B))
                                                }
                                            }
                                        }
                                    }
                                }

                                // Interactive AI Chachi Summarizing trigger
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .background(Color(0xFFFEF3C7).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
                                        .clickable { summarizingPostTitle = post.title }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("👵", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Ask AI Chachi to Summarize Discussion",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFB45309)
                                    )
                                }

                                HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(top = 8.dp))

                                // Social Actions row (Like, Comment, Save, Share) with generous interactive bounds
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Like Action
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { PremiumCirclesFeedStore.toggleLikePost(post.id) }
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = "Like item click",
                                            tint = if (post.isLiked) Color(0xFFEF4444) else Color(0xFF64748B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "${post.likesCount}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (post.isLiked) Color(0xFFEF4444) else Color(0xFF1E293B)
                                        )
                                    }

                                    // Comment Action
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { isCommentsExpanded = !isCommentsExpanded }
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.ChatBubbleOutline,
                                            contentDescription = "Comments toggle",
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "${post.commentsCount}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                    }

                                    // Save Action
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { PremiumCirclesFeedStore.toggleBookmarkPost(post.id) }
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (post.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = "Save post toggle",
                                            tint = if (post.isBookmarked) Color(0xFFF59E0B) else Color(0xFF64748B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "${post.bookmarksCount}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (post.isBookmarked) Color(0xFFF59E0B) else Color(0xFF1E293B)
                                        )
                                    }

                                    // Share Action
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { showWhatsAppShareToast = true }
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Share,
                                            contentDescription = "WhatsApp share element",
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                    }
                                }

                                // Interactive Inline Threaded Comments Section
                                if (isCommentsExpanded) {
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF8FAFC))
                                            .padding(14.dp)
                                    ) {
                                        Text(
                                            text = "Comments Thread (" + post.comments.size + ")",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569),
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )

                                        post.comments.forEach { comment ->
                                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                                Row(verticalAlignment = Alignment.Top) {
                                                    AvatarImage(name = comment.authorName, size = 32)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            Text(comment.authorName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E293B))
                                                            if (comment.isVerifiedExpert) {
                                                                Icon(Icons.Filled.Verified, "Verified Expert Badge Check", tint = Color(0xFF3B82F6), modifier = Modifier.size(12.dp))
                                                            }
                                                            Text("• " + comment.timestamp, fontSize = 9.sp, color = Color(0xFF94A3B8))
                                                        }
                                                        Text(comment.text, fontSize = 12.sp, color = Color(0xFF334155))

                                                        // Inline nested replies toggle triggers
                                                        var showReplyInput by remember { mutableStateOf(false) }
                                                        var replyText by remember { mutableStateOf("") }

                                                        Text(
                                                            text = "Reply",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier
                                                                .clickable { showReplyInput = !showReplyInput }
                                                                .padding(vertical = 2.dp)
                                                        )

                                                        if (showReplyInput) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                OutlinedTextField(
                                                                    value = replyText,
                                                                    onValueChange = { replyText = it },
                                                                    placeholder = { Text("Write reply...", fontSize = 11.sp) },
                                                                    modifier = Modifier.weight(1f).height(44.dp),
                                                                    shape = RoundedCornerShape(10.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                IconButton(
                                                                    onClick = {
                                                                        if (replyText.isNotBlank()) {
                                                                            PremiumCirclesFeedStore.addReplyToComment(post.id, comment.id, replyText, "Shreya Iyer")
                                                                            replyText = ""
                                                                            showReplyInput = false
                                                                        }
                                                                    },
                                                                    modifier = Modifier.size(36.dp)
                                                                ) {
                                                                    Icon(Icons.Filled.Send, "Send replies icon", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                                }
                                                            }
                                                        }

                                                        // Nested replies list
                                                        comment.childReplies.forEach { sub ->
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp, start = 14.dp),
                                                                verticalAlignment = Alignment.Top
                                                            ) {
                                                                AvatarImage(name = sub.authorName, size = 24)
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Column {
                                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                        Text(sub.authorName, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1E293B))
                                                                        Text("• " + sub.timestamp, fontSize = 8.sp, color = Color(0xFF94A3B8))
                                                                    }
                                                                    Text(sub.text, fontSize = 11.sp, color = Color(0xFF475569))
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Form inline to comment directly
                                        var newCommentText by remember { mutableStateOf("") }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = newCommentText,
                                                onValueChange = { newCommentText = it },
                                                placeholder = { Text("Write inline comment...", fontSize = 12.sp) },
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.weight(1f).height(48.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            IconButton(
                                                onClick = {
                                                    if (newCommentText.isNotBlank()) {
                                                        PremiumCirclesFeedStore.addCommentToPost(post.id, newCommentText, "Shreya Iyer")
                                                        newCommentText = ""
                                                    }
                                                },
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                            ) {
                                                Icon(Icons.Filled.Send, "Send comment text option", tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // DISCOVER CIRCLES TAB
                // Search Circle bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Retired Teachers, Gardening tips...") },
                    leadingIcon = { Icon(Icons.Filled.Search, "Search icon design") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("community_search_bar"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // Filter horizontal Category scroll list
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategoryTab == cat
                        Button(
                            onClick = { selectedCategoryTab = cat },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                            ),
                            border = BorderStroke(1.dp, if (isSelected) Color.Transparent else Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else Color(0xFF334155),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Grid list of Circles with cover images, soft shadows and clean glass elements!
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    if (filteredCommunities.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = "Empty state icon info",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No direct Circles match search criteria.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(filteredCommunities) { community ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCommunitySelected(community.id) }
                                    .shadow(
                                        8.dp,
                                        shape = RoundedCornerShape(24.dp),
                                        clip = false,
                                        ambientColor = Color.Black.copy(alpha = 0.03f),
                                        spotColor = Color.Black.copy(alpha = 0.05f)
                                    )
                                    .testTag("circle_card_${community.id}"),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column {
                                    // Custom cover background gradient according to category!
                                    val (gradient, emoji) = when (community.id) {
                                        "comm_gardening" -> listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9)) to "🌿"
                                        "comm_music" -> listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3)) to "🍳"
                                        "comm_grandparents" -> listOf(Color(0xFFF3E5F5), Color(0xFFE1BEE7)) to "👵"
                                        "comm_finance" -> listOf(Color(0xFFE0F2F1), Color(0xFFB2DFDB)) to "💰"
                                        "comm_teachers" -> listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)) to "🏫"
                                        else -> listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB)) to "👋"
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(96.dp)
                                            .background(Brush.linearGradient(gradient)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emoji, fontSize = 44.sp)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.15f))))
                                        )
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = community.category,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = community.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${community.memberCount} active grandparents & neighbors",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = community.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF334155),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.Security, "Secure mentor icon", modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.secondary)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Verified Hosts • " + (community.moderators.firstOrNull() ?: "Mentor"),
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF475569),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Button(
                                                onClick = { onCommunitySelected(community.id) },
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("Join Feed →", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to instantly Create Post with Text/Photo/Poll/Recipe/Story
        FloatingActionButton(
            onClick = { showCreatePost = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 16.dp)
                .testTag("create_community_post_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Add, "Plus icon")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create Post", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Dialogs management
        if (showCreatePost) {
            CreatePostDialog(
                currentCommunityId = null,
                onDismiss = { showCreatePost = false },
                onCreated = { post ->
                    PremiumCirclesFeedStore.postsState.add(0, post)
                    showCreatePost = false
                }
            )
        }

        summarizingPostTitle?.let { postTitle ->
            AIChachiSummaryDialog(
                postTitle = postTitle,
                onDismiss = { summarizingPostTitle = null }
            )
        }

        if (showWhatsAppShareToast) {
            AlertDialog(
                onDismissRequest = { showWhatsAppShareToast = false },
                confirmButton = {
                    Button(onClick = { showWhatsAppShareToast = false }) {
                        Text("Wonderful!")
                    }
                },
                title = { Text("WhatsApp Family Share", fontWeight = FontWeight.Bold) },
                text = { Text("Discussion link has been copied successfully!\n\nShare this beautiful wisdom with your grandchildren and children on WhatsApp. 💚", fontSize = 14.sp) },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

// SCREEN 3 & 4: COMMUNITY HOME & ITS FEEDS/RESOURCES
@Composable
fun CommunityHomeScreen(
    viewModel: AgeNoBarViewModel,
    communityId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val communities by viewModel.communities.collectAsState()
    val events by viewModel.events.collectAsState()
    val recordingActive by viewModel.recordingVoiceNote.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    val community = communities.firstOrNull { it.id == communityId } ?: return

    var innerTab by remember { mutableStateOf("Chat & Feed") } // "Chat & Feed", "Events", "Resources", "Members"
    val innerTabs = listOf("Chat & Feed", "Events", "Resources", "Members")

    var chatText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("community_back_button")) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Go back", tint = MaterialTheme.colorScheme.primary)
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(community.iconEmoji, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = community.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${community.memberCount} members • Live voice rooms enabled",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }

            // Quick button to join or host voice room associated with this circle!
            IconButton(
                onClick = {
                    // Match a room based on details
                    val roomMap = mapOf(
                        "comm_teachers" to "vr_english",
                        "comm_finance" to "vr_career",
                        "comm_grandparents" to "vr_storytime",
                        "comm_gardening" to "vr_gardening",
                        "comm_music" to "vr_gardening" // default fallback
                    )
                    viewModel.joinVoiceRoom(roomMap[communityId] ?: "vr_gardening")
                    viewModel.selectTab(AppTab.VoiceRooms)
                },
                modifier = Modifier.testTag("launch_room_from_community")
            ) {
                Icon(Icons.Filled.Hearing, "Launch Voice Room for Circle", modifier = Modifier.size(26.dp), tint = MaterialTheme.colorScheme.secondary)
            }
        }

        // Category Inner tab navigation
        TabRow(
            selectedTabIndex = innerTabs.indexOf(innerTab),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            innerTabs.forEach { tabName ->
                Tab(
                    selected = innerTab == tabName,
                    onClick = { innerTab = tabName },
                    text = { Text(tabName, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }

        // Inner Content
        Box(modifier = Modifier.weight(1f)) {
            when (innerTab) {
                "Chat & Feed" -> {
                    val communityPosts = getSortedPosts(communityId, "Recent")
                    var summarizingPostTitle by remember { mutableStateOf<String?>(null) }
                    var showWhatsAppShareToast by remember { mutableStateOf(false) }

                    Column(modifier = Modifier.fillMaxSize()) {
                        if (showWhatsAppShareToast) {
                            LaunchedEffect(Unit) {
                                delay(2000)
                                showWhatsAppShareToast = false
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = SoftGreenCard.copy(alpha = 0.95f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("✅ ", fontSize = 16.sp)
                                    Text("Circle discussion link successfully generated & copied for WhatsApp!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A1E))
                                }
                            }
                        }

                        if (summarizingPostTitle != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("👵 AI Chachi Summary:", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFFB45309))
                                        Spacer(modifier = Modifier.weight(1f))
                                        IconButton(onClick = { summarizingPostTitle = null }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Filled.Close, "Close summary", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Our neighbors Ramesh and Mohan discuss windbreaks using simple bamboo sticks covered with organic wrap. Nisha highlights proper pot drainage for heavy monsoons. Warm, practical suggestions!",
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF78350F)
                                    )
                                }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp)
                        ) {
                            items(communityPosts) { post ->
                                var isCommentsExpanded by remember { mutableStateOf(false) }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(
                                            elevation = 8.dp,
                                            shape = RoundedCornerShape(26.dp),
                                            clip = false,
                                            ambientColor = Color.Black.copy(alpha = 0.03f),
                                            spotColor = Color.Black.copy(alpha = 0.05f)
                                        ),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                    shape = RoundedCornerShape(26.dp)
                                ) {
                                    Column {
                                        // Optional cover image block
                                        if (post.type == "Photo" || post.type == "Recipe" || post.type == "Story") {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(130.dp)
                                                    .background(Brush.linearGradient(post.coverGradient)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = post.coverEmoji,
                                                    fontSize = 54.sp,
                                                    modifier = Modifier.alpha(0.85f)
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(
                                                            Brush.verticalGradient(
                                                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f))
                                                            )
                                                        )
                                                )
                                                Text(
                                                    text = post.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier
                                                        .align(Alignment.BottomStart)
                                                        .padding(14.dp),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        // Author panel with Verified constraints
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            AvatarImage(name = post.authorName, size = 44)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(
                                                        text = post.authorName,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF1E293B)
                                                    )
                                                    if (post.isVerifiedExpert) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Verified,
                                                            contentDescription = "Verified expert Blue Badge",
                                                            tint = Color(0xFF3B82F6),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Surface(
                                                            color = Color(0xFFEFF6FF),
                                                            shape = RoundedCornerShape(4.dp)
                                                        ) {
                                                            Text(
                                                                text = "EXPERT",
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = Color(0xFF1D4ED8),
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                if (post.authorTitle != null) {
                                                    Text(
                                                        text = post.authorTitle,
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF475569),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                } else {
                                                    Text(
                                                        text = post.authorRole,
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF64748B)
                                                    )
                                                }
                                                Text(
                                                    text = post.timestamp,
                                                    fontSize = 9.sp,
                                                    color = Color(0xFF94A3B8)
                                                )
                                            }
                                        }

                                        // Title and description (Not duplicated if photo cover is drawn)
                                        if (post.type != "Photo" && post.type != "Recipe" && post.type != "Story") {
                                            Text(
                                                text = post.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF1E293B),
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                        }

                                        Text(
                                            text = post.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF334155),
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                        )

                                        // Conditional Poll component
                                        if (post.pollOptions != null) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                post.pollOptions.forEach { opt ->
                                                    val votedByMe = opt.votedByMe
                                                    Card(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 4.dp)
                                                            .clickable {
                                                                PremiumCirclesFeedStore.toggleLikePost(post.id) // increments interaction
                                                            },
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = if (votedByMe) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
                                                        ),
                                                        border = BorderStroke(1.dp, if (votedByMe) Color(0xFF93C5FD) else Color(0xFFE2E8F0))
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(10.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(opt.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                            Text("${opt.votes} votes", fontSize = 11.sp, color = Color(0xFF64748B))
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Interactive AI Chachi Summarizing trigger
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                                .background(Color(0xFFFEF3C7).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                                .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
                                                .clickable { summarizingPostTitle = post.title }
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text("👵", fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Ask AI Chachi to Summarize Discussion",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFB45309)
                                            )
                                        }

                                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(top = 8.dp))

                                        // Social Actions row (Like, Comment, Save, Share) with generous interactive bounds
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Like Action
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { PremiumCirclesFeedStore.toggleLikePost(post.id) }
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                                    contentDescription = "Like item click",
                                                    tint = if (post.isLiked) Color(0xFFEF4444) else Color(0xFF64748B),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = "${post.likesCount}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (post.isLiked) Color(0xFFEF4444) else Color(0xFF1E293B)
                                                )
                                            }

                                            // Comment Action
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { isCommentsExpanded = !isCommentsExpanded }
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.ChatBubbleOutline,
                                                    contentDescription = "Comments toggle",
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = "${post.commentsCount}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1E293B)
                                                )
                                            }

                                            // Save Action
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { PremiumCirclesFeedStore.toggleBookmarkPost(post.id) }
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (post.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                                    contentDescription = "Save post toggle",
                                                    tint = if (post.isBookmarked) Color(0xFFF59E0B) else Color(0xFF64748B),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = "${post.bookmarksCount}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (post.isBookmarked) Color(0xFFF59E0B) else Color(0xFF1E293B)
                                                )
                                            }

                                            // Share Action
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { showWhatsAppShareToast = true }
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Share,
                                                    contentDescription = "WhatsApp share element",
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                            }
                                        }

                                        // Threaded comments section inside Card
                                        if (isCommentsExpanded && post.comments.isNotEmpty()) {
                                            HorizontalDivider(color = Color(0xFFF1F5F9))
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFF8FAFC))
                                                    .padding(14.dp),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                post.comments.forEach { comment ->
                                                    Column(modifier = Modifier.fillMaxWidth()) {
                                                        Row(verticalAlignment = Alignment.Top) {
                                                            AvatarImage(name = comment.authorName, size = 30)
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                    Text(comment.authorName, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                                                    if (comment.isVerifiedExpert) {
                                                                        Surface(color = Color(0xFFEFF6FF), shape = RoundedCornerShape(4.dp)) {
                                                                            Text("EXPERT", fontSize = 7.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1D4ED8), modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp))
                                                                        }
                                                                    }
                                                                }
                                                                Text(comment.text, fontSize = 11.5.sp, color = Color(0xFF334155))
                                                                Text(comment.timestamp, fontSize = 9.sp, color = Color(0xFF94A3B8))
                                                            }
                                                        }

                                                        // Nested replies
                                                        comment.childReplies.forEach { reply ->
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(start = 38.dp, top = 6.dp),
                                                                verticalAlignment = Alignment.Top
                                                            ) {
                                                                AvatarImage(name = reply.authorName, size = 24)
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Column {
                                                                    Text(reply.authorName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                                                    Text(reply.text, fontSize = 11.sp, color = Color(0xFF475569))
                                                                    Text(reply.timestamp, fontSize = 8.5.sp, color = Color(0xFF94A3B8))
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Text / Voice messaging input bar allowing to create dynamic posts instantly!
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = chatText,
                                onValueChange = { chatText = it },
                                placeholder = { Text("Write a post in this circle...") },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("community_chat_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(
                                onClick = {
                                    if (chatText.isNotBlank()) {
                                        val authorName = user.name.ifBlank { "Senior Neighbor" }
                                        PremiumCirclesFeedStore.postsState.add(
                                            0,
                                            PremiumCommunityPost(
                                                id = "post_user_${System.currentTimeMillis()}",
                                                communityId = communityId,
                                                authorName = authorName,
                                                authorRole = "Community Member",
                                                timestamp = "Just now",
                                                title = "Update",
                                                description = chatText,
                                                type = "Text",
                                                likesCount = 0,
                                                commentsCount = 0,
                                                bookmarksCount = 0
                                            )
                                        )
                                        chatText = ""
                                        focusManager.clearFocus()
                                    }
                                },
                                modifier = Modifier.testTag("send_chat_message_button")
                            ) {
                                Icon(Icons.Filled.Send, "Publish post text to feed", modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                "Events" -> {
                    val communityEvents = events.filter { it.communityName == community.name }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (communityEvents.isEmpty()) {
                            item {
                                Text("No upcoming voice/meetup gatherings scheduled. Contact moderators to organize an AMA!")
                            }
                        } else {
                            items(communityEvents) { event ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                                Text(event.type, color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(2.dp))
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(event.localTime, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(event.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                AvatarImage(name = event.hostName, size = 30)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(event.hostName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = { viewModel.toggleEventRsvp(event.id) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (event.isUserRsvped) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
                                                )
                                            ) {
                                                Text(
                                                    text = if (event.isUserRsvped) "Joined RSVP ✓" else "RSVP (${event.rsvpCount})",
                                                    color = if (event.isUserRsvped) MaterialTheme.colorScheme.primary else Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Resources" -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(community.resources) { res ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (res.type == "Voice Guide") Icons.Filled.RecordVoiceOver else Icons.Filled.Description,
                                        contentDescription = "Resource type",
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(res.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("${res.type} • Uploded by ${res.authorName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(res.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                                    }
                                    IconButton(onClick = {}) {
                                        Icon(Icons.Filled.Download, "Download resource", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }

                "Members" -> {
                    // Display members list + Reputation triggers
                    val membersList = listOf(
                        UserProfile("member_1", "S. Subramaniam", "Senior Guide", "avatar_subru", "Hindustani classical vocalist.", false),
                        UserProfile("member_2", "Grandma Leela", "Circle Host", "avatar_leela", "Folklore enthusiast.", true),
                        UserProfile("member_3", "Priya Mehta", "Circle Guide", "avatar_priya", "Financial Planner.", true),
                        UserProfile("member_4", "Mohan Rao", "Circle Host", "avatar_mohan", "Veteran science tutor.", false),
                        UserProfile("member_5", "Nisha Hegde", "Active Member", "avatar_nisha", "Young tech professional.", false)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text("Circle Leaders & Helpers", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        items(membersList) { member ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AvatarImage(name = member.name, size = 36)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(member.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                if (member.isVerifiedExpert) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(Icons.Filled.Verified, "Verified expert badge", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                                                }
                                            }
                                            Text(member.role, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Let seniors easily show appreciation by tapping direct warm badges!
                                    Text("Award Reputation Badger:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        ReputationKind.values().forEach { rep ->
                                            Surface(
                                                modifier = Modifier
                                                    .clickable {
                                                        viewModel.awardReputationToUser(
                                                            user.name, // award points state to currentUser
                                                            rep
                                                        )
                                                    }
                                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(rep.emoji, fontSize = 11.sp)
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(rep.label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// SCREEN 5 & 6: VOICE ROOMS DIRECTORY & LIVE ROOM VIEW
@Composable
fun VoiceRoomsDirectoryScreen(
    viewModel: AgeNoBarViewModel,
    modifier: Modifier = Modifier
) {
    val voiceRooms by viewModel.voiceRooms.collectAsState()
    var showCreateVoiceRoom by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.07f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.RecordVoiceOver, "Listening icon", modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("No Typing Needed! join Group Audio Forums", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Instant listen-only rooms. Raise hand to voice opinions comfortably.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Active Audio Circles",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = { showCreateVoiceRoom = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("create_room_trigger")
                ) {
                    Icon(Icons.Filled.Add, "add icon", modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Host Room")
                }
            }
        }

        items(voiceRooms) { room ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.joinVoiceRoom(room.id) }
                    .testTag("voice_room_card_${room.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (room.scheduledTime != null) MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = if (room.scheduledTime != null) "📅 SCHEDULED" else "🔴 LIVE NOW",
                                color = if (room.scheduledTime != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = room.typeName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = room.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = room.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarImage(name = room.hostName, size = 32)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(room.hostName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(room.hostRole, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                            }
                        }

                        if (room.scheduledTime != null) {
                            Text(room.scheduledTime, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Group, "Participants count", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${room.activeSpeakerCount} speakers • ${room.totalListenerCount} listeners",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    if (room.scheduledTime == null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.joinVoiceRoom(room.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.Phone, "Phone symbol", modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TAP TO JOIN INSTANTLY", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showCreateVoiceRoom) {
        var hostRoomTitle by remember { mutableStateOf("") }
        var typeSelected by remember { mutableStateOf("Open Community Room") }
        var categorySelected by remember { mutableStateOf("Education") }
        var descSelected by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateVoiceRoom = false },
            title = { Text("Host a Voice Forum Circle", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextField(
                        value = hostRoomTitle,
                        onValueChange = { hostRoomTitle = it },
                        placeholder = { Text("e.g. Grandma Story Hour or Healthy Joints Tips") },
                        label = { Text("Voice Room Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = descSelected,
                        onValueChange = { descSelected = it },
                        placeholder = { Text("Describe what we will talk about...") },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Category: ", fontWeight = FontWeight.Bold)
                        val cats = listOf("Gardening", "Career", "Wellness", "Parents")
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            cats.forEach { cat ->
                                Button(
                                    onClick = { categorySelected = cat },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (categorySelected == cat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(cat, fontSize = 9.sp, color = if (categorySelected == cat) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Text("Room Type Selection:", fontWeight = FontWeight.Bold)
                    val types = listOf("Open Community Room", "Story Time Room", "Learning Circle")
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        types.forEach { t ->
                            Button(
                                onClick = { typeSelected = t },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (typeSelected == t) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(t, fontSize = 9.sp, color = if (typeSelected == t) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (hostRoomTitle.isNotBlank()) {
                            viewModel.createVoiceRoom(
                                hostRoomTitle,
                                typeSelected,
                                categorySelected,
                                descSelected.ifBlank { "Welcome to our senior interactive circle!" }
                            )
                        }
                        showCreateVoiceRoom = false
                    },
                    modifier = Modifier.testTag("submit_create_room_btn")
                ) {
                    Text("START STAGE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateVoiceRoom = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LiveVoiceRoomView(
    viewModel: AgeNoBarViewModel,
    modifier: Modifier = Modifier
) {
    val voiceRooms by viewModel.voiceRooms.collectAsState()
    val activeRoomId by viewModel.currentVoiceRoomId.collectAsState()
    val participants by viewModel.activeVoiceParticipants.collectAsState()

    val room = voiceRooms.firstOrNull { it.id == activeRoomId } ?: return

    val isMuted by viewModel.micMuted.collectAsState()
    val isSpeaker by viewModel.isSpeaker.collectAsState()
    val handRaised by viewModel.userHandRaised.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // App header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.minimizeVoiceRoom() },
                modifier = Modifier.testTag("minimize_voice_btn")
            ) {
                Icon(Icons.Filled.KeyboardArrowDown, "Minimize room", tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("DISCORD ACTIVE STAGE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp)
                Text(room.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Button(
                onClick = { viewModel.disconnectVoiceRoom() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("disconnect_voice_btn")
            ) {
                Text("Leave Stage", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Live Audio Spectrum Wave (beautiful simulation)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.MusicNote, "Sound status", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                CustomWaveform(
                    modifier = Modifier
                        .width(200.dp)
                        .height(30.dp),
                    wavePower = if (isMuted) 0.1f else 0.8f,
                    active = !isMuted
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isMuted) "Your mic is muted" else "Active Speaking Wave",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid of Speakers
        Text(
            text = "SPEAKERS ON STAGE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.primary
        )

        val speakers = participants.filter { it.isSpeaker }
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(speakers) { speaker ->
                val isMe = speaker.id == "user_senior_101"
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(76.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AvatarImage(
                            name = speaker.name,
                            size = 56,
                            isSpeaking = true,
                            wavePower = speaker.wavePower
                        )

                        // Muted icon layer
                        if (speaker.isMuted) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.BottomEnd)
                                    .background(Color.Red, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.MicOff, "Muted symbol", modifier = Modifier.size(10.dp), tint = Color.White)
                            }
                        }

                        // Floating reaction bubble
                        if (speaker.recentEmojiReaction != null) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.TopEnd)
                                    .background(Color.Yellow, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(speaker.recentEmojiReaction, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isMe) "You" else speaker.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (speaker.wavePower > 0.1f) "💬 talking" else "silent",
                        fontSize = 10.sp,
                        color = if (speaker.wavePower > 0.1f) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

        // Grid of Listeners
        Text(
            text = "LISTENERS IN AUDIENCE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )

        val listeners = participants.filter { !it.isSpeaker }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(listeners) { listener ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarImage(name = listener.name, size = 36)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(listener.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Active listener", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }

                    if (listener.isHandRaised) {
                        Icon(
                            imageVector = Icons.Filled.PanTool,
                            contentDescription = "Hand is raised",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Moderator action (Allow speaking if leader/Ramesh)
                        TextButton(
                            onClick = { viewModel.makeSpeaker(listener.id) },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Make Speaker", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { viewModel.toggleFollowSpeaker(listener.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (listener.isFollowed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = if (listener.isFollowed) "Following" else "Follow",
                            fontSize = 10.sp,
                            color = if (listener.isFollowed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Voice Stage Control Panel Elements (Mute, Raise Hand, Reactions)
        Surface(
            tonalElevation = 12.dp,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Mute/Unmute
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                if (isSpeaker) {
                                    viewModel.toggleMic()
                                } else {
                                    // Let listener unmute by requesting speaking automatically!
                                    viewModel.becomeSpeaker()
                                }
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    if (isSpeaker && !isMuted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape
                                )
                                .testTag("voice_room_mic_toggle")
                        ) {
                            Icon(
                                imageVector = if (isSpeaker && !isMuted) Icons.Filled.Mic else Icons.Filled.MicOff,
                                contentDescription = "Mute Unmute microphone",
                                tint = if (isSpeaker && !isMuted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isSpeaker) (if (isMuted) "Unmute" else "Muted") else "Speak Up",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 2. Raise Hand
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { viewModel.toggleHandRaise() },
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    if (handRaised) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape
                                )
                                .testTag("voice_room_hand_toggle")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PanTool,
                                contentDescription = "Raise hand",
                                tint = if (handRaised) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (handRaised) "Lower Hand" else "Raise Hand",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 3. Emojies reactions floating engine for seniors
                    val reactions = listOf("❤️", "🌟", "🙏", "🎓", "🤝")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        reactions.forEach { emoji ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                    .clickable { viewModel.castEmojiReaction(emoji) }
                            ) {
                                Text(emoji, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// SCREEN 7: LEARN & MENTOR SERVICES / NEW DISCOVER EXPERIENCE
@Composable
fun LearnAndMentorScreen(
    viewModel: AgeNoBarViewModel,
    modifier: Modifier = Modifier
) {
    val experts by viewModel.experts.collectAsState()
    val followedIds by viewModel.followedExpertIds.collectAsState()
    val bookedSessions by viewModel.bookedSessionExpertIds.collectAsState()
    val privateQuestions by viewModel.privateQuestions.collectAsState()
    val questions by viewModel.questions.collectAsState()
    
    // AI Chachi Profile Builder wizard state
    val chachiOnboardingStep by viewModel.chachiOnboardingStepIndex.collectAsState()
    
    // UI selections
    var selectedSubTab by remember { mutableStateOf("Discover Experts") } // "Discover Experts", "Ask Community", "Free Library"
    val subTabs = listOf("Discover Experts", "Ask Community", "Free Library")
    
    // Navigation / Detail overlay states
    var selectedExpertForProfile by remember { mutableStateOf<Expert?>(null) }
    var activeActionExpert by remember { mutableStateOf<Expert?>(null) }
    var activeActionType by remember { mutableStateOf<String?>(null) } // "Book", "Message", "VoiceCall", "VideoCall", "PrivateQuestion"

    // Search and filters states
    var searchQuery by remember { mutableStateOf("") }
    var selectedEmojiFilter by remember { mutableStateOf<String?>(null) }
    
    // Filter dropdown states
    val homeSelectedTopic by viewModel.homeSelectedTopic.collectAsState()
    var selectedLanguageFilter by remember { mutableStateOf("All") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    LaunchedEffect(homeSelectedTopic) {
        if (homeSelectedTopic != null && homeSelectedTopic != "All") {
            val mappedCategory = when (homeSelectedTopic) {
                "Education & Tutoring", "Education" -> "Education & Tutoring"
                "Finance & Banking", "Finance" -> "Financial Literacy"
                "Legal Guidance", "Legal" -> "Legal Knowledge"
                "Science & Technology" -> "Traditional Skills"
                "Career & Interviews", "Career" -> "HR & Interviews"
                "Gardening" -> "Gardening"
                "Cooking" -> "Cooking"
                "Yoga & Wellness", "Health & Wellness", "Wellness" -> "Wellness"
                "Ayurveda" -> "Wellness"
                "Physiotherapy" -> "Wellness"
                "Indian Dance Forms" -> "Traditional Skills"
                "Stories, Shlokas & Wisdom" -> "Traditional Skills"
                "Astrology & Astronomy", "Astrology" -> "Traditional Skills"
                "Languages" -> "Traditional Skills"
                "Parenting" -> "Wellness"
                "Music" -> "Traditional Skills"
                "Senior Living" -> "Wellness"
                "Health Understanding" -> "Wellness"
                else -> homeSelectedTopic
            }
            if (mappedCategory != null) {
                selectedCategoryFilter = mappedCategory
            }
        } else {
            selectedCategoryFilter = "All"
        }
    }

    var selectVerifiedOnly by remember { mutableStateOf(false) }
    var selectOnlineOnly by remember { mutableStateOf(false) }
    var showAdditionalFiltersBlock by remember { mutableStateOf(false) }

    val emojisFilterList = listOf(
        "📐 Topic (Math)" to "Education & Tutoring",
        "🌿 Circle" to "Gardening",
        "📊 Expertise" to "Financial Literacy",
        "💼 Mentor" to "HR & Interviews",
        "🎓 Skill" to "Traditional Skills",
        "🍲 Cooking" to "Cooking",
        "⚖️ Legal" to "Legal Knowledge",
        "🧘 Wellness" to "Wellness"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Render subtab top navigation (Cozy Elder-Centered Header)
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Discover Knowledgeable Neighbors",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Intergenerational teaching, values, and life skill circles.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.61f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub tabs bar
            TabRow(
                selectedTabIndex = subTabs.indexOf(selectedSubTab),
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(48.dp)
            ) {
                subTabs.forEach { tabName ->
                    val isTabSelected = selectedSubTab == tabName
                    Tab(
                        selected = isTabSelected,
                        onClick = { selectedSubTab = tabName },
                        text = {
                            Text(
                                text = tabName,
                                fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isTabSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier.testTag("discover_subtab_${tabName.lowercase().replace(" ", "_")}")
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Subtab content panel
        Box(modifier = Modifier.weight(1f)) {
            when (selectedSubTab) {
                "Discover Experts" -> {
                    // FILTER LOGIC
                    val filteredExperts = experts.filter { expert ->
                        val matchesSearch = searchQuery.isBlank() || 
                                expert.name.contains(searchQuery, ignoreCase = true) ||
                                expert.title.contains(searchQuery, ignoreCase = true) ||
                                expert.bio.contains(searchQuery, ignoreCase = true) ||
                                expert.skillsTags.any { it.contains(searchQuery, ignoreCase = true) }
                        
                        val matchesEmoji = selectedEmojiFilter == null || expert.category == selectedEmojiFilter
                        
                        val matchesLanguage = selectedLanguageFilter == "All" || expert.languages.contains(selectedLanguageFilter)
                        
                        val matchesCategory = selectedCategoryFilter == "All" || expert.category == selectedCategoryFilter
                        
                        val matchesVerified = !selectVerifiedOnly || expert.isVerifiedExpert
                        
                        val matchesOnline = !selectOnlineOnly || expert.isOnlineNow

                        matchesSearch && matchesEmoji && matchesLanguage && matchesCategory && matchesVerified && matchesOnline
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 95.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // AI Chachi profile builder promotional Banner card
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.09f)),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("👵", fontSize = 28.sp)
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Have wisdom or a nice story to share?",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Spend 3 minutes chatting with Chachi to auto-generate your professional neighbor profile!",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = { viewModel.startChachiOnboarding() },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("chachi_build_profile_btn")
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.AutoAwesome, "Auto icon", modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Build Profile With Chachi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Search and inline Filter chips
                        item {
                            Column {
                                // Search Input
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    leadingIcon = { Icon(Icons.Filled.Search, "Search icon") },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(Icons.Outlined.Cancel, "Clear Search")
                                            }
                                        }
                                    },
                                    placeholder = { Text("Search by name, expertise, or keywords...", fontSize = 13.sp) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("discover_search_input"),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Dynamic Emoji Filter Bar
                                Text(
                                    text = "Filter by Popular Area Labels:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(emojisFilterList.size) { index ->
                                        val (label, categoryRef) = emojisFilterList[index]
                                        val isEmojiSelected = selectedEmojiFilter == categoryRef
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = if (isEmojiSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(
                                                1.dp, 
                                                if (isEmojiSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                            ),
                                            modifier = Modifier.clickable {
                                                selectedEmojiFilter = if (isEmojiSelected) null else categoryRef
                                            }
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isEmojiSelected) Color.White else MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Toggle buttons for fast matchmaking filters
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Button(
                                        onClick = { showAdditionalFiltersBlock = !showAdditionalFiltersBlock },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("toggle_filters_panel_btn")
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.FilterList, "Filter icon", modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("More Filters", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (showAdditionalFiltersBlock) "▲" else "▼", fontSize = 9.sp)
                                        }
                                    }

                                    // Quick toggle verified only
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { selectVerifiedOnly = !selectVerifiedOnly }
                                    ) {
                                        Checkbox(
                                            checked = selectVerifiedOnly,
                                            onCheckedChange = { selectVerifiedOnly = it },
                                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                        )
                                        Text("Verified Only", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Quick toggle online now
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { selectOnlineOnly = !selectOnlineOnly }
                                    ) {
                                        Checkbox(
                                            checked = selectOnlineOnly,
                                            onCheckedChange = { selectOnlineOnly = it },
                                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                        )
                                        Text("Online Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Expandable parameters block
                                if (showAdditionalFiltersBlock) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            // Category filter dropdown
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Expertise Topic Area:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                // Simplified selector for testing: horizontal row
                                                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    items(4) { i ->
                                                        val cats = listOf("All", "Education & Tutoring", "Financial Literacy", "Gardening")
                                                        val itemCat = cats[i]
                                                        val isSel = selectedCategoryFilter == itemCat
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                                            modifier = Modifier.clickable { selectedCategoryFilter = itemCat }
                                                        ) {
                                                            Text(
                                                                text = itemCat.take(12) + "...",
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isSel) Color.White else MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            // Spoken Language Filter
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Spoken Language:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    items(4) { i ->
                                                        val lngs = listOf("All", "Hindi", "English", "Kannada")
                                                        val itemLng = lngs[i]
                                                        val isSel = selectedLanguageFilter == itemLng
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                                            modifier = Modifier.clickable { selectedLanguageFilter = itemLng }
                                                        ) {
                                                            Text(
                                                                text = itemLng,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isSel) Color.White else MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Grid results heading count
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Neighbors With Wisdom (${filteredExperts.size}):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (selectedEmojiFilter != null || selectedLanguageFilter != "All" || selectedCategoryFilter != "All" || selectVerifiedOnly || selectOnlineOnly || searchQuery.isNotEmpty()) {
                                    TextButton(
                                        onClick = {
                                            searchQuery = ""
                                            selectedEmojiFilter = null
                                            selectedLanguageFilter = "All"
                                            selectedCategoryFilter = "All"
                                            selectVerifiedOnly = false
                                            selectOnlineOnly = false
                                        }
                                    ) {
                                        Text("Clear Filters", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (filteredExperts.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("👵", fontSize = 48.sp)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("No matching neighbors found, beta.", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
                                        Text("Try relaxing filters, or ask AI Chachi to help find a tutor!", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        } else {
                            items(filteredExperts.size) { i ->
                                val expert = filteredExperts[i]
                                ExpertCardItem(
                                    expert = expert,
                                    isFollowed = followedIds.contains(expert.id),
                                    onViewProfile = { selectedExpertForProfile = expert },
                                    onActionTriggered = { actionType ->
                                        if (actionType == "Follow_Trigger") {
                                            viewModel.toggleFollowExpert(expert.id)
                                        } else {
                                            activeActionExpert = expert
                                            activeActionType = actionType
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                "Ask Community" -> {
                    // Render traditional community questions
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 95.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            var questionQuery by remember { mutableStateOf("") }
                            var questionCategory by remember { mutableStateOf("Education") }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Post a Question to Community Circles", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = questionQuery,
                                        onValueChange = { questionQuery = it },
                                        placeholder = { Text("e.g. My grandchild is throwing tantrums, visual storytelling help?") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("learn_post_q_input")
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Topic: ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            listOf("Education", "Finance", "Gardening").forEach { topic ->
                                                val isSelected = questionCategory == topic
                                                Text(
                                                    text = topic,
                                                    modifier = Modifier
                                                        .clickable { questionCategory = topic }
                                                        .background(
                                                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                            RoundedCornerShape(4.dp)
                                                        )
                                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontSize = 11.sp,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                if (questionQuery.isNotBlank()) {
                                                    viewModel.postQuestion(questionQuery, questionCategory)
                                                    questionQuery = ""
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Post Question")
                                        }
                                    }
                                }
                            }
                        }

                        items(questions.size) { index ->
                            val question = questions[index]
                            var answerText by remember { mutableStateOf("") }
                            var isReplyingActive by remember { mutableStateOf(false) }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AvatarImage(name = question.authorName, size = 32)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(question.authorName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text(question.authorRole, fontSize = 11.sp, color = Color.Gray)
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(question.timestamp, fontSize = 10.sp, color = Color.Gray)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(question.text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

                                    if (question.voiceNoteUrl != null) {
                                        SimulatedVibrantVoicePlayer(duration = 24, transcription = question.transcription, onPlayToggle = {})
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f)
                                        ) {
                                            Text(
                                                text = question.category,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.secondary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        IconButton(
                                            onClick = { viewModel.toggleHelpfulQuestion(question.id) },
                                            modifier = Modifier.testTag("helpful_vote_${question.id}")
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.ThumbUp, "Helpful thumbs up icon", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("${question.helpfulCount} helpful", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.weight(1f))

                                        TextButton(onClick = { isReplyingActive = !isReplyingActive }) {
                                            Text("Reply (${question.replies.size})", fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (isReplyingActive) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
                                        Spacer(modifier = Modifier.height(8.dp))

                                        question.replies.forEach { r ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                AvatarImage(name = r.authorName, size = 26)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(r.authorName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(r.authorRole, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                    if (r.isVoiceReply && r.voiceDuration != null) {
                                                        SimulatedVibrantVoicePlayer(duration = r.voiceDuration, transcription = "Visual suggestions on Division methods...", onPlayToggle = {})
                                                    } else {
                                                        Text(r.text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.82f))
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedTextField(
                                                value = answerText,
                                                onValueChange = { answerText = it },
                                                placeholder = { Text("Write helpful advice...") },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(44.dp)
                                                    .testTag("reply_input_${question.id}"),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            IconButton(
                                                onClick = {
                                                    if (answerText.isNotBlank()) {
                                                        viewModel.submitReplyToQuestion(question.id, answerText)
                                                        answerText = ""
                                                        isReplyingActive = false
                                                    }
                                                },
                                                modifier = Modifier.testTag("submit_reply_${question.id}")
                                            ) {
                                                Icon(Icons.Filled.Send, "Send reply button", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Free Library" -> {
                    // Unchanged for total platform fidelity
                    val tutorials = listOf(
                        Triple("Balcony Organic Composting Step Guide", "Ramesh Kumar", "Quick 3 minute download. Learn dry tea waste ratios, aerating balcony mud, and composting secrets."),
                        Triple("Vedic Mathematics Multiplication Secrets", "Sharla Devi", "10 page visual flashcards. Teach grandchildren multiplication using quick vertical and crosswise slides."),
                        Triple("Senior Post Office FD Returns Chart", "Priya Mehta", "Comprehensive secured deposit benefits with senior citizens comparative tables."),
                        Triple("Intergenerational Dialogue Hesitation Breaking", "Satya Narayanan", "Ex-HR Vice President secrets to boost English speaking pacing.")
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 95.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(tutorials.size) { index ->
                            val tut = tutorials[index]
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.Share, "Download content", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(tut.first, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            Text("By ${tut.second} • PDF Booklet Guide", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(tut.third, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {},
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), contentColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("TAP TO INSTANT STREAM", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // -- BOTTOM SECTION OVERLAYS --

    // Overlay 1: Rich Wisdom Profile View
    selectedExpertForProfile?.let { expert ->
        WisdomProfileDetailView(
            expert = expert,
            isFollowed = followedIds.contains(expert.id),
            bookedTime = bookedSessions[expert.id],
            privateQuestionsCount = privateQuestions[expert.id]?.size ?: 0,
            onClose = { selectedExpertForProfile = null },
            onActionType = { actionType ->
                if (actionType == "Follow_Trigger") {
                    viewModel.toggleFollowExpert(expert.id)
                } else if (actionType == "Book") {
                    viewModel.openScheduler(expert.id)
                    selectedExpertForProfile = null
                } else {
                    activeActionExpert = expert
                    activeActionType = actionType
                }
            },
            onFollowToggle = { viewModel.toggleFollowExpert(expert.id) }
        )
    }

    // Overlay 2: Dynamic Action Dialog (Book Call, Voice Call, Video Call, Ask private)
    activeActionType?.let { actionType ->
        activeActionExpert?.let { expert ->
            ConnectActionDialog(
                expert = expert,
                actionType = actionType,
                onDismiss = {
                    activeActionType = null
                    activeActionExpert = null
                },
                onConfirmBooking = { chosenTime, duration, isVideo ->
                    viewModel.bookSessionWithExpert(expert.id, chosenTime, duration, isVideo)
                    activeActionType = null
                    activeActionExpert = null
                },
                onConfirmPrivateQuestion = { textQuestion ->
                    viewModel.submitPrivateQuestionToExpert(expert.id, textQuestion)
                    viewModel.startDirectChat(expert.id, textQuestion)
                    activeActionType = null
                    activeActionExpert = null
                    selectedExpertForProfile = null // Dismiss profile too on message transition
                }
            )
        }
    }

    // Overlay 3: AI Chachi Onboarding Profile Builder Wizard
    if (chachiOnboardingStep > 0) {
        ChachiOnboardingWizardDialog(
            stepIndex = chachiOnboardingStep,
            answers = viewModel.chachiOnboardingAnswers.collectAsState().value,
            proposal = viewModel.chachiOnboardingProposal.collectAsState().value,
            onDismiss = { viewModel.cancelChachiOnboarding() },
            onAnswerSubmitted = { ans -> viewModel.answerChachiOnboardingStep(ans) },
            onPublish = { viewModel.completeChachiOnboardingAndSave() }
        )
    }
}

// -- SUBCOMPONENTS FOR DISCOVER EXPERTS --

@Composable
fun ExpertCardItem(
    expert: Expert,
    isFollowed: Boolean,
    onViewProfile: () -> Unit,
    onActionTriggered: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PastelBlueCard),
        border = BorderStroke(1.dp, Color(0xFFDDE8FF).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(28.dp))
            .testTag("expert_card_${expert.id}")
    ) {
        ExpertCoverPhoto(
            expert = expert,
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            // Profile top line
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Photo represented as custom circle
                Box(contentAlignment = Alignment.Center) {
                    AvatarImage(
                        name = expert.name,
                        size = 52
                    )
                    // Online Indicator Dot
                    if (expert.isOnlineNow) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(12.dp)
                                .background(Color(0xFF5A8F76), CircleShape) // Sage Green online dot
                                .border(BorderStroke(2.dp, Color.White), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = expert.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        
                        // Emoji Area Identifier
                        Text(
                            text = expert.areaEmoji,
                            fontSize = 15.sp,
                            modifier = Modifier.testTag("expert_emoji_${expert.id}")
                        )
                    }

                    Text(
                        text = expert.title,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${expert.yearsOfExperience} Years Experience",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Follow bookmark tag
                IconButton(onClick = { onActionTriggered("Follow_Trigger") }) {
                    Icon(
                        imageVector = if (isFollowed) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Followed star check",
                        tint = if (isFollowed) Color(0xFFF1C40F) else Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Biography summary
            Text(
                text = expert.bio,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.76f),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Badges details (Rating, helped counter)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, "Rating Star", modifier = Modifier.size(16.dp), tint = Color(0xFFF1C40F))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(java.util.Locale.US, "%.2f", expert.rating),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Help counting
                Text(
                    text = "❤️ Helped ${expert.peopleHelpedCount} Learners",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                // Languages
                Text(
                    text = "💬 " + expert.languages.joinToString(", "),
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // Action connect buttons bar (Book, Message, Voice, Video)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // View Profile button
                OutlinedButton(
                    onClick = onViewProfile,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("View Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Message Button
                Button(
                    onClick = { onActionTriggered("Message") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), contentColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .size(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Filled.Send, "Message expert button", modifier = Modifier.size(15.dp))
                }

                // Book Button
                Button(
                    onClick = { onActionTriggered("Book") },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Book Session", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -- OVERLAY VIEW: DEEP WISDOM PROFILE DRAWER PANEL --

@Composable
fun WisdomProfileDetailView(
    expert: Expert,
    isFollowed: Boolean,
    bookedTime: String?,
    privateQuestionsCount: Int,
    onClose: () -> Unit,
    onActionType: (String) -> Unit,
    onFollowToggle: () -> Unit
) {
    var activeProfileSubTab by remember { mutableStateOf("About") } // "About", "Wall", "Library", "Testimonials"
    var showVideoIntroScreen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onClose,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = {},
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp)
            ) {
                // Top Sticky Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, BorderLightSystem), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("close_wisdom_profile")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back to discover",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Find an Expert",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = expert.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = expert.title,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Cover icon follow button
                    Button(
                        onClick = onFollowToggle,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("profile_follow_button")
                    ) {
                        Icon(
                            imageVector = if (isFollowed) Icons.Filled.Star else Icons.Outlined.Star,
                            modifier = Modifier.size(14.dp),
                            contentDescription = "Follow check"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isFollowed) "Following" else "Follow", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable container for profile elements
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // COVER IMAGE & VISUAL HEADER
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            ExpertCoverPhoto(
                                expert = expert,
                                modifier = Modifier.fillMaxSize()
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color.White.copy(alpha = 0.8f)
                                    ) {
                                        Text(
                                            text = "🎓 " + expert.category,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFE2ECE9) // Soft sage green
                                    ) {
                                        Text(
                                            text = "Verified Expert",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // PRIMARY BIO INFO
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarImage(
                                name = expert.name,
                                size = 72
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(expert.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                                Text(expert.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Star, "Rating star", modifier = Modifier.size(14.dp), tint = Color(0xFFF1C40F))
                                    val formattedRating = String.format(java.util.Locale.US, "%.2f", expert.rating)
                                    Text(" $formattedRating • ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("${expert.testimonialsCount} custom reviews", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    // MEET ME: Simulated Intro Video panel
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Meet Me Intro-Video Representation", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp)
                                Text("Listen or watch Rajesh as they give a 35 second visual intro to their teaching passion.", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(10.dp))

                                if (showVideoIntroScreen) {
                                    // Simulated active video playback
                                    var ticksCount by remember { mutableStateOf(0) }
                                    LaunchedEffect(Unit) {
                                        ticksCount = 0
                                        while (ticksCount < 10) {
                                            delay(1000)
                                            ticksCount++
                                        }
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Black, RoundedCornerShape(10.dp))
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("🎥 Simulated Intro Stream Active", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LinearProgressIndicator(
                                            progress = ticksCount / 10f,
                                            modifier = Modifier.fillMaxWidth(),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        // Dynamic subtitle track
                                        val subCaptions = listOf(
                                            "Namaste children and neighbors! I'm Rajesh.",
                                            "I retired from Govt high school after 35 golden years.",
                                            "Here on AgeNoBar, I want to clear your divisions fear.",
                                            "We will folds origami papers, draw grids...",
                                            "No formula rote learning! Simply click View Profile and we can voice meet!"
                                        )
                                        val currentSubIndex = (ticksCount / 2).coerceAtMost(subCaptions.size - 1)
                                        Text(
                                            text = "\"" + subCaptions[currentSubIndex] + "\"",
                                            color = Color.Yellow,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        TextButton(onClick = { showVideoIntroScreen = false }) {
                                            Text("Close Presentation Video", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = { showVideoIntroScreen = true },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("▶ Play Video Introduction (30s)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ABOUT NAVIGATION SUBTABS
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val subTabsList = listOf("About", "Wall", "Library", "Reviews")
                            subTabsList.forEach { tabKey ->
                                val isSelected = activeProfileSubTab == tabKey
                                Text(
                                    text = tabKey,
                                    modifier = Modifier
                                        .clickable { activeProfileSubTab = tabKey }
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Render selected subtab panel content
                    when (activeProfileSubTab) {
                        "About" -> {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    // STORY CORNER
                                    Column {
                                        Text("Our Journey (Compiled by AI Chachi 👵)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = if (expert.myStoryText.isNotEmpty()) expert.myStoryText else "AI Chachi says: 'They are a beautiful addition to our Wisdom bridge. They have served decades with high integrity!'",
                                                modifier = Modifier.padding(14.dp),
                                                fontSize = 12.sp,
                                                lineHeight = 18.sp,
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                                            )
                                        }
                                    }

                                    // AREAS I CAN HELP WITH
                                    Column {
                                        Text("Areas I Can Help With:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            expert.skillsTags.forEach { tag ->
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                ) {
                                                    Text(
                                                        text = "⭐ $tag",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // CONTRIBUTIONS DIRECT BADGES
                                    Column {
                                        Text("Community Contributions & Status:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf(
                                                Triple("❤️ ${expert.peopleHelpedCount}", "Learners Helped", MaterialTheme.colorScheme.primary),
                                                Triple("❓ ${expert.questionsAnswered}", "Q&A Cleared", MaterialTheme.colorScheme.secondary),
                                                Triple("📞 ${expert.sessionsHosted}", "Hours Hosted", MaterialTheme.colorScheme.secondary)
                                            ).forEach { (count, label, col) ->
                                                Card(
                                                    modifier = Modifier.weight(1f),
                                                    colors = CardDefaults.cardColors(containerColor = col.copy(alpha = 0.04f)),
                                                    border = BorderStroke(1.dp, col.copy(alpha = 0.12f))
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(8.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(count, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                                        Text(label, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "Wall" -> {
                            if (expert.communityWall.isEmpty()) {
                                item {
                                    Text("No posts or articles shared lately by ${expert.name}.", fontStyle = FontStyle.Italic, fontSize = 12.sp, color = Color.Gray)
                                }
                            } else {
                                items(expert.communityWall.size) { index ->
                                    val post = expert.communityWall[index]
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                ) {
                                                    Text(post.type, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(post.timestamp, fontSize = 10.sp, color = Color.Gray)
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(post.text, fontSize = 12.sp, lineHeight = 16.sp)
                                            post.attachmentLabel?.let { attachment ->
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.background,
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text("📂 Attached File: $attachment", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "Library" -> {
                            item {
                                Column {
                                    Text("Downloadable Homework Sets & PDF Guides", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("📐", fontSize = 24.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Pythagoreans Theorem Made Easy.pdf", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text("PDF Document • 4.1MB", fontSize = 10.sp, color = Color.Gray)
                                            }
                                            Button(onClick = {}, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                                                Text("Get Guide", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "Reviews" -> {
                            if (expert.testimonialsList.isEmpty()) {
                                item {
                                    Text("No review evaluations submitted yet.", fontStyle = FontStyle.Italic, fontSize = 12.sp, color = Color.Gray)
                                }
                            } else {
                                items(expert.testimonialsList.size) { index ->
                                    val t = expert.testimonialsList[index]
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(t.authorName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Spacer(modifier = Modifier.weight(1f))
                                                Text(t.date, fontSize = 10.sp, color = Color.Gray)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.Star, "Star rating", modifier = Modifier.size(12.dp), tint = Color(0xFFF1C40F))
                                                Text(" ${t.rating} rating", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(t.text, fontSize = 12.sp, fontStyle = FontStyle.Italic)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Connect Options Drawer buttons list
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Ways To Instantly Connect with ${expert.name}:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                "Message" to Icons.Filled.Send,
                                "Book" to Icons.Filled.CalendarMonth
                            ).forEach { (typeKey, icon) ->
                                Button(
                                    onClick = { onActionType(typeKey) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(icon, typeKey, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(typeKey, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

// -- OVERLAY VIEW: CHACHI ONBOARDING PROFILE BUILDER WIZARD DIALOG --

@Composable
fun ChachiOnboardingWizardDialog(
    stepIndex: Int,
    answers: Map<Int, String>,
    proposal: Expert?,
    onDismiss: () -> Unit,
    onAnswerSubmitted: (String) -> Unit,
    onPublish: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    var isRecordingDictationSimulated by remember { mutableStateOf(false) }

    val chachiQuestions = listOf(
        "", // 0 fallback
        "Ramesh beta! Welcome! Let's build your neighborhood wisdom card together. What did you do professionally during your career days? (e.g., Senior School Mathematics Teacher / ex-Mechanical Engineer)",
        "Ah, excellent. How many years of experience did you invest in this area, beta? (e.g., 35 Years)",
        "Understood. What are the specific hobbies or topics you want to guide our neighborhood grandchildren with? (e.g., Vedic multiplication tips, paper-folding geometry, composting, traditional Marathi soup preps)",
        "Beautiful! What spoken languages are you comfortable speaking with kids on voice call dialogues? (e.g., English, Hindi, Kannada)",
        "Last question, beta: Why do you love sharing your lifetime of experiences? What drives you? (e.g., Seeing children's eyes light up, keeping regional stories alive)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Visual Chachi header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👵", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("AI Chachi Profile Builder", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                        Text(if (stepIndex <= 5) "Step $stepIndex of 5 conversational answers" else "Verified Wisdom Card Proposal", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                if (stepIndex <= 5) {
                    // Chat question bubble
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = chachiQuestions[stepIndex],
                            modifier = Modifier.padding(12.dp),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 18.sp
                        )
                    }

                    // Answer input area
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Speak or type your answer here...", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .testTag("chachi_onboarding_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Speak Simulated Dictation button
                    if (isRecordingDictationSimulated) {
                        LaunchedEffect(Unit) {
                            delay(1800)
                            textInput = when (stepIndex) {
                                1 -> "Professional retired central school Primary Educator."
                                2 -> "25 Years proud teaching achievements."
                                3 -> "Teach Vedic multiplication short math tricks and Kannada storytelling rules."
                                4 -> "Kannada, English and basic Hindi."
                                else -> "I love seeing our neighbor hood grandchildren learn and keep values alive."
                            }
                            isRecordingDictationSimulated = false
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Mic, "Recording dictation", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Dictating spoken answer Beta... Please speak clearly", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    } else {
                        Button(
                            onClick = { isRecordingDictationSimulated = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Mic, "Speak answer button", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Speak Spoken Answer (Voice Dictation)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel Onboarding", color = Color.Gray, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    onAnswerSubmitted(textInput)
                                    textInput = ""
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Next Step ▶")
                        }
                    }
                } else {
                    // STEP 6: Review proposal synthesized card of professional matchmaker
                    proposal?.let { p ->
                        Text(
                            text = "Amaryllis, beta! Look what your AI Chachi generated from your speech answers. It is fully responsive and verified!",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("✨ WORKPLACE / TEACHING CARD PROPOSAL", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(p.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(p.title, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text("${p.yearsOfExperience} Years of professional milestones", fontSize = 11.sp, color = Color.Gray)
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = p.bio,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    p.skillsTags.forEach { tg ->
                                        Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)) {
                                            Text(tg, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Motherly story bubble generated
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                        ) {
                            Text(
                                text = p.myStoryText,
                                modifier = Modifier.padding(10.dp),
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                fontStyle = FontStyle.Italic
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text("Discard Draft", color = Color.Gray)
                            }

                            Button(
                                onClick = onPublish,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Star, "Publish icon", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Publish Profile", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

// -- OVERLAY VIEW: ACTION PROCESS DIALOG (CALL, CHAT, BOOK) --

@Composable
fun ConnectActionDialog(
    expert: Expert,
    actionType: String,
    onDismiss: () -> Unit,
    onConfirmBooking: (String, Int, Boolean) -> Unit,
    onConfirmPrivateQuestion: (String) -> Unit
) {
    var currentStepType by remember { mutableStateOf(actionType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        text = {
            when (currentStepType) {
                "Book" -> {
                    var selectedDate by remember { mutableStateOf("Tomorrow (June 6)") }
                    var selectedTime by remember { mutableStateOf("3:30 PM") }
                    var selectedDuration by remember { mutableStateOf(30) }
                    var isVideoSession by remember { mutableStateOf(false) }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Reserve Wisdom Session",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Choose a friendly custom consultation block with ${expert.name}. Brainstorming, sharing stories, and guidance sessions are always free.",
                            fontSize = 11.5.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Category themed subtitle
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👤 Mentor Focus: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(expert.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        // 1. DATE PICKER
                        Text("1. Select Date", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        val dateOptions = listOf("Tomorrow (June 6)", "Fri, June 7", "Sat, June 8", "Sun, June 9", "Mon, June 10")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(dateOptions) { date ->
                                val isChosen = selectedDate == date
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.5.dp, if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                    color = if (isChosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .clickable { selectedDate = date }
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(date.substringBefore(",").substringBefore("(").trim(), fontSize = 11.sp, color = if (isChosen) MaterialTheme.colorScheme.primary else Color.Gray, fontWeight = FontWeight.Bold)
                                        Text(if (date.contains("(")) "June 6" else date.substringAfter(",").trim(), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }

                        // 2. TIME SLOT PICKER
                        Text("2. Select Time Slot", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        val timeOptions = listOf("10:30 AM", "11:00 AM", "3:30 PM", "4:00 PM", "5:30 PM")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(timeOptions) { time ->
                                val isChosen = selectedTime == time
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.5.dp, if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                    color = if (isChosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .clickable { selectedTime = time }
                                ) {
                                    Text(
                                        text = time,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        // 3. DURATION PICKER
                        Text("3. Choose Duration", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        val durationOptions = listOf(30, 45, 60)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            durationOptions.forEach { dur ->
                                val isChosen = selectedDuration == dur
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.5.dp, if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                    color = if (isChosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedDuration = dur }
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                        Text("$dur Mins", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // 4. FORMAT: Voice vs Video
                        Text("4. Select Classroom Format", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            // Voice
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.5.dp, if (!isVideoSession) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                color = if (!isVideoSession) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { isVideoSession = false }
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Icon(Icons.Default.Phone, "Voice Call", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Voice Consult", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Video
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.5.dp, if (isVideoSession) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                color = if (isVideoSession) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { isVideoSession = true }
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Icon(Icons.Filled.Videocam, "Video Session", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Video Session", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.0f).testTag("cancel_booking_btn")
                            ) {
                                Text("Cancel", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onConfirmBooking("$selectedDate at $selectedTime", selectedDuration, isVideoSession) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.5f).testTag("confirm_booking_btn")
                            ) {
                                Text("Confirm Booking", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                "Message" -> {
                    var chatText by remember { mutableStateOf("") }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Send Direct Message to ${expert.name}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("All chats are translated in real-time if necessary.", fontSize = 11.sp, color = Color.Gray)

                        OutlinedTextField(
                            value = chatText,
                            onValueChange = { chatText = it },
                            placeholder = { Text("Write your message context here...", fontSize = 13.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("cancel_message_btn")
                            ) {
                                Text("Cancel", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { 
                                    if (chatText.isNotEmpty()) {
                                        onConfirmPrivateQuestion(chatText)
                                    }
                                },
                                enabled = chatText.isNotEmpty(),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1.5f).testTag("confirm_message_btn")
                            ) {
                                Text("Send", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                "VoiceCall" -> {
                    if (!expert.isOnlineNow) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📴", fontSize = 32.sp)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Mentor Currently Offline", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("${expert.name} is not available for instant voice talk right now.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "AI Chachi says: 'They are likely resting or sharing wisdom offline on Wisdom Bridge. Let's schedule a formal free session instead!'",
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 12.sp,
                                    fontStyle = FontStyle.Italic,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Button(
                                onClick = { currentStepType = "Book" },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Schedule calendar slot instead", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        var ticksCount by remember { mutableStateOf(0) }
                        LaunchedEffect(Unit) {
                            ticksCount = 0
                            while (ticksCount < 95) {
                                delay(1000)
                                ticksCount++
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📞", fontSize = 32.sp)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Neighborhood Hotline Connected", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Speaking with ${expert.name} live", fontSize = 12.sp, color = Color.Gray)
                                Text("00:${ticksCount.toString().padStart(2, '0')}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                            }

                            // Simulated active talk box
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = if (ticksCount < 4) "Dialing line..." 
                                           else if (ticksCount < 10) "\"Hello beta! Rajesh here. Deepa told me you had doubts on fractions subtraction. Let's solve!\"" 
                                           else "\"Fractions subtraction is like sharing hot flat breads. If you have 3/4 and take away 1/4...\"",
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 12.sp,
                                    fontStyle = FontStyle.Italic,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE05B5B)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Disconnect Call", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                "VideoCall" -> {
                    if (!expert.isOnlineNow) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📹", fontSize = 32.sp)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Mentor Currently Offline", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("${expert.name}'s high-definition video feed is currently offline.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "AI Chachi says: 'Oh no, Anand stands ready to meet you via video when online. Let's secure a scheduled calendar time slots swap right now so they receive a prompt reminder!'",
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 12.sp,
                                    fontStyle = FontStyle.Italic,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Button(
                                onClick = { currentStepType = "Book" },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Schedule calendar slot instead", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📹", fontSize = 32.sp)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Simulated High-Definition Stream", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Connecting video with ${expert.name}", fontSize = 12.sp, color = Color.Gray)
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.Black)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🎥 Camera Feed: ${expert.name} (Smiling 📐)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE05B5B)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Close Video Room", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                else -> {
                    Button(onClick = onDismiss) {
                        Text("Close overlay")
                    }
                }
            }
        }
    )
}

// SCREEN 8: MESSAGES CHAT LIST OVERVIEW
@Composable
fun MessagesScreen(
    viewModel: AgeNoBarViewModel,
    onCommunitySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val communities by viewModel.communities.collectAsState()
    val experts by viewModel.experts.collectAsState()
    val bookingsList by viewModel.bookingsList.collectAsState()
    val chachiChat by viewModel.chachiChat.collectAsState()
    val isChachiTyping by viewModel.isChachiTyping.collectAsState()
    val directConversations by viewModel.directConversations.collectAsState()
    val selectedDirectConvId by viewModel.selectedDirectConversationId.collectAsState()

    var activeInboxTab by remember { mutableStateOf("Chachi") } // "Chachi" or "Circles"
    var userDraftMsg by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()

    LaunchedEffect(selectedDirectConvId) {
        if (selectedDirectConvId == null) {
            activeInboxTab = "Chachi"
        }
    }

    // Slide/Scroll automatic focus when new messages arrive
    LaunchedEffect(chachiChat.size, isChachiTyping) {
        if (chachiChat.isNotEmpty()) {
            val lastMsgIndex = (chachiChat.size - 1).coerceAtLeast(0)
            val targetIndex = if (isChachiTyping) chachiChat.size else lastMsgIndex
            lazyListState.animateScrollToItem(targetIndex)
        }
    }

    // 1. CHAT CONVERSATION FULL-DASHBOARD OVERLAY
    if (selectedDirectConvId != null) {
        val activeConv = directConversations.find { it.id == selectedDirectConvId }
        if (activeConv != null) {
            var directDraftText by remember { mutableStateOf("") }
            val directListState = rememberLazyListState()

            LaunchedEffect(activeConv.messages.size) {
                if (activeConv.messages.isNotEmpty()) {
                    directListState.scrollToItem(activeConv.messages.size - 1)
                }
            }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Topic/Recipient Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.selectDirectConversation(null) },
                        modifier = Modifier.testTag("direct_chat_back_btn")
                    ) {
                        Icon(Icons.Default.ArrowBack, "Back to Inbox", tint = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    AvatarImage(name = activeConv.recipientName, size = 42)

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeConv.recipientName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF5A8F76), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Verified Expert",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("1:1 Conversation", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                // Scrollable Chat bubble list
                LazyColumn(
                    state = directListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(activeConv.messages) { msg ->
                        val isUser = msg.senderId == "user_senior_101" || msg.senderName.contains("Ramesh")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(
                                    topStart = if (isUser) 20.dp else 4.dp,
                                    topEnd = if (isUser) 4.dp else 20.dp,
                                    bottomStart = 20.dp,
                                    bottomEnd = 20.dp
                                ),
                                modifier = Modifier.widthIn(max = 270.dp),
                                border = if (!isUser) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)) else null
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = if (isUser) "ME" else msg.senderName.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUser) MaterialTheme.colorScheme.primary else Color(0xFF5A8F76),
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = msg.timestamp,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 9.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }

                // Elderly Friendly quick response buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(top = 8.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = "QUICK WISDOM SWAP RESPONSES:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Pranam! 🙏" to "Pranam! Thank you for connecting with me.",
                            "Excited to learn! 📐" to "I am very excited to learn this from you in our upcoming session.",
                            "Let's swap stories! ☕" to "I look forward to our video call and swapping rich life stories."
                        ).forEach { (lbl, rawText) ->
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.sendDirectMessage(activeConv.id, rawText)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(lbl, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // Direct chat input bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = directDraftText,
                        onValueChange = { directDraftText = it },
                        placeholder = { Text("Write reply to ${activeConv.recipientName}...") },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.weight(1f).testTag("direct_msg_input"),
                        maxLines = 3,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (directDraftText.isNotBlank()) {
                                        viewModel.sendDirectMessage(activeConv.id, directDraftText)
                                        directDraftText = ""
                                    }
                                },
                                modifier = Modifier.testTag("send_direct_msg_btn")
                            ) {
                                Icon(Icons.Filled.Send, "Send Message Icon", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                }
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Large Premium Title
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "My Companion & Chats",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Sit comfortably, speak with Chachi, or message your circle threads.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clean Custom Toggle (Headspace / Apple Health aesthetic)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (activeInboxTab == "Chachi") MaterialTheme.colorScheme.surface else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { activeInboxTab = "Chachi" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👵 ", fontSize = 16.sp)
                        Text(
                            text = "AI Chachi Lounge",
                            fontWeight = FontWeight.Bold,
                            color = if (activeInboxTab == "Chachi") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (activeInboxTab == "Circles") MaterialTheme.colorScheme.surface else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { activeInboxTab = "Circles" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👥 ", fontSize = 16.sp)
                        Text(
                            text = "Circle Messages",
                            fontWeight = FontWeight.Bold,
                            color = if (activeInboxTab == "Circles") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeInboxTab == "Chachi") {
                // Immersive Chat Companion Screen
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    // Header Details
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .background(Brush.linearGradient(listOf(Color(0xFFF8E7EE), Color(0xFFEADCF8))))
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👵", fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "AI Chachi",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color(0xFF5A8F76), CircleShape) // Cozy active Green
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Ready to listen with endless love",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Scrollable Conversation Stream
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(chachiChat) { msg ->
                            val isFromChachi = msg.senderId == "ai_chachi"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isFromChachi) Arrangement.Start else Arrangement.End
                            ) {
                                if (isFromChachi) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                                        modifier = Modifier.widthIn(max = 345.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .background(Brush.linearGradient(listOf(Color(0xFFF5EEFD), Color(0xFFEADCF8))))
                                                .border(1.dp, Color(0xFFEADCF8).copy(alpha = 0.5f), RoundedCornerShape(topStart = 4.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp))
                                                .padding(14.dp)
                                        ) {
                                            Text(
                                                text = "👵 AI CHACHI",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF5A8F76),
                                                letterSpacing = 1.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = msg.text,
                                                style = MaterialTheme.typography.bodyLarge, // easily readable scale
                                                color = MaterialTheme.colorScheme.onBackground,
                                                lineHeight = 24.sp
                                            )
                                            
                                            // 1. Attached Experts
                                            msg.attachedExpertIds?.let { list ->
                                                if (list.isNotEmpty()) {
                                                    val matchedExperts = list.mapNotNull { id -> experts.find { it.id == id } }
                                                    matchedExperts.forEach { expert ->
                                                        Card(
                                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                                                            shape = RoundedCornerShape(12.dp),
                                                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                                        ) {
                                                            Column(modifier = Modifier.padding(12.dp)) {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Surface(
                                                                        shape = CircleShape,
                                                                        color = MaterialTheme.colorScheme.primaryContainer,
                                                                        modifier = Modifier.size(40.dp)
                                                                    ) {
                                                                        Box(contentAlignment = Alignment.Center) {
                                                                            Text(expert.areaEmoji.ifEmpty { "🎓" }, fontSize = 20.sp)
                                                                        }
                                                                    }
                                                                    Spacer(modifier = Modifier.width(8.dp))
                                                                    Column(modifier = Modifier.weight(1f)) {
                                                                        Text(
                                                                            expert.name,
                                                                            style = MaterialTheme.typography.titleMedium,
                                                                            fontWeight = FontWeight.Bold,
                                                                            color = MaterialTheme.colorScheme.onSurface
                                                                        )
                                                                        Text(
                                                                            expert.title,
                                                                            style = MaterialTheme.typography.bodySmall,
                                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                        )
                                                                    }
                                                                    val formattedRating = String.format(java.util.Locale.US, "%.2f", expert.rating)
                                                                    Text(
                                                                        "⭐ $formattedRating",
                                                                        style = MaterialTheme.typography.bodyMedium,
                                                                        fontWeight = FontWeight.SemiBold,
                                                                        color = Color(0xFFD4AF37)
                                                                    )
                                                                }
                                                                Spacer(modifier = Modifier.height(4.dp))
                                                                Text(
                                                                    expert.bio,
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                    maxLines = 2,
                                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                                )
                                                                Spacer(modifier = Modifier.height(8.dp))
                                                                
                                                                var hasBooked by remember { mutableStateOf(false) }
                                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                    Button(
                                                                        onClick = {
                                                                            viewModel.openScheduler(expert.id)
                                                                            hasBooked = true
                                                                        },
                                                                        modifier = Modifier.weight(1.2f),
                                                                        colors = ButtonDefaults.buttonColors(
                                                                            containerColor = if (hasBooked) Color(0xFF5A8F76) else MaterialTheme.colorScheme.primary
                                                                        ),
                                                                        shape = RoundedCornerShape(8.dp),
                                                                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 6.dp)
                                                                    ) {
                                                                        Text(
                                                                            "📅 Open Calendar",
                                                                            fontSize = 11.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            color = Color.White
                                                                        )
                                                                    }
                                                                    OutlinedButton(
                                                                        onClick = {
                                                                            viewModel.startDirectChat(expert.id, "Hello! I saw your profile through Chachi.")
                                                                            viewModel.selectDirectConversation(expert.id)
                                                                        },
                                                                        modifier = Modifier.weight(1f),
                                                                        shape = RoundedCornerShape(8.dp),
                                                                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 6.dp)
                                                                    ) {
                                                                        Text(
                                                                            "Chat Direct",
                                                                            fontSize = 11.sp,
                                                                            fontWeight = FontWeight.Bold
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            // 2. Attached Communities/Circles
                                            msg.attachedCommunityIds?.let { list ->
                                                if (list.isNotEmpty()) {
                                                    val matchedCommunities = list.mapNotNull { id -> communities.find { it.id == id } }
                                                    matchedCommunities.forEach { comm ->
                                                        Card(
                                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                                                            shape = RoundedCornerShape(12.dp),
                                                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                                        ) {
                                                            Column(modifier = Modifier.padding(12.dp)) {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Surface(
                                                                        shape = CircleShape,
                                                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                                                        modifier = Modifier.size(40.dp)
                                                                    ) {
                                                                        Box(contentAlignment = Alignment.Center) {
                                                                            Text(comm.iconEmoji.ifEmpty { "👥" }, fontSize = 20.sp)
                                                                        }
                                                                    }
                                                                    Spacer(modifier = Modifier.width(8.dp))
                                                                    Column(modifier = Modifier.weight(1f)) {
                                                                        Text(
                                                                            comm.name,
                                                                            style = MaterialTheme.typography.titleMedium,
                                                                            fontWeight = FontWeight.Bold,
                                                                            color = MaterialTheme.colorScheme.onSurface
                                                                        )
                                                                        Text(
                                                                            "${comm.memberCount} members • ${comm.category}",
                                                                            style = MaterialTheme.typography.bodySmall,
                                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                        )
                                                                    }
                                                                }
                                                                Spacer(modifier = Modifier.height(4.dp))
                                                                Text(
                                                                    comm.description,
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                    maxLines = 2,
                                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                                )
                                                                Spacer(modifier = Modifier.height(8.dp))
                                                                Button(
                                                                    onClick = {
                                                                        viewModel.enterCommunity(comm.id)
                                                                        viewModel.selectTab(AppTab.Communities)
                                                                    },
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    shape = RoundedCornerShape(8.dp),
                                                                    contentPadding = PaddingValues(vertical = 4.dp)
                                                                ) {
                                                                    Text(
                                                                        "Open Circle Chat",
                                                                        fontSize = 11.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = Color.White
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            // 3. Attached Bookings
                                            msg.attachedBookingIds?.let { list ->
                                                if (list.isNotEmpty()) {
                                                    val matchedBookings = list.mapNotNull { id -> bookingsList.find { it.id == id } }
                                                    matchedBookings.forEach { booking ->
                                                        Card(
                                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                                                            shape = RoundedCornerShape(12.dp),
                                                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                                        ) {
                                                            Column(modifier = Modifier.padding(12.dp)) {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Surface(
                                                                        shape = CircleShape,
                                                                        color = Color(0xFF5A8F76).copy(alpha = 0.15f),
                                                                        modifier = Modifier.size(40.dp)
                                                                    ) {
                                                                        Box(contentAlignment = Alignment.Center) {
                                                                            Text(if (booking.isVideo) "📹" else "📞", fontSize = 20.sp)
                                                                        }
                                                                    }
                                                                    Spacer(modifier = Modifier.width(8.dp))
                                                                    Column(modifier = Modifier.weight(1f)) {
                                                                        Text(
                                                                            booking.expertName,
                                                                            style = MaterialTheme.typography.titleMedium,
                                                                            fontWeight = FontWeight.Bold,
                                                                            color = MaterialTheme.colorScheme.onSurface
                                                                        )
                                                                        Text(
                                                                            "${if (booking.isVideo) "Video" else "Voice"} Class • ${booking.timing}",
                                                                            style = MaterialTheme.typography.bodySmall,
                                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                        )
                                                                    }
                                                                }
                                                                Spacer(modifier = Modifier.height(8.dp))
                                                                
                                                                var hasJoined by remember { mutableStateOf(false) }
                                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                    Button(
                                                                        onClick = {
                                                                            hasJoined = true
                                                                        },
                                                                        modifier = Modifier.weight(1.2f),
                                                                        colors = ButtonDefaults.buttonColors(
                                                                            containerColor = if (hasJoined) Color(0xFF5A8F76) else MaterialTheme.colorScheme.primary
                                                                        ),
                                                                        shape = RoundedCornerShape(8.dp),
                                                                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 6.dp)
                                                                    ) {
                                                                        Text(
                                                                            if (hasJoined) "Calling..." else "Join Room Live",
                                                                            fontSize = 11.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            color = Color.White
                                                                        )
                                                                    }
                                                                    OutlinedButton(
                                                                        onClick = {
                                                                            viewModel.setEditingBooking(booking)
                                                                        },
                                                                        modifier = Modifier.weight(1f),
                                                                        shape = RoundedCornerShape(8.dp),
                                                                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 6.dp)
                                                                    ) {
                                                                        Text(
                                                                            "✏️ Modify Class",
                                                                            fontSize = 11.sp,
                                                                            fontWeight = FontWeight.Bold
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), // sage container background
                                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                                        modifier = Modifier.widthIn(max = 280.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = msg.text,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                lineHeight = 24.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (isChachiTyping) {
                            item {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Text(
                                            "Chachi is thinking beta...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontStyle = FontStyle.Italic,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Inline metaphory prompt boards
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(
                            "TAP TO ASK IN SIMPLE METAPHORS:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5A8F76),
                            letterSpacing = 1.sp
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            listOf(
                                "What is 'Crypto'? 🪙" to "Explain what 'Crypto' means simply",
                                "What is 'PDF'? 📄" to "Explain what 'PDF' means",
                                "Bedtime Story 🌸" to "Suggest a comforting bedtime story"
                            ).forEach { (label, rawVal) ->
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                                        .clickable { viewModel.askChachi(rawVal) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Voice & Input Message row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 96.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = userDraftMsg,
                            onValueChange = { userDraftMsg = it },
                            placeholder = { Text("Write to Chachi...") },
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (userDraftMsg.isNotBlank()) {
                                            viewModel.askChachi(userDraftMsg)
                                            userDraftMsg = ""
                                        }
                                    }
                                ) {
                                    Icon(Icons.Filled.Send, "Send text query", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))

                        // Cozy audio input shortcut
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                .clickable {
                                    viewModel.askChachi("Tell me a comforting story")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Mic, "Microphone speak companion button", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            } else {
                // Dual Column Chat list: Private DM Thread rows, followed by Practice Circle threads
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 110.dp, start = 20.dp, end = 20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // SEC 1: PRIVATE EXPERT DIRECT DISCUSSIONS
                    item {
                        Text(
                            text = "💬 DIRECT DISCUSSIONS WITH EXPERTS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                        )
                    }

                    if (directConversations.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("💡", fontSize = 22.sp)
                                    Column {
                                        Text("No active expert chats yet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("Tap 'Message' or 'Book' on any verified senior mentor profile to start exchanging deep wisdom directly.", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    } else {
                        items(directConversations) { conv ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectDirectConversation(conv.id) }
                                    .testTag("direct_chat_row_${conv.id}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AvatarImage(name = conv.recipientName, size = 48)

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(conv.recipientName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold)
                                            Text(
                                                text = conv.lastMessageTimestamp,
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = conv.lastMessageText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Filled.ChevronRight, "Open chat thread", modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    // SEC 2: GENERAL WISDOM CIRCLES
                    item {
                        Text(
                            text = "👥 JOINED PRACTICE & STORY CIRCLES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 4.dp, end = 4.dp)
                        )
                    }

                    items(communities) { comm ->
                        val lastMsg = comm.chatMessages.lastOrNull()
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCommunitySelected(comm.id) }
                                .testTag("chat_row_${comm.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(comm.iconEmoji, fontSize = 24.sp)
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(comm.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = lastMsg?.timestamp ?: "09:00 AM",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = if (lastMsg == null) "No messaging activity." else "${lastMsg.senderName}: ${lastMsg.text}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Filled.ChevronRight, "Enter chat thread button", modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// SCREEN 9: DEEP MEMBER PROFILE & LEADER PROGRAM APPLICATION & DASHBOARDS
@Composable
fun ProfileAndLeaderScreen(
    viewModel: AgeNoBarViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val showDashboard by viewModel.showLeaderDashboard.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var notificationSettingsEnabled by remember { mutableStateOf(true) }
    var communityDigestEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 95.dp)
    ) {
        // Jane Foster style beautiful top profile header
        item {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Background cover with modern procedural organic design
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                )
                            )
                        )
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.35f),
                            radius = size.width * 0.35f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.1f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.18f),
                            radius = size.width * 0.25f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.8f)
                        )
                    }

                    // Back To Home Button at the top-left
                    IconButton(
                        onClick = { viewModel.selectTab(AppTab.Home) },
                        modifier = Modifier
                            .statusBarsPadding()
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .background(Color.White.copy(alpha = 0.7f), CircleShape)
                            .size(38.dp)
                            .testTag("profile_back_home")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Settings Gear Button at the top-right
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier
                            .statusBarsPadding()
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(Color.White.copy(alpha = 0.7f), CircleShape)
                            .size(38.dp)
                            .testTag("profile_settings_cog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Profile Settings",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // White Card panel sliding on top of Cover picture
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 190.dp),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Drag Indicator Bar at top
                        Box(
                            modifier = Modifier
                                .size(width = 44.dp, height = 4.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                        )

                        // Relative Circular Avatar
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                .padding(3.dp)
                        ) {
                            ProceduralLinkedInAvatar(
                                name = user.name,
                                sizeDp = 78,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Username and Subtitle Header
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = user.name,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "☕",
                                    fontSize = 20.sp
                                )
                            }

                            Text(
                                text = user.role,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Email representation below name
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Mail,
                                    contentDescription = "Email address",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(13.dp)
                                )
                                val userEmail = user.name.lowercase().replace(" ", "") + "@gmail.com"
                                Text(
                                    text = userEmail,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Bio line representation
                            Text(
                                text = user.bio,
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Three Metric Cards row matching photo colors and spacing
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Card 1: Balance points
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color(0xFFFEF3EB), RoundedCornerShape(16.dp))
                                        .border(BorderStroke(1.dp, Color(0xFFFCDDC8)), RoundedCornerShape(16.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("🏆", fontSize = 15.sp)
                                            Text(
                                                text = "17",
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFD35400)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "points balance",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE67E22)
                                        )
                                    }
                                }

                                // Card 2: Total Reviews
                                Box(
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .background(Color(0xFFFFF0EC), RoundedCornerShape(16.dp))
                                        .border(BorderStroke(1.dp, Color(0xFFFFDCD0)), RoundedCornerShape(16.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("💬", fontSize = 15.sp)
                                            Text(
                                                text = "28",
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFC0392B)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Total reviews",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE74C3C)
                                        )
                                    }
                                }

                                // Card 3: Saved Mentors
                                Box(
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .background(Color(0xFFF5F6F8), RoundedCornerShape(16.dp))
                                        .border(BorderStroke(1.dp, Color(0xFFE2E4E8)), RoundedCornerShape(16.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("📍", fontSize = 15.sp)
                                            Text(
                                                text = "17",
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF2C3E50)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Saved mentors",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF7F8C8D)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Saved mentorship circle circular items list
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "My Saved Mentorship Circle",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "See All",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    listOf(
                                        "Anand Verma",
                                        "Gauri Deshpande",
                                        "Savita Shinde",
                                        "Rajesh K.",
                                        "Nirmala Devi",
                                        "Karan Sharma"
                                    ).forEach { mentorName ->
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                        ) {
                                            ProceduralLinkedInAvatar(
                                                name = mentorName,
                                                sizeDp = 54,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Intergenerational Preference Configuration
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("onboarding_preference_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Your Intergenerational Path",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Are you here to learn from others, share your lifetime of expertise, or both?",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    val preferences = listOf(
                        "Learn" to "📚 I want to learn from others.",
                        "Teach" to "🎓 I want to share my experience .",
                        "Both" to "🤝 I want to learn and teach."
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        preferences.forEach { (typeKey, typeLabel) ->
                            val isSelected = user.userRoleType == typeKey
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp, 
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.changeUserRoleType(typeKey) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.changeUserRoleType(typeKey) },
                                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = typeLabel,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Reputation badges
        item {
            Text(
                text = "Your Neighborhood Trusted Reputation",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("We steer away from sterile 'Likes'. This system tracks direct contributions of kindness & wisdom awarded to you by the community.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        user.reputation.forEach { badge ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(badge.kind.emoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(badge.kind.label, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("+${badge.count}", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Community Leader Ambassador Enrollment & Dashboard
        item {
            Text(
                text = "AgeNoBar Community Leader Program",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("COMMUNITY AMBASSADOR PLATINUM STATUS", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Top senior contributors become verified experts, circles moderators, and host live voice rooms to guide members.", fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!user.isCommunityLeader) {
                        Button(
                            onClick = { viewModel.participateAsLeader() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Enrol & Apply as Circle Host", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Column {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            ) {
                                Text("✓ YOU ARE A COMMITTED CIRCLE HOST", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(6.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.setLeaderDashboard(!showDashboard) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("leader_dashboard_toggle"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(if (showDashboard) "Hide Leader Dashboard" else "Open Circle Leader Dashboard")
                            }
                        }
                    }
                }
            }
        }

        // Displays actual Leader Moderation console!
        if (showDashboard && user.isCommunityLeader) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Moderator Tools & Control Console", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Pending New Member Registrations:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))

                        // Applicant 1
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarImage(name = "Karan Sharma", size = 28)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Karan Sharma (Aspirant)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Bio: Seeking retirement planning advice.", fontSize = 10.sp, color = Color.Gray)
                                }
                                Button(
                                    onClick = {},
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("Accept", fontSize = 10.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Circle System Integrity Actions:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {},
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Flag Spammers (0)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {},
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Broadcast Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// FlowRow layout implementation backport since standard is available in Compose 1.4+ or accessible with simple layout
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}


// SCREEN 10: MINIMIZED ACTIVE VOICE FLOATING BANNER BAR (PERSISTENT ON ALL TABS!)
@Composable
fun MinimizedVoiceBar(
    viewModel: AgeNoBarViewModel,
    modifier: Modifier = Modifier
) {
    val activeRoomId by viewModel.currentVoiceRoomId.collectAsState()
    val isMinimized by viewModel.isVoiceRoomMinimized.collectAsState()
    val voiceRooms by viewModel.voiceRooms.collectAsState()

    val room = voiceRooms.firstOrNull { it.id == activeRoomId } ?: return

    if (activeRoomId != null && isMinimized) {
        // Continuous wave scale animation for high-fidelity feel (Whatsapp/Live call style)
        val infiniteTransition = rememberInfiniteTransition(label = "audio_bubble_pulse")
        val pulseScale1 by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse1"
        )
        val pulseScale2 by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.45f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse2"
        )

        Box(
            modifier = modifier
                .testTag("floating_room_mini_bar")
                .size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulse rings/halo like Whatsapp active calls
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .graphicsLayer(scaleX = pulseScale2, scaleY = pulseScale2)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .graphicsLayer(scaleX = pulseScale1, scaleY = pulseScale1)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f), CircleShape)
            )

            // Main Interactive Circle Bubble
            Card(
                modifier = Modifier
                    .size(56.dp)
                    .clickable { viewModel.maximizeVoiceRoom() },
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BorderStroke(2.dp, Color.White)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🎙️",
                            fontSize = 20.sp
                        )
                        Text(
                            text = "LIVE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.4.sp
                        )
                    }
                }
            }

            // Overlay Close (End Session) Red X Button in the upper right corner of the bubble
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 2.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF5350))
                    .border(1.5.dp, Color.White, CircleShape)
                    .clickable { viewModel.disconnectVoiceRoom() }
                    .testTag("floating_room_quit_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Hang up or End Voice Room Session",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}


// --- FLOATING CHACHI ASSISTANT BUTTON & MASCOT ---
@Composable
fun FloatingChachiAssistantButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ChachiMascotAnim")
    
    // Breathing/Scale animation (0.97f - 1.04f)
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2300, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chachiScale"
    )
    
    // Slow drift bobbing (vertical bounce)
    val bobbingOffset by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2900, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chachiBobbing"
    )
    
    // Pulse rings scaling & alpha
    val pulse1Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(2100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse1"
    )
    val pulse1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse1Alpha"
    )
    
    val pulse2Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse2"
    )
    val pulse2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse2Alpha"
    )

    val softMicGlow by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = EaseInElastic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micGlow"
    )

    Box(
        modifier = modifier
            .size(80.dp)
            .testTag("chachi_assistant_button_container"),
        contentAlignment = Alignment.Center
    ) {
        // Pulse Ring 2
        Box(
            modifier = Modifier
                .size(56.dp)
                .scale(pulse2Scale)
                .alpha(pulse2Alpha)
                .background(Color(0xFF4A55A2).copy(alpha = 0.3f), CircleShape)
        )
        
        // Pulse Ring 1
        Box(
            modifier = Modifier
                .size(56.dp)
                .scale(pulse1Scale)
                .alpha(pulse1Alpha)
                .background(Color(0xFF5A8F76).copy(alpha = 0.4f), CircleShape)
        )

        // Main Mascot Button Container
        Card(
            modifier = Modifier
                .size(58.dp)
                .offset(y = bobbingOffset.dp)
                .scale(scale)
                .clickable { onClick() }
                .testTag("chachi_mascot_fab"),
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(2.5.dp, Brush.linearGradient(listOf(Color(0xFF4A55A2), Color(0xFFC5A3FF))))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Vector mascot clipped to circle
                Image(
                    painter = painterResource(id = R.drawable.img_ai_chachi),
                    contentDescription = "AI Chachi, loving Indian grandma mascot",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                // Outer Microphone status badge overlay
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-2).dp, y = (-2).dp)
                        .background(Color(0xFF5A8F76), CircleShape)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .alpha(softMicGlow)
                            .background(Color.White, CircleShape)
                    )
                }
            }
        }
    }
}


@Composable
fun ChachiVoiceAssistantOverlay(
    viewModel: AgeNoBarViewModel,
    onDismiss: () -> Unit
) {
    var overlayState by remember { mutableStateOf("idle") } // "idle", "listening", "transcribing", "replying"
    var queryText by remember { mutableStateOf("") }
    var responseText by remember { mutableStateOf("") }
    
    val coroutineScope = rememberCoroutineScope()
    
    val infiniteTransition = rememberInfiniteTransition(label = "OverlayWaveAnim")
    
    // Wave pulse scale animations
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveScale"
    )
    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveAlpha"
    )

    // Waveform bars
    val barHeights = List(6) { index ->
        infiniteTransition.animateFloat(
            initialValue = 14f + (index * 4f),
            targetValue = 44f - (index * 2f),
            animationSpec = infiniteRepeatable(
                animation = tween(380 + (index * 70), easing = EaseInOutBounce),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }

    fun startChachiExchange(query: String) {
        queryText = query
        responseText = ""
        overlayState = "listening"
        
        coroutineScope.launch {
            delay(1700) 
            overlayState = "transcribing"
            delay(1100) 
            overlayState = "replying"
            
            viewModel.askChachi(query)
            
            responseText = when {
                query.contains("pdf", ignoreCase = true) || query.contains("download", ignoreCase = true) -> {
                    "Ah, Ramesh beta! Think of a 'PDF' as a beautiful flower pressed carefully inside a heavy recipe book. No matter which house you carry this book to, or whose hands open it, the flower looks exactly the same, with not a single petal shifted. It is simply a document that never loses its shape or looks scrambled, so you can read it with total peace of mind!"
                }
                query.contains("scam", ignoreCase = true) || query.contains("safe", ignoreCase = true) || query.contains("internet", ignoreCase = true) -> {
                    "My dear Ramesh, exploring the online world is like entering a bustling traditional bazaar—it has beautiful tapestries of knowledge to explore, but keep your bag zipped close! Remember, your bank password is like a sacred household key: never read it to anyone, especially strangers who create false urgency. If anyone pressures you, take a slow breath, shut the screen, and let's call your chosen guides. We are all here to walk beside you safely."
                }
                query.contains("story", ignoreCase = true) || query.contains("folklore", ignoreCase = true) || query.contains("wisdom", ignoreCase = true) -> {
                    "Oh, what a lovely request! Let me share a peaceful folklore with you. Long ago in a mountain village, a traveler sat by a cold hearth holding a single smooth pebble, telling children he was cooking a grand 'stone soup'. Curious, one granny brought a tiny tomato, a young boy brought coriander, and a teacher shared salt. In no time, everyone in the village sat down to a delicious feast. Love and connection, beta, are the hot soup; we only need a small stone of trust to bring everyone to the table."
                }
                else -> {
                    "I hear you, Ramesh beta. Your words carry a beautiful, thoughtful reflection. In our Wisdom Bridge, we cherish simplicity, patience, and mutual belonging above all. Take a slow sip of water, enjoy this serene day, and tell me: would you like me to guide you to any of our warm Circle gatherings, or suggest a breathing session?"
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF50D1129)) 
            .testTag("chachi_voice_assistant_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "AI CHACHI ASSISTANT",
                        color = Color(0xFFA5B4FC),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF5A8F76), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "OFFLINE ASSISTANT",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                
                IconButton(
                    onClick = { onDismiss() },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close overlay",
                        tint = Color.White
                    )
                }
            }

            // Mascot & Waves
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (overlayState != "idle") {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(waveScale)
                            .alpha(waveAlpha)
                            .background(Color(0xFF7895CB).copy(alpha = 0.2f), CircleShape)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        .border(1.5.dp, Color(0xFFC5A3FF).copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.size(110.dp),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(3.dp, Brush.linearGradient(listOf(Color(0xFF4A55A2), Color(0xFF5A8F76)))),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_ai_chachi),
                            contentDescription = "AI Chachi logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Info Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.4f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D2447)),
                    border = BorderStroke(1.dp, Color(0xFF3B488C))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (overlayState) {
                                "idle" -> "Namaste, I am listening with love..."
                                "listening" -> "Listening to your request... 🌸"
                                "transcribing" -> "Transcribing your voice notes... 👵"
                                "replying" -> "Chachi says: ❤️"
                                else -> "Namaste beta"
                            },
                            color = Color(0xFFECEFF1),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = when (overlayState) {
                                "idle" -> "Tap the microphone below to talk, or choose a topic to guide you."
                                "listening" -> "“${if (queryText.isEmpty()) "Capturing voice commands..." else queryText}”"
                                "transcribing" -> "Converting your spoken words to safe wisdom lessons..."
                                "replying" -> responseText
                                else -> ""
                            },
                            color = if (overlayState == "replying") Color(0xFFE0E6ED) else Color(0xFF90A4AE),
                            fontSize = if (overlayState == "replying") 15.sp else 13.sp,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (overlayState == "listening" || overlayState == "transcribing") {
                    Row(
                        modifier = Modifier.height(40.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        barHeights.forEach { heightVal ->
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(heightVal.value.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF818CF8), Color(0xFF34D399))
                                        ),
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                }
            }

            // Bottom controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (overlayState == "idle" || overlayState == "replying") {
                    Text(
                        text = "CHOOSE A TOPIC TO ASK CHACHI",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { startChachiExchange("Help me find experts, mentors and teachers on Wisdom Bridge") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF283161)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Find Experts 👨‍🏫", color = Color.White, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { startChachiExchange("Help me find discussions, groups and communities") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF283161)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Find Communities 👥", color = Color.White, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { startChachiExchange("Help me find recipes, festival traditions and food stories") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF283161)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Find Recipes 🍲", color = Color.White, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { startChachiExchange("Can you translate basic languages for me?") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF283161)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Translate 🗣", color = Color.White, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { startChachiExchange("Explain app navigation: How do I use Wisdom Bridge?") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF283161)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Explain App 📱", color = Color.White, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { startChachiExchange("Help me increase the text font size of the app") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF283161)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Increase Font 🔎", color = Color.White, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { startChachiExchange("Where do I locate emergency help and first aid resources?") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF283161)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Emergency Help 🆘", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }

                IconButton(
                    onClick = {
                        if (overlayState == "idle" || overlayState == "replying") {
                            startChachiExchange("How do I stay safe on the internet?")
                        } else {
                            overlayState = "idle"
                        }
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF818CF8), Color(0xFF4F46E5))
                            ),
                            CircleShape
                        )
                        .testTag("chachi_mic_toggle")
                ) {
                    Icon(
                        imageVector = if (overlayState == "listening") Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice input mic icon",
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = if (overlayState == "listening") "Tap to Stop Listening" else "Tap and Speak to Chachi",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun EntryBlossomingFlowerAnimation() {
    var stage by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1200)
            stage = (stage + 1) % 4
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val centerScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val centerRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .size(175.dp)
            .testTag("entry_blossom_animation"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(115.dp)) {
            drawCircle(
                color = Color(0xFFC8E6C9).copy(alpha = 0.35f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                )
            )
        }

        // Circular coordinates
        // Top: 🌰
        Box(modifier = Modifier.align(Alignment.TopCenter).offset(y = (-4).dp)) {
            LifecycleStageIcon(emoji = "🌰", stageIndex = 0, currentStage = stage)
        }
        // Right: 🌱
        Box(modifier = Modifier.align(Alignment.CenterEnd).offset(x = 4.dp)) {
            LifecycleStageIcon(emoji = "🌱", stageIndex = 1, currentStage = stage)
        }
        // Bottom: 🌿
        Box(modifier = Modifier.align(Alignment.BottomCenter).offset(y = 4.dp)) {
            LifecycleStageIcon(emoji = "🌿", stageIndex = 2, currentStage = stage)
        }
        // Left: 🌸
        Box(modifier = Modifier.align(Alignment.CenterStart).offset(x = (-4).dp)) {
            LifecycleStageIcon(emoji = "🌸", stageIndex = 3, currentStage = stage)
        }

        // Centered Blossom
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFF9C4).copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    )
                )
                .graphicsLayer(
                    scaleX = centerScale,
                    scaleY = centerScale,
                    rotationZ = centerRotation
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("🌸", fontSize = 52.sp)
        }

        Text("✨", fontSize = 16.sp, modifier = Modifier.align(Alignment.TopEnd).offset(x = (-16).dp, y = 16.dp))
        Text("✨", fontSize = 14.sp, modifier = Modifier.align(Alignment.BottomStart).offset(x = 16.dp, y = (-16).dp))
    }
}

@Composable
fun LifecycleStageIcon(
    emoji: String,
    stageIndex: Int,
    currentStage: Int
) {
    val isActive = stageIndex == currentStage
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.35f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )
    val alpha by animateFloatAsState(
        targetValue = if (isActive) 1.0f else 0.6f,
        animationSpec = tween(500)
    )

    Card(
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFFE8F5E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 6.dp else 2.dp
        ),
        border = if (isActive) BorderStroke(1.5.dp, Color(0xFF81C784)) else null,
        modifier = Modifier
            .size(38.dp)
            .scale(scale)
            .alpha(alpha)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 20.sp)
        }
    }
}

@Composable
fun OnboardingRoleSelectionOverlay(
    viewModel: AgeNoBarViewModel,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = { /* forces choice */ },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            shape = RoundedCornerShape(0.dp), // Full screen experience
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Heartfelt welcome and mascot decoration with flower lifecycle blossom
                EntryBlossomingFlowerAnimation()
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Welcome to Wisdom Bridge",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextCharcoalDark,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Connecting senior citizens and passionate learners for rich, intergenerational guidance with love.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextGraySub,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                
                Spacer(modifier = Modifier.height(36.dp))
                
                Text(
                    text = "Who are you today?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextCharcoalDark
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Option A: Learner Mode
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.changeUserRoleType("Learn")
                        },
                    border = BorderStroke(2.dp, PrimaryiOSBlue.copy(alpha = 0.8f)),
                    colors = CardDefaults.cardColors(containerColor = SolidCardWhite),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📚", fontSize = 36.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Enter Learner Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryiOSBlue
                            )
                            Text(
                                text = "Search subject area experts, ask questions, and attend circles.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGraySub
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                // Option B: Teacher Mode
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.changeUserRoleType("Teach")
                        },
                    border = BorderStroke(2.dp, PrimaryiOSCoral.copy(alpha = 0.8f)),
                    colors = CardDefaults.cardColors(containerColor = SolidCardWhite),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎓", fontSize = 36.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Enter Teacher Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryiOSCoral
                            )
                            Text(
                                text = "Share your expertise, help answer questions, and earn rewards credited to your UPI wallet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGraySub
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(30.dp))
                
                Text(
                    text = "You can instantly swap between paths any time inside the app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGraySub,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Composable
fun CustomFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
            .clickable(onClick = onClick)
            .height(34.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PremiumLinkedInMentorCard(
    expert: Expert,
    isFollowed: Boolean,
    bookingTime: String? = null,
    onBookSession: () -> Unit,
    onContactExpert: () -> Unit,
    onViewProfile: () -> Unit,
    onFollowToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, BorderLightSystem),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewProfile)
            .testTag("premium_mentor_card_${expert.id}")
    ) {
        // LinkedIn style profile cover & overlapping avatar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
        ) {
            ExpertCoverPhoto(
                expert = expert,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.TopCenter)
            )

            Box(
                modifier = Modifier
                    .padding(start = 18.dp)
                    .align(Alignment.BottomStart)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    ProceduralLinkedInAvatar(
                        name = expert.name,
                        sizeDp = 60,
                        modifier = Modifier
                            .background(Color.White, CircleShape)
                            .border(BorderStroke(2.5.dp, Color.White), CircleShape)
                    )
                    // Online indicator
                    if (expert.isOnlineNow) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(14.dp)
                                .background(Color(0xFF2ECC71), CircleShape) // Vibrant online green
                                .border(BorderStroke(2.2.dp, Color.White), CircleShape)
                        )
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 18.dp, top = 6.dp)) {
            // 1. Name Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = expert.name,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    // Verification Check badge
                    if (expert.isVerifiedExpert) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified Blue Badge",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "Verified Mentor",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Follow tag bookmark
                IconButton(onClick = onFollowToggle, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (isFollowed) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Follow bookmark toggle",
                        tint = if (isFollowed) Color(0xFFF1C40F) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Title Section
            Text(
                text = expert.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 3. Experience Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "💼 ${expert.yearsOfExperience} Years Experience",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "💬 " + expert.languages.joinToString(" • "),
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bio Summary Preview (Reduced Visual Clutter)
            Text(
                text = expert.bio,
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                lineHeight = 17.sp,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Rating + Sessions Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Rating Star icon",
                    modifier = Modifier.size(15.dp),
                    tint = Color(0xFFF1C40F)
                )
                val formattedRating = String.format(java.util.Locale.US, "%.2f", expert.rating)
                Text(
                    text = formattedRating,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "•   ${expert.peopleHelpedCount} Sessions",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Price Section
            val feeDisplay = if (expert.flatSessionFee > 0) "₹${expert.flatSessionFee}" else "₹299"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = feeDisplay,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "/ 30 min class",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Availability Block (Glassmorphism Styled Panel Container)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF2ECC71), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Available Today",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF27AE60)
                        )
                    )
                }

                val upcomingSlots = if (expert.activeOfflineAvailability.contains("Value", ignoreCase = true) || expert.activeOfflineAvailability.isEmpty()) {
                    "Weekdays 3:00 PM - 7:00 PM"
                } else {
                    expert.activeOfflineAvailability
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = "Upcoming slot list calendar icon",
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Slots: $upcomingSlots",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // 10. Response Speed (Airbnb-style)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⚡",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Usually responds within 2 hours",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 3 & 8. Booked Separated Confirmation Badge (Never mixed with availability)
            if (bookingTime != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFA5D6A7)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Booking Confirmed CheckCircle Badge",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Class Scheduled Successfully",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = "Timing: $bookingTime",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF388E3C)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(14.dp))

            // 7. Actions Section (Contact and Book buttons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Outlined Contact Expert button
                OutlinedButton(
                    onClick = onContactExpert,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Forum,
                            contentDescription = "Contact bubble",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("Contact Expert", fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                val hasBooked = bookingTime != null
                // Book Session Primary button (Changes to Green when booked)
                Button(
                    onClick = {
                        if (!hasBooked) {
                            onBookSession()
                        }
                    },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasBooked) Color(0xFF2ECC71) else MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (hasBooked) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Booked Status check mark icon",
                                modifier = Modifier.size(15.dp),
                                tint = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = "Booking calendar icon symbol",
                                modifier = Modifier.size(15.dp),
                                tint = Color.White
                            )
                        }
                        Text(
                            text = if (hasBooked) "✓ Booked" else "Book Session",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

fun getExpertThemeGradients(expert: Expert): Pair<Color, Color> {
    val cat = expert.category.lowercase()
    val title = expert.title.lowercase()
    val skillTags = expert.skillsTags.map { it.lowercase() }
    return when {
        title.contains("math") || title.contains("arithmetic") || title.contains("algebra") || skillTags.any { it.contains("math") || it.contains("algebra") } -> {
            Pair(Color(0xFF8B1A1A), Color(0xFFC0392B)) // red
        }
        title.contains("finance") || title.contains("wealth") || title.contains("provident") || title.contains("saving") || skillTags.any { it.contains("finance") || it.contains("saving") } -> {
            Pair(Color(0xFF2D5016), Color(0xFF4A8B25)) // green
        }
        title.contains("language") || title.contains("sanskrit") || title.contains("english") || title.contains("hindi") || skillTags.any { it.contains("language") || it.contains("sanskrit") || it.contains("english") || it.contains("hindi") } -> {
            Pair(Color(0xFF1A4A8B), Color(0xFF2E6DB4)) // blue
        }
        title.contains("yoga") || title.contains("medit") || title.contains("nutri") || title.contains("physio") || title.contains("well") || skillTags.any { it.contains("yoga") || it.contains("well") || it.contains("nutri") || it.contains("physio") } -> {
            Pair(Color(0xFF6B1A8B), Color(0xFF9B4ABB)) // purple
        }
        title.contains("legal") || title.contains("law") || title.contains("arbitrat") || title.contains("attorney") || skillTags.any { it.contains("legal") || it.contains("law") } -> {
            Pair(Color(0xFF8B6A1A), Color(0xFFC09B25)) // gold
        }
        title.contains("science") || title.contains("physics") || title.contains("stem") || title.contains("tech") || title.contains("chem") || skillTags.any { it.contains("physics") || it.contains("science") || it.contains("tech") } -> {
            Pair(Color(0xFF1A6B8B), Color(0xFF25A0C0)) // teal
        }
        else -> {
            // Default based on category
            when {
                cat.contains("math") -> Pair(Color(0xFF8B1A1A), Color(0xFFC0392B))
                cat.contains("finance") -> Pair(Color(0xFF2D5016), Color(0xFF4A8B25))
                cat.contains("language") || cat.contains("stories") || cat.contains("culture") -> Pair(Color(0xFF1A4A8B), Color(0xFF2E6DB4))
                cat.contains("wellness") || cat.contains("health") -> Pair(Color(0xFF6B1A8B), Color(0xFF9B4ABB))
                cat.contains("legal") -> Pair(Color(0xFF8B6A1A), Color(0xFFC09B25))
                cat.contains("science") || cat.contains("nature") || cat.contains("lifestyle") -> Pair(Color(0xFF1A6B8B), Color(0xFF25A0C0))
                else -> Pair(Color(0xFF1A6B8B), Color(0xFF25A0C0)) // Default nice teal gradient
            }
        }
    }
}

@Composable
fun CompactLinkedInMentorCard(
    expert: Expert,
    isFollowed: Boolean,
    bookingTime: String? = null,
    onBookSession: () -> Unit,
    onContactExpert: () -> Unit,
    onViewProfile: () -> Unit,
    onFollowToggle: () -> Unit
) {
    val themeGradients = remember(expert.id) { getExpertThemeGradients(expert) }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onViewProfile)
            .testTag("compact_mentor_card_${expert.id}")
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left side (30% width)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.3f)
                    .background(Brush.verticalGradient(listOf(themeGradients.first, themeGradients.second))),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Large avatar centered (56px)
                    Box(modifier = Modifier.size(56.dp)) {
                        ProceduralLinkedInAvatar(
                            name = expert.name,
                            sizeDp = 56,
                            modifier = Modifier.clip(CircleShape).background(Color.White)
                        )
                        // Small green dot if active now
                        if (expert.isOnlineNow) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(12.dp)
                                    .background(Color(0xFF2ECC71), CircleShape)
                                    .border(BorderStroke(1.5.dp, Color.White), CircleShape)
                            )
                        }
                    }
                    
                    // Star rating below avatar (e.g. ⭐ 4.88)
                    val formattedRating = remember(expert.rating) { String.format(java.util.Locale.US, "%.2f", expert.rating) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⭐ $formattedRating",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Right side (70% width)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.7f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Row 1: Name (bold 15px) + Verified ✓ badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = expert.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (expert.isVerifiedExpert) {
                        Text(
                            text = "✓",
                            color = Color(0xFF2F80ED),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Row 2: Title in brand red, 12px
                Text(
                    text = expert.title,
                    color = Color(0xFFC0392B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                // Row 3: Experience · Language (grey, 11px)
                Text(
                    text = "${expert.yearsOfExperience} yrs Exp • ${expert.languages.firstOrNull() ?: "Hindi"}",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                
                // Row 4: Expertise tags — max 2 pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    expert.skillsTags.take(2).forEach { skill ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF2F4F8), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = skill,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
                
                // Row 5: Rate (bold green) + Availability (small grey text)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val rate = if (expert.flatSessionFee > 0) expert.flatSessionFee else 199
                    Text(
                        text = "₹$rate/30m",
                        color = Color(0xFF27AE60),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Available Today",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
                
                // Row 6: Two buttons side by side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val rate = if (expert.flatSessionFee > 0) expert.flatSessionFee else 199
                    Button(
                        onClick = onBookSession,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .testTag("book_button_${expert.id}")
                    ) {
                        Text(
                            text = "Book ₹$rate",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    OutlinedButton(
                        onClick = onContactExpert,
                        border = BorderStroke(1.dp, Color(0xFFC0392B)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .testTag("message_button_${expert.id}")
                    ) {
                        Text(
                            text = "💬 Message",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC0392B)
                        )
                    }
                }
            }
        }
    }
}

data class TopicItem(
    val name: String,
    val imageRes: Int,
    val emoji: String,
    val tag: String,
    val assetPath: String? = null
)

fun getGroupedTopics(categoryName: String, fallbackSubExperiences: List<SubExperience>): List<Pair<String, List<TopicItem>>> {
    val cleanName = categoryName.uppercase()
    return when {
        cleanName.contains("LEARN") || cleanName.contains("GROW") -> listOf(
            "Academic & Science Topics" to listOf(
                TopicItem("Maths", R.drawable.img_cat_education, "📐", "Maths"),
                TopicItem("Science", R.drawable.img_cat_science, "🥼", "Science"),
                TopicItem("Languages", R.drawable.img_cat_stories, "🗣️", "Languages", "languages2.jpg"),
                TopicItem("Physics", R.drawable.img_cat_science, "🧲", "Physics"),
                TopicItem("Technology", R.drawable.img_cat_careers, "💻", "Technology", "technology_.webp")
            ),
            "Professional, Banking & Legal" to listOf(
                TopicItem("Finance", R.drawable.img_cat_finance, "💰", "Finance", "finance.jpg"),
                TopicItem("Career", R.drawable.img_cat_careers, "💼", "Career"),
                TopicItem("Banking", R.drawable.img_cat_finance, "🏦", "Banking", "banking.webp"),
                TopicItem("Legal", R.drawable.img_cat_legal, "⚖️", "Legal"),
                TopicItem("Attorneys", R.drawable.img_cat_legal, "📜", "Attorneys")
            )
        )
        cleanName.contains("HEALTH") || cleanName.contains("WELLNESS") -> listOf(
            "Mindfulness & Healing" to listOf(
                TopicItem("Ayurveda", R.drawable.img_cat_ayurveda, "🌿", "Ayurveda"),
                TopicItem("Yoga", R.drawable.img_cat_wellness, "🧘", "Yoga", "yoga.png"),
                TopicItem("Meditation", R.drawable.img_cat_wellness, "🫁", "Meditation")
            ),
            "Therapy & Care" to listOf(
                TopicItem("Physiotherapy", R.drawable.img_cat_physio, "🦵", "Physiotherapy"),
                TopicItem("Nutritionist", R.drawable.img_cat_ayurveda, "🍎", "Nutritionist", "nutrition.webp")
            )
        )
        cleanName.contains("RECIPES") || cleanName.contains("TRADITIONS") -> listOf(
            "Traditional Kitchen" to listOf(
                TopicItem("Cuisine", R.drawable.img_cat_cooking, "🍲", "Cuisine"),
                TopicItem("Cooking", R.drawable.img_cat_cooking, "🍛", "Cooking")
            ),
            "Festivals & Heritage" to listOf(
                TopicItem("Festival", R.drawable.img_cat_recipes, "🎉", "Festival"),
                TopicItem("Family Traditions", R.drawable.img_cat_recipes, "📜", "Family Traditions")
            )
        )
        cleanName.contains("ARTS") || cleanName.contains("MUSIC") || cleanName.contains("CULTURE") -> listOf(
            "Classical vocal, Instruments and Bhajans" to listOf(
                TopicItem("Vocal", R.drawable.img_cat_music, "🎤", "Vocal", "carnatic.webp"),
                TopicItem("Veena", R.drawable.img_cat_music, "🪕", "Veena", "veena.webp"),
                TopicItem("Violin", R.drawable.img_cat_music, "🎻", "Violin", "violin.webp"),
                TopicItem("Bhajans and Shlokas", R.drawable.img_cat_music, "🙏", "Bhajans and Shlokas")
            ),
            "Dance forms and Crafts" to listOf(
                TopicItem("Dance", R.drawable.img_cat_dance, "💃", "Dance"),
                TopicItem("Traditional Arts", R.drawable.img_cat_recipes, "🎨", "Traditional Arts")
            )
        )
        cleanName.contains("STORIES") || cleanName.contains("HERITAGE") -> listOf(
            "Children Wisdom Lore" to listOf(
                TopicItem("Panchatantra", R.drawable.img_cat_stories, "🦁", "Panchatantra"),
                TopicItem("Sanskrit Stories", R.drawable.img_cat_stories, "🗣️", "Sanskrit Stories"),
                TopicItem("Story Time", R.drawable.img_cat_stories, "🎙️", "Story Time")
            ),
            "Epics & Legends" to listOf(
                TopicItem("Ramayana", R.drawable.img_cat_stories, "🏹", "Ramayana"),
                TopicItem("Mahabharata", R.drawable.img_cat_stories, "🛡️", "Mahabharata"),
                TopicItem("Indian Heritage", R.drawable.img_cat_stories, "🏛️", "Indian Heritage")
            )
        )
        cleanName.contains("NATURE") || cleanName.contains("LIFESTYLE") -> listOf(
            "Green Balcony & Cultivation" to listOf(
                TopicItem("Gardening", R.drawable.img_cat_gardening, "🌱", "Gardening"),
                TopicItem("Terrace Gardening", R.drawable.img_cat_gardening, "🪴", "Terrace Gardening", "terrace_.webp")
            ),
            "Organic Gardening & Miniature Bonsai" to listOf(
                TopicItem("Organic Farming", R.drawable.img_cat_gardening, "🌽", "Organic Farming"),
                TopicItem("Bonsai", R.drawable.img_cat_gardening, "🌳", "Bonsai", "bonsai.png")
            )
        )
        else -> {
            if (fallbackSubExperiences.isNotEmpty()) {
                listOf(
                    "Select a Topic" to fallbackSubExperiences.map { sub ->
                        TopicItem(
                            name = sub.name,
                            imageRes = sub.imageRes,
                            emoji = sub.emoji,
                            tag = sub.name
                        )
                    }
                )
            } else {
                emptyList()
            }
        }
    }
}

@Composable
fun LocalAssetImage(
    assetPath: String,
    contentDescription: String?,
    contentScale: ContentScale,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bitmap by remember(assetPath) { mutableStateOf<ImageBitmap?>(null) }
    var loadFailed by remember(assetPath) { mutableStateOf(false) }

    LaunchedEffect(assetPath) {
        try {
            context.assets.open(assetPath).use { inputStream ->
                val bytes = inputStream.readBytes()
                val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (decoded != null) {
                    bitmap = decoded.asImageBitmap()
                    loadFailed = false
                } else {
                    loadFailed = true
                }
            }
        } catch (e: Exception) {
            loadFailed = true
        }
    }

    if (bitmap != null && !loadFailed) {
        Image(
            bitmap = bitmap!!,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF2C3E50), Color(0xFF000000))
                    )
                )
        )
    }
}

@Composable
fun TopicImage(topic: TopicItem, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var bitmap by remember(topic.assetPath) { mutableStateOf<ImageBitmap?>(null) }
    var loadFailed by remember(topic.assetPath) { mutableStateOf(false) }

    LaunchedEffect(topic.assetPath) {
        if (topic.assetPath != null) {
            try {
                context.assets.open("topics/${topic.assetPath}").use { inputStream ->
                    val bytes = inputStream.readBytes()
                    // If the asset file size is very small (like our 69-bytes transparent mock files),
                    // treat it as loadFailed so the gorgeous gradient fallback is shown instead.
                    if (bytes.size > 200) {
                        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (decoded != null) {
                            bitmap = decoded.asImageBitmap()
                            loadFailed = false
                        } else {
                            loadFailed = true
                        }
                    } else {
                        loadFailed = true
                    }
                }
            } catch (e: Exception) {
                loadFailed = true
            }
        } else {
            loadFailed = true
        }
    }

    if (bitmap != null && !loadFailed) {
        Image(
            bitmap = bitmap!!,
            contentDescription = topic.name,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else if (topic.imageRes != 0) {
        Image(
            painter = painterResource(id = topic.imageRes),
            contentDescription = topic.name,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        // Render a gorgeous dynamic gradient background with a subtle abstract design
        val gradientColors = remember(topic.name) {
            val hash = topic.name.hashCode()
            val index = kotlin.math.abs(hash) % 6
            listOf(
                listOf(Color(0xFF8E24AA), Color(0xFF3F51B5)), // Purple to Royal Blue
                listOf(Color(0xFFE91E63), Color(0xFFFF5722)), // Pink to Deep Orange
                listOf(Color(0xFF009688), Color(0xFF004D40)), // Teal to Forest Green
                listOf(Color(0xFFFF9800), Color(0xFFE65100)), // Warm Orange to Amber
                listOf(Color(0xFF2196F3), Color(0xFF00BCD4)), // Blue to Cyan
                listOf(Color(0xFF673AB7), Color(0xFFE91E63))  // Indigo to Pink
            )[index]
        }
        
        Box(
            modifier = modifier
                .background(Brush.linearGradient(gradientColors))
                .fillMaxSize()
        )
    }
}

@Composable
fun TopicVisualCard(
    topic: TopicItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(width = 150.dp, height = 110.dp)
            .clickable(onClick = onClick)
            .testTag("topic_visual_card_${topic.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isSelected) 3.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            TopicImage(
                topic = topic,
                modifier = Modifier.fillMaxSize()
            )
            
            // Shaded dark vertical gradient overlay exactly matching luxury bookbuy spa styling
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.25f), CircleShape)
                            .size(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = topic.emoji,
                            fontSize = 14.sp
                        )
                    }
                    
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .background(Color.White, CircleShape)
                                .size(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
                
                Text(
                    text = topic.name.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CategoryDetailScreen(
    category: MainCategory,
    viewModel: AgeNoBarViewModel,
    onBack: () -> Unit,
    onSubcategoryClick: (SubExperience) -> Unit,
    onExpertClick: (Expert) -> Unit,
    onCommunityClick: (Community) -> Unit,
    onEventClick: (CommunityEvent) -> Unit
) {
    val experts by viewModel.experts.collectAsState()
    val communities by viewModel.communities.collectAsState()
    val voiceRooms by viewModel.voiceRooms.collectAsState()
    val followedIds by viewModel.followedExpertIds.collectAsState()
    val bookedSessions by viewModel.bookedSessionExpertIds.collectAsState()

    var activeTab by remember { mutableStateOf("FIND AN EXPERT") } // "FIND AN EXPERT", "JOIN A CIRCLE"
    var selectedFilterChip by remember { mutableStateOf("All") }
    var joinedCommunitiesLocal by remember { mutableStateOf(setOf<String>()) }
    var bookedExpertNameForDialog by remember { mutableStateOf<String?>(null) }
    var viewModeState by remember { mutableStateOf("compact") } // "compact", "detailed", "ai_match"
    var discoveryFilterState by remember { mutableStateOf("All Experts") }
    var bookingExpertForSlotsDialog by remember { mutableStateOf<Expert?>(null) }
    var showAllExperts by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val showStickyHeader by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 80
        }
    }

    val searchInterestQuery by viewModel.searchInterestQuery.collectAsState()
    val aiRecommendedExperts by viewModel.aiRecommendedExperts.collectAsState()
    val isAiRecommending by viewModel.isAiRecommending.collectAsState()
    val aiRecommendationMessage by viewModel.aiRecommendationMessage.collectAsState()

    LaunchedEffect(discoveryFilterState, selectedFilterChip) {
        if (discoveryFilterState == "AI Matches") {
            val query = if (selectedFilterChip == "All") category.name else selectedFilterChip
            viewModel.searchTeachersByInterest(query)
        }
    }

    // Dynamic Filter Chips for subcategories depending on the category selected
    val filterChips = when (category.name.uppercase()) {
        "LEARN & GROW", "LEARN AND GROW" -> listOf("All", "Maths", "Science", "Languages", "Finance", "Legal")
        "HEALTH & WELLNESS" -> listOf("All", "Wellness")
        "RECIPES & TRADITIONS", "RECIPES AND TRADITIONS" -> listOf("All", "Wellness")
        "ARTS, MUSIC & CULTURE", "ARTS & CULTURE", "ARTS, MUSIC AND CULTURE" -> listOf("All", "Music")
        "STORIES & HERITAGE", "STORIES AND HERITAGE" -> listOf("All", "Languages")
        "NATURE & LIFESTYLE", "NATURE AND LIFESTYLE" -> listOf("All", "Gardening")
        else -> listOf("All")
    }

    val matchesFilter: (Expert, String) -> Boolean = { expert, filter ->
        if (filter == "All") true
        else expert.topic == filter.lowercase()
    }

    // Filtered experts relevant to this category name, AND further filtered by subcategory chip instantly
    val filteredExperts = experts.filter { expert ->
        val belongsToCategory = when (category.name.uppercase()) {
            "LEARN & GROW", "LEARN AND GROW" -> expert.topic in listOf("maths", "science", "languages", "finance", "legal")
            "HEALTH & WELLNESS" -> expert.topic == "wellness"
            "RECIPES & TRADITIONS", "RECIPES AND TRADITIONS" -> expert.topic == "wellness"
            "ARTS, MUSIC & CULTURE", "ARTS & CULTURE", "ARTS, MUSIC AND CULTURE" -> expert.topic == "music"
            "STORIES & HERITAGE", "STORIES AND HERITAGE" -> expert.topic == "languages"
            "NATURE & LIFESTYLE", "NATURE AND LIFESTYLE" -> expert.topic == "gardening"
            else -> true
        }
        belongsToCategory && matchesFilter(expert, selectedFilterChip)
    }

    // Additional filter based on discovery filter state (Available Today, Evening, Weekend)
    val displayedExperts = filteredExperts.filter { expert ->
        when (discoveryFilterState) {
            "Available Today" -> {
                val calendar = java.util.Calendar.getInstance()
                val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
                val avail = expert.activeOfflineAvailability.lowercase()
                if (avail.contains("daily")) true
                else when (dayOfWeek) {
                    java.util.Calendar.SUNDAY -> avail.contains("sun") || avail.contains("weekend")
                    java.util.Calendar.MONDAY -> avail.contains("mon") || avail.contains("weekdays")
                    java.util.Calendar.TUESDAY -> avail.contains("tue") || avail.contains("weekdays")
                    java.util.Calendar.WEDNESDAY -> avail.contains("wed") || avail.contains("weekdays")
                    java.util.Calendar.THURSDAY -> avail.contains("thu") || avail.contains("weekdays")
                    java.util.Calendar.FRIDAY -> avail.contains("fri") || avail.contains("weekdays")
                    java.util.Calendar.SATURDAY -> avail.contains("sat") || avail.contains("weekend")
                    else -> false
                }
            }
            "Evening" -> {
                val avail = expert.activeOfflineAvailability.lowercase()
                avail.contains("pm") && (avail.contains("3 pm") || avail.contains("4 pm") || avail.contains("5 pm") || avail.contains("6 pm") || avail.contains("7 pm") || avail.contains("8 pm") || avail.contains("evening"))
            }
            "Weekend" -> {
                val avail = expert.activeOfflineAvailability.lowercase()
                avail.contains("sat") || avail.contains("sun") || avail.contains("weekend")
            }
            else -> true
        }
    }

    // Filtered communities relevant to category
    val filteredCommunities = communities.filter { comm ->
        val belongsToCategory = comm.category.contains(category.name, ignoreCase = true) ||
                category.subExperiences.any { sub -> comm.name.contains(sub.name, ignoreCase = true) || comm.description.contains(sub.name, ignoreCase = true) }
        val matchesChip = if (selectedFilterChip == "All") true else {
            val f = selectedFilterChip.lowercase()
            comm.name.lowercase().contains(f) || comm.description.lowercase().contains(f) || comm.category.lowercase().contains(f)
        }
        belongsToCategory && matchesChip
    }.ifEmpty {
        communities.filter { comm ->
            if (selectedFilterChip == "All") true else {
                val f = selectedFilterChip.lowercase()
                comm.name.lowercase().contains(f) || comm.description.lowercase().contains(f) || comm.category.lowercase().contains(f)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
                        // Hero section / header overlay
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Image(
                        painter = painterResource(id = category.imageRes),
                        contentDescription = category.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0.5f to Color.Transparent,
                                    1.0f to Color.Black.copy(alpha = 0.90f)
                                )
                            )
                    )

                    // Row with back icon and name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.44f), RoundedCornerShape(20.dp))
                                .size(40.dp)
                                .testTag("category_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${category.emoji} Wisdom Bridge",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Large Title and Descriptor
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 26.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = category.desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // NAVIGATION BAR: ONE SCREEN WITH TWO TABS
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("FIND AN EXPERT", "JOIN A CIRCLE").forEach { tabName ->
                        val isSelected = activeTab == tabName
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { activeTab = tabName }
                                .padding(vertical = 10.dp)
                                .testTag("category_tab_${tabName.replace(" ", "_")}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (tabName == "FIND AN EXPERT") "👨‍🏫" else "👥",
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = tabName,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // TAB 1 CONTENT: FIND AN EXPERT
            if (activeTab == "FIND AN EXPERT") {

                    // Collate multiple categories/topics into one single visual slider/carousel
                    val groupedTopics = getGroupedTopics(category.name, category.subExperiences)
                    val collatedTopics = groupedTopics.flatMap { it.second }.distinctBy { it.name }
                    val singleTitle = when (category.name.uppercase()) {
                        "LEARN & GROW", "LEARN AND GROW" -> "Academic & Professional Topics"
                        "HEALTH & WELLNESS" -> "Health, Mindfulness & Therapy"
                        "RECIPES & TRADITIONS", "RECIPES AND TRADITIONS" -> "Traditional Kitchen & Heritage"
                        "ARTS, MUSIC & CULTURE", "ARTS & CULTURE", "ARTS, MUSIC AND CULTURE" -> "Classical Music, Vocal & Arts"
                        "STORIES & HERITAGE", "STORIES AND HERITAGE" -> "Wisdom Lore, Epics & Legends"
                        "NATURE & LIFESTYLE", "NATURE AND LIFESTYLE" -> "Nature, Gardening & Lifestyle"
                        else -> "Topics of Interest"
                    }

                    if (collatedTopics.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 18.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = singleTitle,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "See All",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .clickable { 
                                                selectedFilterChip = "All"
                                                showAllExperts = false
                                            }
                                    )
                                }

                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(collatedTopics) { topic ->
                                        val isSelected = selectedFilterChip.lowercase() == topic.tag.lowercase()
                                        TopicVisualCard(
                                            topic = topic,
                                            isSelected = isSelected,
                                            onClick = {
                                                selectedFilterChip = if (isSelected) "All" else topic.name
                                                showAllExperts = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                item {
                    // LinkedIn / Airbnb Style Search Filter Chips LazyRow
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf("All Experts", "AI Matches", "Available Today", "Evening", "Weekend")) { chip ->
                            val isSelected = discoveryFilterState == chip
                            val bgSelected = MaterialTheme.colorScheme.primary
                            val bgUnselected = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            val borderCol = if (isSelected) MaterialTheme.colorScheme.primary else BorderLightSystem
                            val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            val icon = when (chip) {
                                "All Experts" -> "👥 "
                                "AI Matches" -> "✨ "
                                "Available Today" -> "📅 "
                                "Evening" -> "🌙 "
                                "Weekend" -> "🗓️ "
                                else -> ""
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) bgSelected else bgUnselected,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .border(BorderStroke(1.dp, borderCol), RoundedCornerShape(20.dp))
                                    .clickable {
                                        discoveryFilterState = chip
                                        viewModeState = if (chip == "AI Matches") "ai_match" else "compact"
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .testTag("filter_chip_${chip.lowercase().replace(" ", "_")}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = icon,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = chip,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }

                if (viewModeState == "ai_match") {
                    if (isAiRecommending) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AI Chachi is matching experts...",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    } else if (aiRecommendationMessage.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    AvatarImage(name = "ai_chachi", size = 30)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Chachi's AI Matchmaker 👵",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = aiRecommendationMessage,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (aiRecommendedExperts.isEmpty() && !isAiRecommending) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, BorderLightSystem)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("🔍", fontSize = 34.sp)
                                    Text("No direct AI matches found yet for \"${if (selectedFilterChip == "All") category.name else selectedFilterChip}\".", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    Text("But you can still browse other excellent experts or adjust topics and subcategory filter chips!", fontSize = 12.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                            }
                        }
                    } else {
                        items(aiRecommendedExperts) { expert ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                CompactLinkedInMentorCard(
                                    expert = expert,
                                    isFollowed = followedIds.contains(expert.id),
                                    bookingTime = bookedSessions[expert.id],
                                    onBookSession = {
                                        bookingExpertForSlotsDialog = expert
                                    },
                                    onContactExpert = {
                                        viewModel.startDirectChat(expert.id, "Hello, I would like to establish personal guidance under your mentorship.")
                                        viewModel.selectDirectConversation(expert.id)
                                        viewModel.selectTab(AppTab.Messages)
                                    },
                                    onViewProfile = { onExpertClick(expert) },
                                    onFollowToggle = { viewModel.toggleFollowExpert(expert.id) }
                                )
                            }
                        }
                    }
                } else {
                    if (displayedExperts.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, BorderLightSystem)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("🔍", fontSize = 34.sp)
                                    Text("No experts matching current filters yet.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Try selecting another filter chip to connect with other real people.", fontSize = 12.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                            }
                        }
                    } else {
                        val visibleExperts = if (showAllExperts) displayedExperts else displayedExperts.take(5)
                        items(visibleExperts) { expert ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                CompactLinkedInMentorCard(
                                    expert = expert,
                                    isFollowed = followedIds.contains(expert.id),
                                    bookingTime = bookedSessions[expert.id],
                                    onBookSession = {
                                        bookingExpertForSlotsDialog = expert
                                    },
                                    onContactExpert = {
                                        viewModel.startDirectChat(expert.id, "Hello, I would like to establish personal guidance under your mentorship.")
                                        viewModel.selectDirectConversation(expert.id)
                                        viewModel.selectTab(AppTab.Messages)
                                    },
                                    onViewProfile = { onExpertClick(expert) },
                                    onFollowToggle = { viewModel.toggleFollowExpert(expert.id) }
                                )
                            }
                        }

                        if (!showAllExperts && displayedExperts.size > 5) {
                            item {
                                val hiddenCount = displayedExperts.size - 5
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showAllExperts = true }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "See $hiddenCount more experts →",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC0392B),
                                        style = androidx.compose.ui.text.TextStyle(
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2 CONTENT: JOIN A CIRCLE
            if (activeTab == "JOIN A CIRCLE") {
                if (filteredCommunities.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, BorderLightSystem)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("👥", fontSize = 34.sp)
                                Text("No active circles found in this category.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    items(filteredCommunities) { comm ->
                        val isJoined = joinedCommunitiesLocal.contains(comm.id)
                        
                        // Circle details card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable { onCommunityClick(comm) }
                                .testTag("circle_details_card_${comm.id}"),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.2.dp, BorderLightSystem)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                // Cover styling: beautiful colorful background gradient
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                                    category.bgGrad.last().copy(alpha = 0.5f)
                                                )
                                            ),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.align(Alignment.BottomStart)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(Color.White.copy(alpha = 0.3f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(comm.iconEmoji, fontSize = 20.sp)
                                        }
                                        Text(
                                            text = comm.name,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Description
                                Text(
                                    text = comm.description,
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                                Spacer(modifier = Modifier.height(10.dp))

                                // Community leader and metrics
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("👑", fontSize = 10.sp)
                                            }
                                            Text(
                                                text = "Leader: " + (comm.moderators.firstOrNull() ?: "Ramesh Kumar"),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "👥 ${comm.memberCount} members",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                text = "•   12 Discussions Today",
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    // Join Button (with join/joined state toggle)
                                    Button(
                                        onClick = {
                                            joinedCommunitiesLocal = if (isJoined) {
                                                joinedCommunitiesLocal - comm.id
                                            } else {
                                                joinedCommunitiesLocal + comm.id
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isJoined) MaterialTheme.colorScheme.outline.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary,
                                            contentColor = if (isJoined) MaterialTheme.colorScheme.onSurface else Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(36.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp)
                                    ) {
                                        Text(
                                            text = if (isJoined) "Joined ✓" else "Join",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // COMPACT LIVE ROOMS STRIP NEAR THE BOTTOM
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "🔴 LIVE AUDIO DISCUSSION ROOMS",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE74C3C), // Vibrant alert red
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.2.dp, BorderLightSystem),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (voiceRooms.isEmpty()) {
                                Text(
                                    text = "No live voice rooms active right now. Check back soon!",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(4.dp)
                                )
                            } else {
                                voiceRooms.take(3).forEach { room ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.joinVoiceRoom(room.id) }
                                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .background(Color(0xFFE74C3C).copy(alpha = 0.1f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("🎙️", fontSize = 18.sp)
                                            }
                                            Column {
                                                Text(
                                                    text = room.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "${room.activeSpeakerCount + room.totalListenerCount} participating",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.secondary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Text(
                                            text = "Tap to Join  ➔",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 10. Sliding Bottom Sheet for Slot Selection (Shown after clicking Book)
        bookingExpertForSlotsDialog?.let { expert ->
            val context = androidx.compose.ui.platform.LocalContext.current
            
            // Scrim dim background covering the screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { bookingExpertForSlotsDialog = null }
                    .zIndex(90f)
            )

            // Animated slide up bottom sheet container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(95f),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Bottom Sheet Panel with subtle shadow and white background
                Card(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = false) { } // prevent Scrim clicks filtering down
                        .navigationBarsPadding()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Drag Handle
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(width = 40.dp, height = 4.dp)
                                .background(Color.LightGray, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Header: Expert name + avatar + close button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar (56px)
                            Box(modifier = Modifier.size(56.dp)) {
                                ProceduralLinkedInAvatar(
                                    name = expert.name,
                                    sizeDp = 56,
                                    modifier = Modifier.clip(CircleShape).background(Color.White)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = expert.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = expert.title,
                                    fontSize = 12.sp,
                                    color = Color(0xFFC0392B),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            // Close icon button
                            IconButton(
                                onClick = { bookingExpertForSlotsDialog = null },
                                modifier = Modifier.testTag("close_booking_sheet")
                            ) {
                                Text("✕", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }
                        }

                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                        // Week strip (Mon-Sun, Saturday highlighted as dynamic today in local calendar context)
                        val currentWeekDays = remember {
                            val today = java.time.LocalDate.now()
                            val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
                            (0..6).map { offset ->
                                val date = monday.plusDays(offset.toLong())
                                val shortName = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.US)
                                val labelNum = date.dayOfMonth.toString()
                                val isDateToday = date.isEqual(today)
                                Triple(shortName, labelNum, isDateToday)
                            }
                        }
                        val daysOfWeek = remember(currentWeekDays) { currentWeekDays.map { it.first } }
                        val dayDates = remember(currentWeekDays) { currentWeekDays.map { it.second } }
                        var selectedDayIndex by remember(currentWeekDays) {
                            val todayIdx = currentWeekDays.indexOfFirst { it.third }
                            mutableStateOf(if (todayIdx != -1) todayIdx else 0)
                        }
                        val monthYearLabel = remember(selectedDayIndex, currentWeekDays) {
                            val today = java.time.LocalDate.now()
                            val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
                            val selectedDate = monday.plusDays(selectedDayIndex.toLong())
                            val monthName = selectedDate.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.US)
                            val yearVal = selectedDate.year
                            "$monthName $yearVal"
                        }

                        // Week strip (Mon-Sun, dynamically generated based on current week)
                        Text(
                            text = "Select a Day ($monthYearLabel):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            daysOfWeek.forEachIndexed { idx, day ->
                                val dateStr = dayDates[idx]
                                val isToday = currentWeekDays[idx].third
                                val isSelected = selectedDayIndex == idx
                                
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSelected) Color(0xFFC0392B).copy(alpha = 0.08f) else Color.Transparent,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            border = BorderStroke(
                                                width = if (isSelected) 2.dp else if (isToday) 2.dp else 1.dp,
                                                color = if (isSelected) Color(0xFFC0392B) 
                                                       else if (isToday) Color(0xFFC0392B).copy(alpha = 0.5f) 
                                                       else Color.LightGray.copy(alpha = 0.6f)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedDayIndex = idx }
                                        .padding(vertical = 10.dp)
                                        .testTag("week_strip_day_$idx")
                                ) {
                                    Text(
                                        text = day,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFFC0392B) else Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = dateStr,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) Color(0xFFC0392B) else Color.Black
                                    )
                                    if (isToday) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFC0392B), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "TODAY",
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Available slots as green chips, Taken slots as grey strikethrough chips
                        Text(
                            text = "Choose Available Slot:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val slotsForDays = remember {
                            mapOf(
                                0 to listOf("9:00 AM" to "taken", "11:00 AM" to "available", "2:00 PM" to "taken", "4:00 PM" to "available", "6:00 PM" to "available"), // Mon
                                1 to listOf("10:00 AM" to "available", "12:00 PM" to "taken", "3:00 PM" to "available", "5:00 PM" to "available"), // Tue
                                2 to listOf("9:00 AM" to "available", "11:00 AM" to "taken", "1:00 PM" to "taken", "4:00 PM" to "available", "6:00 PM" to "available"), // Wed
                                3 to listOf("10:00 AM" to "taken", "2:00 PM" to "available", "3:30 PM" to "available", "5:00 PM" to "available"), // Thu
                                4 to listOf("9:00 AM" to "available", "11:30 AM" to "available", "2:00 PM" to "taken", "4:30 PM" to "available"), // Fri
                                5 to listOf("10:00 AM" to "available", "12:00 PM" to "taken", "3:00 PM" to "available", "5:00 PM" to "available"), // Sat (today)
                                6 to listOf("11:00 AM" to "available", "1:00 PM" to "available", "4:00 PM" to "taken", "5:30 PM" to "available") // Sun
                            )
                        }

                        val selectedSlotsList = slotsForDays[selectedDayIndex] ?: emptyList()
                        var selectedSlotIndex by remember(selectedDayIndex) {
                            mutableStateOf(selectedSlotsList.indexOfFirst { it.second == "available" })
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            selectedSlotsList.forEachIndexed { slotIdx, (timeStr, status) ->
                                val isAvailable = status == "available"
                                val isSelected = isAvailable && selectedSlotIndex == slotIdx
                                
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (!isAvailable) Color(0xFFF2F2F2) 
                                                   else if (isSelected) Color(0xFF27AE60) 
                                                   else Color(0xFFE8F8F5),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (!isAvailable) Color.LightGray.copy(alpha = 0.5f) 
                                                   else if (isSelected) Color(0xFF1E8449) 
                                                   else Color(0xFF2ECC71),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable(enabled = isAvailable) {
                                            selectedSlotIndex = slotIdx
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                        .testTag("slot_chip_${selectedDayIndex}_$slotIdx")
                                ) {
                                    Text(
                                        text = timeStr,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isAvailable) Color.Gray 
                                               else if (isSelected) Color.White 
                                               else Color(0xFF27AE60),
                                        style = if (!isAvailable) androidx.compose.ui.text.TextStyle(
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                        ) else androidx.compose.ui.text.TextStyle.Default
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Confirm button on bottom sheet "Book [slot] · ₹[rate]"
                        val selectedSlotPair = selectedSlotsList.getOrNull(selectedSlotIndex)
                        val selectedSlotTime = selectedSlotPair?.first
                        val isConfirmEnabled = selectedSlotTime != null
                        val rate = if (expert.flatSessionFee > 0) expert.flatSessionFee else 199

                        val buttonText = if (isConfirmEnabled) {
                            "Book ${daysOfWeek[selectedDayIndex]} $selectedSlotTime · ₹$rate"
                        } else {
                            "Select an Available Slot"
                        }

                        Button(
                            onClick = {
                                if (isConfirmEnabled) {
                                    val dayLabel = daysOfWeek[selectedDayIndex]
                                    val slotTiming = "$dayLabel • $selectedSlotTime"
                                    
                                    viewModel.bookSessionWithExpert(
                                        expertId = expert.id,
                                        selectedTime = slotTiming,
                                        onComplete = {
                                            // Write success toast message instantly
                                            android.widget.Toast.makeText(
                                                context,
                                                "Session booked with ${expert.name}!",
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
                                            
                                            // Close bottom sheet
                                            bookingExpertForSlotsDialog = null
                                        }
                                    )
                                }
                            },
                            enabled = isConfirmEnabled,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC0392B),
                                disabledContainerColor = Color.LightGray
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("confirm_booking_button")
                        ) {
                            Text(
                                text = buttonText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Direct booking completion overlay dialog
        bookedExpertNameForDialog?.let { expertName ->
            AlertDialog(
                onDismissRequest = { bookedExpertNameForDialog = null },
                confirmButton = {
                    Button(onClick = { bookedExpertNameForDialog = null }) {
                        Text("Shukriya Chachi! (Thank You)")
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🎉", fontSize = 24.sp)
                        Text("Session Booked Successfully")
                    }
                },
                text = {
                    Text(
                        text = "A consultation with your chosen mentor, $expertName, has been scheduled on Wisdom Bridge.\n\nAI Chachi says: 'Great choice Ramesh beta! They are already looking forward to speaking with you! Check your calendar for details.'",
                        fontSize = 13.5.sp,
                        lineHeight = 18.sp
                    )
                },
                shape = RoundedCornerShape(20.dp)
            )
        }

        // Sticky collapsing glassmorphic header
        AnimatedVisibility(
            visible = showStickyHeader,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
                    .border(BorderStroke(1.dp, BorderLightSystem), RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(vertical = 10.dp, horizontal = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .size(38.dp)
                            .testTag("sticky_category_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "👨‍🏫 Find an Expert",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpotifySubcategoryTile(
    sub: SubExperience,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("spotify_tile_${sub.name.lowercase().replace(" ", "_").replace("&", "and")}"),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = sub.imageRes),
                contentDescription = sub.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Content bottom left
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = sub.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${sub.expertCount} Experts",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                        )
                    }
                    Text(
                        text = "•",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                    Text(
                        text = "${sub.memberCount} joined",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 9.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DetailFeaturedExpertCard(
    expert: Expert,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var showMessageToast by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .width(220.dp)
            .clickable(onClick = onClick)
            .testTag("detail_expert_card_${expert.name.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        ExpertCoverPhoto(
            expert = expert,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )

        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(23.dp))
                        .padding(2.dp)
                ) {
                    AvatarImage(name = expert.name, modifier = Modifier.fillMaxSize())
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = expert.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = expert.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${expert.yearsOfExperience}+ Years Experience • ${expert.areaEmoji}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating star",
                        tint = Color(0xFFF39C12),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    val formattedRating = String.format(java.util.Locale.US, "%.2f", expert.rating)
                    Text(
                        text = "$formattedRating (${expert.testimonialsCount})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${expert.peopleHelpedCount} helped",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = { showMessageToast = true },
                    modifier = Modifier
                        .size(34.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .testTag("msg_expert_icon_btn_${expert.name.lowercase().replace(" ", "_")}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Message Expert",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("consult_expert_btn_${expert.name.lowercase().replace(" ", "_")}"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Consult Profile", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }
        }
    }

    if (showMessageToast) {
        AlertDialog(
            onDismissRequest = { showMessageToast = false },
            title = { Text("Start Conversation", fontWeight = FontWeight.Bold) },
            text = { Text("Initializing intergenerational message bubble with ${expert.name}. Connecting you instantly...") },
            confirmButton = {
                TextButton(onClick = { showMessageToast = false }) {
                    Text("Decline")
                }
                TextButton(onClick = { showMessageToast = false }) {
                    Text("Connect Room", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun DetailCommunityCard(
    comm: Community,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var userJoinedCircle by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .width(220.dp)
            .clickable(onClick = onClick)
            .testTag("detail_community_card_${comm.name.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(comm.iconEmoji, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = comm.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${comm.memberCount} members",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = comm.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.5.sp,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { userJoinedCircle = !userJoinedCircle },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(34.dp)
                        .testTag("join_community_btn_${comm.name.lowercase().replace(" ", "_")}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (userJoinedCircle) Color.DarkGray else accentColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Text(
                        text = if (userJoinedCircle) "Joined ✓" else "Join",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Text("Chat Room", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun DetailEventCard(
    event: CommunityEvent,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit,
    onRsvpClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onCardClick)
            .testTag("detail_event_card_${event.title.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = event.type.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 9.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = event.localTime,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Hosted by ${event.hostName} • ${event.communityName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onRsvpClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (event.isUserRsvped) Color.DarkGray else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(38.dp)
                    .testTag("rsvp_event_action_btn_${event.id}"),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(
                    text = if (event.isUserRsvped) "Joined ✓" else "Join Session",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SubcategoryDetailScreen(
    subExp: SubExperience,
    category: MainCategory,
    viewModel: AgeNoBarViewModel,
    onBack: () -> Unit,
    onExpertClick: (Expert) -> Unit,
    onCommunityClick: (Community) -> Unit,
    onEventClick: (CommunityEvent) -> Unit
) {
    val experts by viewModel.experts.collectAsState()
    val communities by viewModel.communities.collectAsState()
    val events by viewModel.events.collectAsState()

    var userJoinedSubcategory by remember { mutableStateOf(false) }
    var activeSubcategoryToast by remember { mutableStateOf<String?>(null) }

    // Subcategory experts search
    val subcategoryExperts = experts.filter { expert ->
        expert.title.contains(subExp.name, ignoreCase = true) ||
                expert.name.contains(subExp.name, ignoreCase = true) ||
                expert.skillsTags.contains(subExp.name)
    }.ifEmpty { experts.take(3) }

    // Subcategory communities search
    val subcategoryCommunities = communities.filter { comm ->
        comm.name.contains(subExp.name, ignoreCase = true) ||
                comm.description.contains(subExp.name, ignoreCase = true)
    }.ifEmpty { communities.take(2) }

    // Subcategory events search
    val subcategoryEvents = events.filter { ev ->
        ev.title.contains(subExp.name, ignoreCase = true) ||
                ev.description.contains(subExp.name, ignoreCase = true)
    }.ifEmpty { events.take(2) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Hero Section & Back Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                Image(
                    painter = painterResource(id = subExp.imageRes),
                    contentDescription = subExp.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                // Top Floating Back Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .size(40.dp)
                            .testTag("subcategory_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(category.bgGrad.last().copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${subExp.emoji}  ${category.name}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Title and description on overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = subExp.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subExp.desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Join Category Circle Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { userJoinedSubcategory = !userJoinedSubcategory },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (userJoinedSubcategory) Color.DarkGray else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("join_subcategory_btn")
                        ) {
                            Text(
                                text = if (userJoinedSubcategory) "Joined Circle ✓" else "Join Subcategory Circle",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            }
        }

        // Live Audio Room Option
        val isGardeningTheme = subExp.name.contains("gardening", ignoreCase = true) || 
                subExp.name.contains("farming", ignoreCase = true) || 
                subExp.name.contains("bonsai", ignoreCase = true) || 
                subExp.name.contains("plant", ignoreCase = true) ||
                category.name.contains("nature", ignoreCase = true)

        if (!isGardeningTheme) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clickable {
                            viewModel.createVoiceRoom("Acoustic Audio Call: ${subExp.name}", "Wellness Room", category.name, "Live audio discussion and sharing circle for ${subExp.name}")
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔊", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Acoustic Audio Room",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Drop in and talk about ${subExp.name} in high-fidelity vocal quality.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Join Room",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Featured Mentors
        item {
            Text(
                text = "Featured Experts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        if (subcategoryExperts.isEmpty()) {
            item {
                Text(
                    text = "No experts live in this subcategory now.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(subcategoryExperts) { expert ->
                        DetailFeaturedExpertCard(
                            expert = expert,
                            onClick = { onExpertClick(expert) }
                        )
                    }
                }
            }
        }

        // Subcategory Circles
        item {
            Text(
                text = "Active Circle Communities",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(subcategoryCommunities) { comm ->
                    DetailCommunityCard(
                        comm = comm,
                        accentColor = category.bgGrad.last(),
                        onClick = { onCommunityClick(comm) }
                    )
                }
            }
        }

        // Live Audio Workshops Events
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Upcoming Workshops",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        items(subcategoryEvents) { ev ->
            DetailEventCard(
                event = ev,
                onCardClick = { onEventClick(ev) },
                onRsvpClick = { viewModel.toggleEventRsvp(ev.id) }
            )
        }

        // recommended resources
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Curated Resources",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📖", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${subExp.name} Ultimate Starter Guide.pdf",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "PDF Document • Generated by verified specialist mentors.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { activeSubcategoryToast = "📥 Downloading Guide: ${subExp.name} Starter Guide..." }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Resource",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (activeSubcategoryToast != null) {
        AlertDialog(
            onDismissRequest = { activeSubcategoryToast = null },
            title = { Text("Resource Status ✓", fontWeight = FontWeight.Bold) },
            text = { Text(activeSubcategoryToast!!) },
            confirmButton = {
                TextButton(onClick = { activeSubcategoryToast = null }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun EventDetailScreen(
    event: CommunityEvent,
    onBack: () -> Unit,
    onHostClick: (String) -> Unit
) {
    var userRsvpedState by remember { mutableStateOf(event.isUserRsvped) }
    var rsvpsCountState by remember { mutableStateOf(event.rsvpCount) }
    var detailsNotificationToast by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // App header bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Event invitation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Event Hero graphic
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_cat_music),
                        contentDescription = "Event banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.2f),
                                        Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.error, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = event.type.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }

        // Details Panel
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏰", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Local Time Schedule",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = event.localTime,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Live on Wisdom Bridge Audio Channels",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Join this Circle Gatherings",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$rsvpsCountState members attending",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = {
                                    if (userRsvpedState) {
                                        userRsvpedState = false
                                        rsvpsCountState = maxOf(0, rsvpsCountState - 1)
                                        detailsNotificationToast = "Cancelled your RSVP for ${event.title}."
                                    } else {
                                        userRsvpedState = true
                                        rsvpsCountState += 1
                                        detailsNotificationToast = "Successfully RSVPed for ${event.title}! Added to calendar."
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (userRsvpedState) Color.DarkGray else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = if (userRsvpedState) "RSVPed ✓" else "RSVP Now",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Host Details Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Event Host & Speaker",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onHostClick(event.hostName) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(25.dp))
                                .padding(2.dp)
                        ) {
                            AvatarImage(name = event.hostName, modifier = Modifier.fillMaxSize())
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.hostName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Verified specialist mentor",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Click to consult mentor profile",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Description Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = event.description.ifEmpty {
                        "Participate in this warm intergenerational learning event live. Meet verified specialist mentors and talk about traditional cooking, music, yoga and senior wisdom."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }

        // Agenda Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Agenda",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val items = listOf(
                        "🎤  00:00 - Introduction & welcome address by host.",
                        "🗣️  00:15 - Core specialist lecture and live slides presentation.",
                        "🎙️  00:40 - Opened listener mics for Q&A, comments, and experience sharing.",
                        "🌸  00:55 - Closing remarks & resource sharing links."
                    )
                    items.forEach { bullet ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = bullet,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (detailsNotificationToast != null) {
        AlertDialog(
            onDismissRequest = { detailsNotificationToast = null },
            title = { Text("RSVP Status 🎉", fontWeight = FontWeight.Bold) },
            text = { Text(detailsNotificationToast!!) },
            confirmButton = {
                TextButton(onClick = { detailsNotificationToast = null }) {
                    Text("Awesome", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun PremiumBookingCalendarScreen(
    expertId: String,
    rescheduleBookingId: String? = null,
    viewModel: AgeNoBarViewModel,
    onBack: () -> Unit
) {
    val experts by viewModel.experts.collectAsState()
    val bookingsList by viewModel.bookingsList.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isTeacherMode = currentUser.userRoleType == "Teach"
    
    val expert = experts.find { it.id == expertId }
    
    // Apple Health style Day selection dataset dynamically computed
    val today = java.time.LocalDate.now()
    val currentMonday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
    val weekDays = remember {
        (0..6).map { i ->
            val date = currentMonday.plusDays(i.toLong())
            val shortName = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()).uppercase() // "MON", "TUE"...
            val labelNum = date.dayOfMonth.toString()
            Pair(shortName.take(3), labelNum)
        }
    }
    
    val initiallySelectedDay = remember {
        today.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()).uppercase().take(3)
    }
    var selectedDay by remember(initiallySelectedDay) { mutableStateOf(initiallySelectedDay) }
    
    // Large details-focused vertical slots list
    val standardSlots = listOf(
        Pair("10:00 AM", "10:30 AM"),
        Pair("11:30 AM", "12:00 PM"),
        Pair("03:00 PM", "03:30 PM"),
        Pair("04:00 PM", "04:30 PM"),
        Pair("05:00 PM", "05:30 PM"),
        Pair("06:30 PM", "07:00 PM")
    )
    
    // Mapping of slot indexes to mock premium tutoring offerings for high-fidelity content
    val tutoringOfferings = listOf(
        Triple("📐 Mathematics & Algebra Refresher", "Master core equations, formula trees and real-world basic statistics.", "📐"),
        Triple("✍️ Language Arts & Creative Writing", "Polishing grammar, descriptive vocabulary, and engaging composition.", "✍️"),
        Triple("🌳 Nature, Life Sciences & Biology", "Exploring organic life, agricultural systems, and botany fundamentals.", "🌳"),
        Triple("🧩 Logical Reasoning & Cognitive Drills", "Interactive brain teasers, diagnostic puzzles and problem solving.", "🧩"),
        Triple("💻 Digital Literacy & Computing Basics", "Navigating online resources, smart communications and workspace tools.", "💻"),
        Triple("🌟 Creative Arts & Calligraphy Lab", "Unlocking artistic expression, visual composition, and stroke styling.", "🌟")
    )

    var alertMessage by remember { mutableStateOf<String?>(null) }
    var showBookingConfirmationBySlot by remember { mutableStateOf<String?>(null) }
    var blockedSlots by remember { mutableStateOf(setOf<String>()) } // Format: "WED • 10:00 AM - 10:30 AM"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PrimaryBackgroundWarmWhite,
                        SecondarySurfaceSoftIvory
                    )
                )
            )
            .testTag("premium_scheduler_screen")
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(bottom = 8.dp)
                ) {
                    Spacer(modifier = Modifier.height(34.dp))
                    
                    // Elegant minimal top bar heading
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.8f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                                .clickable { onBack() }
                                .testTag("scheduler_back"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back back button arrow",
                                tint = Color.DarkGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = if (rescheduleBookingId != null) "Reschedule Workspace" else "Premium Tutoring Intake",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "1:1 Live Interactive Classrooms",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Expert Details Mini-Card, Glassmorphic Floating Style
                    if (expert != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                                .border(BorderStroke(1.dp, Color.White), RoundedCornerShape(20.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProceduralLinkedInAvatar(
                                name = expert.name,
                                sizeDp = 46,
                                modifier = Modifier
                                    .background(Color.White, RoundedCornerShape(10.dp))
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Instructor: ${expert.name}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                )
                                Text(
                                    text = expert.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    val formattedRating = String.format(java.util.Locale.US, "%.2f", expert.rating)
                                    Text("⭐ $formattedRating Rating", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFF39C12))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("₹299 / 30 mins session", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Sticky Header Day Selector at top of screen body
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp).padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "June 2026",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                            Text(
                                text = "Choose Day",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            weekDays.forEach { dayPair ->
                                val (dayLabel, dateNum) = dayPair
                                val isSelected = selectedDay == dayLabel
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 3.dp)
                                        .height(68.dp)
                                        .shadow(
                                            elevation = if (isSelected) 4.dp else 0.dp,
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        .background(
                                            brush = if (isSelected) Brush.linearGradient(
                                                colors = listOf(Color(0xFFF8E7EE), Color(0xFFEADCF8)) // Blush Pink to Soft Lavender
                                            ) else Brush.linearGradient(
                                                colors = listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0.3f))
                                            ),
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        .clickable { selectedDay = dayLabel }
                                        .testTag("week_day_$dayLabel"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = dayLabel,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color(0xFF2C2625) else Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = dateNum,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isSelected) Color(0xFF2C2625) else Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Hourly Intake Timeline • $selectedDay",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 8.dp)
                )

                // Smooth Scrolling Timeline Agenda View
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(standardSlots.size) { index ->
                        val slotPair = standardSlots[index]
                        val (startTime, endTime) = slotPair
                        val slotTimeRange = "$startTime - $endTime"
                        val timingString = "$selectedDay • $slotTimeRange"
                        
                        val matchingBooking = bookingsList.find { b ->
                            b.expertId == expertId && b.timing == timingString
                        }
                        val isBookedByYou = matchingBooking != null && matchingBooking.status == "Upcoming"
                        val isBookedByPeer = !isBookedByYou && (index == 1 || index == 3)
                        val isBlockedSlot = blockedSlots.contains(timingString)
                        
                        val classDetails = tutoringOfferings[index % tutoringOfferings.size]
                        val (className, classDesc, classIcon) = classDetails

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("slot_row_$index"),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Timeline clock section on the left
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(66.dp)
                                    .padding(top = 10.dp)
                            ) {
                                Text(
                                    text = startTime,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "30 min",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Elegant vertical dashed connector line
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(98.dp)
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }

                            // Large glassmorphic vertical appointment card on the right
                            Card(
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        isBookedByYou -> SoftGreenCard
                                        isBookedByPeer -> LightGreyCard
                                        isBlockedSlot -> Color.Red.copy(alpha = 0.05f)
                                        else -> PastelBlueCard
                                    }
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = when {
                                        isBookedByYou -> Color(0xFF88C999).copy(alpha = 0.4f)
                                        isBookedByPeer -> Color.LightGray.copy(alpha = 0.4f)
                                        isBlockedSlot -> Color.Red.copy(alpha = 0.15f)
                                        else -> Color.White.copy(alpha = 0.5f)
                                    }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("slot_card_$index")
                                    .clickable {
                                        if (isTeacherMode) {
                                            // Handle teacher modes
                                        } else {
                                            if (!isBookedByPeer && !isBookedByYou && !isBlockedSlot) {
                                                if (rescheduleBookingId != null) {
                                                    viewModel.rescheduleBooking(rescheduleBookingId, timingString)
                                                    alertMessage = "Your session was successfully rescheduled to $timingString!"
                                                } else {
                                                    showBookingConfirmationBySlot = timingString
                                                }
                                            }
                                        }
                                    },
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (isBookedByYou) 4.dp else 1.dp
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    // Header of card (Class status indicator badge)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(classIcon, fontSize = 13.sp)
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Slot • $startTime",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Gray
                                            )
                                        }

                                        // Apple native style status capsule
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = when {
                                                        isBookedByYou -> Color(0xFF2ECC71).copy(alpha = 0.15f)
                                                        isBookedByPeer -> Color.Gray.copy(alpha = 0.1f)
                                                        isBlockedSlot -> Color.Red.copy(alpha = 0.14f)
                                                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                    },
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = when {
                                                    isBookedByYou -> "✓ Appointed"
                                                    isBookedByPeer -> "Occupied"
                                                    isBlockedSlot -> "Unavailable"
                                                    else -> "Available"
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = when {
                                                    isBookedByYou -> Color(0xFF27AE60)
                                                    isBookedByPeer -> Color.Gray
                                                    isBlockedSlot -> Color.Red
                                                    else -> MaterialTheme.colorScheme.primary
                                                }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Class / Lesson Title & Details
                                    Text(
                                        text = className,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = classDesc,
                                        fontSize = 11.sp,
                                        color = Color.DarkGray,
                                        lineHeight = 15.sp
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Large Action Buttons providing excellent 48dp+ Tap Targets
                                    if (isTeacherMode) {
                                        if (isBookedByPeer || isBookedByYou) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Button(
                                                    onClick = { alertMessage = "Connecting secure Video classroom call with student..." },
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                    modifier = Modifier
                                                        .weight(1.2f)
                                                        .height(44.dp)
                                                ) {
                                                    Text("Join Video call 📹", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                OutlinedButton(
                                                    onClick = { alertMessage = "Opening secure peer communication portal..." },
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(44.dp)
                                                ) {
                                                    Text("Message", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    blockedSlots = if (isBlockedSlot) {
                                                        blockedSlots - timingString
                                                    } else {
                                                        blockedSlots + timingString
                                                    }
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = if (isBlockedSlot) MaterialTheme.colorScheme.primary else Color(0xFFC0392B)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(44.dp)
                                            ) {
                                                Text(if (isBlockedSlot) "Unblock Intake Space" else "Block Intake Space 🚫", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        when {
                                            isBookedByYou -> {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Button(
                                                        onClick = { alertMessage = "Connecting secure Video classroom lesson call on Wisdom Bridge..." },
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60)),
                                                        modifier = Modifier
                                                            .weight(1.2f)
                                                            .height(44.dp)
                                                    ) {
                                                        Text("Enter Class 📹", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    OutlinedButton(
                                                        onClick = {
                                                            val bId = matchingBooking?.id
                                                            if (bId != null) {
                                                                viewModel.openScheduler(expertId, bId)
                                                            }
                                                        },
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(44.dp)
                                                    ) {
                                                        Text("Reschedule", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            isBookedByPeer -> {
                                                OutlinedButton(
                                                    onClick = { alertMessage = "Success! You will be notified instantly if this slot becomes vacant." },
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(44.dp)
                                                ) {
                                                    Text("Notify Me If Empty 🔔", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                                }
                                            }

                                            isBlockedSlot -> {
                                                Text(
                                                    text = "🚫 The tutor is currently offline or busy during this timeframe.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color.Red.copy(alpha = 0.7f)
                                                )
                                            }

                                            else -> {
                                                Button(
                                                    onClick = {
                                                        if (rescheduleBookingId != null) {
                                                            viewModel.rescheduleBooking(rescheduleBookingId, timingString)
                                                            alertMessage = "Your session was successfully rescheduled to $timingString!"
                                                        } else {
                                                            showBookingConfirmationBySlot = timingString
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(44.dp)
                                                ) {
                                                    Text(
                                                        text = if (rescheduleBookingId != null) "Confirm Reschedule Slot" else "Instant Live Booking • ₹299",
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (alertMessage != null) {
        val msg = alertMessage!!
        AlertDialog(
            onDismissRequest = { 
                alertMessage = null 
                if (msg.contains("rescheduled")) {
                    viewModel.closeScheduler()
                }
            },
            title = { Text("Scheduling Update 🎉", fontWeight = FontWeight.Bold) },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { 
                    alertMessage = null 
                    if (msg.contains("rescheduled")) {
                        viewModel.closeScheduler()
                    }
                }) {
                    Text("Awesome", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    if (showBookingConfirmationBySlot != null) {
        val timing = showBookingConfirmationBySlot!!
        AlertDialog(
            onDismissRequest = { showBookingConfirmationBySlot = null },
            title = { Text("Confirm Appointment Session 🤝", fontWeight = FontWeight.Bold) },
            text = { Text("Would you like to finalize booking a 1:1 Live Lesson with ${expert?.name ?: "Mentor"} on $timing? Chachi will register the slot instantly.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.bookSessionWithExpert(expertId, timing, isVideo = true)
                        showBookingConfirmationBySlot = null
                        alertMessage = "Success! Your live lesson is booked for $timing."
                    }
                ) {
                    Text("Confirm Booking & Close", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBookingConfirmationBySlot = null }) {
                    Text("Back", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}


// --- NEW SCREEN COMPOSABLES FOR AI SEARCH & RECOMMENDATIONS AND LEARNERS FEED ---

@Composable
fun SearchAndAiRecommendationsScreen(
    viewModel: AgeNoBarViewModel,
    modifier: Modifier = Modifier
) {
    val searchInterestQuery by viewModel.searchInterestQuery.collectAsState()
    val aiRecommendedExperts by viewModel.aiRecommendedExperts.collectAsState()
    val isAiRecommending by viewModel.isAiRecommending.collectAsState()
    val aiRecommendationMessage by viewModel.aiRecommendationMessage.collectAsState()
    val followedIds by viewModel.followedExpertIds.collectAsState()

    var searchQueryLocal by remember { mutableStateOf(searchInterestQuery) }
    
    // Synced if searchInterestQuery changes from outside (e.g. Home dashboard dashboard search)
    LaunchedEffect(searchInterestQuery) {
        searchQueryLocal = searchInterestQuery
    }

    var selectedExpertForProfile by remember { mutableStateOf<Expert?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "AI Match Finder 🔮",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Search any topic or interest, and let AI Chachi handpick the perfect mentors for you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            // Interactive Search Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .border(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search icon",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQueryLocal.isEmpty()) {
                            Text(
                                text = "Search interest (e.g. History, Coding, Yoga, Finance)...",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                fontSize = 14.sp
                            )
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQueryLocal,
                            onValueChange = { searchQueryLocal = it },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("ai_match_search_input")
                        )
                    }
                    if (searchQueryLocal.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { searchQueryLocal = "" }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Button(
                        onClick = {
                            if (searchQueryLocal.trim().isNotEmpty()) {
                                viewModel.searchTeachersByInterest(searchQueryLocal.trim())
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.height(36.dp).testTag("ai_match_search_submit")
                    ) {
                        Text("Search", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // AI Recommendations Grandmother's Advice Bubble
            if (isAiRecommending) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Beta, Chachi is looking up our neighborhood teachers and experts for you...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            } else if (aiRecommendationMessage.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AvatarImage(name = "ai_chachi", size = 44)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Chachi's Guide 👵",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "AI Matchmaking Assist",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = aiRecommendationMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // Heading for contact cards
            item {
                Text(
                    text = if (aiRecommendedExperts.isNotEmpty()) "Handpicked Mentor Contact Cards 🎴" else "No matching guides found yet, beta.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Direct render of matching contact cards using ExpertCardItem
            if (aiRecommendedExperts.isNotEmpty()) {
                items(aiRecommendedExperts.size) { index ->
                    val expert = aiRecommendedExperts[index]
                    ExpertCardItem(
                        expert = expert,
                        isFollowed = followedIds.contains(expert.id),
                        onViewProfile = { selectedExpertForProfile = expert },
                        onActionTriggered = { actionType ->
                            if (actionType == "Follow_Trigger") {
                                viewModel.toggleFollowExpert(expert.id)
                            } else {
                                viewModel.openScheduler(expert.id)
                            }
                        }
                    )
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👵", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Try typing something else like 'Math', 'Yoga', 'Astrology', 'Coding', or 'Career'!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    if (selectedExpertForProfile != null) {
        val expert = selectedExpertForProfile!!
        AlertDialog(
            onDismissRequest = { selectedExpertForProfile = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarImage(name = expert.name, size = 44)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(expert.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(expert.title, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "About Mentor 📚", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(expert.bio, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Languages spoken:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(expert.languages.joinToString(", "), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Experience & Rating:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    val formattedRating = String.format(java.util.Locale.US, "%.2f", expert.rating)
                    Text("${expert.yearsOfExperience} years • Rating: ⭐ $formattedRating", fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedExpertForProfile = null
                        viewModel.openScheduler(expert.id)
                    }
                ) {
                    Text("Schedule Live Lesson 📅")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedExpertForProfile = null }) {
                    Text("Close")
                }
            }
        )
    }
}


@Composable
fun LearningRequestsFeedScreen(
    viewModel: AgeNoBarViewModel,
    modifier: Modifier = Modifier
) {
    val requests by viewModel.learningRequests.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Lifelong Learning Requests Feed 📋",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Beta, find intergenerational guidance queries posted by elders and youngsters alike. Tap any to help or chat!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            if (requests.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📝", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No learning request postings currently active, beta.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                items(requests.size) { index ->
                    val request = requests[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("request_item_${request.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AvatarImage(name = request.learner_id, size = 32)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (request.learner_id.contains("senior")) "Senior Member Ramesh" else "Learner Beta",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (request.status == "open") Color(0xFFE8F5E9) else Color(0xFFECEFF1)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = request.status.uppercase(),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (request.status == "open") Color(0xFF2E7D32) else Color(0xFF37474F)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Topic: ${request.topic}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = request.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.selectTab(AppTab.Messages)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Outlined.Sms, contentDescription = "Reply", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Offer Guidance • Live", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModifyBookingDialog(
    booking: Booking,
    viewModel: AgeNoBarViewModel,
    onDismiss: () -> Unit
) {
    var selectedTiming by remember { mutableStateOf(booking.timing) }
    var selectedDuration by remember { mutableStateOf(booking.durationMinutes) }
    var isVoice by remember { mutableStateOf(booking.isVoice) }
    var isVideo by remember { mutableStateOf(booking.isVideo) }
    
    val availableAlternateSlots = listOf(
        "MON • 4:00 PM - 4:30 PM",
        "TUE • 10:00 AM - 10:30 AM",
        "WED • 3:00 PM - 3:30 PM",
        "THU • 2:00 PM - 2:30 PM",
        "FRI • 11:00 AM - 11:30 AM",
        "SAT • 4:00 PM - 4:30 PM"
    ).filter { it != booking.timing }

    var showCancelConfirm by remember { mutableStateOf(false) }

    if (showCancelConfirm) {
        AlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            title = { Text("Cancel Appointment? 🚨", fontWeight = FontWeight.Bold, color = Color(0xFFC62828)) },
            text = { Text("Are you absolutely sure you want to cancel your upcoming session with ${booking.expertName} on ${booking.timing}? This slot will be released back to other learners.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelBooking(booking.id)
                        showCancelConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Text("Yes, Cancel Session", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirm = false }) {
                    Text("Keep Appointment")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text("✏️ Modify Appointment", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🤝", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = booking.expertName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Current: ${booking.timing}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Class Duration",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(15, 30, 45, 60).forEach { mins ->
                                val selected = selectedDuration == mins
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedDuration = mins },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primary else Color(0xFFF0F0F0)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$mins min",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) Color.White else Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Interaction Medium Type",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        isVideo = true
                                        isVoice = false
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isVideo) MaterialTheme.colorScheme.secondaryContainer else Color(0xFFF5F5F5)
                                ),
                                border = if (isVideo) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🎥", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Video Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        isVoice = true
                                        isVideo = false
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isVoice) MaterialTheme.colorScheme.secondaryContainer else Color(0xFFF5F5F5)
                                ),
                                border = if (isVoice) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📞", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Voice Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Reschedule Timing Slot",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        OutlinedTextField(
                            value = selectedTiming,
                            onValueChange = { selectedTiming = it },
                            label = { Text("Timing Slot String") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Or quickly swap with expert alternate availability:",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            availableAlternateSlots.forEach { slot ->
                                Card(
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .clickable { selectedTiming = slot },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedTiming == slot) MaterialTheme.colorScheme.tertiaryContainer else Color(0xFFE9ECEF)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = slot,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        
                        OutlinedButton(
                            onClick = {
                                viewModel.openScheduler(booking.expertId, booking.id)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("📅 Open Visual Scheduler Calendar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateBookingDetails(
                            bookingId = booking.id,
                            newTiming = selectedTiming,
                            newDuration = selectedDuration,
                            isVoice = isVoice,
                            isVideo = isVideo
                        )
                        onDismiss()
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { showCancelConfirm = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFC62828))
                    ) {
                        Text("🗑️ Cancel Session", fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { onDismiss() }) {
                        Text("Close", fontWeight = FontWeight.Medium)
                    }
                }
            }
        )
    }
}

@Composable
fun LiveClassroomDialog(
    booking: Booking,
    viewModel: AgeNoBarViewModel,
    onDismiss: () -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }
    var isCameraOff by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }
    
    // Simulate elapsed minutes tracker
    var elapsedSeconds by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedSeconds += 1
        }
    }
    
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)
    
    AlertDialog(
        onDismissRequest = { onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        ),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(vertical = 12.dp)
            .testTag("live_classroom_dialog"),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 6.dp,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Wisdom Bridge Classroom 🚀",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF8B1A1A)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE CONNECTED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                    IconButton(onClick = { onDismiss() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close screen")
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Active Class Workspace Simulation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(18.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (booking.isVideo && !isCameraOff) {
                         Column(
                             modifier = Modifier.fillMaxSize(),
                             horizontalAlignment = Alignment.CenterHorizontally,
                             verticalArrangement = Arrangement.SpaceBetween
                         ) {
                             Box(
                                 modifier = Modifier
                                     .weight(1f)
                                     .fillMaxWidth(),
                                 contentAlignment = Alignment.Center
                             ) {
                                 Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                     AvatarImage(
                                         name = booking.expertName,
                                         size = 90,
                                         isSpeaking = true,
                                         wavePower = 0.7f
                                     )
                                     Spacer(modifier = Modifier.height(6.dp))
                                     Text(
                                         text = booking.expertName,
                                         color = Color.White,
                                         fontSize = 14.sp,
                                         fontWeight = FontWeight.Bold
                                     )
                                     Text(
                                         text = "Teaching Refresher Session",
                                         color = Color.LightGray,
                                         fontSize = 10.sp
                                     )
                                 }
                             }
                             
                             Row(
                                 modifier = Modifier.fillMaxWidth(),
                                 horizontalArrangement = Arrangement.End
                             ) {
                                 Box(
                                     modifier = Modifier
                                         .size(70.dp, 80.dp)
                                         .background(Color(0xFF333333), RoundedCornerShape(10.dp))
                                         .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                                     contentAlignment = Alignment.Center
                                 ) {
                                     Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                         Text("🙋‍♂️", fontSize = 20.sp)
                                         Spacer(modifier = Modifier.height(2.dp))
                                         Text("You", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                     }
                                 }
                             }
                         }
                    } else {
                         Column(
                             horizontalAlignment = Alignment.CenterHorizontally,
                             verticalArrangement = Arrangement.Center
                         ) {
                             AvatarImage(
                                 name = booking.expertName,
                                 size = 100,
                                 isSpeaking = true,
                                 wavePower = 0.5f
                             )
                             Spacer(modifier = Modifier.height(10.dp))
                             Text(
                                 text = booking.expertName,
                                 color = Color.White,
                                 fontSize = 15.sp,
                                 fontWeight = FontWeight.Bold
                             )
                             Text(
                                 text = if (isMuted) "You are muted" else "Active Voice Session",
                                 color = if (isMuted) Color.Red else Color(0xFF4CAF50),
                                 fontSize = 11.sp,
                                 fontWeight = FontWeight.Bold
                             )
                             
                             Spacer(modifier = Modifier.height(10.dp))
                             Row(
                                 horizontalArrangement = Arrangement.spacedBy(4.dp),
                                 verticalAlignment = Alignment.CenterVertically
                             ) {
                                 repeat(6) {
                                     Box(
                                         modifier = Modifier
                                             .size(4.dp, 20.dp)
                                             .background(
                                                 if (isMuted) Color.Gray else Color(0xFF8B1A1A), 
                                                 RoundedCornerShape(2.dp)
                                             )
                                     )
                                 }
                             }
                         }
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.6f),
                            contentColor = Color.White
                        ) {
                            Text(
                                text = "⏱️ $timeString",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = { isMuted = !isMuted },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isMuted) Color(0xFFE53935) else Color(0xFFEEEEEE),
                            contentColor = if (isMuted) Color.White else Color.Black
                        ),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Text(if (isMuted) "🔇" else "🎙️", fontSize = 16.sp)
                    }
                    
                    if (booking.isVideo) {
                        FilledIconButton(
                            onClick = { isCameraOff = !isCameraOff },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (isCameraOff) Color(0xFFE53935) else Color(0xFFEEEEEE),
                                contentColor = if (isCameraOff) Color.White else Color.Black
                            ),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Text(if (isCameraOff) "❌🎥" else "📹", fontSize = 14.sp)
                        }
                    }
                    
                    FilledIconButton(
                        onClick = { isSpeakerOn = !isSpeakerOn },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isSpeakerOn) Color(0xFFE2F0D9) else Color(0xFFEEEEEE),
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Text(if (isSpeakerOn) "🔊" else "🔇", fontSize = 16.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onDismiss()
                            viewModel.setEditingBooking(booking)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Reschedule")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Modify / Reschedule Booking ✏️",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC62828),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Leave Class 📞",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        dismissButton = {}
    )
}


