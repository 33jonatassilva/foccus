package com.foccus.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.foccus.app.data.local.preferences.UserPreferences
import com.foccus.app.domain.repository.BlockedAppRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class BlockerAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var preferences: UserPreferences

    @Inject
    lateinit var blockedAppRepo: BlockedAppRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val handler = Handler(Looper.getMainLooper())
    private var blockedPackages = emptySet<String>()
    private var isBlockingEnabled = true
    private var isShortsBlockingEnabled = true
    private var overlayView: View? = null
    private lateinit var windowManager: WindowManager

    private var currentForegroundPackage: String? = null
    private var lastShortsBackTime = 0L
    private var shortsBackCount = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        serviceInfo = serviceInfo.also { info ->
            info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            info.notificationTimeout = 200
        }

        serviceScope.launch {
            launch {
                preferences.blockingEnabled.collect { enabled ->
                    isBlockingEnabled = enabled
                    if (!enabled) removeBlockOverlay()
                }
            }
            launch {
                preferences.blockShortsEnabled.collect { enabled ->
                    isShortsBlockingEnabled = enabled
                }
            }
            launch {
                blockedAppRepo.getEnabledBlockedApps().collect { apps ->
                    blockedPackages = apps.map { it.packageName }.toSet()
                }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!isBlockingEnabled) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName == this.packageName) return
        if (packageName == "com.android.systemui") return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val className = event.className?.toString() ?: ""
                handleWindowStateChanged(packageName, className)
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                if (packageName == YOUTUBE_PACKAGE && isShortsBlockingEnabled) {
                    scheduleShortsCheck()
                }
            }
        }
    }

    private fun handleWindowStateChanged(packageName: String, className: String) {
        currentForegroundPackage = packageName

        if (packageName != YOUTUBE_PACKAGE) {
            resetShortsState()
        }

        if (packageName in blockedPackages) {
            showBlockOverlay()
            incrementBlockCount(packageName)
            return
        }

        if (packageName == YOUTUBE_PACKAGE && isShortsBlockingEnabled) {
            if (isShortsClassName(className)) {
                navigateBackFromShorts()
                return
            }
            scheduleShortsCheck()
            return
        }

        removeBlockOverlay()
    }

    private fun isShortsClassName(className: String): Boolean {
        val lower = className.lowercase()
        return lower.contains("shorts") || lower.contains("reelwatch")
    }

    // -- Shorts: detecção com retry e ação de Back --

    private var shortsCheckRunnable: Runnable? = null

    private fun scheduleShortsCheck() {
        shortsCheckRunnable?.let { handler.removeCallbacks(it) }

        val runnable = object : Runnable {
            private var retryCount = 0

            override fun run() {
                if (currentForegroundPackage != YOUTUBE_PACKAGE) return
                if (!isShortsBlockingEnabled) return

                try {
                    val root = rootInActiveWindow ?: return
                    val isShortsVisible = detectShortsScreen(root)

                    if (isShortsVisible) {
                        navigateBackFromShorts()
                    } else if (retryCount < MAX_RETRIES) {
                        retryCount++
                        handler.postDelayed(this, RETRY_DELAY_MS)
                    }
                } catch (_: Exception) {
                }
            }
        }

        shortsCheckRunnable = runnable
        handler.postDelayed(runnable, INITIAL_CHECK_DELAY_MS)
    }

    private fun detectShortsScreen(root: AccessibilityNodeInfo): Boolean {
        for (viewId in SHORTS_FULL_VIEW_IDS) {
            val nodes = root.findAccessibilityNodeInfosByViewId(viewId)
            if (!nodes.isNullOrEmpty()) {
                return true
            }
        }
        return false
    }

    private fun navigateBackFromShorts() {
        val now = System.currentTimeMillis()

        if (now - lastShortsBackTime < BACK_COOLDOWN_MS) return

        if (now - lastShortsBackTime > BACK_RESET_WINDOW_MS) {
            shortsBackCount = 0
        }

        if (shortsBackCount >= MAX_BACK_PRESSES) return

        lastShortsBackTime = now
        shortsBackCount++

        performGlobalAction(GLOBAL_ACTION_BACK)
        incrementBlockCount(YOUTUBE_PACKAGE)

        handler.postDelayed({
            if (currentForegroundPackage == YOUTUBE_PACKAGE && isShortsBlockingEnabled) {
                try {
                    val root = rootInActiveWindow ?: return@postDelayed
                    if (detectShortsScreen(root)) {
                        performGlobalAction(GLOBAL_ACTION_BACK)
                    }
                } catch (_: Exception) {
                }
            }
        }, BACK_VERIFY_DELAY_MS)
    }

    private fun resetShortsState() {
        shortsBackCount = 0
        lastShortsBackTime = 0L
        shortsCheckRunnable?.let { handler.removeCallbacks(it) }
        shortsCheckRunnable = null
    }

    // -- Apps bloqueados: overlay --

    private fun incrementBlockCount(packageName: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                blockedAppRepo.incrementBlockedCount(packageName)
            } catch (_: Exception) {
            }
        }
    }

    private fun showBlockOverlay() {
        if (overlayView != null) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        overlayView = createBlockView()
        try {
            windowManager.addView(overlayView, params)
        } catch (_: Exception) {
            overlayView = null
        }
    }

    private fun createBlockView(): View {
        return View(this).apply {
            setBackgroundColor(0xF00A0A0A.toInt())
            setOnClickListener {
                removeBlockOverlay()
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(homeIntent)
            }
        }
    }

    private fun removeBlockOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
            overlayView = null
        }
    }

    override fun onInterrupt() {
        removeBlockOverlay()
    }

    override fun onDestroy() {
        removeBlockOverlay()
        resetShortsState()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val YOUTUBE_PACKAGE = "com.google.android.youtube"

        private const val INITIAL_CHECK_DELAY_MS = 50L
        private const val RETRY_DELAY_MS = 180L
        private const val MAX_RETRIES = 5
        private const val BACK_COOLDOWN_MS = 1500L
        private const val BACK_VERIFY_DELAY_MS = 800L
        private const val BACK_RESET_WINDOW_MS = 5000L
        private const val MAX_BACK_PRESSES = 4

        private val SHORTS_FULL_VIEW_IDS = listOf(
            "com.google.android.youtube:id/reel_watch_fragment_root",
            "com.google.android.youtube:id/reel_recycler",
            "com.google.android.youtube:id/reel_player_page_container",
            "com.google.android.youtube:id/shorts_player_container",
            "com.google.android.youtube:id/reel_watch_player",
        )
    }
}
