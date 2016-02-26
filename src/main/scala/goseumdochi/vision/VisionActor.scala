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

import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacv._

import akka.actor._
import akka.routing._

import scala.concurrent.duration._

object VisionActor
{
  // sent messages
  final case class DimensionsKnownMsg(corner : PlanarPos)
  trait ObjDetectedMsg { def eventTime : Long }

  // internal messages
  final case class GrabFrameMsg(lastTime : Long)

  // received messages
  final case class ActivateAnalyzersMsg(
    analyzerClassNames : Seq[String])
  final case class HintBodyLocationMsg(pos : PlanarPos)
}
import VisionActor._

class VisionActor(videoStream : VideoStream)
    extends Actor with Listeners
{
  private val settings = Settings(context)

  private val throttlePeriod = settings.Vision.throttlePeriod

  private val canvas = initCanvas()

  private var analyzers : Seq[VisionAnalyzer] = Seq.empty

  private var lastGray : Option[IplImage] = None

  private var cornerSeen = false

  private var hintBodyPos : Option[PlanarPos] = None

  def receive =
  {
    case GrabFrameMsg(lastTime) => {
      if (canvas.waitKey(-1) != null) {
        videoStream.quit()
      }
      grabOne()
      val thisTime = System.currentTimeMillis
      val delay = throttlePeriod.toLong - (thisTime - lastTime)
      if (delay <= 0) {
        self ! GrabFrameMsg(thisTime)
      } else {
        import context.dispatcher
        context.system.scheduler.scheduleOnce(Duration(delay, MILLISECONDS)) {
          self ! GrabFrameMsg(thisTime)
        }
      }
    }
    case ActivateAnalyzersMsg(analyzerClassNames) => {
      analyzers = analyzerClassNames.map(
        settings.instantiateObject(_).asInstanceOf[VisionAnalyzer])
    }
    case HintBodyLocationMsg(pos) => {
      hintBodyPos = Some(pos)
    }
    case m : Any => {
      listenerManagement(m)
    }
  }

  private def initCanvas() =
  {
    val canvas = new CanvasFrame("Webcam")
    canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)
    /*
    canvas.getCanvas.addMouseListener(new MouseAdapter {
      override def mouseClicked(e : MouseEvent) {
        gossip(
          MotionDetectedMsg(
            PlanarPos(e.getX, e.getY), System.currentTimeMillis))
      }
    })
     */
    canvas
  }

  private def analyzeFrame(img : IplImage, now : Long)
  {
    val gray = OpenCvUtil.grayscale(img)

    lastGray.foreach(
      prevGray => {
        analyzers.map(
          analyzer => {
            analyzer.analyzeFrame(img, gray, prevGray, now, hintBodyPos).
              foreach(msg => {
                msg match {
                  case BodyDetector.BodyDetectedMsg(pos, _) => {
                    hintBodyPos = Some(pos)
                  }
                  case _ => {}
                }
                gossip(msg)
              }
            )
          }
        )
        prevGray.release
      }
    )
    lastGray = Some(gray)
  }

  private def grabOne()
  {
    try {
      videoStream.beforeNext()
      val (orig, now) = videoStream.nextFrame()
      val img = OpenCvUtil.convert(orig)
      if (!cornerSeen) {
        val corner = PlanarPos(img.width, img.height)
        gossip(DimensionsKnownMsg(corner))
        cornerSeen = true
      }
      analyzeFrame(img, now)
      val converted = OpenCvUtil.convert(img)
      canvas.showImage(converted)
      img.release
    } catch {
      case ex : Throwable => {
        ex.printStackTrace
      }
    } finally {
      videoStream.afterNext()
    }
  }

  override def preStart()
  {
    self ! GrabFrameMsg(0L)
  }
}
