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

// nearCenter corresponds to world origin
// farRight corresponds to (worldDist, 0) in world coordinates

case class Perspective(
  nearCenter : RetinalPos,
  farCenter : RetinalPos,
  nearRight : RetinalPos,
  farRight : RetinalPos,
  worldDist : Double)
{
  // from https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection
  private def intersection(
    lineA : (RetinalPos, RetinalPos),
    lineB : (RetinalPos, RetinalPos)) =
  {
    val x1 = lineA._1.x
    val y1 = lineA._1.y
    val x2 = lineA._2.x
    val y2 = lineA._2.y
    val x3 = lineB._1.x
    val y3 = lineB._1.y
    val x4 = lineB._2.x
    val y4 = lineB._2.y
    val denom = ((x1 - x2)*(y3 - y4)) - ((y1 - y2)*(x3 - x4))
    val n1 = x1*y2 - y1*x2
    val n2 = x3*y4 - y3*x4
    val x = (n1*(x3 - x4) - n2*(x1 - x2)) / denom
    val y = (n1*(y3 - y4) - n2*(y1 - y2)) / denom
    RetinalPos(x, y)
  }

  def horizontalScale = worldDist / (nearRight.x - nearCenter.x)

  def vanishingPoint : RetinalPos =
    intersection((nearCenter, farCenter), (nearRight, farRight))

  def nearProjection(p : RetinalPos) : RetinalPos =
    intersection((nearCenter, nearRight), (vanishingPoint, p))

  def verticalFar = (farCenter.y - vanishingPoint.y)

  def verticalNear = (nearCenter.y - vanishingPoint.y)

  def verticalB = worldDist*verticalFar / (verticalNear - verticalFar)

  def verticalA = verticalB*verticalNear

  def retinaToWorld(p : RetinalPos) : PlanarPos =
  {
    val np = nearProjection(p)
    val x = horizontalScale * (np.x - nearCenter.x)
    val y = verticalA/(p.y - vanishingPoint.y) - verticalB
    PlanarPos(x, y)
  }

  def worldToRetina(p : PlanarPos) : RetinalPos =
  {
    val y = vanishingPoint.y + verticalA / (verticalB + p.y)
    val yCenter = RetinalPos(nearCenter.x, y)
    val yRight = RetinalPos(nearRight.x, y)
    val xProj = RetinalPos(nearCenter.x + (p.x / horizontalScale), nearCenter.y)
    intersection((vanishingPoint, xProj),(yCenter, yRight))
  }
}
