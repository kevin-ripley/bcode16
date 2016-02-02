package team389;

import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class RadarUtils {

  public static RobotInfo getClosestRobot(RobotInfo[] robots, MapLocation loc) {
    int closestDist = 99999;
    RobotInfo closest = null;
    for (int i = robots.length; --i >= 0;) {
      RobotInfo robot = robots[i];
      int dist = robot.location.distanceSquaredTo(loc);
      if (dist < closestDist) {
        closestDist = dist;
        closest = robot;
      }
    }
    return closest;
  }
}
