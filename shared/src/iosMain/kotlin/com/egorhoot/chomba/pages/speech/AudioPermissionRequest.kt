package com.egorhoot.chomba.pages.speech

// iOSMain
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted

class IOSAudioPermissionHandler : PermissionHandler {
    override val isGranted: Boolean
        get() = AVAudioSession.sharedInstance().recordPermission() == AVAudioSessionRecordPermissionGranted
    override val shouldShowRationale: Boolean
        get() = false // iOS does not have rationale
    override fun requestPermission() {
        AVAudioSession.sharedInstance().requestRecordPermission { granted ->
            // handle result
        }
    }
}

