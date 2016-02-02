package team389;

import battlecode.common.MapLocation;

public class LawnMowerPatrolWaypointCalculator implements PatrolWaypointCalculator {

  private final int laneHalfWidth;
  private final int mapBoundaryMargin;

  public LawnMowerPatrolWaypointCalculator(int laneHalfWidth, int mapBoundaryMargin) {
    this.laneHalfWidth = laneHalfWidth;
    this.mapBoundaryMargin = mapBoundaryMargin;
  }

  @Override
  public MapLocation[] calculate(int minX, int maxX, int minY, int maxY) {
    int numLanes = ((maxY - minY - 2 * mapBoundaryMargin + (2 * laneHalfWidth - 1)) / (2
        * laneHalfWidth)) + 1;
    MapLocation[] waypoints = new MapLocation[2 * numLanes];
    for (int lane = 0; lane < numLanes; lane++) {
      int firstX = lane % 2 == 0 ? minX + mapBoundaryMargin : maxX - mapBoundaryMargin;
      int lastX = lane % 2 == 0 ? maxX - mapBoundaryMargin : minX + mapBoundaryMargin;
      int y = (lane == numLanes - 1)
          ? maxY - mapBoundaryMargin
          : minY + mapBoundaryMargin + (2 * lane * laneHalfWidth);
      waypoints[2 * lane] = new MapLocation(firstX, y);
      waypoints[(2 * lane) + 1] = new MapLocation(lastX, y);
    }

    return waypoints;
  }
}
