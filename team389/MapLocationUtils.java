package team389;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;

public class MapLocationUtils {
  public static int mapLocationToIndex(MapLocation loc) {
    return (loc.x + 16000 + GameConstants.MAP_MAX_HEIGHT * (loc.y + 16000))
        % (GameConstants.MAP_MAX_HEIGHT * GameConstants.MAP_MAX_WIDTH);
  }

  public static MapLocation getClosest(MapLocation loc, MapLocation[] locs) {
    int closestDist = 99999;
    MapLocation closest = null;
    for (int i = locs.length; --i >= 0;) {
      int dist = loc.distanceSquaredTo(locs[i]);
      if (dist < closestDist) {
        closestDist = dist;
        closest = locs[i];
      }
    }

    return closest;
  }

  public static MapLocation[] getFringeSquares(MapLocation loc, int sensorRadiusSquared) {
    MapLocation[] out = null;
    switch (sensorRadiusSquared) {
      case 53: // SCOUT
        out = new MapLocation[] {
          new MapLocation(loc.x + 0, loc.y + 7),
          new MapLocation(loc.x + 1, loc.y + 7),
          new MapLocation(loc.x + 2, loc.y + 7),
          new MapLocation(loc.x + 3, loc.y + 6),
          new MapLocation(loc.x + 4, loc.y + 6),
          new MapLocation(loc.x + 5, loc.y + 5),
          new MapLocation(loc.x + 6, loc.y + 4),
          new MapLocation(loc.x + 6, loc.y + 3),
          new MapLocation(loc.x + 7, loc.y + 2),
          new MapLocation(loc.x + 7, loc.y + 1),
          new MapLocation(loc.x + 7, loc.y + 0),
          new MapLocation(loc.x + 7, loc.y - 1),
          new MapLocation(loc.x + 7, loc.y - 2),
          new MapLocation(loc.x + 6, loc.y - 3),
          new MapLocation(loc.x + 6, loc.y - 4),
          new MapLocation(loc.x + 5, loc.y - 5),
          new MapLocation(loc.x + 4, loc.y - 6),
          new MapLocation(loc.x + 3, loc.y - 6),
          new MapLocation(loc.x + 2, loc.y - 7),
          new MapLocation(loc.x + 1, loc.y - 7),
          new MapLocation(loc.x + 0, loc.y - 7),
          new MapLocation(loc.x - 1, loc.y - 7),
          new MapLocation(loc.x - 2, loc.y - 7),
          new MapLocation(loc.x - 3, loc.y - 6),
          new MapLocation(loc.x - 4, loc.y - 6),
          new MapLocation(loc.x - 5, loc.y - 5),
          new MapLocation(loc.x - 6, loc.y - 4),
          new MapLocation(loc.x - 6, loc.y - 3),
          new MapLocation(loc.x - 7, loc.y - 2),
          new MapLocation(loc.x - 7, loc.y - 1),
          new MapLocation(loc.x - 7, loc.y + 0),
          new MapLocation(loc.x - 7, loc.y + 1),
          new MapLocation(loc.x - 7, loc.y + 2),
          new MapLocation(loc.x - 6, loc.y + 3),
          new MapLocation(loc.x - 6, loc.y + 4),
          new MapLocation(loc.x - 5, loc.y + 5),
          new MapLocation(loc.x - 4, loc.y + 6),
          new MapLocation(loc.x - 3, loc.y + 6),
          new MapLocation(loc.x - 2, loc.y + 7),
          new MapLocation(loc.x - 1, loc.y + 7),
        };
        break;
      case 35: // ARCHON
        out = new MapLocation[] {
          new MapLocation(loc.x + 0, loc.y + 5),
          new MapLocation(loc.x + 1, loc.y + 5),
          new MapLocation(loc.x + 2, loc.y + 5),
          new MapLocation(loc.x + 3, loc.y + 5),
          new MapLocation(loc.x + 4, loc.y + 4),
          new MapLocation(loc.x + 5, loc.y + 3),
          new MapLocation(loc.x + 5, loc.y + 2),
          new MapLocation(loc.x + 5, loc.y + 1),
          new MapLocation(loc.x + 5, loc.y + 0),
          new MapLocation(loc.x + 5, loc.y - 1),
          new MapLocation(loc.x + 5, loc.y - 2),
          new MapLocation(loc.x + 5, loc.y - 3),
          new MapLocation(loc.x + 4, loc.y - 4),
          new MapLocation(loc.x + 3, loc.y - 5),
          new MapLocation(loc.x + 2, loc.y - 5),
          new MapLocation(loc.x + 1, loc.y - 5),
          new MapLocation(loc.x + 0, loc.y - 5),
          new MapLocation(loc.x - 1, loc.y - 5),
          new MapLocation(loc.x - 2, loc.y - 5),
          new MapLocation(loc.x - 3, loc.y - 5),
          new MapLocation(loc.x - 4, loc.y - 4),
          new MapLocation(loc.x - 5, loc.y - 3),
          new MapLocation(loc.x - 5, loc.y - 2),
          new MapLocation(loc.x - 5, loc.y - 1),
          new MapLocation(loc.x - 5, loc.y + 0),
          new MapLocation(loc.x - 5, loc.y + 1),
          new MapLocation(loc.x - 5, loc.y + 2),
          new MapLocation(loc.x - 5, loc.y + 3),
          new MapLocation(loc.x - 4, loc.y + 4),
          new MapLocation(loc.x - 3, loc.y + 5),
          new MapLocation(loc.x - 2, loc.y + 5),
          new MapLocation(loc.x - 1, loc.y + 5),
        };
        break;
      default:
        out = new MapLocation[] {};
        break;
    }
    return out;
  }
}