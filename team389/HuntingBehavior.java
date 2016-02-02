package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class HuntingBehavior implements Behavior {

  private final RobotController rc;
  private final NavigationSystem navigation;
  private final ZombieDenReporter zombieDenReporter;

  private MapLocation currentTarget;
  private int currentTargetRound;

  public HuntingBehavior(
      RobotController rc,
      NavigationSystem navigation,
      ZombieDenReporter zombieDenReporter) {
    this.rc = rc;
    this.navigation = navigation;
    this.zombieDenReporter = zombieDenReporter;
    currentTarget = null;
    currentTargetRound = -1;
  }

  @Override
  public void run() throws GameActionException {
    MapLocation target = getTarget();
    if (target != null) {
      rc.setIndicatorString(0, "I'm hunting " + target + ".");
      boolean clearRubble = rc.getType() != RobotType.TTM && rc.getType() != RobotType.SCOUT;
      if (!navigation.directTo(target, true /* avoidAttackers */, clearRubble)) {
        navigation.directTo(target, false /* avoidAttackers */, clearRubble);
      }
    } else {
      rc.setIndicatorString(0, "I'm a lost hunter.");
      navigation.moveRandomly();
    }
  }

  public MapLocation getTarget() {
    int currentRound = rc.getRoundNum();
    if (currentTargetRound >= currentRound) {
      return currentTarget;
    }

    currentTargetRound = currentRound;
    MapLocation myLoc = rc.getLocation();
    currentTarget = zombieDenReporter.getClosestDen(myLoc, null);
    return currentTarget;
  }
}
