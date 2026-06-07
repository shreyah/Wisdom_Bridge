package com.example

import java.util.UUID

// -- REPUTATION / REACTION SYSTEMS --
enum class ReputationKind(val emoji: String, val label: String) {
    HELPFUL("❤️", "Helpful"),
    WISE("🌟", "Wise"),
    INSPIRING("🙏", "Inspiring"),
    GREAT_TEACHER("🎓", "Great Teacher"),
    SUPPORTIVE("🤝", "Supportive"),
    ENCOURAGING("🌱", "Encouraging")
}

data class ReputationScore(
    val kind: ReputationKind,
    var count: Int
)

// -- USERS & PROFILES --
data class UserProfile(
    val id: String,
    val name: String,
    val role: String, // "Senior Mentor", "Verified Teacher", "Community Leader", "New Member", "Aspirant"
    val avatarUrl: String, 
    val bio: String,
    val isVerifiedExpert: Boolean,
    val ratingsCount: Int = 12,
    val points: Int = 120,
    val reputation: List<ReputationScore> = listOf(
        ReputationScore(ReputationKind.HELPFUL, 14),
        ReputationScore(ReputationKind.WISE, 28),
        ReputationScore(ReputationKind.INSPIRING, 9),
        ReputationScore(ReputationKind.GREAT_TEACHER, 35),
        ReputationScore(ReputationKind.SUPPORTIVE, 18),
        ReputationScore(ReputationKind.ENCOURAGING, 22)
    ),
    val isCommunityLeader: Boolean = false,
    val userRoleType: String = "Both" // "Learn", "Teach", or "Both"
)

// -- COMMUNITIES / CIRCLES --
data class Community(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val memberCount: Int,
    val category: String, // "Education", "Finance", "Family", "Interests", "Music"
    val description: String,
    val chatMessages: List<GroupMessage>,
    val resources: List<CommunityResource>,
    val moderators: List<String> // User names
)

data class CommunityResource(
    val id: String,
    val title: String,
    val description: String,
    val type: String, // "PDF Document", "Video Link", "Voice Guide", "List"
    val downloadUrl: String,
    val authorName: String
)

// -- GROUP CHAT MESSAGES --
data class GroupMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val senderName: String,
    val senderAvatar: String,
    val senderRole: String,
    val text: String,
    val timestamp: String,
    val voiceNoteDurationSec: Int? = null, // null if text, value if voice note
    val transcription: String? = null, 
    val imageUrl: String? = null,
    val isAnnouncement: Boolean = false,
    val reactions: Map<ReputationKind, Int> = emptyMap(),
    val pollQuestion: String? = null,
    val pollOptions: List<PollOption>? = null,
    val attachedExpertIds: List<String>? = null,
    val attachedCommunityIds: List<String>? = null,
    val attachedBookingIds: List<String>? = null
)

data class PollOption(
    val optionText: String,
    val voteCount: Int,
    val votedUserIds: List<String> = emptyList()
)

// -- VOICE ROOMS --
data class VoiceRoom(
    val id: String,
    val title: String,
    val typeName: String, // "Open Community Room", "Scheduled Expert Room", "Story Time Room", "Learning Circle", "Wellness Room"
    val description: String,
    val category: String, // "Gardening", "Career", "Parenting", "English", "Wellness"
    val hostName: String,
    val hostAvatar: String,
    val hostRole: String,
    val activeSpeakerCount: Int,
    val totalListenerCount: Int,
    val isLive: Boolean = false,
    val scheduledTime: String? = null // null if currently active/joinable
)

data class VoiceParticipant(
    val id: String,
    val name: String,
    val avatar: String,
    val isSpeaker: Boolean,
    val isHandRaised: Boolean = false,
    val wavePower: Float = 0.0f, // Simulated voice activity animation
    val isMuted: Boolean = false,
    val isFollowed: Boolean = false,
    val recentEmojiReaction: String? = null
)

