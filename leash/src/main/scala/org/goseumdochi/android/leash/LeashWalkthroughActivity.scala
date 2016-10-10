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
import android.graphics.drawable._
import android.os._
import android.preference._
import android.view._

class LeashWalkthroughActivity
    extends PrerequisitesActivityBase
    with TypedFindView
    with View.OnTouchListener
{
  private var x1 = 0f

  private var iFrame = 0

  private lazy val animation = loadAnimation

  private lazy val textArray =
    getResources.getStringArray(R.array.walkthrough_text)

  private lazy val textView = findView(TR.walkthrough_text)

  private lazy val hintView = findView(TR.walkthrough_hint)

  private lazy val buttonView = findView(TR.close_walkthrough_button)

  override def onTouch(v : View, event : MotionEvent) : Boolean =
  {
    event.getAction match {
      case MotionEvent.ACTION_DOWN => {
        x1 = event.getX
        true
      }
      case MotionEvent.ACTION_UP => {
        val x2 = event.getX
        val delta = x2 - x1
        if (Math.abs(delta) > 100) {
          if (delta < 0) {
            // swipe right
            if (iFrame < (animation.getNumberOfFrames - 1)) {
              iFrame += 1
              updateFrame
            }
          } else {
            // swipe left
            if (iFrame > 0) {
              iFrame -= 1
              updateFrame
            }
          }
        }
        true
      }
      case _ =>
        false
    }
  }

  private def updateFrame()
  {
    animation.selectDrawable(iFrame)
    textView.setText(textArray(iFrame))
    if (iFrame >= (animation.getNumberOfFrames - 1)) {
      buttonView.setVisibility(View.VISIBLE)
      hintView.setVisibility(View.INVISIBLE)
    } else {
      buttonView.setVisibility(View.INVISIBLE)
      hintView.setVisibility(View.VISIBLE)
    }
    LeashAnalytics.trackScreen("Walkthrough " + iFrame)
  }

  private def loadAnimation =
  {
    val img = findView(TR.walkthrough_animation_image)
    img.setBackgroundResource(R.drawable.walkthrough_animation)
    img.setOnTouchListener(this)
    img.getBackground.asInstanceOf[AnimationDrawable]
  }

  override protected def onCreate(savedInstanceState : Bundle)
  {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.walkthrough)
  }

  override protected def onResume()
  {
    super.onResume
    updateFrame
  }

  override def onStartClicked(v : View)
  {
    if (iFrame >= (animation.getNumberOfFrames - 1)) {
      super.onStartClicked(v)
    } else {
      iFrame += 1
      updateFrame
    }
  }

  override protected def startNextActivity()
  {
    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
    val editor = prefs.edit
    editor.putBoolean(LeashSettingsActivity.PREF_WALKTHROUGH, true)
    editor.apply
    val intent = new Intent(this, classOf[LeashControlActivity])
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    finish
    startActivity(intent)
  }
}