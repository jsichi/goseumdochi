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

package goseumdochi.vision

import goseumdochi.common._

import scala.math._
import goseumdochi.common.MoreMath._

import org.bytedeco.javacpp.opencv_highgui._
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.helper.opencv_core._
import org.bytedeco.javacpp.opencv_imgproc._
import org.bytedeco.javacv._

trait VideoStream
{
  def beforeNext() {}

  def nextFrame() : (Frame, Long)

  def afterNext() {}

  def quit() {}
}

object LocalVideoStream extends VideoStream
{
  private val frameGrabber = startGrabber

  private def startGrabber() =
  {
    val grabber = new OpenCVFrameGrabber(0)
    grabber.setBitsPerPixel(CV_8U)
    grabber.setImageMode(FrameGrabber.ImageMode.COLOR)
    grabber.start
    grabber
  }

  override def nextFrame() = (frameGrabber.grab, System.currentTimeMillis)

  override def quit()
  {
    frameGrabber.stop
  }
}

class RemoteVideoStream(settings : Settings) extends VideoStream
{
  private var frameGrabber : Option[IPCameraFrameGrabber] = None

  override def beforeNext()
  {
    val grabber = new IPCameraFrameGrabber(settings.Vision.cameraUrl)
    grabber.setBitsPerPixel(CV_8U)
    grabber.setImageMode(FrameGrabber.ImageMode.COLOR)
    grabber.start
    frameGrabber = Some(grabber)
  }

  override def nextFrame() = {
    (frameGrabber.get.grab, System.currentTimeMillis)
  }

  override def afterNext()
  {
    frameGrabber.get.stop
    frameGrabber = None
  }

  override def quit()
  {
  }
}

class PlaybackStream(keyFrames : Seq[(String, Int)], realTime : Boolean = true)
    extends VideoStream
{
  private val circular = Iterator.continually(keyFrames).flatten

  private var simulatedTime = 1000L

  override def nextFrame() =
  {
    val (filename, delayMillis) = circular.next
    val now = {
      if (realTime) {
        Thread.sleep(delayMillis)
        System.currentTimeMillis
      } else {
        simulatedTime += delayMillis
        simulatedTime
      }
    }
    val img = OpenCvUtil.convert(cvLoadImage(filename))
    (img, now)
  }
}