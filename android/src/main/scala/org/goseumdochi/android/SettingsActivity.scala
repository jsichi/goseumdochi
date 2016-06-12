// goseumdochi:  experiments with incarnation
// Copyright 2016 John V. Sichi
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.goseumdochi.android

import android._
import android.app._
import android.content._
import android.os._
import android.preference._

import java.util._

object SettingsActivity
{
  final val PREF_ENABLE_VOICE = "pref_key_enable_voice"
  final val PREF_INTRUDER_ALERT = "pref_key_intruder_alert"
  final val PREF_RECORD_VIDEO = "pref_key_record_video"
  final val PREF_DETECT_BUMPS = "pref_key_detect_bumps"
  final val PREF_WHITE_BALANCE = "pref_key_white_balance"

  def applyFormat(context : Activity, phrase : String, params : Seq[Any])
      : String =
  {
    try {
      String.format(phrase, params.map(_.asInstanceOf[AnyRef]):_*)
    } catch {
      case e : IllegalFormatException => {
        context.getString(R.string.speech_invalid_format)
      }
    }
  }
}

import SettingsActivity._

class SettingsActivity
    extends PreferenceActivity with ActivityBase
    with SharedPreferences.OnSharedPreferenceChangeListener
{
  override def onCreate(savedInstanceState : Bundle)
  {
    super.onCreate(savedInstanceState)
    addPreferencesFromResource(R.xml.preferences)
  }

  private def getPrefs = PreferenceManager.getDefaultSharedPreferences(this)

  override protected def onPause()
  {
    super.onPause
    getPrefs.unregisterOnSharedPreferenceChangeListener(this)
  }

  override protected def onResume()
  {
    super.onResume
    getPrefs.registerOnSharedPreferenceChangeListener(this)
  }

  override def onSharedPreferenceChanged(
    prefs : SharedPreferences, key : String)
  {
    key match {
      case PREF_INTRUDER_ALERT => {
        val phrase = prefs.getString(key, "")
        if (!phrase.isEmpty) {
          val speech = applyFormat(this, phrase, Seq(5))
          GlobalTts.speak(speech)
        }
      }
      case PREF_RECORD_VIDEO => {
        val record = prefs.getBoolean(key, false)
        if (record) {
          requestWritePermission
        }
      }
      case _ =>
    }
  }

  private def requestWritePermission()
  {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      val hasWritePermission =
        hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
      if (!hasWritePermission) {
        val permissions = new ArrayList[String]
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissions(
          permissions.toArray(
            new Array[String](permissions.size)),
          PERMISSION_REQUEST)
      }
    }
  }
}
