package com.blockchain.morph.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AlertDialog
import android.view.View

internal fun showHelpDialog(activity: Activity, startKyc: () -> Unit) {
    AlertDialog.Builder(activity, R.style.AlertDialogStyle)
        .setView(View.inflate(activity, R.layout.morph_dialog_help, null))
        .setNeutralButton(R.string.morph_help_dialog_view_swap_limit) { _, _ ->
            startKyc()
        }
        .setPositiveButton(R.string.morph_help_dialog_contact_support) { _, _ ->
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(activity.getString(R.string.morph_help_dialog_contact_support_url))
                )
            )
        }
        .show()
}
