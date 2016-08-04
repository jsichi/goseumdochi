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

package org.goseumdochi.android.leash

import org.goseumdochi.android.lib._

import android.content._
import android.net._
import android.os._
import android.view._

class LeashMainActivity extends MainMenuActivityBase with TypedFindView
{
  override protected def onCreate(savedInstanceState : Bundle)
  {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)
  }

  def onSetupClicked(v : View)
  {
  }

  override protected def startAboutActivity()
  {
    val intent = new Intent(this, classOf[LeashAboutActivity])
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    startActivity(intent)
  }

  override protected def startHelpActivity()
  {
    val uri = Uri.parse(getString(R.string.help_url))
    startActivity(new Intent(Intent.ACTION_VIEW, uri))
  }

  override protected def startBugsActivity()
  {
    val uri = Uri.parse(getString(R.string.bugs_url))
    startActivity(new Intent(Intent.ACTION_VIEW, uri))
  }

  override protected def startSettingsActivity()
  {
    val intent = new Intent(this, classOf[LeashSettingsActivity])
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    startActivity(intent)
  }
}
