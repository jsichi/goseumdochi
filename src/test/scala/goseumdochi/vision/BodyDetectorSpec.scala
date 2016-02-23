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

import org.bytedeco.javacpp.opencv_highgui._

import collection._

import org.specs2.mutable._
import org.specs2.specification._
import org.specs2.execute._

class BodyDetectorSpec extends VisualizableSpecification("simulation.conf")
{
  private val bodyDetector = new RoundBodyDetector(settings)

  "BodyDetector" should
  {
    "detect nothing" in
    {
      val img = cvLoadImage("data/empty.jpg")
      val gray = OpenCvUtil.grayscale(img)
      val posOpt = bodyDetector.detectBody(img, gray)
      posOpt must beEmpty
    }

    "detect round body" in
    {
      val img = cvLoadImage("data/table1.jpg")
      val gray = OpenCvUtil.grayscale(img)
      val posOpt = bodyDetector.detectBody(img, gray)
      posOpt must not beEmpty

      val pos = posOpt.get
      visualize(img, pos)

      pos.x must be closeTo(563.0 +/- 0.1)
      pos.y must be closeTo(451.0 +/- 0.1)
    }
  }
}