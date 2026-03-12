package com.foccus.app.domain.model

data class BlockedApp(
    val packageName: String,
    val appName: String,
    val isEnabled: Boolean = true,
    val blockedCount: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)

// Apps bloqueados por padrão (sugestões)
val DEFAULT_BLOCKED_APPS = listOf(
    "com.instagram.android" to "Instagram",
    "com.zhiliaoapp.musically" to "TikTok",
    "com.ss.android.ugc.trill" to "TikTok",
    "com.google.android.youtube" to "YouTube",
    "com.facebook.katana" to "Facebook",
    "com.twitter.android" to "X (Twitter)",
    "com.snapchat.android" to "Snapchat",
    "com.reddit.frontpage" to "Reddit",
    "com.pinterest" to "Pinterest",
    "com.tumblr" to "Tumblr",
    "tv.twitch.android.app" to "Twitch",
    "com.zhihu.android" to "Zhihu",
)
