package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class ExploringBehavior implements Behavior {

  private final RobotController rc;
  private final NavigationSystem navigation;
  private final MapBoundaryCalculator mapBoundaryCalculator;
  private final ExplorationCalculator explorationCalculator;

  public ExploringBehavior(
      RobotController rc,
      NavigationSystem navigation,
      PatrolWaypointCalculator patrolWaypointCalculator) {
    this.rc = rc;
    this.navigation = navigation;

    mapBoundaryCalculator = new MapBoundaryCalculator(rc);
    explorationCalculator = new ExplorationCalculator(
        rc, mapBoundaryCalculator, patrolWaypointCalculator);
  }

  @Override
  public void run() throws GameActionException {
    RobotPlayer.profiler.split("start of exploring behavior");
    mapBoundaryCalculator.update();
    RobotPlayer.profiler.split("after map boundary calculator");
    MapLocation loc = explorationCalculator.calculate();
    RobotPlayer.profiler.split("after exploration calculator calculate");
    rc.setIndicatorString(0, "I'm exploring " + loc + ".");
    boolean clearRubble = rc.getType() != RobotType.TTM && rc.getType() != RobotType.SCOUT;
    if (!navigation.directTo(loc, true /* avoidAttackers */, clearRubble)) {
      navigation.directTo(loc, false /* avoidAttackers */, clearRubble);
    }
  }
}
