package com.notrash.data

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle

class NotrashProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        return true
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        val context = context ?: return null
        val bundle = Bundle()
        when (method) {
            "get_config" -> {
                bundle.putBoolean(
                    NotrashConfig.KEY_CAMERA_SOUND_UNLOCK,
                    NotrashConfig.isCameraSoundUnlockEnabled(context)
                )
                bundle.putBoolean(
                    NotrashConfig.KEY_ESSENTIAL_VOICE_UNLOCK,
                    NotrashConfig.isEssentialVoiceUnlockEnabled(context)
                )
                return bundle
            }
            "is_module_active" -> {
                bundle.putBoolean("active", NotrashConfig.isModuleActive)
                return bundle
            }
        }
        return super.call(method, arg, extras)
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
