package team389;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import team389.AlliedArchonTracker.AlliedArchonInfo;

public class FollowerArchonBehavior implements Behavior {

  private final RobotController rc;
  private final NavigationSystem navigation;
  private final AlliedArchonTracker alliedArchonTracker;

  public FollowerArchonBehavior(
      RobotController rc, NavigationSystem navigation, AlliedArchonTracker alliedArchonTracker) {
    this.rc = rc;
    this.navigation = navigation;
    this.alliedArchonTracker = alliedArchonTracker;
  }

  @Override
  public void run() throws GameActionException {
    AlliedArchonInfo leader = alliedArchonTracker.getLowestIdAlliedArchon(rc.getLocation());
    if (leader != null) {
      rc.setIndicatorString(0, "I'm following " + leader.id + ".");
      navigation.directToAvoidingAlliedArchons(
          leader.loc, 2 /* avoidDist */, true /* clearRubble */);
    } else {
      rc.setIndicatorString(0, "I'm a lost follower.");
      navigation.moveRandomly();
    }
  }
}
