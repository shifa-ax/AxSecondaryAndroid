package com.ax.axsecondaryapp.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Environment
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log
import java.io.File

class RecordingService : Service() {
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var audioFile: File

    override fun onCreate() {
        super.onCreate()
        // Create the MediaRecorder instance
//        if (isCallRecordingSupported()) {
            mediaRecorder = MediaRecorder()
        val maxAudioSource = MediaRecorder.getAudioSourceMax()
        Log.d("AudioSources", "Max Audio Source: $maxAudioSource")
            // Set the source and output format
//            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL)
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)

            // Create a file to store the recorded audio
            audioFile = File(Environment.getExternalStorageDirectory(), "recorded_call.wav")
            mediaRecorder.setOutputFile(audioFile.absolutePath)

            // Set the audio encoder
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                // Prepare the MediaRecorder for recording
                mediaRecorder.prepare()

                // Start recording
                mediaRecorder.start()
                Log.d("Shifa", "Recorded audio file path: ${audioFile.absolutePath}")

            } catch (e: Exception) {
                Log.e("Shifa", "Failed to start recording: ${e.message}")
            }
//        } else {
//            Log.e("Shifa", "Call recording is not supported")
//        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop and release the MediaRecorder
        try {
            mediaRecorder.stop()
            mediaRecorder.release()
        } catch (e: Exception) {
            Log.e("RecordingService", "Failed to stop recording: ${e.message}")
        }
    }

    private fun isCallRecordingSupported(): Boolean {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var test = telephonyManager.callState != TelephonyManager.CALL_STATE_IDLE
        return test
    }
}

