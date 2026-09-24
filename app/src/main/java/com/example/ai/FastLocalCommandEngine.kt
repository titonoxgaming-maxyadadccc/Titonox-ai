package com.example.ai

import com.example.automation.AccessibilityController
import com.example.data.TitonoxRepository
import com.example.device.DeviceController
import com.example.productivity.ProductivityManager
import java.util.Locale

class FastLocalCommandEngine(
    private val deviceController: DeviceController,
    private val accessibilityController: AccessibilityController,
    private val productivityManager: ProductivityManager,
    private val repository: TitonoxRepository
) {

    // Contextual Task Memory for progressive multi-turn clarifications
    var activeStoryMedia: String? = null
    var activeStorySong: String? = null
    var activeStoryText: String? = null
    var activeContact: ContactRecord? = null

    suspend fun tryExecuteLocal(command: String): LocalCommandResult {
        val raw = command.trim()
        val text = raw.lowercase(Locale.ROOT)

        // 1. IDENTITY MANDATES
        if (text == "who are you" || text.contains("who are you") || text.contains("tum kaun ho") || text.contains("aap kaun hain")) {
            return LocalCommandResult.Handled(
                spokenReply = "I am TITONOX, your personal AI assistant.",
                displayMessage = "I am TITONOX, your personal AI assistant.",
                actionTag = "IDENTITY"
            )
        }

        if (text.contains("youtube channel") || text.contains("official youtube") || text == "youtube") {
            return LocalCommandResult.Handled(
                spokenReply = "My official YouTube channel is TITONOXOFFICIAL.",
                displayMessage = "Official YouTube: TITONOXOFFICIAL",
                actionTag = "IDENTITY"
            )
        }

        if (text.contains("instagram") && (text.contains("official") || text.contains("channel") || text.contains("account"))) {
            return LocalCommandResult.Handled(
                spokenReply = "My official Instagram account is TITONOXOFFICIAL.",
                displayMessage = "Official Instagram: TITONOXOFFICIAL",
                actionTag = "IDENTITY"
            )
        }

        if (text.contains("owner") || text.contains("creator") || text.contains("who made you") || text.contains("kisne banaya") || text.contains("who created you")) {
            return LocalCommandResult.Handled(
                spokenReply = "I was created as TITONOX by Aditya Yadav.",
                displayMessage = "Creator: Aditya Yadav",
                actionTag = "IDENTITY"
            )
        }

        // 2. STOP / PAUSE / RESUME COMMANDS
        if (text == "stop" || text == "cancel" || text == "abort" || text == "ruko" || text == "bas" || text == "task band karo") {
            return LocalCommandResult.Handled(
                spokenReply = "Task stopped.",
                displayMessage = "Active automation paused/stopped by user.",
                actionTag = "PAUSE_OR_STOP"
            )
        }

        if (text == "resume" || text == "continue" || text == "aage chalo" || text.contains("continue where you stopped")) {
            return LocalCommandResult.Handled(
                spokenReply = "Resuming from last verified step.",
                displayMessage = "Resuming task execution.",
                actionTag = "RESUME"
            )
        }

        // 3. CONTACT CALLING & DISAMBIGUATION SYSTEM
        // Examples: "Aditya ko call karo", "Call Aditya", "Call lagao"
        if (text.startsWith("call ") || text.contains("ko call karo") || text.contains("ko call lagao") || text.contains("call lagao")) {
            val contactQuery = raw
                .replace(Regex("(?i)^(call|call to|dial)\\s+"), "")
                .replace(Regex("(?i)\\s+(ko call karo|ko call lagao|call karo|call lagao)$"), "")
                .trim()

            val targetName = if (contactQuery.isEmpty() || contactQuery == "karo") "Aditya" else contactQuery
            val matches = deviceController.searchContacts(targetName)

            if (matches.size > 1) {
                // Ambiguity detected: Prompt for disambiguation without guessing
                return LocalCommandResult.RequiresDisambiguation(
                    DisambiguationPrompt(
                        title = "Sir, $targetName naam ke ${matches.size} contacts mile hain. Kaunse $targetName ko call karun?",
                        query = targetName,
                        contacts = matches
                    )
                )
            } else if (matches.size == 1) {
                val contact = matches.first()
                activeContact = contact
                val steps = listOf(
                    TaskStep(1, "make_call", contact.phoneNumber, "Initiate phone call to ${contact.name}"),
                    TaskStep(2, "verify_app", "dialer", "Verify call connection screen")
                )
                return LocalCommandResult.MultiStepPlan(
                    steps = steps,
                    query = raw,
                    taskTitle = "Call ${contact.name}",
                    targetApp = "Phone Dialer"
                )
            } else {
                return LocalCommandResult.Handled(
                    spokenReply = "Sir, $targetName naam ka contact nahi mila.",
                    displayMessage = "Contact '$targetName' not found on device.",
                    actionTag = "CONTACTS"
                )
            }
        }

        // 4. SMART PROGRESSIVE CLARIFICATION FOR COMPLEX WORKFLOWS: INSTAGRAM STORY
        // Case A: Full compound command: "Instagram kholo, latest photo story mein lagao, Arijit ka song lagao aur Good Vibes likh do"
        if (text.contains("story") && text.contains("instagram") && (text.contains("song") || text.contains("music") || text.contains("good vibes") || text.contains("likh"))) {
            val photoDesc = if (text.contains("latest")) "Latest photo" else "Selected photo"
            val songName = if (text.contains("arijit")) "Arijit Singh" else "Trending audio"
            val textNote = if (text.contains("good vibes")) "Good vibes" else "TITONOX Post"

            val steps = listOf(
                TaskStep(1, "open_app", "Instagram", "Open Instagram"),
                TaskStep(2, "verify_app", "com.instagram.android", "Verify Instagram is active"),
                TaskStep(3, "open_story", "Story", "Open Story creation"),
                TaskStep(4, "select_photo", photoDesc, "Select requested image"),
                TaskStep(5, "add_song", songName, "Add requested song ($songName)"),
                TaskStep(6, "add_text", textNote, "Add requested text ($textNote)"),
                TaskStep(7, "verify_preview", "Preview", "Verify story preview"),
                TaskStep(8, "post_story", "Share", "Post story to Instagram", isDestructiveOrSensitive = true),
                TaskStep(9, "verify_app", "com.instagram.android", "Verify result")
            )
            return LocalCommandResult.MultiStepPlan(
                steps = steps,
                query = raw,
                taskTitle = "Instagram Story Creation",
                targetApp = "Instagram"
            )
        }

        // Case B: Progressive Ambiguous Command ("Instagram par ek story laga do", "Story laga do")
        if (text.contains("story") && (text.contains("instagram") || text.contains("laga do") || text.contains("bana do"))) {
            if (activeStoryMedia == null && !text.contains("photo") && !text.contains("video") && !text.contains("ye wali")) {
                return LocalCommandResult.RequiresClarification(
                    ClarificationPrompt(
                        parameterKey = "story_media",
                        question = "Sir, kaunsi image ya video use karni hai?",
                        options = listOf("Ye wali photo", "Latest photo", "Gallery select karo")
                    )
                )
            }
            if (activeStoryMedia != null && activeStorySong == null && !text.contains("song") && !text.contains("music")) {
                return LocalCommandResult.RequiresClarification(
                    ClarificationPrompt(
                        parameterKey = "story_song",
                        question = "Photo selected. Iske saath song bhi lagana hai?",
                        options = listOf("Haan, Arijit ka song", "Haan, Trending song", "Nahi, bina song ke")
                    )
                )
            }
            if (activeStoryMedia != null && activeStoryText == null && !text.contains("likh") && !text.contains("text")) {
                return LocalCommandResult.RequiresClarification(
                    ClarificationPrompt(
                        parameterKey = "story_text",
                        question = "Text ya note bhi add karna hai?",
                        options = listOf("Haan, likho: Good vibes", "Haan, custom text", "Nahi, text nahi chahiye")
                    )
                )
            }
        }

        // Clarification reply handles: "Ye wali photo" -> advance state
        if (text.contains("ye wali photo") || text.contains("this photo") || text.contains("latest photo")) {
            activeStoryMedia = "Selected photo"
            return LocalCommandResult.RequiresClarification(
                ClarificationPrompt(
                    parameterKey = "story_song",
                    question = "Photo selected. Iske saath song bhi lagana hai?",
                    options = listOf("Haan, song lagao", "Arijit ka song", "Nahi, bina song ke")
                )
            )
        }

        // Clarification reply handles: "Haan, song lagao" or "Arijit ka song"
        if (activeStoryMedia != null && (text.contains("arijit") || text.contains("song") || text.contains("gaana") || text == "haan")) {
            activeStorySong = if (text.contains("arijit")) "Arijit Singh" else "Popular Song"
            return LocalCommandResult.RequiresClarification(
                ClarificationPrompt(
                    parameterKey = "story_text",
                    question = "Text ya note bhi add karna hai?",
                    options = listOf("Haan, likho: Good vibes", "Nahi, bas itna hi")
                )
            )
        }

        // Clarification reply handles: "Haan, likho: Good vibes" -> now execute full verified plan!
        if (activeStoryMedia != null && (text.contains("good vibes") || text.contains("likho") || text.contains("text"))) {
            val noteText = if (text.contains("good vibes")) "Good vibes" else "TITONOX Story"
            activeStoryText = noteText

            val steps = listOf(
                TaskStep(1, "open_app", "Instagram", "Open Instagram"),
                TaskStep(2, "verify_app", "com.instagram.android", "Verify Instagram is active"),
                TaskStep(3, "open_story", "Story", "Open Story creation"),
                TaskStep(4, "select_photo", activeStoryMedia ?: "Selected photo", "Select requested image"),
                TaskStep(5, "add_song", activeStorySong ?: "Trending Song", "Add requested song"),
                TaskStep(6, "add_text", noteText, "Add requested text"),
                TaskStep(7, "verify_preview", "Preview", "Verify story preview"),
                TaskStep(8, "post_story", "Share", "Post story to Instagram", isDestructiveOrSensitive = true),
                TaskStep(9, "verify_app", "com.instagram.android", "Verify result")
            )

            // Reset conversation memory
            activeStoryMedia = null
            activeStorySong = null
            activeStoryText = null

            return LocalCommandResult.MultiStepPlan(
                steps = steps,
                query = raw,
                taskTitle = "Instagram Story Creation",
                targetApp = "Instagram"
            )
        }

        // 5. EXISTING CONTENT DETECTION CHECK ("Story par ek note laga do", "Ek note laga do")
        if (text.contains("story par ek note") || text.contains("note laga do") || text.contains("add a note")) {
            val existing = accessibilityController.detectExistingContent("note")
            if (existing != null) {
                return LocalCommandResult.RequiresClarification(
                    ClarificationPrompt(
                        parameterKey = "existing_content_conflict",
                        question = "Sir, ek note already laga hua hai: \"$existing\". Kya use replace karun, usme edit karun, ya waise hi rehne du?",
                        options = listOf("Replace karo", "Edit karo", "Waise hi rehne do", "Cancel"),
                        isDestructiveConflict = true
                    )
                )
            }
        }

        // 6. MULTI-APP COMPLEX WORKFLOW: Gallery -> Instagram -> WhatsApp
        // Example: "Gallery se ye photo uthao, Instagram par story mein lagao aur WhatsApp par Aditya ko bhejo"
        if (text.contains("gallery") && text.contains("instagram") && text.contains("whatsapp")) {
            val steps = listOf(
                TaskStep(1, "open_app", "Gallery", "Open Gallery and select photo"),
                TaskStep(2, "verify_app", "gallery", "Verify Gallery photo selection"),
                TaskStep(3, "open_app", "Instagram", "Open Instagram"),
                TaskStep(4, "open_story", "Story", "Create story from photo"),
                TaskStep(5, "verify_preview", "Preview", "Verify Instagram Story preview"),
                TaskStep(6, "open_app", "WhatsApp", "Open WhatsApp"),
                TaskStep(7, "click_ui", "Aditya", "Select contact Aditya"),
                TaskStep(8, "whatsapp_message", "Sent photo via TITONOX workflow", "Attach and send photo with confirmation", isDestructiveOrSensitive = true)
            )
            return LocalCommandResult.MultiStepPlan(
                steps = steps,
                query = raw,
                taskTitle = "Multi-App Workflow (Gallery -> Instagram -> WhatsApp)",
                targetApp = "Cross-App"
            )
        }

        // 7. APP-AWARE AUTOMATION: "Isko save kar do"
        if (text == "isko save kar do" || text.contains("save this") || text.contains("ise save karo")) {
            val currentPkg = accessibilityController.getCurrentForegroundPackage()
            val screenSummary = accessibilityController.readCurrentScreen()
            val appLabel = currentPkg.substringAfterLast('.')

            val snippet = (screenSummary?.textElements ?: emptyList()).take(3).joinToString("; ")
            val contentToSave = if (snippet.isNotEmpty()) snippet else "Screen content from $appLabel"

            repository.saveNote(title = "Saved from $appLabel", content = contentToSave)
            return LocalCommandResult.Handled(
                spokenReply = "I have inspected $appLabel and saved the active content to your notes.",
                displayMessage = "Saved from $appLabel:\n\"$contentToSave\"",
                actionTag = "CONTEXT_SAVE"
            )
        }

        // 8. FLASHLIGHT
        if (text.contains("torch on") || text.contains("flashlight on") || text.contains("torch jalao") || text.contains("flashlight jalao") || text.contains("turn flashlight on")) {
            val ok = deviceController.toggleFlashlight(true)
            val msg = if (ok) "Flashlight turned on." else "Flashlight is not available on this device."
            return LocalCommandResult.Handled(msg, msg, "HARDWARE")
        }
        if (text.contains("torch off") || text.contains("flashlight off") || text.contains("torch band") || text.contains("turn flashlight off")) {
            val ok = deviceController.toggleFlashlight(false)
            val msg = if (ok) "Flashlight turned off." else "Flashlight could not be toggled."
            return LocalCommandResult.Handled(msg, msg, "HARDWARE")
        }

        // 9. BATTERY INFO
        if (text.contains("battery") || text.contains("charge") || text.contains("kitni charging")) {
            val info = deviceController.getBatteryInfo()
            val chargingStatus = if (info.isCharging) "and currently charging" else "discharging"
            val msg = "Battery is at ${info.percentage} percent, $chargingStatus."
            return LocalCommandResult.Handled(msg, msg, "DEVICE_INFO")
        }

        // 10. VOLUME CONTROLS
        if (text.contains("volume up") || text.contains("increase volume") || text.contains("awaaz badhao") || text.contains("turn volume up") || text.contains("turn the volume up")) {
            val newVol = deviceController.adjustVolume(increase = true)
            val msg = "Media volume increased to $newVol percent."
            return LocalCommandResult.Handled(msg, msg, "VOLUME")
        }
        if (text.contains("volume down") || text.contains("lower volume") || text.contains("decrease volume") || text.contains("turn the volume down") || text.contains("awaaz kam karo")) {
            val newVol = deviceController.adjustVolume(increase = false)
            val msg = "Media volume lowered to $newVol percent."
            return LocalCommandResult.Handled(msg, msg, "VOLUME")
        }
        if (text.contains("mute") || text.contains("silent") || text.contains("volume zero")) {
            val newVol = deviceController.setVolumePercentage(0)
            val msg = "Media volume muted."
            return LocalCommandResult.Handled(msg, msg, "VOLUME")
        }
        if (text.contains("volume") && Regex("\\d+").containsMatchIn(text)) {
            val percent = Regex("\\d+").find(text)?.value?.toIntOrNull() ?: 50
            val clamped = percent.coerceIn(0, 100)
            val newVol = deviceController.setVolumePercentage(clamped)
            val msg = "Media volume set to $newVol percent."
            return LocalCommandResult.Handled(msg, msg, "VOLUME")
        }

        // Camera & Gallery
        if (text == "open camera" || text.contains("camera kholo") || text.contains("take photo")) {
            deviceController.openCamera()
            val msg = "Opening camera."
            return LocalCommandResult.Handled(msg, msg, "CAMERA")
        }
        if (text.contains("find my latest photo") || text.contains("latest photo") || text == "open gallery" || text == "gallery kholo") {
            deviceController.openGallery()
            val msg = "Opening photos gallery."
            return LocalCommandResult.Handled(msg, msg, "GALLERY")
        }

        // Screenshot
        if (text.contains("take a screenshot") || text.contains("screenshot") || text.contains("screen shot")) {
            val msg = "Screen snapshot requested."
            return LocalCommandResult.Handled(msg, msg, "SCREENSHOT")
        }

        // WhatsApp messaging workflow with HIGH risk confirmation gate
        if ((text.contains("message") || text.contains("whatsapp")) && (text.contains("that") || text.contains(":") || text.contains("ko message"))) {
            val contactName = if (text.contains("rahul")) "Rahul" else if (text.contains("aditya")) "Aditya" else "Contact"
            val messageContent = if (raw.contains(":")) {
                raw.substringAfter(":").trim()
            } else if (raw.contains("that ", ignoreCase = true)) {
                raw.substringAfter("that ", "").trim()
            } else {
                "I will call you later."
            }

            val contacts = deviceController.searchContacts(contactName)
            if (contacts.size > 1) {
                return LocalCommandResult.RequiresDisambiguation(
                    DisambiguationPrompt(
                        title = "Found ${contacts.size} contacts named $contactName. Which one would you like to message?",
                        query = contactName,
                        contacts = contacts
                    )
                )
            }

            val targetContact = contacts.firstOrNull() ?: ContactRecord(
                id = "1",
                name = contactName,
                phoneNumber = ""
            )

            val confirmPrompt = ConfirmationPrompt(
                actionTitle = "Send WhatsApp Message",
                actionDetail = "Message ready for ${targetContact.name}:\n\"$messageContent\"\n\nSend it?",
                riskLevel = "HIGH",
                targetApp = "WhatsApp",
                confirmButtonText = "Send",
                cancelButtonText = "Cancel"
            )

            val pendingPlan = ExecutionPlan(
                id = "whatsapp_${System.currentTimeMillis()}",
                userQuery = raw,
                taskTitle = "Message ${targetContact.name}",
                targetApp = "WhatsApp",
                steps = listOf(
                    TaskStep(1, "open_app", "WhatsApp", "Open WhatsApp"),
                    TaskStep(2, "verify_app", "com.whatsapp", "Verify WhatsApp is active"),
                    TaskStep(3, "whatsapp_message", messageContent, "Send message to ${targetContact.name}", isDestructiveOrSensitive = true)
                )
            )

            return LocalCommandResult.RequiresConfirmation(
                confirmation = confirmPrompt,
                pendingPlan = pendingPlan
            )
        }

        // Chrome search
        if (text.contains("chrome") && text.contains("search")) {
            val q = raw.replace(Regex("(?i)^(open chrome and search for|open chrome and search|search chrome for|search in chrome|chrome search)\\s*"), "").trim()
            val cleanQuery = if (q.isEmpty()) "latest Android news" else q
            deviceController.openWebSearch(cleanQuery)
            val msg = "Searching '$cleanQuery' on Chrome."
            return LocalCommandResult.Handled(msg, msg, "WEB_SEARCH")
        }

        // 11. HARDWARE SETTINGS (Wi-Fi, Bluetooth, SIM, Display)
        if (text.contains("bluetooth on") || text.contains("bluetooth off") || text.contains("turn bluetooth on") || text.contains("open bluetooth")) {
            deviceController.openBluetoothSettings()
            val msg = "Opening Bluetooth settings."
            return LocalCommandResult.Handled(msg, msg, "SETTINGS")
        }
        if (text.contains("wifi") || text.contains("wi-fi") || text.contains("wi fi")) {
            deviceController.openWifiSettings()
            val msg = "Opening Wi-Fi controls."
            return LocalCommandResult.Handled(msg, msg, "SETTINGS")
        }
        if (text.contains("brightness") || text.contains("display")) {
            deviceController.openDisplaySettings()
            val msg = "Opening Display and Brightness settings."
            return LocalCommandResult.Handled(msg, msg, "SETTINGS")
        }
        if (text.contains("sim") || text.contains("network")) {
            deviceController.openSimSettings()
            val msg = "Opening SIM and Network settings."
            return LocalCommandResult.Handled(msg, msg, "SIM")
        }

        // 12. GLOBAL ACCESSIBILITY ACTIONS (Back, Home, Notifications)
        if (text == "go back" || text == "back" || text == "piche jao" || text == "wapas jao") {
            val ok = accessibilityController.pressBack()
            val msg = if (ok) "Navigating back." else "Accessibility Service is not active. Enable it in Settings."
            return LocalCommandResult.Handled(msg, msg, "ACCESSIBILITY")
        }
        if (text == "go home" || text == "home screen" || text == "home jao") {
            val ok = accessibilityController.pressHome()
            val msg = if (ok) "Navigating to home screen." else "Accessibility Service required for Home action."
            return LocalCommandResult.Handled(msg, msg, "ACCESSIBILITY")
        }
        if (text.contains("notifications") || text.contains("notification panel")) {
            val ok = accessibilityController.openNotifications()
            val msg = if (ok) "Opening notification shade." else "Accessibility Service required."
            return LocalCommandResult.Handled(msg, msg, "ACCESSIBILITY")
        }

        // 13. SCREEN READING
        if (text.contains("read my screen") || text.contains("read screen") || text.contains("what is on my screen") || text.contains("screen padho") || text.contains("tell me what i'm looking at")) {
            val summary = accessibilityController.readCurrentScreen()
            if (summary != null && (summary.textElements.isNotEmpty() || summary.buttonLabels.isNotEmpty())) {
                val appLabel = summary.packageName.substringAfterLast('.')
                val contentSnippet = (summary.textElements + summary.buttonLabels).take(5).joinToString(", ")
                val spoken = "You are on $appLabel. I can see elements like $contentSnippet."
                val display = "Screen [${summary.packageName}]:\nTexts: ${summary.textElements.take(5).joinToString("\n- ")}\nButtons: ${summary.buttonLabels.take(4).joinToString(", ")}"
                return LocalCommandResult.Handled(spoken, display, "SCREEN_READER")
            } else {
                val fallbackMsg = "Could not read screen content. Ensure Accessibility Service is enabled for TITONOX."
                return LocalCommandResult.Handled(fallbackMsg, fallbackMsg, "SCREEN_READER")
            }
        }

        // 14. PRODUCTIVITY (Note, Todo, Timer, Calendar)
        if (text.startsWith("note ") || text.startsWith("create note") || text.startsWith("note this") || text.startsWith("note banao")) {
            val noteBody = raw.replace(Regex("(?i)^(note this:|note this|create a note saying|create a note|create note|note banao|note)\\s*"), "").trim()
            if (noteBody.isNotEmpty()) {
                repository.saveNote(title = "Voice Note", content = noteBody)
                val msg = "Note saved: \"$noteBody\""
                return LocalCommandResult.Handled(msg, msg, "PRODUCTIVITY")
            }
        }

        if (text.startsWith("add todo") || text.startsWith("add to my todo") || text.startsWith("todo ") || text.startsWith("task add karo")) {
            val taskBody = raw.replace(Regex("(?i)^(add to my todo list:|add to my todo list|add todo|todo|task add karo)\\s*"), "").trim()
            if (taskBody.isNotEmpty()) {
                repository.addTodo(task = taskBody)
                val msg = "Added to todo list: \"$taskBody\""
                return LocalCommandResult.Handled(msg, msg, "PRODUCTIVITY")
            }
        }

        if (text.contains("timer for") || text.contains("set a timer") || text.contains("timer lagao")) {
            val digits = Regex("\\d+").find(text)?.value?.toIntOrNull() ?: 5
            productivityManager.setSystemTimer(digits * 60, "TITONOX Timer")
            val msg = "Setting timer for $digits minutes."
            return LocalCommandResult.Handled(msg, msg, "PRODUCTIVITY")
        }

        // 15. COMPOUND YOUTUBE SEARCH AND PLAY
        if (text.contains("youtube") && (text.contains("search") || text.contains("play"))) {
            val queryMatch = Regex("(?i)search (?:for )?(.+?)(?: and |$)").find(raw)?.groupValues?.get(1)
                ?: Regex("(?i)search (.+)").find(raw)?.groupValues?.get(1)
                ?: "Minecraft survival"

            val steps = mutableListOf<TaskStep>()
            steps.add(TaskStep(1, "open_app", "YouTube", "Launch YouTube app"))
            steps.add(TaskStep(2, "verify_app", "com.google.android.youtube", "Verify YouTube is in foreground"))
            steps.add(TaskStep(3, "click_ui", "Search", "Tap Search icon in YouTube"))
            steps.add(TaskStep(4, "type_ui", queryMatch.trim(), "Enter '$queryMatch' into search bar"))
            steps.add(TaskStep(5, "click_ui", queryMatch.trim(), "Execute search query"))
            steps.add(TaskStep(6, "click_first_result", "video", "Select and play first video result"))
            if (text.contains("volume") || text.contains("lower")) {
                steps.add(TaskStep(7, "volume", "down", "Lower media volume"))
            }

            return LocalCommandResult.MultiStepPlan(
                steps = steps,
                query = raw,
                taskTitle = "YouTube Search & Play",
                targetApp = "YouTube"
            )
        }

        // 16. Single App Launching
        if (text.startsWith("open ") || text.endsWith("kholo")) {
            val appTarget = raw.replace(Regex("(?i)^open\\s+"), "").replace(Regex("(?i)\\s+kholo$"), "").trim()
            val res = deviceController.launchAppByName(appTarget)
            return if (res.success) {
                LocalCommandResult.Handled("Opening $appTarget.", res.message, "APP_LAUNCH")
            } else {
                LocalCommandResult.Handled(res.message, res.message, "APP_LAUNCH")
            }
        }

        // 17. Web Search command
        if (text.startsWith("search google for") || text.startsWith("search the web for") || text.startsWith("google ")) {
            val query = raw.replace(Regex("(?i)^(search google for|search the web for|google)\\s+"), "").trim()
            deviceController.openWebSearch(query)
            val msg = "Searching the web for $query."
            return LocalCommandResult.Handled(msg, msg, "WEB_SEARCH")
        }

        return LocalCommandResult.NotHandled
    }
}
