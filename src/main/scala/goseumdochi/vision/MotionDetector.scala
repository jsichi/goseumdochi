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

import org.bytedeco.javacpp._
import org.bytedeco.javacpp.opencv_highgui._
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.helper.opencv_core._
import org.bytedeco.javacpp.opencv_imgproc._
import org.bytedeco.javacv._

import collection._

object MotionDetector
{
  // result messages
  final case class MotionDetectedMsg(pos : PlanarPos, eventTime : Long)
      extends VisionActor.ObjDetectedMsg
}

import MotionDetector._

class MotionDetector(val settings : Settings) extends VisionAnalyzer
{
  override def analyzeFrame(
    img : IplImage, gray : IplImage, prevGray : IplImage, now : Long) =
  {
    detectIntruder(prevGray, gray).map(
      pos => {
        val center = OpenCvUtil.point(pos)
        cvCircle(img, center, 2, AbstractCvScalar.BLUE, 6, CV_AA, 0)
        MotionDetectedMsg(pos, now)
      }
    )
  }

  def detectIntruder(beforeImg : IplImage, afterImg : IplImage)
      : Option[PlanarPos] =
  {
    val diff = AbstractIplImage.create(
      beforeImg.width, beforeImg.height, IPL_DEPTH_8U, 1)

    val storage = AbstractCvMemStorage.create
    cvClearMemStorage(storage)
    cvAbsDiff(afterImg, beforeImg, diff)
    cvThreshold(diff, diff, 64, 255, CV_THRESH_BINARY)

    var contour = new CvSeq(null)
    cvFindContours(diff, storage, contour)

    try {
      while (contour != null && !contour.isNull) {
        if (contour.elem_size > 0) {
          val box = cvMinAreaRect2(contour, storage);
          if (box != null) {
            val size = box.size
            if ((size.width > 40) && (size.height > 40)) {
              val center = box.center
              return Some(PlanarPos(center.x, center.y))
            }
          }
        }
        contour = contour.h_next();
      }
      return None
    } finally {
      diff.release
      storage.release
    }
  }
}