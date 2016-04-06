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

package org.goseumdochi.vision

import org.goseumdochi.common._

import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.helper.opencv_core._
import org.bytedeco.javacpp.opencv_imgproc._

import MoreMath._

import collection._
import util._

object BodyDetector
{
  // result messages
  final case class BodyDetectedMsg(pos : PlanarPos, eventTime : TimePoint)
      extends VisionActor.ObjDetectedMsg
}

import BodyDetector._

trait BodyDetector extends VisionAnalyzer
{
  protected val conf = settings.BodyRecognition.subConf
}

class FlashyBodyDetector(
  val settings : Settings, val xform : RetinalTransform)
    extends BodyDetector
{
  class BodyMotionDetector extends MotionDetector(
    settings, xform, settings.MotionDetection.bodyThreshold, true)

  private val motionDetector = new BodyMotionDetector

  override def analyzeFrame(
    img : IplImage, prevImg : IplImage, gray : IplImage, prevGray : IplImage,
    frameTime : TimePoint, hintBodyPos : Option[PlanarPos]) =
  {
    motionDetector.detectMotion(prevGray, gray).map(
      pos => {
        BodyDetectedMsg(pos, frameTime)
      }
    )
  }
}

// use integers to allow for hash-based filtering (which is tricky with doubles)
case class RetinalCircle(
  centerX : Int,
  centerY : Int,
  radius : Int)

class RoundBodyDetector(
  val settings : Settings, val xform : RetinalTransform)
    extends BodyDetector
{
  private val sensitivity = conf.getInt("sensitivity")

  private var minRadius = conf.getInt("min-radius")

  private var maxRadius = conf.getInt("max-radius")

  private val filteredCircles = new mutable.LinkedHashSet[RetinalCircle]

  override def analyzeFrame(
    img : IplImage, prevImg : IplImage, gray : IplImage, prevGray : IplImage,
    frameTime : TimePoint, hintBodyPos : Option[PlanarPos]) =
  {
    hintBodyPos match {
      case Some(hintPos) => {
        detectBody(img, gray, hintPos).map(
          pos => {
            BodyDetectedMsg(pos, frameTime)
          }
        )
      }
      case _ => {
        findBackgroundCircles(gray)
        Iterable.empty
      }
    }
  }

  private[vision] def findBackgroundCircles(gray : IplImage)
  {
    filteredCircles ++= findCircles(gray)
  }

  private[vision] def detectBody(
    img : IplImage, gray : IplImage, hintBodyPos : PlanarPos)
      : Option[PlanarPos] =
  {
    val circles = findCircles(gray)

    val expectedPos = xform.worldToRetina(hintBodyPos)
    val expectedCircle = RetinalCircle(
      Math.round(expectedPos.x).toInt, Math.round(expectedPos.y).toInt, 0)

    def metric(c : RetinalCircle) =
    {
      val dx = c.centerX - expectedCircle.centerX
      val dy = c.centerY - expectedCircle.centerY
      sqr(dx) + sqr(dy)
    }
    val newCircles = circles -- filteredCircles
    filteredCircles ++= newCircles
    Try(newCircles.minBy(metric)) match {
      case Success(c : RetinalCircle) => {
        filteredCircles -= c
        visualizeCircles(img, Iterable(c))
        minRadius = c.radius - 8
        if (minRadius < 1) {
          minRadius = 1
        }
        maxRadius = c.radius + 8
        Some(xform.retinaToWorld(RetinalPos(c.centerX, c.centerY)))
      }
      case _ => {
        None
      }
    }
  }

  private[vision] def findCircles(gray : IplImage) : Set[RetinalCircle] =
  {
    val mem = AbstractCvMemStorage.create
    try {
      val circles = cvHoughCircles(
        gray,
        mem,
        CV_HOUGH_GRADIENT,
        2,
        50,
        100,
        sensitivity,
        minRadius,
        maxRadius)

      (0 until circles.total).map(
        i => {
          val circle = new CvPoint3D32f(cvGetSeqElem(circles, i))
          RetinalCircle(
            Math.round(circle.x), Math.round(circle.y), Math.round(circle.z))
        }
      ).toSet
    } finally {
      mem.release
    }
  }

  private[vision] def visualizeCircles(
    img : IplImage, circles : Iterable[RetinalCircle])
  {
    circles.foreach(c => {
      val point = new CvPoint2D32f
      point.x(c.centerX.toFloat)
      point.y(c.centerY.toFloat)
      val center = cvPointFrom32f(point)
      cvCircle(img, center, c.radius, AbstractCvScalar.RED, 6, CV_AA, 0)
    })
  }
}
