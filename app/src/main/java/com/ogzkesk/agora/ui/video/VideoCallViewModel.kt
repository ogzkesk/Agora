package com.ogzkesk.agora.ui.video

import com.ogzkesk.agora.mvi.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VideoCallViewModel @Inject constructor(

) : ViewModel<VideoCallScreenState, VideoCallScreenEvent>(VideoCallScreenState()) {

    override fun onEvent(event: VideoCallScreenEvent) {

    }
}
