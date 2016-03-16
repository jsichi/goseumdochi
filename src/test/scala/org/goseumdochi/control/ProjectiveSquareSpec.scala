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

package org.goseumdochi.control

import org.goseumdochi.common._
import org.goseumdochi.vision._
import org.goseumdochi.behavior._

import akka.actor._

import scala.math._
import MoreMath._

import scala.concurrent.duration._

class ProjectiveSquareSpec extends AkkaSpecification("square-test.conf")
{
  "ControlActor with ProjectiveOrientationFsm with SquareFsm" should
  {
    "go round in squares" in new AkkaExample
    {
      val actuator = new TestActuator(system, true)
      val controlActor = system.actorOf(
        Props(
          classOf[ControlActor],
          actuator,
          Props(classOf[NullActor]),
          false),
        ControlActor.CONTROL_ACTOR_NAME)

      val zeroTime = TimePoint.ZERO

      val initialPos = PlanarPos(25.0, 10.0)
      val initialTime = zeroTime + 1.second
      val corner = RetinalPos(100.0, 100.0)

      val bodyFoundTime = zeroTime + 10.seconds

      val orientationPos = PlanarPos(50.0, 20.0)
      val orientationTime = zeroTime + 17.seconds

      val backswingPos = PlanarPos(25.0, 20.0)
      val backswingTime = zeroTime + 25.seconds

      val alignedPos = PlanarPos(50.0, 20.0)
      val alignedTime = zeroTime + 30.seconds

      val startPos = PlanarPos(50.0, 10.0)
      val startTime = zeroTime + 35.seconds
      val twirlTime = zeroTime + 50.seconds

      val nextPos = PlanarPos(60.0, 10.0)
      val nextTime = zeroTime + 55.seconds

      controlActor ! VisionActor.DimensionsKnownMsg(corner, initialTime)

      val backwardImpulse = actuator.expectImpulse
      backwardImpulse must be equalTo(PolarImpulse(0.2, 800.milliseconds, PI))

      controlActor ! VisionActor.HintBodyLocationMsg(initialPos, initialTime)

      expectQuiet

      controlActor ! MotionDetector.MotionDetectedMsg(initialPos, initialTime)
      controlActor ! BodyDetector.BodyDetectedMsg(initialPos, bodyFoundTime)

      val firstImpulse = actuator.expectImpulse
      firstImpulse must be equalTo(
        PolarImpulse(0.2, 1500.milliseconds, 0.0))

      controlActor !
        BodyDetector.BodyDetectedMsg(orientationPos, orientationTime)

      val secondImpulse = actuator.expectImpulse
      secondImpulse.theta must be closeTo(2.83 +/- 0.01)

      controlActor !
        BodyDetector.BodyDetectedMsg(backswingPos, backswingTime)

      val thirdImpulse = actuator.expectImpulse
      thirdImpulse.theta must be closeTo(-0.30 +/- 0.01)

      controlActor !
        BodyDetector.BodyDetectedMsg(alignedPos, alignedTime)

      val fourthImpulse = actuator.expectImpulse
      fourthImpulse.theta must be closeTo(-1.87 +/- 0.01)

      controlActor !
        BodyDetector.BodyDetectedMsg(startPos, startTime)

      actuator.expectTwirlMsg.theta must be closeTo(-0.30 +/- 0.01)

      val centeringImpulse = actuator.expectImpulse
      centeringImpulse.speed must be equalTo 0.2
      centeringImpulse.duration must be equalTo 6.seconds
      centeringImpulse.theta must be closeTo(1.57 +/- 0.01)

      controlActor !
        BodyDetector.BodyDetectedMsg(startPos, twirlTime)

      val fifthImpulse = actuator.expectImpulse
      fifthImpulse.theta must be closeTo(0.0 +/- 0.01)

      controlActor !
        BodyDetector.BodyDetectedMsg(nextPos, nextTime)

      val sixthImpulse = actuator.expectImpulse
      sixthImpulse.theta must be closeTo(1.57 +/- 0.01)
    }
  }
}
