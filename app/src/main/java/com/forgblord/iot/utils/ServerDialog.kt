package com.forgblord.iot.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.forgblord.iot.R

class ServerDialog(
    context: Context,
): Dialog(context) {
    init {
        setCancelable(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.server_dialog)
    }
}