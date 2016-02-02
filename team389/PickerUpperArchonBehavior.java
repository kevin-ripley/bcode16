package team389;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class PickerUpperArchonBehavior implements Behavior {

  private final int PICKUP_TIMEOUT = 200;

  private final RobotController rc;
  private final NavigationSystem navigation;
  private final MapLocationIntMap timeoutMap;

  private MapLocation currentTarget;
  private int currentTargetRound;

  public PickerUpperArchonBehavior(RobotController rc, NavigationSystem navigation) {
    this.rc = rc;
    this.navigation = navigation;
    timeoutMap = new ArrayMapLocationIntMap();

    currentTarget = null;
    currentTargetRound = -1;
  }

  @Override
  public void run() throws GameActionException {
    if (rc.isCoreReady()) {
      MapLocation target = getTarget();
      if (target != null) {
        timeoutMap.increment(target);
        rc.setIndicatorString(0, "I'm picking up neutrals or parts at " + target
            + ". Been doing this for " + timeoutMap.getValue(target) + " rounds.");
        if (rc.isCoreReady() && rc.getLocation().isAdjacentTo(target)) {
          RobotInfo robot = rc.senseRobotAtLocation(target);
          if (robot != null && robot.team == Team.NEUTRAL) {
            rc.activate(target);
            return;
          }
        }
        if (!navigation.directTo(target, true /* avoidAttackers */, true /* clearRubble */)) {
          navigation.directTo(target, false /* avoidAttackers */, true /* clearRubble */);
        }
      } else {
        rc.setIndicatorString(0, "I'm a lost picker-upper.");
        navigation.moveRandomly();
      }
    }
  }

  public MapLocation getTarget() {
    int roundNum = rc.getRoundNum();
    if (currentTargetRound >= roundNum) {
      return currentTarget;
    }

    currentTargetRound = roundNum;
    currentTarget = findBestNeutralRobot();
    if (currentTarget != null) {
      return currentTarget;
    }

    currentTarget = findBestParts();
    return currentTarget;
  }

  private MapLocation findBestNeutralRobot() {
    RobotInfo[] neutrals = rc.senseNearbyRobots(
        rc.getLocation(), rc.getType().sensorRadiusSquared, Team.NEUTRAL);
    int lowestScore = 99999;
    MapLocation bestNeutral = null;
    for (int i = neutrals.length; --i >= 0;) {
      MapLocation loc = neutrals[i].location;
      if (timeoutMap.getValue(loc) >= PICKUP_TIMEOUT) {
        continue;
      }
      int score = neutrals[i].type == RobotType.ARCHON
          ? 0
          : loc.distanceSquaredTo(rc.getLocation());
      if (score < lowestScore) {
        lowestScore = score;
        bestNeutral = loc;
      }
    }
    return bestNeutral;
  }

  private MapLocation findBestParts() {
    MapLocation[] parts = rc.sensePartLocations(rc.getType().sensorRadiusSquared);
    int lowestScore = 99999;
    MapLocation bestParts = null;
    for (int i = parts.length; --i >= 0;) {
      MapLocation loc = parts[i];
      if (timeoutMap.getValue(loc) >= PICKUP_TIMEOUT) {
        continue;
      }
      int dist = loc.distanceSquaredTo(rc.getLocation());
      int score = rc.senseRubble(loc) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH
          ? 10000 + dist
          : dist;
      if (score < lowestScore) {
        lowestScore = score;
        bestParts = loc;
      }
    }
    return bestParts;
  }
}