// -- RECURRING WORKSHOPS & LIVE EVENTS --
data class CommunityEvent(
    val id: String,
    val communityName: String,
    val title: String,
    val type: String, // "Voice Workshop", "Video Live Link", "Local Meetup", "AMA Session"
    val localTime: String,
    val description: String,
    val hostName: String,
    val hostAvatar: String,
    val rsvpCount: Int,
    val isUserRsvped: Boolean = false
)

// -- ASK THE COMMUNITY / ASK A MENTOR --
data class CommunityQuestion(
    val id: String,
    val authorName: String,
    val authorAvatar: String,
    val authorRole: String,
    val text: String,
    val timestamp: String,
    val category: String,
    val voiceNoteUrl: String? = null,
    val transcription: String? = null,
    val isPrivateMentorRequest: Boolean = false, // True if sent privately to expert
    val targetMentorName: String? = null,
    val replies: List<QuestionReply> = emptyList(),
    val helpfulCount: Int = 0
)

data class QuestionReply(
    val id: String = UUID.randomUUID().toString(),
    val authorName: String,
    val authorAvatar: String,
    val authorRole: String,
    val text: String,
    val isVoiceReply: Boolean = false,
    val voiceDuration: Int? = null,
    val timestamp: String
)

// -- AGE NO BAR WISDOM SHARING PLATFORM ADDITIONAL MODELS --
data class CommunityWallPost(
    val id: String = UUID.randomUUID().toString(),
    val type: String, // "Text Post", "Photo", "Article", "Video", "Voice Note", "Story", "Tip"
    val text: String,
    val attachmentLabel: String? = null,
    val timestamp: String = "2 days ago",
    val likes: Int = 12
)

data class Testimonial(
    val id: String = UUID.randomUUID().toString(),
    val authorName: String,
    val text: String,
    val rating: Double,
    val date: String,
    val isVideo: Boolean = false
)

data class Expert(
    val id: String,
    val name: String,
    val title: String,
    val category: String,
    val yearsOfExperience: Int,
    val areaEmoji: String,
    val languages: List<String>,
    val rating: Double,
    val testimonialsCount: Int,
    val peopleHelpedCount: Int,
    val avatarUrl: String,
    val bio: String = "",
    val certificationStatus: String = "Verified Specialist",
    val introductionText: String = "",
    val videoIntroductionUrl: String = "",
    val myStoryText: String = "",
    val activeOfflineAvailability: String = "Weekdays 3 PM - 7 PM",
    val flatSessionFee: Int = 0, // 0 for free/swap
    val skillsTags: List<String> = emptyList(),
    val communityWall: List<CommunityWallPost> = emptyList(),
    val testimonialsList: List<Testimonial> = emptyList(),
    val questionsAnswered: Int = 24,
    val sessionsHosted: Int = 14,
    val circlesJoinedCount: Int = 4,
    val knowledgeLibraryResources: List<CommunityResource> = emptyList(),
    val isVerifiedExpert: Boolean = true,
    val isOnlineNow: Boolean = true,
    val topic: String = ""
)

data class PreviousCall(
    val id: String,
    val expertName: String,
    val durationMinutes: Int,
    val date: String,
    val topic: String,
    val rating: Int,
    val isRecordingAvailable: Boolean
)

// -- DIRECT CONVERSATIONS & CHATS --
data class DirectMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: String,
    val isSystemNotification: Boolean = false
)

data class DirectConversation(
    val id: String,
    val recipientId: String,
    val recipientName: String,
    val recipientAvatar: String,
    val lastMessageText: String,
    val lastMessageTimestamp: String,
    val messages: List<DirectMessage>
)

// -- BOOKINGS & CLASS SCHEDULE --
data class Booking(
    val id: String = UUID.randomUUID().toString(),
    val expertId: String,
    val expertName: String,
    val expertAvatar: String,
    val timing: String,
    val durationMinutes: Int = 30,
    val status: String = "Upcoming", // "Upcoming" or "Past"
    val isVoice: Boolean = false,
    val isVideo: Boolean = false
)



