package team389;

import battlecode.common.MapLocation;

public interface PatrolWaypointCalculator {

  /**
   * @return Map locations to patrol after knowing the map's dimensions. They are patrolled in
   *     order.
   */
  public MapLocation[] calculate(int minX, int maxX, int minY, int maxY);
}
