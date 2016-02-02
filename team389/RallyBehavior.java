package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class RallyBehavior implements Behavior {

  private final RobotController rc;
  private final ArmyRally armyRally;
  private final NavigationSystem navigation;

  public RallyBehavior(RobotController rc, ArmyRally armyRally, NavigationSystem navigation) {
    this.rc = rc;
    this.armyRally = armyRally;
    this.navigation = navigation;
  }

  @Override
  public void run() throws GameActionException {
    MapLocation rally = armyRally.getRally();
    if (rally != null) {
      rc.setIndicatorString(0, "I'm rallying to " + rally + ".");
      boolean clearRubble = rc.getType() != RobotType.TTM;
      navigation.directToAvoidingAlliedArchons(rally, 2 /* avoidDist */, clearRubble);
    } else {
      rc.setIndicatorString(0, "I was told to rally but don't have one.");
      navigation.moveRandomly();
    }
  }
}
