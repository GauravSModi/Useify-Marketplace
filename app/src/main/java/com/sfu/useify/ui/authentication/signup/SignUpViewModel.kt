package com.sfu.useify.ui.authentication.signup

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignUpViewModel: ViewModel() {
    val userImg = MutableLiveData<Bitmap>()
}
