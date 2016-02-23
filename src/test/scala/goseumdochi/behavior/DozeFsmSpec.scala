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

package goseumdochi.behavior

import goseumdochi.common._
import goseumdochi.control._
import goseumdochi.vision._

import akka.actor._

import DozeFsm._

class DozeFsmSpec extends AkkaSpecification
{
  "DozeFsm" should
  {
    "catch forty winks" in new AkkaExample
    {
      val fsm = system.actorOf(
        Props(classOf[DozeFsm]))

      val initialPos = PlanarPos(0, 0)

      fsm ! ControlActor.CameraAcquiredMsg
      fsm ! ControlActor.BodyMovedMsg(initialPos, 0)

      expectMsg(ControlActor.ActuateLight(java.awt.Color.GREEN))
      expectMsg(ControlActor.ActuateLight(java.awt.Color.BLUE))
    }
  }
}