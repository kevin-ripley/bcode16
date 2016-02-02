package team389;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class ExplorationCalculator {

  private static final int EXPLORE_TIMEOUT = 100;
  private static final int FAR_AWAY_DISTANCE = 100;

  private final RobotController rc;
  private final MapBoundaryCalculator mapBoundaryCalculator;
  private final PatrolWaypointCalculator patrolWaypointCalculator;

  private Direction unknownBoundaryDir;
  private MapLocation[] patrolLocs;
  private int patrolLocIndex;
  private int currentExploreTime;

  public ExplorationCalculator(
      RobotController rc,
      MapBoundaryCalculator mapBoundaryCalculator,
      PatrolWaypointCalculator patrolWaypointCalculator) {
    this.rc = rc;
    this.mapBoundaryCalculator = mapBoundaryCalculator;
    this.patrolWaypointCalculator = patrolWaypointCalculator;

    patrolLocs = null;
    patrolLocIndex = -1;
    currentExploreTime = 0;
  }

  public MapLocation calculate() throws GameActionException {
    return mapBoundaryCalculator.allBoundariesKnown()
        ? visitSection(patrolWaypointCalculator)
        : findUnknownBoundaries();
  }

  public void showDebugInfo() throws GameActionException {
    MapLocation target = null;
    if (patrolLocs != null && patrolLocIndex != -1) {
      target = patrolLocs[patrolLocIndex];
    } else if (unknownBoundaryDir != null) {
      target = rc.getLocation().add(unknownBoundaryDir, 2);
    }

    if (target != null) {
      rc.setIndicatorDot(target, 255, 255, 255);
    }
  }

  private MapLocation findUnknownBoundaries() throws GameActionException {
    MapLocation myLoc = rc.getLocation();
    boolean minXKnown = mapBoundaryCalculator.isMinXKnown();
    boolean minYKnown = mapBoundaryCalculator.isMinYKnown();
    boolean maxXKnown = mapBoundaryCalculator.isMaxXKnown();
    boolean maxYKnown = mapBoundaryCalculator.isMaxYKnown();
    if (unknownBoundaryDir != null
        && (shouldChangeDirection(unknownBoundaryDir)
            || currentExploreTime > EXPLORE_TIMEOUT)) {
      unknownBoundaryDir = null;
    }

    if (unknownBoundaryDir != null) {
      currentExploreTime++;
      return myLoc.add(unknownBoundaryDir, FAR_AWAY_DISTANCE);
    }

    Direction[] dirs = possibleDirections(minXKnown, minYKnown, maxXKnown,
        maxYKnown);
    unknownBoundaryDir = dirs[(rc.getID() + rc.getRoundNum()) % (dirs.length)];
    currentExploreTime = 0;
    return myLoc.add(unknownBoundaryDir, FAR_AWAY_DISTANCE);

  }

  private boolean shouldChangeDirection(Direction dir)
      throws GameActionException {
    int range;
    switch (rc.getType()) {
      case ARCHON:
        range = 4;
        break;
      case SCOUT:
        range = 5;
        break;
      case SOLDIER:
      case GUARD:
      case VIPER:
      case TURRET:
      case TTM:
        range = 3;
        break;
      default:
        range = 1;
    }

    MapLocation myLoc = rc.getLocation();
    MapLocation addDx = myLoc.add(dir.dx * range, 0);
    MapLocation addDy = myLoc.add(0, dir.dy * range);
    return rc.canSense(addDx) && !rc.onTheMap(addDx) || rc.canSense(addDy) && !rc.onTheMap(addDy);
  }

  private Direction[] possibleDirections(
      boolean minX, boolean minY, boolean maxX, boolean maxY) {
    if (!minX && !minY && !maxX && !maxY) {
      return new Direction[] {
        Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
        Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST
      };
    } else if (!minX && !minY && !maxX && maxY) {
      return new Direction[] {
        Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.WEST, Direction.NORTH_WEST
      };
    } else if (!minX && !minY && maxX && !maxY) {
      return new Direction[] {
        Direction.NORTH, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST
      };
    } else if (!minX && !minY && maxX && maxY) {
      return new Direction[] {
        Direction.NORTH, Direction.WEST, Direction.NORTH_WEST
      };
    } else if (!minX && minY && !maxX && !maxY) {
      return new Direction[] {
        Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST
      };
    } else if (!minX && minY && !maxX && maxY) {
      return new Direction[] {
        Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH_WEST,
        Direction.WEST, Direction.NORTH_WEST
      };
    } else if (!minX && minY && maxX && !maxY) {
      return new Direction[] {
        Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST
      };
    } else if (!minX && minY && maxX && maxY) {
      return new Direction[] {
        Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST
      };
    } else if (minX && !minY && !maxX && !maxY) {
      return new Direction[] {
        Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH
      };
    } else if (minX && !minY && !maxX && maxY) {
      return new Direction[] {
        Direction.NORTH, Direction.NORTH_EAST, Direction.EAST
      };
    } else if (minX && !minY && maxX && !maxY) {
      return new Direction[] {
        Direction.NORTH, Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.SOUTH,
        Direction.SOUTH_WEST, Direction.NORTH_WEST
      };
    } else if (minX && !minY && maxX && maxY) {
      return new Direction[] {
        Direction.NORTH, Direction.NORTH_EAST, Direction.NORTH_WEST
      };
    } else if (minX && minY && !maxX && !maxY) {
      return new Direction[] {
        Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH
      };
    } else if (minX && minY && !maxX && maxY) {
      return new Direction[] {
        Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST
      };
    } else if (minX && minY && maxX && !maxY) {
      return new Direction[] {
        Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST
      };
    } else {
      return new Direction[] {
        Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
        Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST
      };
    }
  }

  private MapLocation visitSection(PatrolWaypointCalculator patrolWaypointCalculator) {
    if (patrolLocs == null) {
      patrolLocs = patrolWaypointCalculator.calculate(
          mapBoundaryCalculator.getMinX(),
          mapBoundaryCalculator.getMaxX(),
          mapBoundaryCalculator.getMinY(),
          mapBoundaryCalculator.getMaxY());
      patrolLocIndex = 0;
      currentExploreTime = 0;
    }

    if (rc.getLocation().distanceSquaredTo(patrolLocs[patrolLocIndex]) <= 2
        || currentExploreTime > EXPLORE_TIMEOUT) {
      patrolLocIndex = (patrolLocIndex + 1) % patrolLocs.length;
      currentExploreTime = 0;
    }

    currentExploreTime++;
    return patrolLocs[patrolLocIndex];
  }
}
