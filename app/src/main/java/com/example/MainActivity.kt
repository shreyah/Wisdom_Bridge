package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel = remember { AgeNoBarViewModel() }
            val context = androidx.compose.ui.platform.LocalContext.current
            LaunchedEffect(viewModel) {
                viewModel.initPrefs(context)
            }
            val user by viewModel.currentUser.collectAsState()
            val isTeacherMode = user.userRoleType == "Teach"

            MyApplicationTheme(isTeacherMode = isTeacherMode) {
                val currentTab by viewModel.currentTab.collectAsState()
                val currentCommunityId by viewModel.currentCommunityId.collectAsState()
                val currentVoiceRoomId by viewModel.currentVoiceRoomId.collectAsState()
                val isRoomMinimized by viewModel.isVoiceRoomMinimized.collectAsState()
                val selectedSchedulerExpertId by viewModel.selectedSchedulerExpertId.collectAsState()
                val selectedRescheduleBookingId by viewModel.selectedRescheduleBookingId.collectAsState()
                val editingBooking by viewModel.editingBooking.collectAsState()
                val activeClassroomBooking by viewModel.activeClassroomBooking.collectAsState()

                if (editingBooking != null) {
                    ModifyBookingDialog(
                        booking = editingBooking!!,
                        viewModel = viewModel,
                        onDismiss = { viewModel.setEditingBooking(null) }
                    )
                }

                if (activeClassroomBooking != null) {
                    LiveClassroomDialog(
                        booking = activeClassroomBooking!!,
                        viewModel = viewModel,
                        onDismiss = { viewModel.setActiveClassroomBooking(null) }
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Render bottom navigation only if we are not locked in a maximized live voice room call
                        val hideBottomBar = (currentVoiceRoomId != null && !isRoomMinimized) || selectedSchedulerExpertId != null
                        if (!hideBottomBar) {
                            AgeNoBarBottomNavigation(
                                selectedTab = currentTab,
                                onTabSelected = { tab -> viewModel.selectTab(tab) },
                                isTeacherMode = isTeacherMode
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Base views based on selected tab state or active selected detail screen
                        when {
                            selectedSchedulerExpertId != null -> {
                                PremiumBookingCalendarScreen(
                                    expertId = selectedSchedulerExpertId!!,
                                    rescheduleBookingId = selectedRescheduleBookingId,
                                    viewModel = viewModel,
                                    onBack = { viewModel.closeScheduler() }
                                )
                            }

                            // Detail Screening 1: Maximized active voice room
                            currentVoiceRoomId != null && !isRoomMinimized -> {
                                LiveVoiceRoomView(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Detail Screening 2: Selected active Community details
                            currentCommunityId != null -> {
                                CommunityHomeScreen(
                                    viewModel = viewModel,
                                    communityId = currentCommunityId!!,
                                    onBack = { viewModel.exitCurrentCommunity() },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // General Tabs
                            else -> {
                                when (currentTab) {
                                    is AppTab.Home -> {
                                        HomeDashboardScreen(
                                            viewModel = viewModel,
                                            onNavigateToCommunity = { id -> viewModel.enterCommunity(id) },
                                            onNavigateToVoiceRooms = { viewModel.selectTab(AppTab.VoiceRooms) },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    is AppTab.Communities -> {
                                        CommunitiesDirectoryScreen(
                                            viewModel = viewModel,
                                            onCommunitySelected = { id -> viewModel.enterCommunity(id) },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    is AppTab.VoiceRooms -> {
                                        VoiceRoomsDirectoryScreen(
                                            viewModel = viewModel,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    is AppTab.Messages -> {
                                        MessagesScreen(
                                            viewModel = viewModel,
                                            onCommunitySelected = { id -> viewModel.enterCommunity(id) },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    is AppTab.Requests -> {
                                        LearningRequestsFeedScreen(
                                            viewModel = viewModel,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    is AppTab.SearchRecommend -> {
                                        SearchAndAiRecommendationsScreen(
                                            viewModel = viewModel,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    is AppTab.Profile -> {
                                        ProfileAndLeaderScreen(
                                            viewModel = viewModel,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }

                        // Persistent background floating WhatsApp-style active voice bubble
                        if (currentVoiceRoomId != null && isRoomMinimized) {
                            MinimizedVoiceBar(
                                viewModel = viewModel,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = 96.dp, end = 16.dp) // Float elegantly above bottom bar tabs
                            )
                        }

                        // Floating Chachi Assistant Mascot has been removed as per user request to avoid distraction.

                        // Start are onboarding role selection popup
                        if (user.userRoleType == "Both") {
                            OnboardingRoleSelectionOverlay(
                                viewModel = viewModel,
                                onDismiss = { /* state changes automatically dismiss */ }
                            )
                        }
                    }
                }
            }
        }
    }
}
