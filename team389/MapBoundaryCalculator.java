package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class MapBoundaryCalculator {

  private final RobotController rc;

  private int minX;
  private int maxX;
  private int minY;
  private int maxY;

  private boolean minXKnown;
  private boolean maxXKnown;
  private boolean minYKnown;
  private boolean maxYKnown;

  public MapBoundaryCalculator(RobotController rc) {
    this.rc = rc;

    minX = 0;
    maxX = 0;
    minY = 0;
    maxY = 0;
    minXKnown = false;
    maxXKnown = false;
    minYKnown = false;
    maxYKnown = false;
  }

  public void update() throws GameActionException {
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
    if (!minXKnown && !rc.onTheMap(myLoc.add(-range, 0))) {
      reportMinX(myLoc.x - range);
    }
    if (!minYKnown && !rc.onTheMap(myLoc.add(0, -range))) {
      reportMinY(myLoc.y - range);
    }
    if (!maxXKnown && !rc.onTheMap(myLoc.add(range, 0))) {
      reportMaxX(myLoc.x + range);
    }
    if (!maxYKnown && !rc.onTheMap(myLoc.add(0, range))) {
      reportMaxY(myLoc.y + range);
    }
  }

  public void reportMinX(int minX) {
    this.minX = minX;
    minXKnown = true;
  }

  public void reportMaxX(int maxX) {
    this.maxX = maxX;
    maxXKnown = true;
  }

  public void reportMinY(int minY) {
    this.minY = minY;
    minYKnown = true;
  }

  public void reportMaxY(int maxY) {
    this.maxY = maxY;
    maxYKnown = true;
  }

  public boolean isMinXKnown() {
    return minXKnown;
  }

  public boolean isMaxXKnown() {
    return maxXKnown;
  }

  public boolean isMinYKnown() {
    return minYKnown;
  }

  public boolean isMaxYKnown() {
    return maxYKnown;
  }

  public boolean allBoundariesKnown() {
    return minXKnown && minYKnown && maxXKnown && maxYKnown;
  }

  public int getMinX() {
    return minX;
  }

  public int getMaxX() {
    return maxX;
  }

  public int getMinY() {
    return minY;
  }

  public int getMaxY() {
    return maxY;
  }
}
