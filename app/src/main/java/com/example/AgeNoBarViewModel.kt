package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import org.json.JSONArray

sealed class AppTab {
    object Home : AppTab()
    object Communities : AppTab()
    object VoiceRooms : AppTab()
    object Requests : AppTab() // Reached via "Requests" in bottom nav
    object Messages : AppTab()
    object Profile : AppTab()
    object SearchRecommend : AppTab() // New tab for query and AI based recommendations
}

class AgeNoBarViewModel : ViewModel() {

    // --- DATABASE DECLARATIONS ---
    private var database: AgeNoBarDatabase? = null
    private var expertDao: ExpertDao? = null
    private var bookingDao: BookingDao? = null
    private var slotDao: SlotDao? = null
    private var requestDao: RequestDao? = null
    private var responseDao: ResponseDao? = null

    private val _activeExpertSlots = MutableStateFlow<List<DbSlot>>(emptyList())
    val activeExpertSlots: StateFlow<List<DbSlot>> = _activeExpertSlots.asStateFlow()

    private val _learningRequests = MutableStateFlow<List<DbRequest>>(emptyList())
    val learningRequests: StateFlow<List<DbRequest>> = _learningRequests.asStateFlow()

    private val _learningResponses = MutableStateFlow<List<DbResponse>>(emptyList())
    val learningResponses: StateFlow<List<DbResponse>> = _learningResponses.asStateFlow()

    private var activeExpertsSlotsJob: Job? = null

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    // -- ACTIVE CURRENT USER PROFILE --
    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "user_senior_101",
            name = "Ramesh Kumar",
            role = "Sernior Member & Retired Principal",
            avatarUrl = "avatar_ramesh",
            bio = "Veteran educator with 35 years in high school administration. Loving organic gardening enthusiast and community mentor.",
            isVerifiedExpert = true,
            isCommunityLeader = true
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    // -- SELECTED NAVIGATION STATE --
    private val _currentTab = MutableStateFlow<AppTab>(AppTab.Home)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _homeResetTrigger = MutableStateFlow(0)
    val homeResetTrigger: StateFlow<Int> = _homeResetTrigger.asStateFlow()

    private val _homeSelectedTopic = MutableStateFlow<String?>("All")
    val homeSelectedTopic: StateFlow<String?> = _homeSelectedTopic.asStateFlow()

    fun setHomeSelectedTopic(topic: String?) {
        _homeSelectedTopic.value = topic
    }

    // --- AI RECOMMENDATION & INTEREST SEARCH STATE ---
    private val _searchInterestQuery = MutableStateFlow("")
    val searchInterestQuery: StateFlow<String> = _searchInterestQuery.asStateFlow()

    private val _aiRecommendedExperts = MutableStateFlow<List<Expert>>(emptyList())
    val aiRecommendedExperts: StateFlow<List<Expert>> = _aiRecommendedExperts.asStateFlow()

    private val _isAiRecommending = MutableStateFlow(false)
    val isAiRecommending: StateFlow<Boolean> = _isAiRecommending.asStateFlow()

    private val _aiRecommendationMessage = MutableStateFlow("")
    val aiRecommendationMessage: StateFlow<String> = _aiRecommendationMessage.asStateFlow()

