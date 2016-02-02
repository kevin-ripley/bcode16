package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class LeaderArchonBehavior implements Behavior {

  private final RobotController rc;
  private final NavigationSystem navigation;
  private final PickupLocationReporter pickupLocationReporter;

  private MapLocation currentTarget;
  private int currentTargetRound;

  public LeaderArchonBehavior(
      RobotController rc,
      NavigationSystem navigation,
      PickupLocationReporter pickupLocationReporter) {
    this.rc = rc;
    this.navigation = navigation;
    this.pickupLocationReporter = pickupLocationReporter;

    currentTarget = null;
    currentTargetRound = -1;
  }

  @Override
  public void run() throws GameActionException {
    MapLocation target = getTarget();

    if (target != null) {
      rc.setIndicatorString(0, "I'm leading, target is " + target + ".");
      if (!navigation.directTo(target, true /* avoidAttackers */, true /* clearRubble */)) {
        navigation.directTo(target, false /* avoidAttackers */, true /* clearRubble */);
      }
    } else {
      rc.setIndicatorString(0, "I'm a lost leader.");
      navigation.moveRandomly();
    }

    pickupLocationReporter.invalidateRetrievedPickups();
  }

  public MapLocation getTarget() {
    int roundNum = rc.getRoundNum();
    if (currentTargetRound < roundNum) {
      currentTargetRound = roundNum;
      currentTarget = pickupLocationReporter.getClosestPickup(rc.getLocation());
    }
    return currentTarget;
  }
}
