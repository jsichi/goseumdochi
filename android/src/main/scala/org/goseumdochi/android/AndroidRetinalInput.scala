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

import java.util.concurrent._

import org.bytedeco.javacv._
import org.bytedeco.javacpp.opencv_imgproc._

import org.goseumdochi.common._
import org.goseumdochi.vision._

class AndroidRetinalInput extends RetinalInput
{
  private val inputQueue =
    new ArrayBlockingQueue[(Frame, TimePoint)](1)

  override def nextFrame() =
  {
    inputQueue.take
  }

  override def frameToImage(frame : Frame) =
  {
    val img = super.frameToImage(frame)
    cvCvtColor(img, img, COLOR_RGBA2BGRA)
    img
  }

  def isReady() : Boolean =
  {
    !inputQueue.isEmpty
  }

  def needsFrame() : Boolean =
  {
    inputQueue.remainingCapacity > 0
  }

  def pushFrame(frame : Frame)
  {
    inputQueue.put((frame, TimePoint.now))
  }
}
