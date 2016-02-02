package team389;

import battlecode.common.MapLocation;

public class ReflectingPatrolWaypointCalculator implements PatrolWaypointCalculator {

  private final PatrolWaypointCalculator delegate;
  private final boolean flipX;
  private final boolean flipY;

  public ReflectingPatrolWaypointCalculator(
      PatrolWaypointCalculator delegate, boolean flipX, boolean flipY) {
    this.delegate = delegate;
    this.flipX = flipX;
    this.flipY = flipY;
  }

  @Override
  public MapLocation[] calculate(int minX, int maxX, int minY, int maxY) {
    MapLocation[] delegateWaypoints = delegate.calculate(minX, maxX, minY, maxY);
    if (!flipX && !flipY) {
      return delegateWaypoints;
    }

    MapLocation[] waypoints = new MapLocation[delegateWaypoints.length];
    for (int i = 0; i < delegateWaypoints.length; i++) {
      MapLocation loc = delegateWaypoints[i];
      waypoints[i] = new MapLocation(
          flipX ? minX + maxX - loc.x : loc.x,
          flipY ? minY + maxY - loc.y : loc.y);
    }

    return waypoints;
  }
}
