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

import org.bytedeco.javacpp.opencv_highgui._

class BodyDetectorSpec extends VisualizableSpecification
{
  // body detectors are mutable, so we need isolation
  isolated

  private val roundBodyDetector =
    new RoundBodyDetector(settings, FlipRetinalTransform)
  private val flashyBodyDetector =
    new FlashyBodyDetector(settings, FlipRetinalTransform)


  "BodyDetector" should
  {
    "detect nothing" in
    {
      val hintPos = PlanarPos(500, 500)
      val img = cvLoadImage("data/empty.jpg")
      val gray = OpenCvUtil.grayscale(img)
      val posOpt = roundBodyDetector.detectBody(img, gray, hintPos)
      posOpt must beEmpty
    }

    "detect round body with useless hint" in
    {
      val hintPos = PlanarPos(0, 0)
      val img = cvLoadImage("data/table1.jpg")
      val gray = OpenCvUtil.grayscale(img)
      val posOpt = roundBodyDetector.detectBody(img, gray, hintPos)
      posOpt must not beEmpty

      val pos = posOpt.get
      visualize(img, pos)

      pos.x must be closeTo(563.0 +/- 0.1)
      pos.y must be closeTo(-451.0 +/- 0.1)
    }

    "detect round body with good hint" in
    {
      val img = cvLoadImage("data/baseline1.jpg")
      val gray = OpenCvUtil.grayscale(img)
      val hintPos = PlanarPos(500, -500)
      val posOpt = roundBodyDetector.detectBody(img, gray, hintPos)
      posOpt must not beEmpty

      val pos = posOpt.get
      visualize(img, pos)

      pos.x must be closeTo(569.0 +/- 0.1)
      pos.y must be closeTo(-471.0 +/- 0.1)
    }

    "detect flashy body" in
    {
      val img1 = cvLoadImage("data/blinkoff.jpg")
      val img2 = cvLoadImage("data/blinkorange.jpg")
      val gray1 = OpenCvUtil.grayscale(img1)
      val gray2 = OpenCvUtil.grayscale(img2)
      val msgOpt = flashyBodyDetector.analyzeFrame(
        img2, img1, gray2, gray1, TimePoint.ZERO, None)
      msgOpt must not beEmpty
      val pos = msgOpt.head.pos
      visualize(img2, pos)

      pos.x must be closeTo(267.5 +/- 0.1)
      pos.y must be closeTo(-363.0 +/- 0.1)
    }

    "find circles for round body background elimination" in
    {
      val img1 = cvLoadImage("data/circles1.jpg")
      val gray1 = OpenCvUtil.grayscale(img1)
      val circles = roundBodyDetector.findCircles(gray1)

      if (shouldVisualize) {
        roundBodyDetector.visualizeCircles(img1, circles)
        visualize(img1)
      }

      circles.size must be equalTo 28
    }

    "detect round body after background elimination" in
    {
      val img1 = cvLoadImage("data/circles1.jpg")
      val gray1 = OpenCvUtil.grayscale(img1)
      val img2 = cvLoadImage("data/circles2.jpg")
      val gray2 = OpenCvUtil.grayscale(img2)
      val hintPos = PlanarPos(500, -500)

      roundBodyDetector.findBackgroundCircles(gray1)

      // img1 should show no body since all circles are considered background
      roundBodyDetector.detectBody(img1, gray1, hintPos) must beEmpty

      // but img2 should have something
      val posOpt = roundBodyDetector.detectBody(img2, gray2, hintPos)
      posOpt must not beEmpty

      val pos = posOpt.get

      val circles = roundBodyDetector.findCircles(gray2)
      if (shouldVisualize) {
        roundBodyDetector.visualizeCircles(img2, circles)
        visualize(img2, pos)
      }

      // even though we've already done background elimination,
      // multiple circles still show up due to sensitivity
      // in the algorithm, so from there we narrow it down
      // via hint position
      circles.size must be equalTo 6

      pos.x must be closeTo(363.0 +/- 0.1)
      pos.y must be closeTo(-539.0 +/- 0.1)
    }
  }
}
