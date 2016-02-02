package team389;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import team389.AlliedArchonTracker.AlliedArchonInfo;

public class SwarmingBehavior implements Behavior {

  private final RobotController rc;
  private final NavigationSystem navigation;
  private final AlliedArchonTracker alliedArchonTracker;

  public SwarmingBehavior(
      RobotController rc,
      NavigationSystem navigation,
      AlliedArchonTracker alliedArchonTracker) {
    this.rc = rc;
    this.navigation = navigation;
    this.alliedArchonTracker = alliedArchonTracker;
  }

  @Override
  public void run() throws GameActionException {
    AlliedArchonInfo closestAlliedArchon = alliedArchonTracker.getClosestAlliedArchon(
        rc.getLocation());
    if (closestAlliedArchon != null) {
      rc.setIndicatorString(0, "I'm swarming.");
      MapLocation target = getArchonSurroundTarget(closestAlliedArchon.loc);
      maybeClearRubble(target);
      RobotPlayer.profiler.split("before navigation to closest archon");
      boolean clearRubble = rc.getType() != RobotType.TTM;
      if (!navigation.directToAvoidingAlliedArchons(target, 2 /* avoidDist */, clearRubble)) {
        navigation.directTo(target, true /* avoidAttackers */, clearRubble);
      }
      RobotPlayer.profiler.split("after navigation to closest archon");
    } else {
      rc.setIndicatorString(0, "I'm lost.");
      navigation.moveRandomly();
      RobotPlayer.profiler.split("after moving randomly");
    }
  }

  private MapLocation getArchonSurroundTarget(MapLocation archonLoc) {
    return archonLoc.add(DirectionUtils.movableDirections[rc.getID() % 8], 2);
  }

  private void maybeClearRubble(MapLocation target) throws GameActionException {
    MapLocation myLoc = rc.getLocation();
    if (rc.isCoreReady() && rc.getType() != RobotType.TTM && myLoc.distanceSquaredTo(target) <= 8) {
      // Just clear rubble
      Direction d = DirectionUtils.getRandomMovableDirection();
      for (int i = 8; --i >= 0;) {
        if (rc.senseRubble(myLoc.add(d)) >= GameConstants.RUBBLE_SLOW_THRESH) {
          rc.clearRubble(d);
          return;
        }
        d = d.rotateLeft();
      }
    }
  }
}