    fun searchTeachersByInterest(query: String) {
        _searchInterestQuery.value = query
        _isAiRecommending.value = true
        _aiRecommendationMessage.value = "AI Chachi is carefully finding the best experts for you..."
        
        viewModelScope.launch(Dispatchers.IO) {
            val allExperts = _experts.value
            val offlineMatches = allExperts.filter { expert ->
                expert.name.contains(query, ignoreCase = true) ||
                expert.title.contains(query, ignoreCase = true) ||
                expert.category.contains(query, ignoreCase = true) ||
                expert.bio.contains(query, ignoreCase = true) ||
                expert.skillsTags.any { it.contains(query, ignoreCase = true) }
            }
            withContext(Dispatchers.Main) {
                _aiRecommendedExperts.value = offlineMatches
            }
            
            val apiKey = try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }
            
            val isValidKey = apiKey.isNotEmpty() && !apiKey.contains("MY_GEMINI_API_KEY") && !apiKey.contains("PLACEHOLDER")
            
            if (isValidKey) {
                try {
                    val modelName = "gemini-3.5-flash"
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
                    
                    val expertsInfo = allExperts.joinToString("\n") { 
                        "ID: ${it.id}, Name: ${it.name}, Title: ${it.title}, Bio: ${it.bio}, Skills: ${it.skillsTags.joinToString(", ")}"
                    }
                    
                    val prompt = """
                        You are AI Chachi 👵, a warm, wise, loving Indian maternal grandmother.
                        Help the user find the best experts for their "area of interest": "$query".
                        
                        Here are the available experts in our database:
                        $expertsInfo
                        
                        Please analyze which experts are the absolute best match for this interest query.
                        Reply in STRICT JSON format:
                        {
                          "reasoning": "A short, extremely warm and loving grandma message in Hindi-English (Hinglish/English) explaining who you found for them and why, starting with 'Beta, ...'",
                          "selectedIds": ["exp_id_1", "exp_id_2"]
                        }
                    """.trimIndent()
                    
                    val requestObj = org.json.JSONObject().apply {
                        put("contents", org.json.JSONArray().apply {
                            put(org.json.JSONObject().apply {
                                put("parts", org.json.JSONArray().apply {
                                    put(org.json.JSONObject().apply {
                                        put("text", prompt)
                                    })
                                })
                            })
                        })
                        put("generationConfig", org.json.JSONObject().apply {
                            put("responseMimeType", "application/json")
                        })
                    }
                    
                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val requestBody = requestObj.toString().toRequestBody(mediaType)
                    
                    val request = Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build()
                        
                    val response = okHttpClient.newCall(request).execute()
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string() ?: ""
                        val responseJson = org.json.JSONObject(bodyString)
                        val candidates = responseJson.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val firstCandidate = candidates.getJSONObject(0)
                            val parts = firstCandidate.getJSONObject("content").getJSONArray("parts")
                            val textResponse = parts.getJSONObject(0).getString("text")
                            
                            val parsedResult = org.json.JSONObject(textResponse)
                            val reasoning = parsedResult.optString("reasoning", "")
                            val selectedIdsJson = parsedResult.optJSONArray("selectedIds")
                            
                            val selectedIds = mutableListOf<String>()
                            if (selectedIdsJson != null) {
                                for (i in 0 until selectedIdsJson.length()) {
                                    selectedIds.add(selectedIdsJson.getString(i))
                                }
                            }
                            
                            val matchingExperts = allExperts.filter { it.id in selectedIds }
                            
                            withContext(Dispatchers.Main) {
                                if (matchingExperts.isNotEmpty()) {
                                    _aiRecommendedExperts.value = matchingExperts
                                } else {
                                    if (_aiRecommendedExperts.value.isEmpty()) {
                                        _aiRecommendedExperts.value = offlineMatches
                                    }
                                }
                                _aiRecommendationMessage.value = reasoning.ifEmpty { "Beta, I found these wonderful souls who can teach you exactly what you need. Take a look!" }
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            _aiRecommendationMessage.value = "Beta, internet issues on my old phone, but I handpicked these wonderful teachers who match your interest in '$query'!"
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        _aiRecommendationMessage.value = "Beta, I have gathered these wonderful guides who match your search for '$query'. Let me know if you'd like to book a session with them!"
                    }
                }
            } else {
                val message = when {
                    offlineMatches.isNotEmpty() -> "Beta, I have lovingly sorted these active specialists who are perfect mentor partners for your interest in '$query'!"
                    else -> "Beta, I looked high and low but couldn't find anyone specifically for '$query' yet. Try searching for 'Maths', 'Finance', 'Yoga', or 'Science'!"
                }
                withContext(Dispatchers.Main) {
                    _aiRecommendationMessage.value = message
                }
            }
            withContext(Dispatchers.Main) {
                _isAiRecommending.value = false
            }
        }
    }

    // -- STATE FOR NESTED SUB-SCREENS --
    private val _currentCommunityId = MutableStateFlow<String?>(null)
    val currentCommunityId: StateFlow<String?> = _currentCommunityId.asStateFlow()

    private val _currentVoiceRoomId = MutableStateFlow<String?>(null)
    val currentVoiceRoomId: StateFlow<String?> = _currentVoiceRoomId.asStateFlow()

    private val _isVoiceRoomMinimized = MutableStateFlow(false)
    val isVoiceRoomMinimized: StateFlow<Boolean> = _isVoiceRoomMinimized.asStateFlow()

    private val _showLeaderDashboard = MutableStateFlow(false)
    val showLeaderDashboard: StateFlow<Boolean> = _showLeaderDashboard.asStateFlow()

    private val _recordingVoiceNote = MutableStateFlow(false)
    val recordingVoiceNote: StateFlow<Boolean> = _recordingVoiceNote.asStateFlow()

    // -- DATA LISTS (STATEFUL) --
    private val _communities = MutableStateFlow<List<Community>>(emptyList())
    val communities: StateFlow<List<Community>> = _communities.asStateFlow()

    private val _voiceRooms = MutableStateFlow<List<VoiceRoom>>(emptyList())
    val voiceRooms: StateFlow<List<VoiceRoom>> = _voiceRooms.asStateFlow()

    private val _activeVoiceParticipants = MutableStateFlow<List<VoiceParticipant>>(emptyList())
    val activeVoiceParticipants: StateFlow<List<VoiceParticipant>> = _activeVoiceParticipants.asStateFlow()

    private val _questions = MutableStateFlow<List<CommunityQuestion>>(emptyList())
    val questions: StateFlow<List<CommunityQuestion>> = _questions.asStateFlow()

    private val _events = MutableStateFlow<List<CommunityEvent>>(emptyList())
    val events: StateFlow<List<CommunityEvent>> = _events.asStateFlow()

    // -- AGE NO BAR DISCOVER & WISDOM PROFILES STATES --
    private val _experts = MutableStateFlow<List<Expert>>(emptyList())
    val experts: StateFlow<List<Expert>> = _experts.asStateFlow()

    private val _followedExpertIds = MutableStateFlow<Set<String>>(emptySet())
    val followedExpertIds: StateFlow<Set<String>> = _followedExpertIds.asStateFlow()

    // -- PREVIOUS SESSIONS & COMPLETED CALLS STATE --
    private val _previousCalls = MutableStateFlow(
        listOf(
            PreviousCall("call_1", "Srinivas Rao (Digital Safety)", 25, "Today, 4:15 PM", "Securing Google Password & OTPs", 5, true),
            PreviousCall("call_2", "Meera Patel (Plant Care)", 40, "Yesterday, 11:30 AM", "Balcony Tomato Soil Mixes", 5, true),
            PreviousCall("call_3", "Aarav Sharma (Coding Basics)", 15, "3 days ago", "Intro to Python Loops", 5, false)
        )
    )
    val previousCalls: StateFlow<List<PreviousCall>> = _previousCalls.asStateFlow()

    // -- TEACHER MODE WALLET BALANCE & REWARDS --
    private val _teacherWalletBalance = MutableStateFlow(2450.0) // INR
    val teacherWalletBalance: StateFlow<Double> = _teacherWalletBalance.asStateFlow()

    fun transferWalletToUpi(amount: Double) {
        _teacherWalletBalance.update { (it - amount).coerceAtLeast(0.0) }
    }

    private val _bookedSessionExpertIds = MutableStateFlow<Map<String, String>>(emptyMap()) // expertIdId -> selected datetime
    val bookedSessionExpertIds: StateFlow<Map<String, String>> = _bookedSessionExpertIds.asStateFlow()

    private val _privateQuestions = MutableStateFlow<Map<String, List<String>>>(emptyMap()) // expertId -> list of questions
    val privateQuestions: StateFlow<Map<String, List<String>>> = _privateQuestions.asStateFlow()

    // -- PROFILE BUILDER WITH CHACHI ONBOARDING STATE --
    private val _chachiOnboardingStepIndex = MutableStateFlow(0)
    val chachiOnboardingStepIndex: StateFlow<Int> = _chachiOnboardingStepIndex.asStateFlow()

    private val _chachiOnboardingAnswers = MutableStateFlow<Map<Int, String>>(emptyMap())
    val chachiOnboardingAnswers: StateFlow<Map<Int, String>> = _chachiOnboardingAnswers.asStateFlow()

    private val _chachiOnboardingProposal = MutableStateFlow<Expert?>(null)
    val chachiOnboardingProposal: StateFlow<Expert?> = _chachiOnboardingProposal.asStateFlow()

    // Voice room status
    private val _micMuted = MutableStateFlow(true)
    val micMuted: StateFlow<Boolean> = _micMuted.asStateFlow()

    private val _listenOnlyMode = MutableStateFlow(false)
    val listenOnlyMode: StateFlow<Boolean> = _listenOnlyMode.asStateFlow()

    private val _userHandRaised = MutableStateFlow(false)
    val userHandRaised: StateFlow<Boolean> = _userHandRaised.asStateFlow()

    private val _isSpeaker = MutableStateFlow(false)
    val isSpeaker: StateFlow<Boolean> = _isSpeaker.asStateFlow()

    // Active Audio simulation Job
    private var audioSimulationJob: Job? = null

    init {
        loadInitialData()
        startVoiceSimulation()
    }

    private fun loadInitialData() {
        // 1. Initial Communities
        val comms = listOf(
            Community(
                id = "comm_teachers",
                name = "Retired Teachers Circle",
                iconEmoji = "🏫",
                memberCount = 1240,
                category = "Education",
                description = "A warm, high-integrity safe space where veteran teachers coordinate homework help, exchange lesson tips, host community tutoring workshops, and run Ask a Teacher forums.",
                chatMessages = listOf(
                    GroupMessage(
                        senderId = "user_sharla",
                        senderName = "Sharla Devi",
                        senderAvatar = "avatar_sharla",
                        senderRole = "Verified Teacher",
                        text = "Good morning colleagues! I just finished uploading our revised Level 5 Fractions printable sheet to the homework library. Feel free to download!",
                        timestamp = "09:12 AM"
                    ),
                    GroupMessage(
                        senderId = "user_mohan",
                        senderName = "Mohan Rao",
                        senderAvatar = "avatar_mohan",
                        senderRole = "Circle Host",
                        text = "Thank you Sharla! We also have our Weekly Math Q&A voice room going live this evening. It's so amazing to stay active and helpful.",
                        timestamp = "09:30 AM",
                        isAnnouncement = true
                    )
                ),
                resources = listOf(
                    CommunityResource("res_fractions", "Level 5 Fractions Practice", "Comprehensive assessment guide with visual items representing food shares.", "PDF Document", "fractions_l5.pdf", "Sharla Devi"),
                    CommunityResource("res_phonics", "Primary Spoken English Guide", "Active phonic sounds for absolute beginners with speech pacing techniques.", "Voice Guide", "phonics_sounds.mp3", "Edward Smith")
                ),
                moderators = listOf("Mohan Rao", "Ramesh Kumar")
            ),
            Community(
                id = "comm_finance",
                name = "Financial Literacy Circle",
                iconEmoji = "💰",
                memberCount = 890,
                category = "Finance",
                description = "Mastering money management across eras. Seniors and youth sharing smart budgeting templates, fixed income evaluation, mutual fund selection, tax basics, and stress-free retirement pacing.",
                chatMessages = listOf(
                    GroupMessage(
                        senderId = "user_anand",
                        senderName = "Anand Shah",
                        senderAvatar = "avatar_anand",
                        senderRole = "Senior Mentor",
                        text = "Can anyone recommend a low-risk recurring deposit scheme with senior citizen benefits?",
                        timestamp = "08:15 AM"
                    ),
                    GroupMessage(
                        senderId = "user_priya",
                        senderName = "Priya Mehta",
                        senderAvatar = "avatar_priya",
                        senderRole = "Verified Expert",
                        text = "Anand, the post-office senior schemes offer 8.2% secure returns this quarter. I posted a comparative table in our Resource Library!",
                        timestamp = "08:45 AM"
                    )
                ),
                resources = listOf(
                    CommunityResource("res_tax", "Senior Tax Exemption Masterclass", "Step by step breakdown of standard deductions and interest limits.", "PDF Document", "senior_tax_in.pdf", "Priya Mehta"),
                    CommunityResource("res_budget", "Simplistic Zero-Based Household Tracker", "Extremely easy spreadsheet file formatted for low-effort input.", "Excel Worksheet", "simple_ledger.xlsx", "Anand Shah")
                ),
                moderators = listOf("Priya Mehta", "Anand Shah")
            ),
            Community(
                id = "comm_grandparents",
                name = "Grandparents Storytelling Circle",
                iconEmoji = "👵",
                memberCount = 1480,
                category = "Family",
                description = "Preserving folklore, nursery rhymes, lullabies, traditional recipes, and life-shaping values. Seniors record audio stories, teach regional dialects, and nurture intergenerational warmth.",
                chatMessages = listOf(
                    GroupMessage(
                        senderId = "user_leela",
                        senderName = "Grandma Leela",
                        senderAvatar = "avatar_leela",
                        senderRole = "Circle Ambassador",
                        text = "I recorded a voice message translating a short grandma folk story from Kannada into English for children. Sharing it here!",
                        timestamp = "10:02 AM"
                    ),
                    GroupMessage(
                        senderId = "user_leela",
                        senderName = "Grandma Leela",
                        senderAvatar = "avatar_leela",
                        senderRole = "Circle Ambassador",
                        text = "Audio story narration",
                        timestamp = "10:03 AM",
                        voiceNoteDurationSec = 52,
                        transcription = "Once upon a time in a lovely little village nestled near the Western Ghats, there lived an old, wise sparrow who taught the squirrels how to pre-save their sweet acorns for the heavy monsoon winds..."
                    )
                ),
                resources = listOf(
                    CommunityResource("res_kannada", "Basic Kannada Conversational Flashcards", "15 essential greetings, polite expressions, and everyday terms for grandchildren.", "PDF Cards", "kannada_kids.pdf", "Grandma Leela"),
                    CommunityResource("res_traditions", "Grandma's Natural Cooling Remedies", "Natural home remedies using cumin, mint, and rock candy.", "Voice Guide", "cooling_remedies.mp3", "Aparna Iyer")
                ),
                moderators = listOf("Grandma Leela", "Ramesh Kumar")
            ),
            Community(
                id = "comm_gardening",
                name = "Gardening & Plant Care Club",
                iconEmoji = "🌿",
                memberCount = 1050,
                category = "Interests",
                description = "Grow what you love. Discuss balcony composting, monsoon pruning, microgreens, pest control, and share photos of organic harvests with fellow green thumbs.",
                chatMessages = listOf(
                    GroupMessage(
                        senderId = "user_nisha",
                        senderName = "Nisha Hegde",
                        senderAvatar = "avatar_nisha",
                        senderRole = "New Member",
                        text = "Why are the leaves of my dynamic hibiscus shrub curling yellow? I water daily.",
                        timestamp = "Yesterday"
                    ),
                    GroupMessage(
                        senderId = "user_ramesh_temp",
                        senderName = "Ramesh Kumar",
                        senderAvatar = "avatar_ramesh",
                        senderRole = "Retired Principal",
                        text = "Nisha, yellow leaves usually suggest either root waterlogging or a nitrogen shortage. Let the soil dry completely before your next watering loop. Add composted tea waste!",
                        timestamp = "Yesterday"
                    )
                ),
                resources = listOf(
                    CommunityResource("res_compost", "Odourless Balcony Composting Guide", "Simple 3-tiered container setup that converts kitchen waste into black gold with zero smell.", "PDF Guide", "easy_compost.pdf", "Ramesh Kumar"),
                    CommunityResource("res_herbs", "Fast Starter Balcony Herbs Checklist", "Simple steps for growing mint, coriander, holy basil, and lemongrass from kitchen scraps.", "PDF Guide", " balcony_herbs.pdf", "Nisha Hegde")
                ),
                moderators = listOf("Ramesh Kumar", "Nisha Hegde")
            ),
            Community(
                id = "comm_music",
                name = "Music & Practice Circle",
                iconEmoji = "🎵",
                memberCount = 670,
                category = "Music",
                description = "A warm harbor for singing, classical keyboard lessons, listening sessions, and instrument advice. Live group performance rooms let everyone share melodies with love.",
                chatMessages = listOf(
                    GroupMessage(
                        senderId = "user_subramaniam",
                        senderName = "S. Subramaniam",
                        senderAvatar = "avatar_subru",
                        senderRole = "Circle Host",
                        text = "Let's practice the basic afternoon ragas in our practice room today! Everyone is welcome to tune in or unmute and play.",
                        timestamp = "Yesterday"
                    )
                ),
                resources = listOf(
                    CommunityResource("res_raga", "Intro to Hindustani Raga Systems", "Fundamental notes and structural morning-to-night schedules for common ragas.", "PDF Sheet", "raga_basics.pdf", "S. Subramaniam")
                ),
                moderators = listOf("S. Subramaniam")
            )
        )
        _communities.value = comms

        // 2. Initial Voice Rooms (Instant and Scheduled)
        _voiceRooms.value = listOf(
            VoiceRoom(
                id = "vr_gardening",
                title = "Monsoon Pruning & Soil Organic Preps",
                typeName = "Open Community Room",
                description = "Anyone can hop in! We are discussing composting tricks, protecting delicate buds from heavy rain, and dynamic coco-peat mixes.",
                category = "Gardening",
                hostName = "Ramesh Kumar",
                hostAvatar = "avatar_ramesh",
                hostRole = "Circle Host",
                activeSpeakerCount = 3,
                totalListenerCount = 18,
                isLive = true
            ),
            VoiceRoom(
                id = "vr_career",
                title = "Resume Polish & Confidence Drills",
                typeName = "Scheduled Expert Room",
                description = "Hosted by veteran corporate leaders. Senior recruiters help younger members frame careers with wisdom.",
                category = "Career Support",
                hostName = "Satya Narayanan",
                hostAvatar = "avatar_satya",
                hostRole = "Verified Mentor (Ex-HR VP)",
                activeSpeakerCount = 2,
                totalListenerCount = 45,
                isLive = true
            ),
            VoiceRoom(
                id = "vr_storytime",
                title = "Grandma Story hour: Tales of Tenali Rama",
                typeName = "Story Time Room",
                description = "Children and families welcome. Classic folk stories loaded with humour, wit, and warm life wisdom.",
                category = "Storytelling",
                hostName = "Grandma Leela",
                hostAvatar = "avatar_leela",
                hostRole = "Circle Ambassador",
                activeSpeakerCount = 1,
                totalListenerCount = 39,
                isLive = true
            ),
            VoiceRoom(
                id = "vr_english",
                title = "Spoken English Daily Hesitation Breaking",
                typeName = "Learning Circle",
                description = "Our weekly recurring comfort session. Purely for beginner speakers to speak short phrases slowly and build secure verbal rhythm.",
                category = "English Practice",
                hostName = "Professor Edward",
                hostAvatar = "avatar_edward",
                hostRole = "Retired English HoD",
                activeSpeakerCount = 4,
                totalListenerCount = 62,
                isLive = true
            ),
            VoiceRoom(
                id = "vr_meditation",
                title = "Morning Mindfulness & Breath Alignment",
                typeName = "Wellness Room",
                description = "Relaxing breathing loops, warm joint movements, and mental focus routines tailored for overall healthy aging.",
                category = "Wellness",
                hostName = "Swami Atmananda",
                hostAvatar = "avatar_swami",
                hostRole = "Yoga Expert",
                activeSpeakerCount = 1,
                totalListenerCount = 24,
                isLive = false,
                scheduledTime = "Tomorrow, 07:00 AM"
            )
        )

        // 3. Initial Questions ("Ask the Community" and "Ask a Mentor")
        _questions.value = listOf(
            CommunityQuestion(
                id = "q_maths",
                authorName = "Deepa Viswanathan",
                authorAvatar = "avatar_deepa",
                authorRole = "Aspirant & Parent",
                text = "My daughter struggles intensely with Grade 5 Maths, especially multi-digit divisions and percentages. How can I explain it visually without confusing her?",
                timestamp = "3 hours ago",
                category = "Education",
                helpfulCount = 8,
                replies = listOf(
                    QuestionReply(
                        authorName = "Sharla Devi",
                        authorAvatar = "avatar_sharla",
                        authorRole = "Verified Teacher",
                        text = "Deepa, try using paper-folding circles or dividing actual chocolate bars in segments! Draw a 100-cell grid for percentages. Let her colour 20 boxes to visually see 20%. It instantly builds intuition over rote formulas.",
                        timestamp = "2 hours ago"
                    ),
                    QuestionReply(
                        authorName = "Ramesh Kumar",
                        authorAvatar = "avatar_ramesh",
                        authorRole = "Retired Principal",
                        text = "Visual learning",
                        isVoiceReply = true,
                        voiceDuration = 45,
                        timestamp = "1 hour ago"
                    )
                )
            ),
            CommunityQuestion(
                id = "q_confident",
                authorName = "Anish Hegde",
                authorAvatar = "avatar_anish",
                authorRole = "New Graduate",
                text = "How can I improve my interview confidence with seniors? I stutter severely when asked 'Tell me about yourself'. Let me know if anyone can practice.",
                timestamp = "1 day ago",
                category = "Career Growth",
                helpfulCount = 12,
                replies = listOf(
                    QuestionReply(
                        authorName = "Satya Narayanan",
                        authorAvatar = "avatar_satya",
                        authorRole = "Verified Mentor (Ex-HR VP)",
                        text = "Anish, the secret is structuring your answer into the PPT formula: Present (your current degree), Past (high points in projects), and Future (why you're thrilled for this role). Practice in front of a mirror or with me tomorrow in our open voice room!",
                        timestamp = "20 hours ago"
                    )
                )
            ),
            CommunityQuestion(
                id = "q_spinach",
                authorName = "Rani Rao",
                authorAvatar = "avatar_rani",
                authorRole = "Junior Gardening Member",
                text = "What leafy vegetables grow well in Bangalore balcony pots during May-June? Any soil preparations?",
                timestamp = "2 days ago",
                category = "Gardening Circle",
                helpfulCount = 5,
                replies = listOf(
                    QuestionReply(
                        authorName = "Ramesh Kumar",
                        authorAvatar = "avatar_ramesh",
                        authorRole = "Retired Principal",
                        text = "Palak (Spinach), Amaranthus (Dant), and Coriander do beautifully! Make sure to mix soil with 30% organic compost and 15% sand to allow proper drainage.",
                        timestamp = "1 day ago"
                    )
                )
            )
        )

        // 4. Events
        _events.value = listOf(
            CommunityEvent(
                id = "ev_gardens",
                communityName = "Gardening & Plant Care Club",
                title = "Visual Balcony Composting Walkthrough",
                type = "Voice Workshop",
                localTime = "Today, 05:00 PM (1.5 hours)",
                description = "Live voice walkthrough showing how to manage dry-to-wet composting ratios to avoid smells or bugs in compact balcony layouts. Open to all green thumbs!",
                hostName = "Ramesh Kumar",
                hostAvatar = "avatar_ramesh",
                rsvpCount = 34,
                isUserRsvped = true
            ),
            CommunityEvent(
                id = "ev_interviews",
                communityName = "Career Growth Circle",
                title = "Fireside Q&A: Crack the HR Screenings",
                type = "AMA Session",
                localTime = "Thursday, 06:30 PM",
                description = "Bring your hardest career questions, resume items, and gaps. Verified expert mentors will evaluate live with zero judgement.",
                hostName = "Satya Narayanan",
                hostAvatar = "avatar_satya",
                rsvpCount = 112,
                isUserRsvped = false
            ),
            CommunityEvent(
                id = "ev_music",
                communityName = "Music & Practice Circle",
                title = "Acoustic Folk Performance & Jam Session",
                type = "Live Workshop",
                localTime = "Saturday, 08:00 PM",
                description = "Tune in to listen or hit Ask to Sing! Let's share some peaceful melodies across generations. Warm organic vibes guaranteed.",
                hostName = "S. Subramaniam",
                hostAvatar = "avatar_subru",
                rsvpCount = 48,
                isUserRsvped = false
            )
        )
        _experts.value = loadGeneratedExpertsForBridge()
    }

    // -- SIMULATE ACTIVE VOICE ROOM PEER WAVEPOWER & EMOJIS --
    private fun randomFloat(min: Float, max: Float): Float = kotlin.random.Random.nextFloat() * (max - min) + min
    private fun randomPercent(): Int = kotlin.random.Random.nextInt(101)

    private fun startVoiceSimulation() {
        audioSimulationJob?.cancel()
        audioSimulationJob = viewModelScope.launch {
            while (true) {
                delay(800)
                if (_currentVoiceRoomId.value != null && !_micMuted.value) {
                    // Update active speakers power waves
                    _activeVoiceParticipants.update { participants ->
                        participants.map { participant ->
                            if (participant.id == "user_senior_101") {
                                // Dynamic power check for user
                                participant.copy(wavePower = randomFloat(0.3f, 1.0f))
                            } else if (participant.isSpeaker && randomPercent() > 40) {
                                participant.copy(wavePower = randomFloat(0.2f, 0.9f))
                            } else {
                                participant.copy(wavePower = 0.0f)
                            }
                        }
                    }
                } else if (_currentVoiceRoomId.value != null) {
                    // Simulating other speakers speaking
                    _activeVoiceParticipants.update { participants ->
                        participants.map { participant ->
                            if (participant.isSpeaker && participant.id != "user_senior_101" && randomPercent() > 40) {
                                participant.copy(wavePower = randomFloat(0.1f, 0.85f))
                            } else {
                                participant.copy(wavePower = 0.0f)
                            }
                        }
                    }
                }
                // Randomly trigger occasional custom emoji bubble animations from peer participants!
                if (_currentVoiceRoomId.value != null && kotlin.random.Random.nextInt(11) > 7) {
                    val emojis = listOf("❤️", "🌟", "🙏", "🎓", "🤝", "🌱", "👍", "🔥")
                    val randomEmoji = emojis.random()
                    _activeVoiceParticipants.update { participants ->
                        if (participants.isNotEmpty()) {
                            val pickIdx = kotlin.random.Random.nextInt(participants.size)
                            participants.mapIndexed { idx, p ->
                                if (idx == pickIdx && p.id != "user_senior_101") {
                                    p.copy(recentEmojiReaction = randomEmoji)
                                } else {
                                    p.copy(recentEmojiReaction = null)
                                }
                            }
                        } else participants
                    }
                }
            }
        }
    }

    // -- MUTATORS & NAVIGATION ACTIONS --
    fun selectTab(tab: AppTab) {
        if (tab == AppTab.Home) {
            _homeResetTrigger.value = _homeResetTrigger.value + 1
            _selectedSchedulerExpertId.value = null
            _selectedRescheduleBookingId.value = null
        }
        _currentTab.value = tab
        _currentCommunityId.value = null
        _showLeaderDashboard.value = false
    }

    fun enterCommunity(communityId: String) {
        _currentCommunityId.value = communityId
        _currentTab.value = AppTab.Communities
    }

    fun exitCurrentCommunity() {
        _currentCommunityId.value = null
    }

    fun setLeaderDashboard(visible: Boolean) {
        _showLeaderDashboard.value = visible
    }

    // -- VOICE ROOM COMPOSITIONS --
    fun joinVoiceRoom(roomId: String) {
        _currentVoiceRoomId.value = roomId
        _isVoiceRoomMinimized.value = false
        _userHandRaised.value = false

        // Determine if they are the host (Ramesh host gardening room)
        val room = _voiceRooms.value.firstOrNull { it.id == roomId }
        val actsAsHost = room?.hostName == "Ramesh Kumar"

        _isSpeaker.value = actsAsHost
        _micMuted.value = !actsAsHost // muted initially if listener

        // Populate dynamic group of simulated listeners and speakers
        val mocks = mutableListOf<VoiceParticipant>()
        mocks.add(
            VoiceParticipant(
                id = "user_senior_101",
                name = "Ramesh Kumar (You)",
                avatar = "avatar_ramesh",
                isSpeaker = actsAsHost,
                isMuted = !actsAsHost
            )
        )
        
        if (roomId == "vr_gardening") {
            mocks.add(VoiceParticipant("user_mohan", "Mohan Rao", "avatar_mohan", isSpeaker = true))
            mocks.add(VoiceParticipant("user_nisha", "Nisha Hegde", "avatar_nisha", isSpeaker = true))
            mocks.add(VoiceParticipant("user_priya", "Priya Mehta", "avatar_priya", isSpeaker = false))
            mocks.add(VoiceParticipant("user_anand", "Anand Shah", "avatar_anand", isSpeaker = false, isHandRaised = true))
            mocks.add(VoiceParticipant("user_sharla", "Sharla Devi", "avatar_sharla", isSpeaker = false))
        } else if (roomId == "vr_career") {
            mocks.add(VoiceParticipant("user_satya", "Satya Narayanan", "avatar_satya", isSpeaker = true))
            mocks.add(VoiceParticipant("user_anish", "Anish Hegde", "avatar_anish", isSpeaker = true))
            mocks.add(VoiceParticipant("user_deepa", "Deepa Viswanathan", "avatar_deepa", isSpeaker = false, isHandRaised = true))
        } else {
            mocks.add(VoiceParticipant("user_leela", "Grandma Leela", "avatar_leela", isSpeaker = true))
            mocks.add(VoiceParticipant("user_rani", "Rani Rao", "avatar_rani", isSpeaker = false))
        }
        
        _activeVoiceParticipants.value = mocks

        // Mark room as active if schedule matches
        _voiceRooms.update { list ->
            list.map { r ->
                if (r.id == roomId) r.copy(isLive = true, totalListenerCount = r.totalListenerCount + 1) else r
            }
        }
    }

    fun toggleMic() {
        if (_isSpeaker.value) {
            _micMuted.value = !_micMuted.value
            _activeVoiceParticipants.update { list ->
                list.map { p ->
                    if (p.id == "user_senior_101") p.copy(isMuted = _micMuted.value) else p
                }
            }
        }
    }

    fun toggleHandRaise() {
        if (!_isSpeaker.value) {
            _userHandRaised.value = !_userHandRaised.value
            _activeVoiceParticipants.update { list ->
                list.map { p ->
                    if (p.id == "user_senior_101") p.copy(isHandRaised = _userHandRaised.value) else p
                }
            }
        }
    }

    fun makeSpeaker(participantId: String) {
        _activeVoiceParticipants.update { list ->
            list.map { p ->
                if (p.id == participantId) p.copy(isSpeaker = true, isHandRaised = false) else p
            }
        }
    }

    fun becomeSpeaker() {
        _isSpeaker.value = true
        _micMuted.value = false
        _userHandRaised.value = false
        _activeVoiceParticipants.update { list ->
            list.map { p ->
                if (p.id == "user_senior_101") p.copy(isSpeaker = true, isMuted = false, isHandRaised = false) else p
            }
        }
    }

    fun toggleFollowSpeaker(pId: String) {
        _activeVoiceParticipants.update { list ->
            list.map { p ->
                if (p.id == pId) p.copy(isFollowed = !p.isFollowed) else p
            }
        }
    }

    fun castEmojiReaction(emoji: String) {
        _activeVoiceParticipants.update { list ->
            list.map { p ->
                if (p.id == "user_senior_101") p.copy(recentEmojiReaction = emoji) else p
            }
        }
        viewModelScope.launch {
            delay(1500)
            _activeVoiceParticipants.update { list ->
                list.map { p ->
                    if (p.id == "user_senior_101" && p.recentEmojiReaction == emoji) p.copy(recentEmojiReaction = null) else p
                }
            }
        }
    }

    fun minimizeVoiceRoom() {
        _isVoiceRoomMinimized.value = true
    }

    fun maximizeVoiceRoom() {
        _isVoiceRoomMinimized.value = false
        _currentTab.value = AppTab.VoiceRooms
    }

    fun disconnectVoiceRoom() {
        val currId = _currentVoiceRoomId.value
        _currentVoiceRoomId.value = null
        _isVoiceRoomMinimized.value = false
        _activeVoiceParticipants.value = emptyList()

        if (currId != null) {
            _voiceRooms.update { list ->
                list.map { r ->
                    if (r.id == currId) r.copy(totalListenerCount = (r.totalListenerCount - 1).coerceAtLeast(0)) else r
                }
            }
        }
    }

    fun createVoiceRoom(title: String, typeSelection: String, category: String, desc: String) {
        val newRoom = VoiceRoom(
            id = "vr_custom_${UUID.randomUUID()}",
            title = title,
            typeName = typeSelection,
            description = desc,
            category = category,
            hostName = _currentUser.value.name,
            hostAvatar = _currentUser.value.avatarUrl,
            hostRole = _currentUser.value.role,
            activeSpeakerCount = 1,
            totalListenerCount = 1,
            isLive = true
        )
        _voiceRooms.update { listOf(newRoom) + it }
        joinVoiceRoom(newRoom.id)
    }

    // -- COMMUNITY MESSAGING IMPLEMENTATIONS --
    fun startVoiceRecording() {
        _recordingVoiceNote.value = true
    }

    fun stopAndSendVoiceNote(targetCommunityId: String, durationSec: Int) {
        _recordingVoiceNote.value = false
        val newMsgId = UUID.randomUUID().toString()
        val defaultTranscribingText = "Transcribing voice with secure offline AI..."
        val realTranscript = "Dear friends, let's keep sharing encouraging gardening notes! Just placed dynamic organic cow dung compost at our local seed swap shelves today. Welcome all new garden enthusiast members!"

        val newMsg = GroupMessage(
            id = newMsgId,
            senderId = _currentUser.value.id,
            senderName = _currentUser.value.name,
            senderAvatar = _currentUser.value.avatarUrl,
            senderRole = _currentUser.value.role,
            text = "Voice note played ($durationSec s)",
            timestamp = "Just Now",
            voiceNoteDurationSec = durationSec,
            transcription = defaultTranscribingText
        )

        // Add to targeted community
        _communities.update { list ->
            list.map { c ->
                if (c.id == targetCommunityId) c.copy(chatMessages = c.chatMessages + newMsg) else c
            }
        }

        // Simulate fast, visual transcription animation in 1.4 seconds!
        viewModelScope.launch {
            delay(1400)
            _communities.update { list ->
                list.map { c ->
                    if (c.id == targetCommunityId) {
                        val updatedMessages = c.chatMessages.map { m ->
                            if (m.id == newMsgId) m.copy(transcription = realTranscript) else m
                        }
                        c.copy(chatMessages = updatedMessages)
                    } else c
                }
            }
        }
    }

    fun sendTextMessage(targetCommunityId: String, text: String, imageUrl: String? = null) {
        val newMsg = GroupMessage(
            id = UUID.randomUUID().toString(),
            senderId = _currentUser.value.id,
            senderName = _currentUser.value.name,
            senderAvatar = _currentUser.value.avatarUrl,
            senderRole = _currentUser.value.role,
            text = text,
            imageUrl = imageUrl,
            timestamp = "Just Now"
        )
        _communities.update { list ->
            list.map { c ->
                if (c.id == targetCommunityId) c.copy(chatMessages = c.chatMessages + newMsg) else c
            }
        }
    }

    fun sendPoll(targetCommunityId: String, question: String, options: List<String>) {
        val newMsg = GroupMessage(
            id = UUID.randomUUID().toString(),
            senderId = _currentUser.value.id,
            senderName = _currentUser.value.name,
            senderAvatar = _currentUser.value.avatarUrl,
            senderRole = _currentUser.value.role,
            text = "Poll: $question",
            timestamp = "Just Now",
            pollQuestion = question,
            pollOptions = options.map { PollOption(it, 0) }
        )
        _communities.update { list ->
            list.map { c ->
                if (c.id == targetCommunityId) c.copy(chatMessages = c.chatMessages + newMsg) else c
            }
        }
    }

    fun submitPollVote(communityId: String, messageId: String, selectedOptionText: String) {
        _communities.update { list ->
            list.map { c ->
                if (c.id == communityId) {
                    val updatedMsgs = c.chatMessages.map { m ->
                        if (m.id == messageId && m.pollOptions != null) {
                            val nextOpts = m.pollOptions.map { opt ->
                                if (opt.optionText == selectedOptionText) {
                                    val containsMe = opt.votedUserIds.contains(_currentUser.value.id)
                                    val nextUsers = if (containsMe) opt.votedUserIds - _currentUser.value.id else opt.votedUserIds + _currentUser.value.id
                                    val countDiff = if (containsMe) -1 else 1
                                    opt.copy(voteCount = (opt.voteCount + countDiff).coerceAtLeast(0), votedUserIds = nextUsers)
                                } else opt
                            }
                            m.copy(pollOptions = nextOpts)
                        } else m
                    }
                    c.copy(chatMessages = updatedMsgs)
                } else c
            }
        }
    }

    // -- ASK COMMUNITY FUNCTIONS --
    fun postQuestion(query: String, category: String, voiceNoteSec: Int? = null) {
        val newQId = UUID.randomUUID().toString()
        val isVoice = voiceNoteSec != null
        val transText = if (isVoice) "Transcribing beautiful audio question..." else null
        val finalTranscript = if (isVoice) "Hello experts, I am Ramesh. I have been facing challenges in getting my winter cauliflower seedlings to grow. They sprout but wilt in two days. Soil drainage is great. Please guide!" else null

        val newQ = CommunityQuestion(
            id = newQId,
            authorName = _currentUser.value.name,
            authorAvatar = _currentUser.value.avatarUrl,
            authorRole = _currentUser.value.role,
            text = if (isVoice) "[Play Voice Question]" else query,
            timestamp = "Just Now",
            category = category,
            voiceNoteUrl = if (isVoice) "voice_q_${newQId}.mp3" else null,
            transcription = transText,
            helpfulCount = 0
        )

        _questions.update { listOf(newQ) + it }

        if (isVoice) {
            viewModelScope.launch {
                delay(1400)
                _questions.update { list ->
                    list.map { q ->
                        if (q.id == newQId) q.copy(transcription = finalTranscript) else q
                    }
                }
            }
        }
    }

    fun savePrivateMentorQuestion(query: String, targetMentor: String) {
        val newQ = CommunityQuestion(
            id = UUID.randomUUID().toString(),
            authorName = _currentUser.value.name,
            authorAvatar = _currentUser.value.avatarUrl,
            authorRole = _currentUser.value.role,
            text = query,
            timestamp = "Just Now",
            category = "Private Session Setup",
            isPrivateMentorRequest = true,
            targetMentorName = targetMentor
        )
        _questions.update { listOf(newQ) + it }
    }

    fun submitReplyToQuestion(questionId: String, text: String, isVoice: Boolean = false, voiceDur: Int? = null) {
        val newReply = QuestionReply(
            authorName = _currentUser.value.name,
            authorAvatar = _currentUser.value.avatarUrl,
            authorRole = _currentUser.value.role,
            text = text,
            isVoiceReply = isVoice,
            voiceDuration = voiceDur,
            timestamp = "Just Now"
        )
        _questions.update { list ->
            list.map { q ->
                if (q.id == questionId) q.copy(replies = q.replies + newReply) else q
            }
        }
    }

    fun toggleHelpfulQuestion(questionId: String) {
        _questions.update { list ->
            list.map { q ->
                if (q.id == questionId) q.copy(helpfulCount = q.helpfulCount + 1) else q
            }
        }
    }

    // -- EVENTS ACTIONS & RSVPS --
    fun toggleEventRsvp(eventId: String) {
        _events.update { list ->
            list.map { ev ->
                if (ev.id == eventId) {
                    val nextRsvpState = !ev.isUserRsvped
                    val diff = if (nextRsvpState) 1 else -1
                    ev.copy(isUserRsvped = nextRsvpState, rsvpCount = (ev.rsvpCount + diff).coerceAtLeast(0))
                } else ev
            }
        }
    }

    // -- REPUTATION SYSTEM AWARDING --
    fun awardReputationToUser(targetMemberName: String, kind: ReputationKind) {
        // If Ramesh awards reputation to someone in replies or chat
        // We can simulate increasing reputation or toast it
        // Simulating receiving appreciation
        if (targetMemberName == _currentUser.value.name) {
            _currentUser.update { profile ->
                val updatedRep = profile.reputation.map { rep ->
                    if (rep.kind == kind) rep.copy(count = rep.count + 1) else rep
                }
                profile.copy(reputation = updatedRep, points = profile.points + 10)
            }
        }
    }

    // -- LEADER APPLICATION PROCESS --
    fun participateAsLeader() {
        _currentUser.update { it.copy(isCommunityLeader = true) }
    }

    // -- AI CHACHI LOVING COMPANION ENGINE --
    private val _chachiChat = MutableStateFlow<List<GroupMessage>>(listOf(
        GroupMessage(
            id = "chachi_init",
            senderId = "ai_chachi",
            senderName = "AI Chachi 👵",
            senderAvatar = "avatar_chachi",
            senderRole = "Loving Companion",
            text = "Namaste Ramesh beta! I am your AI Chachi, always ready to assist you. If technology feels confusing, or you just want a warm story or recipe, speak to me as if I'm your elder sister. How can I bring peace to your heart today?",
            timestamp = "10:00 AM"
        )
    ))
    val chachiChat: StateFlow<List<GroupMessage>> = _chachiChat.asStateFlow()

    private val _isChachiTyping = MutableStateFlow(false)
    val isChachiTyping: StateFlow<Boolean> = _isChachiTyping.asStateFlow()

    fun matchLocalEntities(query: String): Triple<List<String>, List<String>, List<String>> {
        val q = query.lowercase().trim()
        val matchedExperts = mutableListOf<String>()
        val matchedCommunities = mutableListOf<String>()
        val matchedBookings = mutableListOf<String>()

        // Custom triggers for visual scheduling assistant
        if (q.contains("lakshmi") || q.contains("math")) {
            matchedExperts.add("exp_seed_maths_1")
        }
        if (q.contains("garden") || q.contains("dutta") || q.contains("reschedule")) {
            matchedExperts.add("exp_seed_gardening_0")
        }
        if (q.contains("sessions") || q.contains("appointment") || q.contains("schedule") || q.contains("my schedule")) {
            matchedBookings.add("b_default_1")
            matchedBookings.add("b_default_2")
        }

        val expertsList = _experts.value
        val communitiesList = _communities.value
        val bookingsList = _bookingsList.value

        val isSessionQuery = q.contains("session") || q.contains("booking") || q.contains("appointment") ||
                q.contains("upcoming") || q.contains("past") || q.contains("class") || q.contains("schedule")

        // Match Experts
        for (exp in expertsList) {
            val matchesName = exp.name.lowercase().contains(q) || q.contains(exp.name.lowercase())
            val matchesTitle = exp.title.lowercase().contains(q)
            val matchesBio = exp.bio.lowercase().contains(q)
            val matchesTags = exp.skillsTags.any { tag -> q.contains(tag.lowercase()) || tag.lowercase().contains(q) }
            val matchesCategory = exp.category.lowercase().contains(q)
            
            val queryWords = q.split("\\s+".toRegex()).filter { it.length > 2 }
            val wordMatch = queryWords.any { word ->
                exp.name.lowercase().contains(word) || exp.title.lowercase().contains(word) ||
                exp.bio.lowercase().contains(word) || exp.skillsTags.any { it.lowercase().contains(word) }
            }

            if (matchesName || matchesTitle || matchesBio || matchesTags || matchesCategory || wordMatch) {
                matchedExperts.add(exp.id)
            }
        }

        // Match Communities
        for (comm in communitiesList) {
            val matchesName = comm.name.lowercase().contains(q) || q.contains(comm.name.lowercase())
            val matchesDesc = comm.description.lowercase().contains(q)
            val matchesCategory = comm.category.lowercase().contains(q)
            
            val queryWords = q.split("\\s+".toRegex()).filter { it.length > 2 }
            val wordMatch = queryWords.any { word ->
                comm.name.lowercase().contains(word) || comm.description.lowercase().contains(word)
            }

            if (matchesName || matchesDesc || matchesCategory || wordMatch) {
                matchedCommunities.add(comm.id)
            }
        }

        // Match Bookings
        if (isSessionQuery) {
            matchedBookings.addAll(bookingsList.map { it.id })
        } else {
            for (b in bookingsList) {
                if (b.expertName.lowercase().contains(q) || q.contains(b.expertName.lowercase())) {
                    matchedBookings.add(b.id)
                }
            }
        }

        return Triple(matchedExperts, matchedCommunities, matchedBookings)
    }

    fun generateLocalTextMessage(
        queryLower: String,
        matchedExperts: List<String>,
        matchedCommunities: List<String>,
        matchedBookings: List<String>
    ): String {
        return when {
            queryLower.contains("lakshmi") && (queryLower.contains("availability") || queryLower.contains("show")) -> {
                "Pranam Ramesh beta! I have fetched Lakshmi Rao's availability calendar for you. You can inspect her available, booked, and upcoming time slots here. Click 'Open Calendar' to see her week strip!"
            }
            queryLower.contains("math") && (queryLower.contains("book") || queryLower.contains("tomorrow") || queryLower.contains("teacher")) -> {
                "Namaste Ramesh beta! Lakshmi Rao is our wonderful Mathematics Mentor ready to help with algebra or geometry. I have attached her visual status below. Simply tap 'Open Calendar' to book a session tomorrow!"
            }
            queryLower.contains("reschedule") && (queryLower.contains("garden") || queryLower.contains("class") || queryLower.contains("dutta")) -> {
                "Amaryllis beta, let's reschedule your terrace gardening session with Major Gen. Dutta. I have loaded his profile below. Click 'Open Calendar' to pick an alternative timing directly!"
            }
            queryLower.contains("sessions") || queryLower.contains("appointment") || queryLower.contains("my schedule") || queryLower.contains("show my") -> {
                "Dear Ramesh beta, I have compiled your entire weekly schedule of booked sessions and live lessons below. You can join calls, view notes, or reschedule any slot instantly by opening the calendar!"
            }
            queryLower.contains("session") || queryLower.contains("booking") || queryLower.contains("upcoming") || queryLower.contains("schedule") -> {
                if (matchedBookings.isNotEmpty()) {
                    val size = matchedBookings.size
                    "Dear Ramesh beta, I found your registered sessions and appointments! Here are the $size upcoming meetings I have retrieved from our schedule. You can join them directly from here:"
                } else {
                    "Pyaare Ramesh bacha, I checked your calendar but couldn't find any upcoming booked sessions. Would you like me to recommend some experts to swap wisdom with?"
                }
            }
            queryLower.contains("math") || queryLower.contains("tutor") || queryLower.contains("teacher") || queryLower.contains("algebra") -> {
                val expertsCount = matchedExperts.size
                if (expertsCount > 0) {
                    "Pyaare bacha, I found $expertsCount maths teachers in our Wisdom Bridge family who are eager to connect and share knowledge with you. I have listed their profiles below!"
                } else {
                    "I searched our family catalog, beta, but we couldn't find any mathematics teachers online right now. Let me find a general mentor or study circle for you instead!"
                }
            }
            queryLower.contains("garden") || queryLower.contains("plant") || queryLower.contains("farming") || queryLower.contains("nature") || queryLower.contains("compost") -> {
                val expSize = matchedExperts.size
                val commSize = matchedCommunities.size
                "Ah, gardening, my favorite! Ramesh beta, I found $expSize Gardening Experts and $commSize Gardening Circles in our platform where you can swap balcony composting tips and organic garden stories. Take a look at these:"
            }
            else -> {
                if (matchedExperts.isNotEmpty() || matchedCommunities.isNotEmpty() || matchedBookings.isNotEmpty()) {
                    val expCount = matchedExperts.size
                    val commCount = matchedCommunities.size
                    val bookCount = matchedBookings.size
                    
                    var reply = "Dear Ramesh beta, I did a thorough search of our Wisdom Bridge family based on your request! Here is what I found:\n"
                    if (expCount > 0) reply += "• $expCount lovely mentors and experts ready to guide you\n"
                    if (commCount > 0) reply += "• $commCount active community learning circles\n"
                    if (bookCount > 0) reply += "• $bookCount scheduled sessions matching your query\n"
                    reply += "\nI have attached them below so you can connect, chat, or join them with a single tap!"
                    reply
                } else {
                    "I hear you, Ramesh beta. Your words carry a beautiful, thoughtful reflection. In our Wisdom Bridge, we prioritize helping you connect with real people!\n\nI can recommend:\n• Custom experts and mentors matching your goals\n• Active community learning Circles to join\n\nTell me: what are you looking to learn today, beta?"
                }
            }
        }
    }

    fun askChachi(query: String) {
        if (query.isBlank()) return

        val userMsg = GroupMessage(
            id = UUID.randomUUID().toString(),
            senderId = _currentUser.value.id,
            senderName = _currentUser.value.name,
            senderAvatar = _currentUser.value.avatarUrl,
            senderRole = _currentUser.value.role,
            text = query,
            timestamp = "Just Now"
        )

        _chachiChat.update { it + userMsg }
        _isChachiTyping.value = true

        viewModelScope.launch {
            val responseText = withContext(Dispatchers.IO) {
                val apiKey = try {
                    BuildConfig.GEMINI_API_KEY
                } catch (e: Exception) {
                    ""
                }
                
                val isValidKey = apiKey.isNotEmpty() && !apiKey.contains("MY_GEMINI_API_KEY") && !apiKey.contains("PLACEHOLDER")
                
                if (isValidKey) {
                    try {
                        val modelName = "gemini-3.5-flash"
                        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
                        
                        val expertsList = _experts.value
                        val communitiesList = _communities.value
                        val bookingsList = _bookingsList.value
                        
                        val systemsPrompt = """
                            You are AI Chachi 👵, a loving, supportive, warm Indian maternal grandmother who helps senior citizens find real experts, learning circles, and their bookings on our platform 'Wisdom Bridge'.
                            The user is Ramesh Kumar, a Senior Principal & organic gardener.
                            
                            Here are the available Experts in the platform db:
                            ${
                                expertsList.joinToString("\n") { 
                                    "- ID: ${it.id} | Name: ${it.name} | Title: ${it.title} | Category: ${it.category} | Skills: ${it.skillsTags.joinToString(", ")} | Fee: ${it.flatSessionFee}" 
                                }
                            }
                            
                            Here are the available Communities/Circles in the platform db:
                            ${
                                communitiesList.joinToString("\n") { 
                                    "- ID: ${it.id} | Name: ${it.name} | Description: ${it.description} | Category: ${it.category}" 
                                }
                            }
                            
                            Here is the User's schedule /Bookings list:
                            ${
                                bookingsList.joinToString("\n") { 
                                    "- Booking ID: ${it.id} | Expert Name: ${it.expertName} | Expert ID: ${it.expertId} | Time: ${it.timing} | Status: ${it.status}" 
                                }
                            }

                            Instructions:
                            1. Respond to the user with immense elder-friendly warmth, motherly care, and respectful love (using hindi-infused English terms like 'beta', 'bacha', 'Pranam').
                            2. Maintain a highly helpful and action-oriented tone. Highlight what matches their needs.
                            3. Format any matched Expert, Community, or Booking ID under the section ###ATTACHMENTS### at the very end of your response, strictly following this exact structure:
                            
                            ###ATTACHMENTS###
                            experts: expertId1, expertId2
                            communities: communityId1
                            bookings: bookingId1
                            ###
                            
                            Note: If no entities match the query, leave the fields under the attachments header empty, but ALWAYS include the header if they ask for dynamic records. Only include IDs that exist in the lists above.
                        """.trimIndent()

                        val requestObj = JSONObject()
                        val contentsArr = JSONArray()
                        val contentObj = JSONObject()
                        val partsArr = JSONArray()
                        
                        val partObj = JSONObject()
                        partObj.put("text", "User Input: " + query)
                        partsArr.put(partObj)
                        contentObj.put("parts", partsArr)
                        contentsArr.put(contentObj)
                        requestObj.put("contents", contentsArr)
                        
                        val systemInstructionObj = JSONObject()
                        val sysPartsArr = JSONArray()
                        val sysPartObj = JSONObject()
                        sysPartObj.put("text", systemsPrompt)
                        sysPartsArr.put(sysPartObj)
                        systemInstructionObj.put("parts", sysPartsArr)
                        requestObj.put("systemInstruction", systemInstructionObj)
                        
                        val generationConfigObj = JSONObject()
                        generationConfigObj.put("temperature", 0.5)
                        requestObj.put("generationConfig", generationConfigObj)

                        val mediaType = "application/json; charset=utf-8".toMediaType()
                        val body = requestObj.toString().toRequestBody(mediaType)
                        val request = Request.Builder()
                            .url(url)
                            .post(body)
                            .build()
                        
                        okHttpClient.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                val resStr = response.body?.string() ?: ""
                                val responseJson = JSONObject(resStr)
                                val candidate = responseJson.optJSONArray("candidates")?.optJSONObject(0)
                                val text = candidate?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")
                                text
                            } else {
                                null
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                } else {
                    null
                }
            }
            
            var textMessage = ""
            var matchedExperts = listOf<String>()
            var matchedCommunities = listOf<String>()
            var matchedBookings = listOf<String>()
            
            if (responseText != null && responseText.contains("###ATTACHMENTS###")) {
                try {
                    val parts = responseText.split("###ATTACHMENTS###")
                    textMessage = parts[0].trim()
                    
                    val attachmentSection = parts.getOrNull(1)?.replace("###", "")?.trim() ?: ""
                    val lines = attachmentSection.split("\n")
                    
                    for (line in lines) {
                        val trimmed = line.trim()
                        if (trimmed.startsWith("experts:", ignoreCase = true)) {
                            val expPart = trimmed.substringAfter("experts:").trim()
                            if (expPart.isNotEmpty()) {
                                matchedExperts = expPart.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            }
                        } else if (trimmed.startsWith("communities:", ignoreCase = true)) {
                            val commPart = trimmed.substringAfter("communities:").trim()
                            if (commPart.isNotEmpty()) {
                                matchedCommunities = commPart.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            }
                        } else if (trimmed.startsWith("bookings:", ignoreCase = true)) {
                            val bookPart = trimmed.substringAfter("bookings:").trim()
                            if (bookPart.isNotEmpty()) {
                                matchedBookings = bookPart.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    textMessage = responseText
                }
            } else if (responseText != null) {
                textMessage = responseText
                val localTriple = matchLocalEntities(query)
                matchedExperts = localTriple.first
                matchedCommunities = localTriple.second
                matchedBookings = localTriple.third
            } else {
                val localTriple = matchLocalEntities(query)
                matchedExperts = localTriple.first
                matchedCommunities = localTriple.second
                matchedBookings = localTriple.third
                textMessage = generateLocalTextMessage(query.lowercase(), matchedExperts, matchedCommunities, matchedBookings)
            }
            
            val chachiMsg = GroupMessage(
                id = UUID.randomUUID().toString(),
                senderId = "ai_chachi",
                senderName = "AI Chachi 👵",
                senderAvatar = "avatar_chachi",
                senderRole = "Loving Companion",
                text = textMessage,
                timestamp = "Just Now",
                attachedExpertIds = matchedExperts.take(5),
                attachedCommunityIds = matchedCommunities.take(5),
                attachedBookingIds = matchedBookings.take(5)
            )
            
            _chachiChat.update { it + chachiMsg }
            _isChachiTyping.value = false
        }
    }

    // -- AGE NO BAR MATCHMAKING & WISDOM HARVEST ACTIONS --
    fun changeUserRoleType(newRole: String) {
        _currentUser.update { it.copy(userRoleType = newRole) }
    }

    fun toggleFollowExpert(expertId: String) {
        _followedExpertIds.update { current ->
            if (current.contains(expertId)) current - expertId else current + expertId
        }
    }

    // -- SHARED PREFERENCES PERSISTENCE MODULE --
    private var sharedPrefs: android.content.SharedPreferences? = null

    // -- CHATS & CONVERSATIONS STATE FLOWS --
    private val _directConversations = MutableStateFlow<List<DirectConversation>>(emptyList())
    val directConversations: StateFlow<List<DirectConversation>> = _directConversations.asStateFlow()

    private val _selectedDirectConversationId = MutableStateFlow<String?>(null)
    val selectedDirectConversationId: StateFlow<String?> = _selectedDirectConversationId.asStateFlow()

    // -- BOOKINGS STATE FLOWS --
    private val _bookingsList = MutableStateFlow<List<Booking>>(emptyList())
    val bookingsList: StateFlow<List<Booking>> = _bookingsList.asStateFlow()

    // -- GLOBAL SCHEDULER STATE FLOWS --
    private val _selectedSchedulerExpertId = MutableStateFlow<String?>(null)
    val selectedSchedulerExpertId = _selectedSchedulerExpertId.asStateFlow()

    private val _selectedRescheduleBookingId = MutableStateFlow<String?>(null)
    val selectedRescheduleBookingId = _selectedRescheduleBookingId.asStateFlow()

    fun openScheduler(expertId: String, rescheduleBookingId: String? = null) {
        _selectedSchedulerExpertId.value = expertId
        _selectedRescheduleBookingId.value = rescheduleBookingId
    }

    fun closeScheduler() {
        _selectedSchedulerExpertId.value = null
        _selectedRescheduleBookingId.value = null
    }

    // -- APPOINTMENT MODIFICATION & EDIT STATE --
    private val _editingBooking = MutableStateFlow<Booking?>(null)
    val editingBooking = _editingBooking.asStateFlow()

    fun setEditingBooking(booking: Booking?) {
        _editingBooking.value = booking
    }

    private val _activeClassroomBooking = MutableStateFlow<Booking?>(null)
    val activeClassroomBooking = _activeClassroomBooking.asStateFlow()

    fun setActiveClassroomBooking(booking: Booking?) {
        _activeClassroomBooking.value = booking
    }

    fun joinBooking(bookingId: String) {
        val currentBookings = _bookingsList.value
        val bookingIndex = currentBookings.indexOfFirst { it.id == bookingId }
        
        android.util.Log.d("AgeNoBarDB", "joinBooking: ID = $bookingId")
        
        if (bookingIndex != -1) {
            val booking = currentBookings[bookingIndex]
            viewModelScope.launch(Dispatchers.IO) {
                // Insert booking with status 'joined' in background DB
                bookingDao?.insertBooking(
                    DbBooking(
                        id = booking.id,
                        expert_id = booking.expertId,
                        learner_id = "user_senior_101",
                        slot_time = booking.timing,
                        status = "joined",
                        duration_minutes = booking.durationMinutes,
                        is_voice = booking.isVoice,
                        is_video = booking.isVideo,
                        created_at = System.currentTimeMillis()
                    )
                )
                withContext(Dispatchers.Main) {
                    val updatedBooking = booking.copy(status = "Joined")
                    val mutableList = currentBookings.toMutableList()
                    mutableList[bookingIndex] = updatedBooking
                    _bookingsList.value = mutableList
                    
                    saveBookings()
                    _activeClassroomBooking.value = updatedBooking
                }
            }
        }
    }

    fun updateBookingDetails(
        bookingId: String,
        newTiming: String,
        newDuration: Int,
        isVoice: Boolean,
        isVideo: Boolean
    ) {
        val currentBookings = _bookingsList.value
        val bookingIndex = currentBookings.indexOfFirst { it.id == bookingId }
        val expertId = if (bookingIndex != -1) currentBookings[bookingIndex].expertId else ""
        val expert = _experts.value.find { it.id == expertId }
        
        android.util.Log.d("AgeNoBarDB", "updateBookingDetails: ID = $bookingId timing = $newTiming, duration = $newDuration, voice = $isVoice, video = $isVideo")
        
        if (bookingIndex != -1 && expert != null) {
            val oldBooking = currentBookings[bookingIndex]
            val oldTiming = oldBooking.timing
            val currentUserName = _currentUser.value.name
            
            viewModelScope.launch(Dispatchers.IO) {
                // Insert or Replace standard DB booking
                bookingDao?.insertBooking(
                    DbBooking(
                        id = bookingId,
                        expert_id = expertId,
                        learner_id = "user_senior_101",
                        slot_time = newTiming,
                        status = "confirmed",
                        duration_minutes = newDuration,
                        is_voice = isVoice,
                        is_video = isVideo,
                        created_at = System.currentTimeMillis()
                    )
                )
                
                // If timing has changed, reset old slot status so it is clear, and set new slot
                if (oldTiming != newTiming) {
                    try {
                        val oldDay = oldTiming.substringBefore(" • ").trim().uppercase()
                        val oldTimePart = oldTiming.substringAfter(" • ").trim()
                        val oldStartTime = if (oldTimePart.contains(" - ")) oldTimePart.substringBefore(" - ").trim() else oldTimePart
                        val oldSlotId = "${expertId}_${oldDay}_$oldStartTime"
                        
                        slotDao?.updateSlotStatus(oldSlotId, "free", null)
                    } catch (e: java.lang.Exception) {
                        android.util.Log.e("AgeNoBarDB", "Error freeing old slot", e)
                    }
                    
                    try {
                        val newDay = newTiming.substringBefore(" • ").trim().uppercase()
                        val newTimePart = newTiming.substringAfter(" • ").trim()
                        val newStartTime = if (newTimePart.contains(" - ")) newTimePart.substringBefore(" - ").trim() else newTimePart
                        val newSlotId = "${expertId}_${newDay}_$newStartTime"
                        
                        slotDao?.updateSlotStatus(newSlotId, "booked", currentUserName)
                    } catch (e: java.lang.Exception) {
                        android.util.Log.e("AgeNoBarDB", "Error booking new slot", e)
                    }
                }
                
                withContext(Dispatchers.Main) {
                    saveBookings()
                    _editingBooking.value = null
                }
                
                startDirectChat(
                    expertId = expertId,
                    initialText = "Dear ${expert.name}, I have updated our scheduled session details to: $newTiming ($newDuration mins, ${if (isVideo) "Video Call 🎥" else "Voice Call 📞"}).",
                    navigateToTab = false
                )
            }
        }
    }

    fun cancelBooking(bookingId: String) {
        val currentBookings = _bookingsList.value
        val bookingIndex = currentBookings.indexOfFirst { it.id == bookingId }
        val expertId = if (bookingIndex != -1) currentBookings[bookingIndex].expertId else ""
        val expert = _experts.value.find { it.id == expertId }
        
        if (bookingIndex != -1 && expert != null) {
            val oldBooking = currentBookings[bookingIndex]
            val oldTiming = oldBooking.timing
            
            viewModelScope.launch(Dispatchers.IO) {
                bookingDao?.deleteBooking(bookingId)
                
                try {
                    val oldDay = oldTiming.substringBefore(" • ").trim().uppercase()
                    val oldTimePart = oldTiming.substringAfter(" • ").trim()
                    val oldStartTime = if (oldTimePart.contains(" - ")) oldTimePart.substringBefore(" - ").trim() else oldTimePart
                    val oldSlotId = "${expertId}_${oldDay}_$oldStartTime"
                    
                    slotDao?.updateSlotStatus(oldSlotId, "free", null)
                } catch (e: java.lang.Exception) {
                    android.util.Log.e("AgeNoBarDB", "Error clearing slot for canceled booking", e)
                }
                
                withContext(Dispatchers.Main) {
                    saveBookings()
                    _editingBooking.value = null
                }
                
                startDirectChat(
                    expertId = expertId,
                    initialText = "Dear ${expert.name}, I have unfortunately cancelled my scheduled session on $oldTiming. I will book a different slot when convenient!",
                    navigateToTab = false
                )
            }
        }
    }

    // -- INITIALIZE PERSISTENT STORAGE CONTEXT --
    private var hasPreseededBookings = false

    private suspend fun preseedDatabaseIfNeeded() {
        val expertDaoSafe = expertDao ?: return
        val currentExperts = expertDaoSafe.getAllExperts()
        val deservesCleanRebuild = currentExperts.isEmpty() || currentExperts.any { 
            it.topic.contains(" ") || it.topic != it.topic.lowercase() 
        } || (sharedPrefs?.getBoolean("db_seeded_v7", false) == false)
        if (deservesCleanRebuild) {
            expertDaoSafe.clearAllExperts()
            try {
                bookingDao?.clearAllBookings()
            } catch (e: Exception) {
                android.util.Log.e("AgeNoBarDB", "Error clearing bookings", e)
            }
            val generated = loadGeneratedExpertsForBridge()
            val dbExperts = generated.map { exp ->
                DbExpert(
                    id = exp.id,
                    name = exp.name,
                    topic = exp.topic,
                    bio = exp.bio,
                    experience_years = exp.yearsOfExperience,
                    languages = exp.languages.joinToString(";"),
                    rate_per_30min = exp.flatSessionFee,
                    availability = exp.activeOfflineAvailability,
                    rating = exp.rating,
                    session_count = exp.sessionsHosted,
                    avatar_seed = exp.avatarUrl,
                    tags = exp.tags.joinToString(";"),
                    category = exp.category,
                    specialisation = exp.specialisation,
                    specialisation_display = exp.specialisation_display
                )
            }
            expertDaoSafe.insertExperts(dbExperts)
            
            if (currentExperts.isEmpty()) {
                val reqDaoSafe = requestDao ?: return
                val respDaoSafe = responseDao ?: return
                
                val preseededRequests = listOf(
                    DbRequest(
                        id = "req_1",
                        learner_id = "learner_vipin",
                        topic = "Maths",
                        message = "Can someone help me understand Vedic arithmetic subtraction tricks? I am preparing for my banking exam.",
                        status = "open",
                        created_at = System.currentTimeMillis() - 3600000 * 4
                    ),
                    DbRequest(
                        id = "req_2",
                        learner_id = "learner_sharda",
                        topic = "Languages",
                        message = "Need to practice spoken English for my job interviews. Would love a 30 min interactive voice session.",
                        status = "open",
                        created_at = System.currentTimeMillis() - 3600000 * 8
                    ),
                    DbRequest(
                        id = "req_3",
                        learner_id = "user_senior_101",
                        topic = "Science",
                        message = "Looking for a mentor to explain the basic physics behind solar panels. I have 2 response offers!",
                        status = "open",
                        created_at = System.currentTimeMillis() - 3600000 * 12
                    )
                )
                
                preseededRequests.forEach { reqDaoSafe.insertRequest(it) }
                
                val preseededResponses = listOf(
                DbResponse(
                    id = "resp_1",
                    request_id = "req_3",
                    expert_id = "exp_satendra",
                    message = "Hello Ramesh, I can absolutely help you understand solar energy. I worked with solar arrays at ISRO. Let me know when you are free!",
                    created_at = System.currentTimeMillis() - 1800000
                ),
                DbResponse(
                    id = "resp_2",
                    request_id = "req_3",
                    expert_id = "exp_jitendra",
                    message = "I have a couple of simple household experiments demonstrating photovoltaic principles. Let's talk soon!",
                    created_at = System.currentTimeMillis() - 1200000
                )
            )
            preseededResponses.forEach { respDaoSafe.insertResponse(it) }
            
            val slotDaoSafe = slotDao ?: return
            val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
            val times = listOf(
                "06:00 AM", "07:00 AM", "08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
                "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM", "06:00 PM", "07:00 PM",
                "08:00 PM", "09:00 PM"
            )
            val preseededSlots = mutableListOf<DbSlot>()
            
            generated.forEach { expert ->
                days.forEach { day ->
                    times.forEachIndexed { index, time ->
                        val slotId = "${expert.id}_${day}_${time}"
                        val isBookedByOther = (index == 1 || index == 3) && expert.id != "exp_seed_maths_0"
                        val isBookedByMe = index == 4 && expert.id == "exp_seed_maths_0"
                        preseededSlots.add(
                            DbSlot(
                                id = slotId,
                                expert_id = expert.id,
                                day = day,
                                time = time,
                                status = if (isBookedByOther || isBookedByMe) "booked" else "free",
                                booked_by = when {
                                    isBookedByMe -> "Ramesh Kumar"
                                    isBookedByOther -> "Anonymous Learner"
                                    else -> null
                                }
                            )
                        )
                    }
                }
            }
            slotDaoSafe.insertSlots(preseededSlots)
            
            // Seed dynamic default bookings so they exist inside the Room database
            val bookingDaoSafe = bookingDao ?: return
            bookingDaoSafe.insertBooking(
                DbBooking(
                    id = "b_default_1",
                    expert_id = "exp_seed_maths_0",
                    learner_id = "user_senior_101",
                    slot_time = "WED • 04:00 PM - 04:30 PM",
                    status = "confirmed",
                    duration_minutes = 30,
                    is_voice = true,
                    is_video = false,
                    created_at = System.currentTimeMillis() - 259200000
                )
            )
            bookingDaoSafe.insertBooking(
                DbBooking(
                    id = "b_default_2",
                    expert_id = "exp_seed_finance_1",
                    learner_id = "user_senior_101",
                    slot_time = "MON • 10:00 AM - 10:30 AM",
                    status = "cancelled",
                    duration_minutes = 30,
                    is_voice = true,
                    is_video = false,
                    created_at = System.currentTimeMillis() - 432000000
                )
            )
            android.util.Log.d("AgeNoBarDB", "booking inserted: preseeded default bookings")
            println("DATABASE VERIFICATION: preseeded bookings inserted")
            hasPreseededBookings = true
            sharedPrefs?.edit()?.putBoolean("db_seeded_v7", true)?.apply()
        }
    }
}

    private fun observeDatabaseFlows() {
        viewModelScope.launch {
            expertDao?.getAllExpertsFlow()?.collect { dbList ->
                if (dbList.isNotEmpty()) {
                    val mapped = dbList.map { it.toExpert() }
                    _experts.value = mapped
                }
            }
        }
        
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                bookingDao?.getAllBookingsFlow() ?: kotlinx.coroutines.flow.flowOf(emptyList()),
                _experts
            ) { bList, expertsList ->
                android.util.Log.d("AgeNoBarDB", "booking loaded: size = ${bList.size}")
                println("DATABASE VERIFICATION: booking loaded Count = ${bList.size}")
                bList.forEach { 
                    android.util.Log.d("AgeNoBarDB", "booking item: ${it.id} for ${it.expert_id} at ${it.slot_time}")
                }
                bList.map { dbB ->
                    val matchedExpert = expertsList.find { it.id == dbB.expert_id }
                    val expertName = matchedExpert?.name ?: "Expert"
                    val expertAvatar = matchedExpert?.avatarUrl ?: "avatar_default"
                    val displayStatus = when {
                        dbB.id == "b_default_2" || dbB.status == "past" || dbB.status == "cancelled" -> "Past"
                        dbB.status == "joined" -> "Joined"
                        else -> "Upcoming"
                    }
                    
                    Booking(
                        id = dbB.id,
                        expertId = dbB.expert_id,
                        expertName = expertName,
                        expertAvatar = expertAvatar,
                        timing = dbB.slot_time,
                        durationMinutes = dbB.duration_minutes,
                        status = displayStatus,
                        isVoice = dbB.is_voice,
                        isVideo = dbB.is_video
                    )
                }
            }.collect { currentUIBookings ->
                _bookingsList.value = currentUIBookings
                
                val mapping = currentUIBookings.associate { it.expertId to it.timing }
                _bookedSessionExpertIds.value = mapping
                
                android.util.Log.d("AgeNoBarDB", "calendar refreshed: ${currentUIBookings.size} bookings")
                println("DATABASE VERIFICATION: calendar refreshed")
            }
        }
        
        viewModelScope.launch {
            requestDao?.getAllRequestsFlow()?.collect { rList ->
                _learningRequests.value = rList
            }
        }
        
        viewModelScope.launch {
            responseDao?.getAllResponsesFlow()?.collect { respList ->
                _learningResponses.value = respList
            }
        }
    }

    fun loadSlotsForExpert(expertId: String) {
        activeExpertsSlotsJob?.cancel()
        activeExpertsSlotsJob = viewModelScope.launch(Dispatchers.Main) {
            slotDao?.getSlotsForExpertFlow(expertId)?.collect { list ->
                _activeExpertSlots.value = list
            }
        }
    }

    fun DbExpert.toExpert(): Expert {
        val topicLower = topic.lowercase()
        val catId = if (category.isNotEmpty()) category else when(topicLower) {
            "maths", "science", "languages", "finance", "legal", "banking", "sanskrit" -> "LEARN & GROW"
            "wellness", "counselling", "physiotherapy", "meditation", "nutrition" -> "HEALTH & WELLNESS"
            "music", "bharatanatyam", "veena", "violin", "vocal music", "bhajans", "traditional arts" -> "ARTS, MUSIC & CULTURE"
            "gardening", "terrace gardening", "organic farming", "cooking" -> "NATURE & LIFESTYLE"
            "ramayana" -> "STORIES & HERITAGE"
            else -> "science"
        }
        val specId = if (specialisation.isNotEmpty()) specialisation else topicLower
        val specDisplay = if (specialisation_display.isNotEmpty()) specialisation_display else when(topicLower) {
            "maths" -> "Mathematics Mentor"
            "science" -> "Science Instructor"
            "languages" -> "Language Coach"
            else -> "Specialist Mentor"
        }

        return Expert(
            id = id,
            name = name,
            title = if (specialisation_display.isNotEmpty()) {
                specialisation_display
            } else {
                when(topicLower) {
                    "maths" -> "Mathematics Mentor"
                    "science" -> "Science & Tech Instructor"
                    "languages" -> "Language Coach"
                    "finance" -> "Financial Advisor"
                    "wellness" -> "Ayurveda & Yoga Specialist"
                    "legal" -> "Legal Counselor"
                    "music" -> "Music Teacher"
                    "gardening" -> "Gardening Expert"
                    "bharatanatyam" -> "Classical Dance Acharya"
                    "veena" -> "Veena Vidwan / Vidushi"
                    "violin" -> "Carnatic Violin Maestro"
                    "vocal music" -> "Indian Classical Vocal Guru"
                    "bhajans" -> "Satsang & Bhajan Guide"
                    "terrace gardening" -> "Terrace Garden Designer"
                    "organic farming" -> "Organic Farming Consultant"
                    "banking" -> "Banking & Security Specialist"
                    "counselling" -> "Mental Health & Empathy Counselor"
                    "physiotherapy" -> "Senior Joint & Physiotherapy Specialist"
                    "meditation" -> "Dhyana & Mindfulness Guide"
                    "nutrition" -> "Diet & Lifestyle Consultant"
                    "cooking" -> "Traditional Culinary Guru"
                    "sanskrit" -> "Sanskrit Grammar & Sloka Scholar"
                    "ramayana" -> "Puranic & Ramayana Storyteller"
                    "traditional arts" -> "Heritage Art & Craft Mentor"
                    else -> "Specialist Mentor"
                }
            },
            category = catId,
            yearsOfExperience = experience_years,
            areaEmoji = when(catId.lowercase()) {
                "science" -> "🔬"
                "maths" -> "📐"
                "languages" -> "🗣️"
                "career" -> "💼"
                "banking_finance" -> "🏦"
                "ayurveda" -> "🌿"
                "dance" -> "💃"
                "music" -> "🎵"
                "physiotherapy" -> "🦾"
                else -> "🎓"
            },
            languages = languages.split(";").map { it.trim() },
            rating = rating,
            testimonialsCount = 14,
            peopleHelpedCount = session_count * 2,
            avatarUrl = avatar_seed,
            bio = bio,
            certificationStatus = "Certified $specDisplay",
            introductionText = "Hi, I am $name. I specialize in $specDisplay and look forward to our session!",
            videoIntroductionUrl = "intro_video",
            myStoryText = bio,
            activeOfflineAvailability = availability,
            flatSessionFee = rate_per_30min,
            skillsTags = listOf(specId, "Professional Mentorship", "1:1 Live Help"),
            tags = tags.split(";").map { it.trim() }.filter { it.isNotEmpty() },
            communityWall = emptyList(),
            testimonialsList = listOf(
                Testimonial(authorName = "Aarav S.", text = "Excellent explanation of complex concepts! Highly recommended.", rating = 5.0, date = "Last week")
            ),
            questionsAnswered = 18,
            sessionsHosted = session_count,
            circlesJoinedCount = 3,
            knowledgeLibraryResources = emptyList(),
            isVerifiedExpert = true,
            isOnlineNow = true,
            topic = topicLower,
            specialisation = specId,
            specialisation_display = specDisplay
        )
    }

    // --- DB DATA MODIFIERS ---
    fun blockOffSlotInDb(expertId: String, day: String, time: String, isCurrentlyBlocked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val slotId = "${expertId}_${day}_$time"
            if (isCurrentlyBlocked) {
                slotDao?.updateSlotStatus(slotId, "free", null)
            } else {
                slotDao?.updateSlotStatus(slotId, "booked", "Blocked by Expert")
            }
        }
    }

    fun addAvailabilitySlotInDb(expertId: String, day: String, time: String, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val slotId = "${expertId}_${day}_$time"
            slotDao?.insertSlots(
                listOf(
                    DbSlot(
                        id = slotId,
                        expert_id = expertId,
                        day = day,
                        time = time,
                        status = "free",
                        booked_by = null
                    )
                )
            )
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun bookSessionWithExpertInDb(expertId: String, day: String, time: String, slotTime: String, learnerId: String, learnerName: String, rate: Int, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val bookingId = "b_" + UUID.randomUUID().toString().take(8)
            bookingDao?.insertBooking(
                DbBooking(
                    id = bookingId,
                    expert_id = expertId,
                    learner_id = learnerId,
                    slot_time = "$day • $slotTime",
                    status = "confirmed",
                    created_at = System.currentTimeMillis()
                )
            )
            
            val slotId = "${expertId}_${day}_$time"
            slotDao?.updateSlotStatus(slotId, "booked", learnerName)
            
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun submitLearningRequestInDb(learnerId: String, topic: String, message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val requestId = "req_" + UUID.randomUUID().toString().take(8)
            requestDao?.insertRequest(
                DbRequest(
                    id = requestId,
                    learner_id = learnerId,
                    topic = topic,
                    message = message,
                    status = "open",
                    created_at = System.currentTimeMillis()
                )
            )
        }
    }

    fun submitResponseToRequestInDb(requestId: String, expertId: String, message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val responseId = "resp_" + UUID.randomUUID().toString().take(8)
            responseDao?.insertResponse(
                DbResponse(
                    id = responseId,
                    request_id = requestId,
                    expert_id = expertId,
                    message = message,
                    created_at = System.currentTimeMillis()
                )
            )
        }
    }

    fun registerAsExpertInDb(name: String, topic: String, yearsOfExp: Int, bio: String, availability: String, rate: Int, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val expertId = "exp_registered_" + UUID.randomUUID().toString().take(8)
            val dbExpert = DbExpert(
                id = expertId,
                name = name,
                topic = topic,
                bio = bio,
                experience_years = yearsOfExp,
                languages = "English; Hindi",
                rate_per_30min = rate,
                availability = availability,
                rating = 5.0,
                session_count = 0,
                avatar_seed = "avatar_senior_101"
            )
            expertDao?.insertExpert(dbExpert)
            
            val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
            val times = listOf(
                "06:00 AM", "08:00 AM", "10:00 AM", "12:00 PM",
                "02:00 PM", "04:00 PM", "06:00 PM", "08:00 PM"
            )
            val slots = days.flatMap { day ->
                times.map { time ->
                    DbSlot(
                        id = "${expertId}_${day}_$time",
                        expert_id = expertId,
                        day = day,
                        time = time,
                        status = "free",
                        booked_by = null
                    )
                }
            }
            slotDao?.insertSlots(slots)
            
            val updatedProfile = _currentUser.value.copy(
                userRoleType = "Teach",
                role = "$topic Specialist & Teacher"
            )
            _currentUser.value = updatedProfile
            
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun initPrefs(context: android.content.Context) {
        if (sharedPrefs == null) {
            sharedPrefs = context.getSharedPreferences("wisdom_bridge_prefs_v5", android.content.Context.MODE_PRIVATE)
            
            val db = AgeNoBarDatabase.getDatabase(context)
            database = db
            expertDao = db.expertDao()
            bookingDao = db.bookingDao()
            slotDao = db.slotDao()
            requestDao = db.requestDao()
            responseDao = db.responseDao()
            
            viewModelScope.launch(Dispatchers.IO) {
                preseedDatabaseIfNeeded()
                observeDatabaseFlows()
            }
            
            loadPersistedConversations()
            loadPersistedBookings()
        }
    }

    // -- HELPER SERIALIZERS FOR BOOKINGS --
    private fun serializeBooking(b: Booking): String {
        return "${b.id}#${b.expertId}#${b.expertName}#${b.expertAvatar}#${b.timing}#${b.durationMinutes}#${b.status}#${b.isVoice}#${b.isVideo}"
    }

    private fun deserializeBooking(s: String): Booking? {
        val parts = s.split("#")
        if (parts.size >= 9) {
            return Booking(
                id = parts[0],
                expertId = parts[1],
                expertName = parts[2],
                expertAvatar = parts[3],
                timing = parts[4],
                durationMinutes = parts[5].toIntOrNull() ?: 30,
                status = parts[6],
                isVoice = parts[7].toBoolean(),
                isVideo = parts[8].toBoolean()
            )
        } else if (parts.size >= 7) {
            return Booking(
                id = parts[0],
                expertId = parts[1],
                expertName = parts[2],
                expertAvatar = parts[3],
                timing = parts[4],
                durationMinutes = parts[5].toIntOrNull() ?: 30,
                status = parts[6],
                isVoice = true,
                isVideo = false
            )
        }
        return null
    }

    private fun saveBookings() {
        val editor = sharedPrefs?.edit() ?: return
        val set = _bookingsList.value.map { serializeBooking(it) }.toSet()
        editor.putStringSet("persisted_bookings", set)
        editor.apply()
    }

    private fun loadPersistedBookings() {
        val set = sharedPrefs?.getStringSet("persisted_bookings", null)
        if (set != null) {
            val loaded = set.mapNotNull { deserializeBooking(it) }
            _bookingsList.value = loaded
            
            // Sync with map
            val mapping = loaded.associate { it.expertId to it.timing }
            _bookedSessionExpertIds.value = mapping
        } else {
            // Load some default bookings so we never show empty screens
            _bookingsList.value = listOf(
                Booking(
                    id = "b_default_1",
                    expertId = "exp_seed_maths_0",
                    expertName = "Rajesh Sharma",
                    expertAvatar = "avatar_rajesh_sharma",
                    timing = "Wednesday, 4:00 PM",
                    durationMinutes = 30,
                    status = "Upcoming"
                ),
                Booking(
                    id = "b_default_2",
                    expertId = "exp_seed_finance_1",
                    expertName = "Anand Shah",
                    expertAvatar = "avatar_anand_shah",
                    timing = "Yesterday, 10:30 AM",
                    durationMinutes = 30,
                    status = "Past"
                )
            )
            saveBookings()
            
            val mapping = _bookingsList.value.associate { it.expertId to it.timing }
            _bookedSessionExpertIds.value = mapping
        }
    }

    // -- HELPER SERIALIZERS FOR CONVERSATIONS --
    private fun serializeConversation(c: DirectConversation): String {
        val msgsStr = c.messages.joinToString("~") { m ->
            "${m.id}^${m.senderId}^${m.senderName}^${m.text.replace("\n", " ").replace("#", " ").replace("~", " ")}^${m.timestamp}^${m.isSystemNotification}"
        }
        return "${c.id}#${c.recipientId}#${c.recipientName}#${c.recipientAvatar}#${c.lastMessageText.replace("\n", " ").replace("#", " ").replace("~", " ")}#${c.lastMessageTimestamp}#$msgsStr"
    }

    private fun deserializeConversation(s: String): DirectConversation? {
        val parts = s.split("#")
        if (parts.size < 7) return null
        val msgsParts = parts[6].split("~").filter { it.isNotBlank() }
        val msgsList = msgsParts.mapNotNull { mStr ->
            val mParts = mStr.split("^")
            if (mParts.size < 6) null
            else DirectMessage(
                id = mParts[0],
                senderId = mParts[1],
                senderName = mParts[2],
                text = mParts[3],
                timestamp = mParts[4],
                isSystemNotification = mParts[5].toBoolean()
            )
        }
        return DirectConversation(
            id = parts[0],
            recipientId = parts[1],
            recipientName = parts[2],
            recipientAvatar = parts[3],
            lastMessageText = parts[4],
            lastMessageTimestamp = parts[5],
            messages = msgsList
        )
    }

    private fun saveConversations() {
        val editor = sharedPrefs?.edit() ?: return
        val set = _directConversations.value.map { serializeConversation(it) }.toSet()
        editor.putStringSet("persisted_conversations", set)
        editor.apply()
    }

    private fun loadPersistedConversations() {
        val set = sharedPrefs?.getStringSet("persisted_conversations", null)
        if (set != null && set.isNotEmpty()) {
            val loaded = set.mapNotNull { deserializeConversation(it) }
            _directConversations.value = loaded
            if (loaded.isNotEmpty()) {
                _selectedDirectConversationId.value = loaded.first().id
            }
        } else {
            // Seed initial expert message thread so there's never an empty screen!
            val seedConvs = listOf(
                DirectConversation(
                    id = "exp_seed_maths_0",
                    recipientId = "exp_seed_maths_0",
                    recipientName = "Rajesh Sharma",
                    recipientAvatar = "avatar_rajesh_sharma",
                    lastMessageText = "Pranam Ramesh ji! Dhanyaavaad for requesting assistance. I will prepare simple materials for Vedic arithmetic.",
                    lastMessageTimestamp = "11:00 AM",
                    messages = listOf(
                        DirectMessage(
                            id = "msg_seed_1",
                            senderId = "user_senior_101",
                            senderName = "Ramesh Kumar",
                            text = "Sir, my child in the neighborhood has deep fears of simple double-digit division. Can we explain visually?",
                            timestamp = "10:45 AM"
                        ),
                        DirectMessage(
                            id = "msg_seed_2",
                            senderId = "exp_seed_maths_0",
                            senderName = "Rajesh Sharma",
                            text = "Pranam Ramesh ji! Dhanyaavaad for requesting assistance. I will prepare simple materials for Vedic arithmetic.",
                            timestamp = "11:00 AM"
                        )
                    )
                )
            )
            _directConversations.value = seedConvs
            _selectedDirectConversationId.value = "exp_seed_maths_0"
            saveConversations()
        }
    }

    // -- PROFILE CHAT ACTION TRIGGER --
    fun startDirectChat(expertId: String, initialText: String? = null, navigateToTab: Boolean = true) {
        val expert = _experts.value.find { it.id == expertId }
        val recipientName = expert?.name ?: "Expert"
        val recipientAvatar = expert?.avatarUrl ?: "avatar_rajesh"
        
        val list = _directConversations.value.toMutableList()
        val index = list.indexOfFirst { it.recipientId == expertId || it.id == expertId }
        
        if (index >= 0) {
            val existing = list[index]
            var updatedMsgs = existing.messages
            var lastText = existing.lastMessageText
            if (initialText != null && initialText.isNotBlank()) {
                val userMsg = DirectMessage(
                    senderId = "user_senior_101",
                    senderName = "Ramesh Kumar",
                    text = initialText,
                    timestamp = "Just Now"
                )
                updatedMsgs = updatedMsgs + userMsg
                lastText = initialText
            }
            val updatedConv = existing.copy(
                lastMessageText = lastText,
                lastMessageTimestamp = "Just Now",
                messages = updatedMsgs
            )
            list[index] = updatedConv
            _directConversations.value = list
            _selectedDirectConversationId.value = existing.id
            saveConversations()
        } else {
            val msgs = mutableListOf<DirectMessage>()
            if (initialText != null && initialText.isNotBlank()) {
                msgs.add(DirectMessage(
                    senderId = "user_senior_101",
                    senderName = "Ramesh Kumar",
                    text = initialText,
                    timestamp = "Just Now"
                ))
            } else {
                msgs.add(DirectMessage(
                    senderId = expertId,
                    senderName = recipientName,
                    text = "Pranam Ramesh ji! How can I help you share or learn wisdom today?",
                    timestamp = "Just Now"
                ))
            }
            val newConv = DirectConversation(
                id = expertId,
                recipientId = expertId,
                recipientName = recipientName,
                recipientAvatar = recipientAvatar,
                lastMessageText = msgs.last().text,
                lastMessageTimestamp = "Just Now",
                messages = msgs
            )
            _directConversations.value = _directConversations.value + newConv
            _selectedDirectConversationId.value = expertId
            saveConversations()
        }
        
        // Navigate to the messages companion page automatically if specified
        if (navigateToTab) {
            selectTab(AppTab.Messages)
        }
    }

    fun selectDirectConversation(id: String?) {
        _selectedDirectConversationId.value = id
    }

    fun sendDirectMessage(conversationId: String, text: String) {
        if (text.isBlank()) return
        val list = _directConversations.value.toMutableList()
        val index = list.indexOfFirst { it.id == conversationId }
        if (index >= 0) {
            val existing = list[index]
            val userMsg = DirectMessage(
                senderId = "user_senior_101",
                senderName = "Ramesh Kumar",
                text = text,
                timestamp = "Just Now"
            )
            val updatedConv = existing.copy(
                lastMessageText = text,
                lastMessageTimestamp = "Just Now",
                messages = existing.messages + userMsg
            )
            list[index] = updatedConv
            _directConversations.value = list
            saveConversations()
            
            // Auto tutor response after 1.5s
            viewModelScope.launch {
                delay(1200)
                val finalIndex = _directConversations.value.indexOfFirst { it.id == conversationId }
                if (finalIndex >= 0) {
                    val currentList = _directConversations.value.toMutableList()
                    val currentConv = currentList[finalIndex]
                    val responseMsg = DirectMessage(
                        senderId = currentConv.recipientId,
                        senderName = currentConv.recipientName,
                        text = "Thank you so much Ramesh beta! I have received your note: '$text'. Let's coordinate a voice call or meet soon to resolve it! Pranam.",
                        timestamp = "Just Now"
                    )
                    val finalConv = currentConv.copy(
                        lastMessageText = responseMsg.text,
                        lastMessageTimestamp = "Just Now",
                        messages = currentConv.messages + responseMsg
                    )
                    currentList[finalIndex] = finalConv
                    _directConversations.value = currentList
                    saveConversations()
                }
            }
        }
    }

    // -- REPLACES MOCK BOOKING CORE LOGIC --
    fun bookSessionWithExpert(expertId: String, selectedTime: String, durationMinutes: Int = 30, isVideo: Boolean = false, onComplete: (() -> Unit)? = null) {
        val expert = _experts.value.find { it.id == expertId } ?: return
        
        android.util.Log.d("AgeNoBarDB", "bookSessionWithExpert: active trigger for $expertId at $selectedTime")
        println("DATABASE VERIFICATION: bookSessionWithExpert started")
        
        viewModelScope.launch(Dispatchers.IO) {
            val bookingId = "b_" + UUID.randomUUID().toString().take(8)
            val currentUserName = _currentUser.value.name
            
            // Insert the DbBooking
            bookingDao?.insertBooking(
                DbBooking(
                    id = bookingId,
                    expert_id = expertId,
                    learner_id = "user_senior_101",
                    slot_time = selectedTime,
                    status = "confirmed",
                    duration_minutes = durationMinutes,
                    is_voice = !isVideo,
                    is_video = isVideo,
                    created_at = System.currentTimeMillis()
                )
            )
            android.util.Log.d("AgeNoBarDB", "booking inserted: id = $bookingId for slot = $selectedTime")
            println("DATABASE VERIFICATION: booking inserted ($bookingId)")
            
            // Update corresponding DbSlot.status in Room database
            try {
                val day = selectedTime.substringBefore(" • ").trim().uppercase()
                val timeRangePart = selectedTime.substringAfter(" • ").trim()
                val startTime = if (timeRangePart.contains(" - ")) timeRangePart.substringBefore(" - ").trim() else timeRangePart
                val slotId = "${expertId}_${day}_$startTime"
                
                slotDao?.updateSlotStatus(slotId, "booked", currentUserName)
                android.util.Log.d("AgeNoBarDB", "slot updated: slotId = $slotId status = booked by $currentUserName")
                println("DATABASE VERIFICATION: slot updated ($slotId)")
            } catch (e: Exception) {
                android.util.Log.e("AgeNoBarDB", "Error updating slot status in DB", e)
            }
            
            withContext(Dispatchers.Main) {
                _bookedSessionExpertIds.update { it + (expertId to selectedTime) }
                saveBookings()
                onComplete?.invoke()
            }
            
            // Write notification intro chat to thread
            val typeStr = if (isVideo) "Video Classroom Session" else "Voice Consultation"
            startDirectChat(
                expertId = expertId,
                initialText = "Dear ${expert.name}, I have scheduled a 1:1 $typeStr for $selectedTime ($durationMinutes min) on Wisdom Bridge! Looking forward to learning from your experience.",
                navigateToTab = false
            )
        }
    }

    fun rescheduleBooking(bookingId: String, newTiming: String, onComplete: (() -> Unit)? = null) {
        val currentBookings = _bookingsList.value
        val bookingIndex = currentBookings.indexOfFirst { it.id == bookingId }
        val expertId = if (bookingIndex != -1) currentBookings[bookingIndex].expertId else ""
        val expert = _experts.value.find { it.id == expertId }
        
        android.util.Log.d("AgeNoBarDB", "rescheduleBooking: ID = $bookingId to newTiming = $newTiming")
        println("DATABASE VERIFICATION: rescheduleBooking started")
        
        if (bookingIndex != -1 && expert != null) {
            val oldBooking = currentBookings[bookingIndex]
            val oldTiming = oldBooking.timing
            val currentUserName = _currentUser.value.name
            
            viewModelScope.launch(Dispatchers.IO) {
                // Delete/Update existing DbBooking
                bookingDao?.insertBooking(
                    DbBooking(
                        id = bookingId,
                        expert_id = expertId,
                        learner_id = "user_senior_101",
                        slot_time = newTiming,
                        status = "confirmed",
                        duration_minutes = oldBooking.durationMinutes,
                        is_voice = oldBooking.isVoice,
                        is_video = oldBooking.isVideo,
                        created_at = System.currentTimeMillis()
                    )
                )
                android.util.Log.d("AgeNoBarDB", "booking updated (rescheduled): id = $bookingId")
                println("DATABASE VERIFICATION: booking updated for rescheduling")
                
                // Clear old slot to "free"
                try {
                    val oldDay = oldTiming.substringBefore(" • ").trim().uppercase()
                    val oldTimePart = oldTiming.substringAfter(" • ").trim()
                    val oldStartTime = if (oldTimePart.contains(" - ")) oldTimePart.substringBefore(" - ").trim() else oldTimePart
                    val oldSlotId = "${expertId}_${oldDay}_$oldStartTime"
                    
                    slotDao?.updateSlotStatus(oldSlotId, "free", null)
                    android.util.Log.d("AgeNoBarDB", "slot updated: oldSlotId = $oldSlotId status = free")
                } catch (e: Exception) {
                    android.util.Log.e("AgeNoBarDB", "Error resetting old slot in DB", e)
                }
                
                // Book new slot
                try {
                    val newDay = newTiming.substringBefore(" • ").trim().uppercase()
                    val newTimePart = newTiming.substringAfter(" • ").trim()
                    val newStartTime = if (newTimePart.contains(" - ")) newTimePart.substringBefore(" - ").trim() else newTimePart
                    val newSlotId = "${expertId}_${newDay}_$newStartTime"
                    
                    slotDao?.updateSlotStatus(newSlotId, "booked", currentUserName)
                    android.util.Log.d("AgeNoBarDB", "slot updated: newSlotId = $newSlotId status = booked")
                    println("DATABASE VERIFICATION: slots updated for rescheduling")
                } catch (e: Exception) {
                    android.util.Log.e("AgeNoBarDB", "Error setting new slot in DB", e)
                }
                
                withContext(Dispatchers.Main) {
                    _bookedSessionExpertIds.update { it + (expertId to newTiming) }
                    saveBookings()
                    onComplete?.invoke()
                }
                
                // Write notification chat to thread
                startDirectChat(
                    expertId = expertId,
                    initialText = "Dear ${expert.name}, I have rescheduled our session to $newTiming. Apologies for any inconvenience caused. Looking forward to our session!",
                    navigateToTab = false
                )
            }
        }
    }

    fun submitPrivateQuestionToExpert(expertId: String, text: String) {
        val currentQuestions = _privateQuestions.value[expertId] ?: emptyList()
        _privateQuestions.update { current ->
            current + (expertId to (currentQuestions + text))
        }
    }

    // -- AI CHACHI WISDOM ONBOARDING PROFILE BUILDER STATE MACHINE --
    fun startChachiOnboarding() {
        _chachiOnboardingStepIndex.value = 1
        _chachiOnboardingAnswers.value = emptyMap()
        _chachiOnboardingProposal.value = null
    }

    fun answerChachiOnboardingStep(answer: String) {
        val currentStep = _chachiOnboardingStepIndex.value
        _chachiOnboardingAnswers.update { it + (currentStep to answer) }
        
        if (currentStep < 5) {
            _chachiOnboardingStepIndex.value = currentStep + 1
        } else {
            // STEP 5 complete: compile user answers and trigger AI Chachi profile synthesis!
            val profExp = _chachiOnboardingAnswers.value[1] ?: "Primary Educator"
            val yrsRaw = _chachiOnboardingAnswers.value[2] ?: "25"
            val yrs = yrsRaw.filter { it.isDigit() }.toIntOrNull() ?: 24
            val helpTopics = _chachiOnboardingAnswers.value[3] ?: "Math fractions and bedtime folktales"
            val langs = (_chachiOnboardingAnswers.value[4] ?: "Hindi, English").split(",").map { it.trim() }
            val whyEnjoy = _chachiOnboardingAnswers.value[5] ?: "Seeing children's eyes light up with laughter"

            // Constructing beautiful custom bio
            val proposedBio = "Veteran specialized in $helpTopics. Retired professional carrying $yrs years of proud field achievements."
            
            // Maternal grandma tone profile-generator
            val simulatedMascotStory = "Story woven with love by your AI Chachi:\n'Our lovely peer entered their wisdom path as a passionate $profExp, investing $yrs years of hard-earned expertise. They speak $langs. They tell us that they are driven to help others because of \"$whyEnjoy\". AI Chachi is absolutely thrilled to verify their wisdom card and guide them to lead new storytelling gatherings!'"

            val proposalInstance = Expert(
                id = "exp_user_onboarded_${UUID.randomUUID().toString().take(4)}",
                name = _currentUser.value.name,
                title = "Senior Advisor & Retired $profExp",
                category = "Traditional Skills",
                yearsOfExperience = yrs,
                areaEmoji = "🌸",
                languages = langs,
                rating = 5.0,
                testimonialsCount = 0,
                peopleHelpedCount = 0,
                avatarUrl = "avatar_ramesh",
                bio = proposedBio,
                certificationStatus = "$yrs Years Crowned Milestone",
                introductionText = "Greetings! I am looking forward to teaching $helpTopics and supporting your learning path. Book a friendly call!",
                myStoryText = simulatedMascotStory,
                skillsTags = helpTopics.split(",").map { it.trim() },
                flatSessionFee = 0, // Swapping knowledge
                activeOfflineAvailability = "Flexible evenings, 4 PM - 8 PM",
                isVerifiedExpert = true,
                isOnlineNow = true,
                communityWall = listOf(
                    CommunityWallPost(type = "Tip", text = "Welcome to my Wisdom Wall! Feel free to ask private questions. I am excited to help you succeed.", timestamp = "Just now")
                )
            )
            
            _chachiOnboardingProposal.value = proposalInstance
            _chachiOnboardingStepIndex.value = 6 // Show proposed preview card
        }
    }

    fun completeChachiOnboardingAndSave() {
        val proposal = _chachiOnboardingProposal.value
        if (proposal != null) {
            _experts.update { listOf(proposal) + it }
            _currentUser.update { current ->
                current.copy(
                    role = proposal.title,
                    bio = proposal.bio,
                    isVerifiedExpert = true,
                    userRoleType = "Teach"
                )
            }
        }
        _chachiOnboardingStepIndex.value = 0 // Close boarding wizard
    }

    fun cancelChachiOnboarding() {
        _chachiOnboardingStepIndex.value = 0
        _chachiOnboardingAnswers.value = emptyMap()
        _chachiOnboardingProposal.value = null
    }

    // -- LOAD INITIAL SEED DATA FOR AGE NO BAR EXPERTS DIRECTORY --
    private fun loadGeneratedExpertsForBridge(): List<Expert> {
        val list = mutableListOf<Expert>()
        
        val indianFirstNames = listOf(
            "Rajesh", "Savita", "Satyendra", "Lakshmi", "Anand", "Nirmala", "Gopalakrishna", "Meera", "Sanjay", "Sudha",
            "Vijay", "Devyani", "Pradeep", "Harish", "Asha", "Usha", "Arun", "Sharda", "Preeti", "Rohan",
            "Anjali", "Suresh", "Vikram", "Gaurav", "Sita", "Hari", "Vasudev", "Parvathi", "Chidambaram", "Apurva",
            "Shrikant", "Dushyanth", "Kunal", "Sanjeev", "Tarla", "Vikas", "Rujuta", "Sampad", "Pushpa", "Rukmini",
            "Amar", "Bikas", "Chanda", "Deepak", "Eshwar", "Giridhar", "Himanshu", "Ila", "Jayant", "Kiran"
        )
        val indianLastNames = listOf(
            "Sharma", "Rao", "Nath", "Gupta", "Shah", "Iyer", "Krishnan", "Varrier", "Patel", "Mehta",
            "Acharya", "Dutta", "Deshpande", "Kamat", "Sen", "Kaul", "Rohatgi", "Bhushan", "Salve", "Alvares",
            "Pillai", "Mishra", "Sridhar", "Vyas", "Dixit", "Prasad", "Khanna", "Kapur", "Sinha", "Joshi",
            "Bose", "Reddy", "Nair", "Pillai", "Menon", "Mukherjee", "Chatterjee", "Banerjee", "Bhatte", "Iyengar",
            "Swamy", "Acharya", "Hegde", "Varma", "Gowda", "Naidu", "Roy", "Mitra", "Sahu", "Pande"
        )

        val categorySpecs = listOf(
            Pair("science", listOf("physics", "chemistry", "biology", "botany", "zoology", "nature_life_sciences", "environmental_science")),
            Pair("maths", listOf("algebra", "vedic_maths", "board_prep", "calculus", "statistics", "jee_preparation")),
            Pair("languages", listOf("hindi", "tamil", "french", "malayalam", "sanskrit", "kannada", "telugu", "english_speaking")),
            Pair("career", listOf("career_counselling", "interview_coaching", "resume_building", "corporate_mentoring", "startup_guidance", "government_exam_coaching")),
            Pair("banking_finance", listOf("retail_banking", "branch_management", "investment_planning", "fixed_deposits", "loan_guidance", "retirement_planning")),
            Pair("ayurveda", listOf("kerala_ayurveda", "panchakarma", "ayurvedic_nutrition", "herbal_medicine", "ayurvedic_consultation", "naturopathy")),
            Pair("dance", listOf("bharatanatyam", "kuchipudi", "kathak", "odissi", "mohiniyattam", "manipuri", "folk_dance")),
            Pair("music", listOf("carnatic_vocal", "hindustani_vocal", "carnatic_instrumental", "hindustani_instrumental", "bhajans_devotional", "light_music", "film_songs")),
            Pair("physiotherapy", listOf("orthopaedic_physio", "neurological_physio", "sports_physio", "geriatric_physio", "cardiac_physio", "paediatric_physio", "post_surgery_rehab", "joint_specialist", "spine_specialist"))
        )

        fun getSpecialisationDisplay(category: String, spec: String, expertIdx: Int): String {
            return when (category) {
                "career" -> {
                    when (expertIdx % 5) {
                        0 -> "Retired HR Head from TCS/Infosys"
                        1 -> "Ex-IAS officer career guidance"
                        2 -> "Retired Army officer career transition"
                        3 -> "Ex-corporate trainer"
                        4 -> "Retired university professor"
                        else -> "Career Consultant"
                    }
                }
                "banking_finance" -> {
                    when (expertIdx % 5) {
                        0 -> "Retired Branch Manager, SBI"
                        1 -> "Retired Chief Manager, Bank of Baroda"
                        2 -> "Retired Senior Manager, Punjab National Bank"
                        3 -> "Ex-RBI officer"
                        4 -> "Retired LIC branch manager"
                        else -> "Banking & Finance Expert"
                    }
                }
                "ayurveda" -> {
                    when (spec) {
                        "kerala_ayurveda" -> "Kerala Ayurvedic Doctor (BAMS)"
                        "panchakarma" -> "Panchakarma specialist from Thrissur"
                        "ayurvedic_nutrition" -> "Ayurvedic nutrition consultant"
                        "herbal_medicine" -> "Senior Ayurvedic Herbologist"
                        "ayurvedic_consultation" -> "Ayurvedic Consultation (BAMS)"
                        "naturopathy" -> "Traditional Naturopathy Expert"
                        else -> "Ayurveda Doctor (BAMS)"
                    }
                }
                "dance" -> {
                    val base = when (spec) {
                        "bharatanatyam" -> "Bharatanatyam Instructor"
                        "kuchipudi" -> "Kuchipudi Acharya"
                        "kathak" -> "Kathak Teacher"
                        "odissi" -> "Odissi Dancer"
                        "mohiniyattam" -> "Mohiniyattam Guru"
                        "manipuri" -> "Manipuri exponent"
                        "folk_dance" -> "Folk Dance Instructor"
                        else -> "Dance Acharya"
                    }
                    if (expertIdx == 0) "$base · Kalakshetra" else base
                }
                "music" -> {
                    when (spec) {
                        "carnatic_vocal" -> "Carnatic Vocal · Grade 8 Trinity"
                        "hindustani_vocal" -> "Hindustani Classical Vocal Guru"
                        "carnatic_instrumental" -> "Carnatic Violinist"
                        "hindustani_instrumental" -> "Hindustani Sitar Master"
                        "bhajans_devotional" -> "Devotional Bhajan Guide"
                        "light_music" -> "Light Music & Vocal Coach"
                        "film_songs" -> "Classical Retro Film Melodies"
                        else -> "Classical Music Guru"
                    }
                }
                "physiotherapy" -> {
                    when (spec) {
                        "orthopaedic_physio" -> "Orthopaedic Physiotherapist"
                        "neurological_physio" -> "Neurological Stroke rehab specialist"
                        "sports_physio" -> "Sports Injury Physiotherapist"
                        "geriatric_physio" -> "Senior Care Geriatric Physiotherapist"
                        "cardiac_physio" -> "Cardiac Post-op physio specialist"
                        "paediatric_physio" -> "Children's physical rehab coach"
                        "post_surgery_rehab" -> "Post-Surgery joint restoration tutor"
                        "joint_specialist" -> "Knee and Shoulder joint specialist"
                        "spine_specialist" -> "Spinal Decompression specialist"
                        else -> "Senior Physiotherapy Specialist"
                    }
                }
                "science" -> {
                    when (spec) {
                        "physics" -> "High School Physics Teacher"
                        "chemistry" -> "Chemistry Laboratory Tutor"
                        "biology" -> "Biology & Botany Coach"
                        "botany" -> "Plant & Botany Specialist"
                        "zoology" -> "Fauna & Zoology Expert"
                        "nature_life_sciences" -> "Nature & Life Sciences Guide"
                        "environmental_science" -> "Eco Systems & Environmental Scientist"
                        else -> "Science Educator"
                    }
                }
                "maths" -> {
                    when (spec) {
                        "algebra" -> "Algebra & Geometry Mentor"
                        "vedic_maths" -> "Vedic Mathematics Guru"
                        "board_prep" -> "Board Examination Math Coach"
                        "calculus" -> "Applied Calculus Teacher"
                        "statistics" -> "Probability & Statistics Advisor"
                        "jee_preparation" -> "IIT-JEE Advanced Mathematics Coach"
                        else -> "Vedic Maths Guru"
                    }
                }
                "languages" -> {
                    when (spec) {
                        "hindi" -> "Hindi Sahitya Literature Guide"
                        "tamil" -> "Classical Tamil Language Guru"
                        "french" -> "Conversational French Coach"
                        "malayalam" -> "Malayalam Language Advisor"
                        "sanskrit" -> "Sanskrit Grammar & Sloka scholar"
                        "kannada" -> "Kannada Bhasha Literature Scholar"
                        "telugu" -> "Fluent Telugu Vocabulary Tutor"
                        "english_speaking" -> "Interactive English Speaking Coach"
                        else -> "Heritage Language Scholar"
                    }
                }
                else -> "Specialised Mentor"
            }
        }

        var globalIndex = 0
        for ((catId, specs) in categorySpecs) {
            for (specId in specs) {
                for (expertIdx in 0 until 3) {
                    val uniqueId = "exp_seed_${catId}_${specId}_$expertIdx"
                    
                    val firstNameIndex = (globalIndex * 17 + expertIdx * 7) % indianFirstNames.size
                    val lastNameIndex = (globalIndex * 13 + expertIdx * 3) % indianLastNames.size
                    val finalName = "${indianFirstNames[firstNameIndex]} ${indianLastNames[lastNameIndex]}"
                    
                    val specDisplay = getSpecialisationDisplay(catId, specId, expertIdx)
                    val displayYearsOfExperience = 15 + (globalIndex * 3 + expertIdx) % 25
                    
                    val titleText = if (specDisplay.contains("·")) {
                        specDisplay
                    } else {
                        "$specDisplay"
                    }
                    
                    val idSuffix = finalName.lowercase().replace(" ", "_").replace(".", "")
                    
                    val bioText = when (catId) {
                        "science" -> "Empowering youth in astronomy, fundamental equations, and general logic with 25+ years experience."
                        "maths" -> "Simplifying mathematical structures, board exams preparations, and Vedic mental arithmetic tricks."
                        "languages" -> "Dedicated to preserving our ancestral roots, classical literature, grammar, and fluent talking style."
                        "career" -> "Guiding corporate aspirants and competitive preparation strategies based on real professional success."
                        "banking_finance" -> "Helping senior citizens secure their savings certificates, resolve banking claims, and manage investments."
                        "ayurveda" -> "Helping families integrate traditional wellness diet systems, seasonal detox guidelines, and herbal wisdom."
                        "dance" -> "Nurturing aesthetic choreography rhythms, posture control, and cultural classical dances."
                        "music" -> "Sharing the peaceful vibrations of classical melodies and sacred devotion."
                        "physiotherapy" -> "Restoring active mobility, healing spinal decompression pain, and therapeutic rehab."
                        else -> "Providing specialized mentoring and heritage knowledge with warmth and care."
                    }
                    
                    val areaEmojiChar = when (catId) {
                        "science" -> "🔬"
                        "maths" -> "📐"
                        "languages" -> "🗣️"
                        "career" -> "💼"
                        "banking_finance" -> "🏦"
                        "ayurveda" -> "🌿"
                        "dance" -> "💃"
                        "music" -> "🎵"
                        "physiotherapy" -> "🦾"
                        else -> "🎓"
                    }
                    
                    val tagsList = listOf(
                        catId.uppercase().replace("_", " & "),
                        catId,
                        specId,
                        specDisplay
                    )
                    
                    val feeRate = 100 + (globalIndex * 50) % 400
                    
                    list.add(
                        Expert(
                            id = uniqueId,
                            name = finalName,
                            title = titleText,
                            category = catId,
                            yearsOfExperience = displayYearsOfExperience,
                            areaEmoji = areaEmojiChar,
                            isVerifiedExpert = true,
                            languages = listOf("English", if (expertIdx % 2 == 0) "Hindi" else "Regional"),
                            rating = 4.7 + (globalIndex % 4) * 0.1,
                            testimonialsCount = 12 + expertIdx * 4,
                            peopleHelpedCount = 95 + expertIdx * 30,
                            avatarUrl = "avatar_${idSuffix}",
                            bio = bioText,
                            certificationStatus = "Certified $specDisplay",
                            introductionText = "Greetings! Let's explore the depths of $specDisplay together.",
                            myStoryText = "$finalName has dedicated decades to classical and professional mentorship. Passionate about sharing timeless insights, local cultural roots, and active professional techniques. AI Chachi says: 'Their guidance is pure gold Ramesh beta!'",
                            skillsTags = listOf(specId, "1:1 Live Help"),
                            tags = tagsList,
                            activeOfflineAvailability = "Mon-Wed 3 PM - 5 PM",
                            flatSessionFee = feeRate,
                            isOnlineNow = (expertIdx == 0),
                            topic = specId,
                            specialisation = specId,
                            specialisation_display = specDisplay
                        )
                    )
                    globalIndex++
                }
            }
        }
        
        return list
    }

    // Helper data holder for code compactness
    private class CategoryData(
        val category: String,
        val subExp: String,
        val emoji: String,
        val fee: Int,
        val baseNames: List<String>,
        val titles: List<String>,
        val bios: List<String>,
        val skills: List<String>,
        val avail: String,
        val story: String,
        val topic: String
    )

    private fun loadUNUSEDDefaultExpertsDummy(): List<Expert> {
        return listOf(
            Expert(
                id = "exp_rajesh",
                name = "Rajesh Sharma",
                title = "Retired Mathematics Teacher",
                category = "Education & Tutoring",
                yearsOfExperience = 35,
                areaEmoji = "📐",
                languages = listOf("English", "Hindi"),
                rating = 4.9,
                testimonialsCount = 48,
                peopleHelpedCount = 427,
                avatarUrl = "avatar_rajesh",
                bio = "Demystifying high school algebra, basic geometry, and arithmetic. Focused on visual representations, origami folds, and zero rote formulas.",
                certificationStatus = "35 Years State School Educator",
                introductionText = "Namaste learners! Think of mathematics as beautiful symphonies and patterns in nature, not dry classroom rules.",
                myStoryText = "Rajesh retired as the Vice-Principal of a government school in Jaipur. AI Chachi says: 'Rajesh beta is a mountain of patience. He can explain division of integers fifty times without losing his peaceful smile. He loves showing children how fractions match slices of a fresh paratha!'",
                skillsTags = listOf("Fractions Integration", "Vedic Math System", "Geometry Folds", "Basic Algebra Help"),
                activeOfflineAvailability = "Mon, Wed, Fri 4 PM - 6 PM",
                flatSessionFee = 0,
                isOnlineNow = true,
                communityWall = listOf(
                    CommunityWallPost(type = "Article", text = "Visual proof of Pythagoras' theorem using daily kitchen square containers! Extremely easy for Grade 6 kids.", attachmentLabel = "visual_pythagoras.pdf"),
                    CommunityWallPost(type = "Tip", text = "If your child struggles with double-digit divisions, stop drill sheets. Instead, have them physically distribute 72 kidney beans into 6 cups. The logic sticks forever.")
                ),
                testimonialsList = listOf(
                    Testimonial(authorName = "Deepa Viswanathan", text = "Rameshs recommendation was stellar. Sir cleared my 5th grade baby's severe fraction block in a 30 min chat!", rating = 5.0, date = "Yesterday"),
                    Testimonial(authorName = "Aditya K. (Student)", text = "He taught me the 11-times Vedic mental slide rule shortcut. Visual math rules!", rating = 4.8, date = "Last week", isVideo = true)
                )
            ),
            Expert(
                id = "exp_anand",
                name = "Anand Shah",
                title = "Senior Financial Planner",
                category = "Financial Literacy",
                yearsOfExperience = 30,
                areaEmoji = "📊",
                languages = listOf("English", "Gujarati", "Hindi"),
                rating = 4.8,
                testimonialsCount = 22,
                peopleHelpedCount = 114,
                avatarUrl = "avatar_anand",
                bio = "Guidance on senior citizen pension plans, post-office savings schemes, medical insurance matching, tax filing, and secure interest investments.",
                certificationStatus = "Chartered Wealth Specialist",
                introductionText = "Hello! Your savings are a tree you planted early; we simply prune it to give you sweet fruit during retirement.",
                myStoryText = "Anand spent 30 years as a treasury strategist. AI Chachi says: 'Anand beta knows every secure option the bank hides from you! He is extremely careful in helping seniors find safe, low-stress returns so their gold and savings remain secure.'",
                skillsTags = listOf("Senior Tax Exemption", "Pension Pacing", "Fixed Deposit Evaluation", "Budgeting Sheets"),
                activeOfflineAvailability = "Tuesdays & Thursdays 10 AM - 12 PM",
                flatSessionFee = 0,
                isOnlineNow = false,
                communityWall = listOf(
                    CommunityWallPost(type = "Tip", text = "Never keep more than one lakh rupees in a zero-interest savings account. Post-office senior deposit options are secure and yield 8.2% secure interest!")
                ),
                testimonialsList = listOf(
                    Testimonial(authorName = "Ramesh Kumar", text = "Anand helped me calculate my pension exemption limit easily. Practical and zero hard technical jargon.", rating = 5.0, date = "2 weeks ago")
                )
            ),
            Expert(
                id = "exp_satya",
                name = "Satya Narayanan",
                title = "Ex-HR Vice President & Interview Coach",
                category = "HR & Interviews",
                yearsOfExperience = 35,
                areaEmoji = "💼",
                languages = listOf("English", "Tamil", "Hindi"),
                rating = 4.9,
                testimonialsCount = 52,
                peopleHelpedCount = 518,
                avatarUrl = "avatar_satya",
                bio = "Preparing graduates and aspirants for career screening loops, reducing stuttering and interview anxiety, and resume alignment.",
                certificationStatus = "Ex-VP Corporate Talent Management",
                introductionText = "Confidence is a muscle, not an inborn gift. Let's do friendly roleplays to set you up for success!",
                myStoryText = "Satya screened over ten thousand engineers and leaders during his professional years. AI Chachi notes: 'Satya has a booming voice but a very soft grandfatherly heart. He coaches youngsters who feel paralyzed before tech interviews, giving them confidence to speak clearly.'",
                skillsTags = listOf("Confidence Mock Drills", "Resume Structural Trim", "Stutter Relief", "Ex-HR Inside Advice"),
                activeOfflineAvailability = "Every Saturday 2 PM - 5 PM",
                flatSessionFee = 0,
                isOnlineNow = true,
                communityWall = listOf(
                    CommunityWallPost(type = "Video", text = "Spoken video walkthrough of the PPT formula for intro answers.", attachmentLabel = "intro_ppt_drill.mp4"),
                    CommunityWallPost(type = "Story", text = "Last week, a young graduate who was terribly anxious about her first round worked with me. We simulated the exact conversation structure. She sent me a sweet thank you note last night!")
                ),
                testimonialsList = listOf(
                    Testimonial(authorName = "Anish Hegde", text = "Satya Sir is like a strict but warm coach. His resume templates are simple and really stood out.", rating = 5.0, date = "3 days ago")
                )
            ),
            Expert(
                id = "exp_gail",
                name = "Gail D'Souza",
                title = "Professional Legal Counselor",
                category = "Legal Knowledge",
                yearsOfExperience = 25,
                areaEmoji = "⚖️",
                languages = listOf("English", "Konkani"),
                rating = 4.7,
                testimonialsCount = 14,
                peopleHelpedCount = 89,
                avatarUrl = "avatar_gail",
                bio = "Understanding family deeds, secure land registration, tenant rights, and simplified elder law concepts. No complex litigation-speak.",
                certificationStatus = "Retired Bar Council Member",
                introductionText = "Laws are meant to protect clean relationships. Let's clarify your doubts together with ease and zero pressure.",
                myStoryText = "Gail practiced civil law before deciding to dedicate her post-retirement years to free community consultations. AI Chachi says: 'Gail is a strong, brilliant sister who believes every senior citizen must know their standard legal rights without paying heavy lawyer retainers.'",
                skillsTags = listOf("Property Deeds Overview", "Tenant Disputes Guide", "Will & Gift Deeds Assistance"),
                activeOfflineAvailability = "Wednesdays 2 PM - 5 PM",
                flatSessionFee = 0,
                isOnlineNow = false
            ),
            Expert(
                id = "exp_sudha",
                name = "Sudha Murthy",
                title = "Holistic Wellness & Yoga Acharya",
                category = "Wellness",
                yearsOfExperience = 32,
                areaEmoji = "🧘",
                languages = listOf("Kannada", "English"),
                rating = 4.9,
                testimonialsCount = 38,
                peopleHelpedCount = 310,
                avatarUrl = "avatar_sudha",
                bio = "Joint movement, breathing loops, anxiety management, and nutritional balance for healthy, comfortable elder living.",
                certificationStatus = "Certified Yoga Instructor",
                introductionText = "Wellness begins with a single deep breath. Let's move our fingers and joints gently to welcome fresh energy.",
                myStoryText = "Sudha cured her own chronic shoulder pain through guided pranayama techniques three decades ago and has been teaching ever since. AI Chachi notes: 'Sudha's voice is like a warm cup of herbal tea. Just listening to her guide breathing is therapeutic!'",
                skillsTags = listOf("Gentle Joint Mobility", "Breath Realignment", "Stress Relief Loops"),
                activeOfflineAvailability = "Every morning 7 AM - 9 AM",
                flatSessionFee = 0,
                isOnlineNow = true
            ),
            Expert(
                id = "exp_gauri",
                name = "Gauri Deshpande",
                title = "Traditional Culinary & Spices Expert",
                category = "Cooking",
                yearsOfExperience = 40,
                areaEmoji = "🍲",
                languages = listOf("Marathi", "Hindi"),
                rating = 4.8,
                testimonialsCount = 19,
                peopleHelpedCount = 140,
                avatarUrl = "avatar_gauri",
                bio = "Preserving ancient regional recipes, healthy millet prep, natural spice combinations, and diabetic-friendly senior menu planning.",
                certificationStatus = "Traditional Culinary Ambassador",
                introductionText = "Good food is our first and finest medicine. Let's cook with love and simple ingredients!",
                myStoryText = "Gauri has spent four decades hosting traditional festive feasts. AI Chachi says: 'Gauri sister's kitchen secrets are pure gold! She makes healthy millet rotis so soft that even toddlers and grandpas can enjoy them with ease.'",
                skillsTags = listOf("Millet Food Preparations", "Elder Balanced Recipes", "Traditional Spices Blends"),
                activeOfflineAvailability = "Fridays 3 PM - 6 PM",
                flatSessionFee = 0,
                isOnlineNow = true
            )
        )
    }
}

