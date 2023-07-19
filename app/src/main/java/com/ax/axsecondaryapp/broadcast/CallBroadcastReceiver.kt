package com.ax.axsecondaryapp.broadcast

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.os.*
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.*
import com.ax.axsecondaryapp.db.MainWorker
import com.ax.axsecondaryapp.db.SharedPreferencesManager
import com.ax.axsecondaryapp.model.CallLogDetails
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.minutes


class CallBroadcastReceiver : BroadcastReceiver() {
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private var numSav = false
    private var ongoing = false
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    companion object {
        private var callonce = false
        private var inReceived = false
        private var inAnswered = false
        private var ongoingCall = false
        var token: String = ""
        var firebaseToken: String = ""

        @OptIn(ExperimentalTime::class)
        var startduration: Duration? = null
        private var isgetCallLogCalled = false
        private var prev_state: String? = ""
        var incomingNumber: String? = null
        var incomingNumberNew: String? = null
        var incomingNumFirst: String? = null

        //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var callStartTime: Date? = null
        private var isIncoming = false
        private var savedNumber: String? =
            null//because the passed incoming is only valid in ringing
        private var number: String? = null
    }

    override fun onReceive(context: Context?, intent: Intent) {
        firebaseAnalytics = context?.let { FirebaseAnalytics.getInstance(it) }!!

        val bundle = Bundle().apply {
            putString("call_broadcast", "success")
        }
        firebaseAnalytics.logEvent("call_broadcast", bundle)

        sharedPreferencesManager = context?.let { SharedPreferencesManager(it) }!!
        token = sharedPreferencesManager.getToken().toString()
        val authorizationToken = "Bearer $token"
        var userId = sharedPreferencesManager.getUserId()

        firebaseToken = sharedPreferencesManager.getFirebaseToken().toString()
        if (!checkPermissions(context)) {
            //if permission not given call fcm api to send notification with username and token
            val internetConnected: Boolean = isInternetConnected(context)
            val inputData = Data.Builder()
                .putBoolean("permission", true)
                .putString("authorizationToken", authorizationToken)
                .putString("firebaseToken", firebaseToken)
                .putBoolean("isInternetConnected", internetConnected)
                .putInt("userid", userId)
//                .putBoolean("fromForeground", false)
                .build()

            val workRequest = OneTimeWorkRequest.Builder(MainWorker::class.java)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context)
                .beginUniqueWork("unique_work", ExistingWorkPolicy.APPEND, workRequest)
                .enqueue()
        }
        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if (intent.action == "android.intent.action.NEW_OUTGOING_CALL") {
            savedNumber = intent.extras?.getString("android.intent.extra.PHONE_NUMBER")
            Log.e("TAG", "onReceive: outgoing")

        } else {
            val stateStr = intent.extras?.getString(TelephonyManager.EXTRA_STATE)
            number = intent.extras?.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
            Log.e("TAG", "onReceive: incoming")

            var state = 0
            when (stateStr) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    Log.e("CallBroadcastReceiver", "onReceive:EXTRA_STATE_RINGING ")
                    state = TelephonyManager.CALL_STATE_RINGING
                    callonce = false
                    incomingNumberNew =
                        intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    Log.e("CallBroadcastReceiver", "onReceive:EXTRA_STATE_OFFHOOK ")

                    if (number != null || savedNumber != null) {
                        if (number != null && !numSav) {
                            incomingNumFirst = number
                            numSav = true
                        } else if (savedNumber != null && !numSav) {
                            incomingNumFirst = savedNumber
                            numSav = true
                        }
                    }

                    if (!callonce && ongoing) {
                        if (incomingNumFirst != null && incomingNumberNew != null) {
                            Log.e(
                                "TAG",
                                "incomingNumFirst $incomingNumFirst incomingNumberNew $incomingNumberNew",
                            )
                            if (!incomingNumFirst.equals(incomingNumberNew)) {
                                callonce = true // check this
                                val callLogDetailsSet = missedCallService(context, savedNumber)
                                val callLog = callLogDetailsSet.first
                                val filepath = null
                                val callLogDetailsJson = Gson().toJson(callLog)
                                val internetConnected: Boolean = isInternetConnected(context)
                                val authorizationToken = "Bearer $token"
                                var userId = sharedPreferencesManager.getUserId()
                                ongoingCall = true
                                Log.e("CallBroadcastReceiver", "callMainworker 1.offhook up")

                                callMainworker(
                                    callLogDetailsJson, filepath, authorizationToken,
                                    internetConnected, userId, context
                                )
                            }
                        }
                    }
                    state = TelephonyManager.CALL_STATE_OFFHOOK
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    Log.e("CallBroadcastReceiver", "onReceive:EXTRA_STATE_IDLE ")
                    state = TelephonyManager.CALL_STATE_IDLE
                }
            }
            if (number != null) {
                Log.e("CallBroadcastReceiver", "onReceive:incoming num $number ")
                onCallStateChanged(context, state, number)
            }

        }
    }

    private fun checkPermissions(context: Context): Boolean {
        val permissions = arrayOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE
        )

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) {
                return false
            }
        }
        return true
    }

    //Derived classes should override these to respond to specific events of interest
    private fun onIncomingCallReceived(ctx: Context?, number: String?, start: Date?) {
        inReceived = true
        inAnswered = false
    }

    private fun onIncomingCallAnswered(ctx: Context?, number: String?, start: Date?) {
        inAnswered = true
    }

    private fun onIncomingCallEnded(
        ctx: Context?,
        number: String?,
        start: Date?,
        end: Date?
    ) {

        val internetConnected: Boolean = ctx?.let { isInternetConnected(it) } == true
        val authorizationToken = "Bearer $token"
        var userId = sharedPreferencesManager.getUserId()


        if (inReceived && inAnswered) {
            sharedPreferencesManager.savecallAns(true)
            val callLogDetailsSet = ctx?.let { retrieveService(it, number) }
            val callLog = callLogDetailsSet?.first
            val callLogDetailsJson = Gson().toJson(callLog)
            var filepath = callLogDetailsSet?.second
            if (ctx != null) {
                Log.e("CallBroadcastReceiver", "callMainworker 2.onIncomingCallEnded: ")
                callMainworker(
                    callLogDetailsJson, filepath, authorizationToken,
                    internetConnected, userId, ctx
                )
            }
        } else {
            val callLogDetailsSet = ctx?.let { missedCallService(it, number) }
            val callLog = callLogDetailsSet?.first
            val callLogDetailsJson = Gson().toJson(callLog)
            var filepath = null
            if (ctx != null) {
                Log.e("CallBroadcastReceiver", "callMainworker 3.onIncomingCallEnded ")
                callMainworker(
                    callLogDetailsJson, filepath, authorizationToken,
                    internetConnected, userId, ctx
                )
            }
        }
    }


    @OptIn(ExperimentalTime::class)
    private fun onOutgoingCallEnded(
        ctx: Context?,
        number: String?,
        start: Date?,
        end: Date?
    ) {
//for both ans or not it will com here for outgoing
//        fragmentCallCompleted(context, false, false, number)
        val callLogDetailsSet = ctx?.let { retrieveService(it, number) }
        val callLog = callLogDetailsSet?.first
        val callLogDetailsJson = Gson().toJson(callLog)
        var filepath = callLogDetailsSet?.second

        val internetConnected: Boolean = ctx?.let { isInternetConnected(it) } == true
        val authorizationToken = "Bearer $token"
        var userId = sharedPreferencesManager.getUserId()

        if (ctx != null) {
            Log.e("CallBroadcastReceiver", "callMainworker 3.onOutgoingCallEnded ")

            callMainworker(
                callLogDetailsJson, filepath, authorizationToken,
                internetConnected, userId, ctx
            )
        }
//            fragmentCallCompleted(ctx, false, false, number)
    }

    private fun onMissedCall(ctx: Context?, number: String?, start: Date?) {
        val callLogDetailsSet = ctx?.let { missedCallService(it, savedNumber) }
        val callLog = callLogDetailsSet?.first
        if (callLog != null) {
//            callLog.duration = 0L shifatest
        }


        val filepath = null
        val callLogDetailsJson = Gson().toJson(callLog)
        val internetConnected: Boolean = ctx?.let { isInternetConnected(it) } == true

        val authorizationToken = "Bearer $token"
        var userId = sharedPreferencesManager.getUserId()
        if (ctx != null) {
            Log.e("CallBroadcastReceiver", "callMainworker 3.onMissedCall ")

            callMainworker(
                callLogDetailsJson, filepath, authorizationToken,
                internetConnected, userId, ctx
            )
        }
    }

    //Deals with actual events
    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    private fun onCallStateChanged(context: Context?, state: Int, number: String?) {
        if (lastState == state) {
            //No change, debounce extras
            return
        }
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                Log.e("CallBroadcastReceiver", "onCallStateChanged:CALL_STATE_RINGING ")
                isIncoming = true
                callStartTime = Date()
                if (number != null) {
                    savedNumber = number

                }
//                numSav = false // Reset numSav to false for new incoming call
                onIncomingCallReceived(context, number, callStartTime)
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                Log.e("CallBroadcastReceiver", "onCallStateChanged:CALL_STATE_OFFHOOK ")
                ongoing = true
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false
                    callStartTime = Date()
//                    onOutgoingCallStarted(context, savedNumber, callStartTime, Date())
                } else {
                    isIncoming = true
                    callStartTime = Date()
//                    numSav = false // Reset numSav to false for incoming call answered during ringing
                    onIncomingCallAnswered(context, savedNumber, callStartTime)
                }
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                numSav = false // Set numSav to true when call state is idle
                callonce = false //reset
                ongoing = false //reset

                if (number != null) {
                    savedNumber = number
                }

                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    if (savedNumber != null) {
                        onMissedCall(context, savedNumber, callStartTime)//incoming dropped
                    }
                } else if (isIncoming) {
                    onIncomingCallEnded(context, savedNumber, callStartTime, Date())//incoming ans
                } else {
                    onOutgoingCallEnded(context, savedNumber, callStartTime, Date())
                }


            }
        }
        lastState = state
    }


    private fun callMainworker(
        callLogDetailsJson: String,
        filepath: String?,
        authorizationToken: String,
        internetConnected: Boolean,
        userId: Int,
        context: Context
    ) {
        Log.e("CallBroadcastReceiver", "callMainworker:filepath $filepath")
        val inputData = Data.Builder()
            .putString("callLogDetailsJson", callLogDetailsJson)
            .putString("filepath", filepath)
            .putString("authorizationToken", authorizationToken)
            .putString("firebaseToken", firebaseToken)
            .putBoolean("isInternetConnected", internetConnected)
            .putInt("userid", userId)
            .putBoolean("fromForeground", false)
            .build()

        if (!filepath.isNullOrEmpty()) {
            Log.e("TAG", "callMainworker:$filepath ")
            sharedPreferencesManager.saveLastCallpath(filepath)
        }
        val workRequest = OneTimeWorkRequest.Builder(MainWorker::class.java)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context)
            .beginUniqueWork("unique_work", ExistingWorkPolicy.APPEND, workRequest)
            .enqueue()
    }


    private fun missedCallService(
        context: Context,
        phoneNumber: String?
    ): Pair<CallLogDetails?, String?> {

        val callLogDetails: CallLogDetails? =
            phoneNumber?.let { getLatestCallHistoryDetail(context.contentResolver, it) }

        if (callLogDetails != null) {
//            callLogDetails.duration = 0L shifatest
        }

        if (callLogDetails != null)
            if (callLogDetails.callType == 2) {
                callLogDetails.callType = 1 // outgoing
            } else {
                callLogDetails.callType = 0 //incoming
            }
        return Pair(callLogDetails, null)

        return Pair(null, null)

    }

    fun getCallHistory(
        contentResolver: ContentResolver,
        phoneNumber: String
    ): List<CallLogDetails> {
        val callHistory = mutableListOf<CallLogDetails>()
        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE
        )
        val cursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            CallLog.Calls.NUMBER + "=?",
            arrayOf(phoneNumber),
            CallLog.Calls.DATE + " DESC" // Sort by date in descending order
        )

        cursor?.use {
            while (cursor.moveToNext()) {
                val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                val dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
                val durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)
                val nameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val callTypeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)

                if (numberIndex != -1 && dateIndex != -1 && durationIndex != -1 && callTypeIndex != -1) {
                    val number = cursor.getString(numberIndex)
                    val date = cursor.getLong(dateIndex)
                    val duration = cursor.getLong(durationIndex)
                    val name = cursor.getString(nameIndex)
                    val callType = cursor.getInt(callTypeIndex)

                    val callLogDetails = CallLogDetails(number, date, duration, name, callType)
                    callHistory.add(callLogDetails)
                }
            }
        }

        // Adding a delay to ensure all call log entries are retrieved
        Thread.sleep(1000) // Adjust the delay duration as needed

        return callHistory
    }

    fun getLatestCallHistoryDetail(
        contentResolver: ContentResolver,
        phoneNumber: String
    ): CallLogDetails? {
        Thread.sleep(1000)
        val callHistory = getCallHistory(contentResolver, phoneNumber)
        Log.e("TAG", "getLatestCallHistoryDetail:callHistory${callHistory.size} ")
        return callHistory.firstOrNull()
    }

    private fun callFile(
        callLogDetails: CallLogDetails,
        mostRecentFile: File
    ): Pair<CallLogDetails?, String?> {

        if (callLogDetails.callType == 2) {
            callLogDetails.callType = 1
        } else {
            callLogDetails.callType = 0
        }
        val callRecordingPath = mostRecentFile.absolutePath
        var lastCallPath = sharedPreferencesManager.getLastCallpath()

        return if (callRecordingPath.equals(lastCallPath)) {
            Pair(callLogDetails, null)
        } else {
            Pair(callLogDetails, callRecordingPath)
        }
    }

    private fun retrieveService(
        context: Context,
        incomingNumber: String?
    ): Pair<CallLogDetails?, String?> {
        var incomingFormattedNum = incomingNumber?.let { getNumWithCountryCode(it) }
        var phoneNumber: String = ""
        val recordingsDirectory = File(Environment.getExternalStorageDirectory(), "Recordings/Call")
        if (recordingsDirectory.exists() && recordingsDirectory.isDirectory) {
            val files = recordingsDirectory.listFiles()
            files?.sortByDescending { it.lastModified() }
            if (files != null && files.isNotEmpty()) {
                val mostRecentFile = files[0]

                phoneNumber = extractPhoneNumberFromFileNew(mostRecentFile.name)
                if (phoneNumber.isEmpty()) {
                    phoneNumber = extractPhoneNameFromFilde(context, mostRecentFile.name)
                }

                if (phoneNumber.isEmpty()) {
                    val mostRecentFileTime = mostRecentFile.lastModified()
                    val callLogDetailsPh = incomingNumber?.let {
                        getLatestCallHistoryDetail(
                            context.contentResolver,
                            it
                        )
                    }
                    if (callLogDetailsPh != null) {
                        val callLogTime = callLogDetailsPh?.date

                        val timeDifferenceMillis = abs(mostRecentFileTime - callLogTime)

                        val timeDifferenceSeconds = timeDifferenceMillis / 1000
                        val timeDifferenceMinutes = timeDifferenceSeconds / 60

                        return if (timeDifferenceMinutes < 1) {
                            callFile(callLogDetailsPh, mostRecentFile)
                        } else {
                            missedCallService(context, incomingNumber)
                        }
                    }
                } else {
                    val formattedNumber = getNumWithCountryCode(phoneNumber)

                    if (incomingFormattedNum == formattedNumber) {
                        val callLogDetails = getLatestCallHistoryDetail(context.contentResolver, phoneNumber)
                        if (callLogDetails != null) {
                            val callLogTime = callLogDetails.date
                            val mostRecentFileTime = mostRecentFile.lastModified()
                            val timeDifferenceMillis = abs(mostRecentFileTime - callLogTime)
                            val timeDifferenceSeconds = timeDifferenceMillis / 1000
                            val timeDifferenceMinutes = timeDifferenceSeconds / 60

                            return if (timeDifferenceMinutes < 2) { //just additional check
                                callFile(callLogDetails, mostRecentFile)
                            } else {
                                missedCallService(context, incomingNumber)
                            }
                        }
                    } else {
                        return missedCallService(context, incomingNumber)
                    }
                }
            }
        } else {
            val recordingsDirectory =
                File(Environment.getExternalStorageDirectory(), "MIUI/sound_recorder/call_rec")
            if (recordingsDirectory.exists() && recordingsDirectory.isDirectory) {
                Thread.sleep(1000) // adding for safer side to get the updated files list
                val files = recordingsDirectory.listFiles()

                if (files != null && files.isNotEmpty()) {
                    val sortedFiles = files.sortedByDescending { it.lastModified() }
                    val mostRecentFile = sortedFiles[0]
                    phoneNumber = extractContentInParentheses(mostRecentFile.name)

                    val formattedNumber = getNumWithCountryCode(phoneNumber)

                    if (incomingFormattedNum == formattedNumber) {
                        val callLogDetails =
                            getLatestCallHistoryDetail(context.contentResolver, phoneNumber)
                        Log.e("TAG", "retrieveService:callType before ${callLogDetails?.callType}")

                        if (callLogDetails != null) {
                            if (callLogDetails.callType == 2) {
                                callLogDetails.callType = 1
                            } else if (callLogDetails.callType == 1 || callLogDetails.callType == 5 || callLogDetails.callType == 3) {
                                callLogDetails.callType = 0
                            }
                            Log.e(
                                "TAG",
                                "retrieveService:callType after${callLogDetails.callType}",
                            )

                        }
                        if (callLogDetails != null) {
                            val callRecordingPath = mostRecentFile.absolutePath
                            var lastCallPath = sharedPreferencesManager.getLastCallpath()
                            Log.e("TAG", "retrieveService:lastCallPath $lastCallPath ")
                            return if (callRecordingPath.equals(lastCallPath)) {
                                Log.e("TAG", "retrieveService:callRecordingPath uploaded already ")
                                Pair(callLogDetails, null)
                            } else {
                                Log.e(
                                    "callRecordingPath",
                                    "retrieveService: callRecordingPath== $callRecordingPath"
                                )
                                Pair(callLogDetails, callRecordingPath)
                            }
                        }
                    } else {
                        return missedCallService(context, incomingNumber)
                    }
                }
            }
        }
        return missedCallService(context, incomingNumber)
    }

    private fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }

    private fun getNumWithCountryCode(phoneNumber: String): String {
        var formattedNumber: String = ""
        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        val validatedNumber = if (phoneNumber.startsWith("+")) phoneNumber else "$phoneNumber"

        val phoneNumberNew = try {
            phoneNumberUtil.parse(validatedNumber, "AE")
        } catch (e: NumberParseException) {
            Log.e(ContentValues.TAG, "error during parsing a number")
            null
        }
        if (validatedNumber.startsWith("0")) {
            if (phoneNumberUtil.isValidNumber(phoneNumberNew)) {
                val regionCode = phoneNumberUtil.getRegionCodeForNumber(phoneNumberNew)
                var countrycode = phoneNumberUtil.getCountryCodeForRegion(regionCode)
                formattedNumber = phoneNumber.replaceFirst("0", "+${countrycode}")
            }
        } else {
            formattedNumber = phoneNumber
        }
        return formattedNumber
    }

    private fun getCallLogDetails(context: Context, phoneNumber: String): CallLogDetails? {
        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE
        )

        val selection = "${CallLog.Calls.NUMBER} = ?"
        val selectionArgs = arrayOf(phoneNumber)

        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            CallLog.Calls.DATE + " DESC"
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val numberColumnIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                val dateColumnIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
                val durationColumnIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)
                val nameColumnIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val typeColumnIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)

                if (numberColumnIndex >= 0 && dateColumnIndex >= 0 && durationColumnIndex >= 0 && nameColumnIndex >= 0 && typeColumnIndex >= 0) {
                    val number = cursor.getString(numberColumnIndex)
                    val date = cursor.getLong(dateColumnIndex)
                    val duration = cursor.getLong(durationColumnIndex)
                    val name = cursor.getString(nameColumnIndex)
                    val callType = cursor.getInt(typeColumnIndex)

                    return CallLogDetails(number, date, duration, name, callType)
                }
            }
        }
        return null
    }

    fun getCallLogDetailse(
        contentResolver: ContentResolver,
        phoneNumber: String,
        retryCount: Int = 3,
        delayMillis: Long = 1000
    ): CallLogDetails? {
        var remainingRetryCount = retryCount

        while (remainingRetryCount > 0) {
            val cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                null,
                CallLog.Calls.NUMBER + "=?",
                arrayOf(phoneNumber),
                CallLog.Calls.DEFAULT_SORT_ORDER
            )

            cursor?.use {
                if (cursor.moveToFirst()) {
                    val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                    val dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
                    val durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)
                    val nameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
                    val callTypeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)

                    if (numberIndex != -1 && dateIndex != -1 && durationIndex != -1 && callTypeIndex != -1) {
                        val number = cursor.getString(numberIndex)
                        val date = cursor.getLong(dateIndex)
                        val duration = cursor.getLong(durationIndex)
                        val name = cursor.getString(nameIndex)
                        val callType = cursor.getInt(callTypeIndex)

                        return CallLogDetails(number, date, duration, name, callType)
                    }
                }
            }

            remainingRetryCount--
            if (remainingRetryCount > 0) {
                Handler(Looper.getMainLooper()).postDelayed({
                    // Retry after the delay
                    getCallLogDetailse(
                        contentResolver,
                        phoneNumber,
                        remainingRetryCount,
                        delayMillis
                    )
                }, delayMillis)
            }
        }

        return null // Return null if no call log entry is found or columns are not found after retries
    }

    private fun extractPhoneNumberFromFile(context: Context, fileName: String): String {
        val regex = """(\+\d{1,3}|0)(\d{9,10})""".toRegex()
        val matches = regex.findAll(fileName)
        var ph: String = ""
        for (match in matches) {
            val countryCode = match.groupValues[1]
            val phoneNumber = countryCode + match.groupValues[2]
            println(phoneNumber)
            ph = phoneNumber
        }
        return ph // Return an empty string if the phone number is not found
    }

    private fun extractPhoneNumberFromFileNew(fileName: String): String {
        val regex = """Call recording\s+((\+\d{1,3}|0)(\d{9,10}))_\d+_\d+\.\w+$""".toRegex()
        val matchResult = regex.find(fileName)
        val phoneNumber = matchResult?.groupValues?.get(1) ?: ""

        return phoneNumber
    }

    private fun extractContentInParentheses(input: String): String {
        val regex = """\((.*?)\)""".toRegex()
        val matchResult = regex.find(input)
        var extractedContent = ""
        matchResult?.let {
            extractedContent = it.groupValues[1]
        }
        return if (extractedContent.startsWith("00")) {
            "+" + extractedContent.substring(2)
        } else {
            extractedContent
        }
    }

    private fun extractPhoneNameFromFileNew(context: Context, fileName: String): String {
        val pattern = Pattern.compile("Call recording\\s+(.*?)_\\d+_\\d+\\.\\w+$")

        var extractedContent = ""
        val matcher = pattern.matcher(fileName)
        if (matcher.find()) {
            extractedContent = matcher.group(1)
            println(extractedContent)
        }
        return extractedContent
    }

    private fun extractPhoneNameFromFilde(context: Context, fileName: String): String {
        val regex = """Call recording\s+(.*?)_\d+_\d+\.\w+$""".toRegex()
        val matchResult = regex.find(fileName)
        val name = matchResult?.groupValues?.get(1) ?: ""

        val number = getPhoneNumberFromContacts(context, name)
        if (number.isNotEmpty()) {
            return number
        }

        return "" // Return an empty string if the phone number is not found in contacts
    }

    private fun extractPhoneNameFromFile(context: Context, fileName: String): String {
        val nameRegex = Regex("([^_]+)_\\d+_\\d+")
        val substringToRemove = "Call recording "
        val cleanedFilename = fileName.replace(substringToRemove, "").trim()
//here i get grouos search each group if exists in cantact , if yes the get the number
        val nameMatchResult = nameRegex.find(cleanedFilename)
        if (nameMatchResult != null) {
            for (match in nameMatchResult.groupValues) {
                var number = getPhoneNumberFromContacts(context, match)
                if (number.isNotEmpty()) {
                    return number
                }
            }
        }

        return "" // Return an empty string if the phone number is not found
    }

    private fun getPhoneNumberFromContacts(context: Context, name: String): String {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(name)
        val sortOrder = null

        context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val phoneNumberColumn =
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    return cursor.getString(phoneNumberColumn)
                }
            }

        return "" // Return an empty string if the phone number is not found
    }

    enum class CallType {
        MISSED,
        INCOMING,
        OUTGOING,
        UNKNOWN
    }
}
