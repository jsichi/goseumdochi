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

package goseumdochi.common

import akka.actor._
import com.typesafe.config._
import java.util.concurrent._
import scala.concurrent.duration._

class Settings(rootConf : Config, extendedSystem : ExtendedActorSystem)
    extends Extension
{
  private val conf = rootConf.getConfig("goseumdochi")

  private def getMillis(subConf: Config, path : String) =
    TimeSpan(subConf.getDuration(path, TimeUnit.MILLISECONDS), MILLISECONDS)

  object Bluetooth
  {
    val subConf = conf.getConfig("bluetooth")
    val debug = subConf.getBoolean("debug")
  }

  object Sphero
  {
    val subConf = conf.getConfig("sphero")
    val bluetoothId = subConf.getString("bluetooth-id")
  }

  object Vision
  {
    val subConf = conf.getConfig("vision")
    val cameraClass = subConf.getString("camera-class")
    val remoteCameraUrl = subConf.getString("remote-camera-url")
    val throttlePeriod = getMillis(subConf, "throttle-period")
    val sensorDelay = getMillis(subConf, "sensor-delay")
  }

  object Control
  {
    val subConf = conf.getConfig("control")
    val panicDelay = getMillis(subConf, "panic-delay")
    val visibilityCheckFreq = getMillis(subConf, "visibility-check-freq")
  }

  object Motor
  {
    val subConf = conf.getConfig("motor")
    val defaultSpeed = subConf.getDouble("default-speed")
    val fullSpeed = subConf.getDouble("full-speed")
  }

  object Calibration
  {
    val subConf = conf.getConfig("calibration")
    val quietPeriod = getMillis(subConf, "quiet-period")
  }

  object BodyRecognition
  {
    val subConf = conf.getConfig("body-recognition")
    val className = subConf.getString("class-name")
  }

  object MotionDetection
  {
    val subConf = conf.getConfig("motion-detection")
    val fineThreshold = subConf.getInt("fine-threshold")
    val coarseThreshold = subConf.getInt("coarse-threshold")
  }

  object Test
  {
    val subConf = conf.getConfig("test")
    val visualize = subConf.getBoolean("visualize")
  }

  def instantiateObject(className : String) =
    Class.forName(className).getConstructor(this.getClass).newInstance(this)
}

object Settings extends ExtensionId[Settings] with ExtensionIdProvider
{
  override def lookup = Settings

  override def createExtension(system : ExtendedActorSystem) =
    new Settings(system.settings.config, system)

  def apply(context : ActorContext) : Settings = apply(context.system)
}
