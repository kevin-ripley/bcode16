package team389;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class NavigationUtil {
  public static Direction[] getForwardAndSideDirections(MapLocation start,
      MapLocation destination) {
    Direction d = start.directionTo(destination);
    return Math.random() < 0.5
        ? new Direction[] {
          d,
          d.rotateRight(),
          d.rotateLeft(),
          d.rotateRight().rotateRight(),
          d.rotateLeft().rotateLeft(),
    }
        : new Direction[] {
          d,
          d.rotateRight(),
          d.rotateLeft(),
          d.rotateRight().rotateRight(),
          d.rotateLeft().rotateLeft(),
    };
  }

  public static Direction[] getForwardDirections(MapLocation start, MapLocation destination) {
    Direction d = start.directionTo(destination);
    return Math.random() < 0.5
        ? new Direction[] {
          d,
          d.rotateRight(),
          d.rotateLeft(),
    }
        : new Direction[] {
          d,
          d.rotateRight(),
          d.rotateLeft(),
    };
  }

  public static Direction[] getAllDirections(MapLocation start, MapLocation destination) {
    Direction d = start.directionTo(destination);
    return Math.random() < 0.5
        ? new Direction[] {
          d,
          d.rotateRight(),
          d.rotateLeft(),
          d.rotateRight().rotateRight(),
          d.rotateLeft().rotateLeft(),
          d.opposite().rotateLeft(),
          d.opposite().rotateRight(),
          d.opposite()
    }
        : new Direction[] {
          d,
          d.rotateRight(),
          d.rotateLeft(),
          d.rotateRight().rotateRight(),
          d.rotateLeft().rotateLeft(),
          d.opposite().rotateLeft(),
          d.opposite().rotateRight(),
          d.opposite()
    };
  }

  public static Direction[] getBackwardsDirections(MapLocation start, MapLocation destination) {
    Direction d = start.directionTo(destination);
    return Math.random() < 0.5
        ? new Direction[] {
          d.rotateRight().rotateRight(),
          d.rotateLeft().rotateLeft(),
          d.opposite().rotateLeft(),
          d.opposite().rotateRight(),
          d.opposite()
    }
        : new Direction[] {
          d.rotateRight().rotateRight(),
          d.rotateLeft().rotateLeft(),
          d.opposite().rotateLeft(),
          d.opposite().rotateRight(),
          d.opposite()
    };
  }

  public static Direction[] getNonDiagonalDirections(Direction dir) {
    return dir.isDiagonal()
        ? new Direction[] {
          dir.rotateLeft(), dir.rotateRight()
    }
        : new Direction[] {
          dir
    };
  }
}
