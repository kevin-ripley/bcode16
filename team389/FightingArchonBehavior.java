package team389;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class FightingArchonBehavior implements Behavior {

  private static final int RALLY_MESSAGE_DISTANCE_MULTIPLIER = 9;

  private final RobotController rc;
  private final Radar radar;
  private final NavigationSystem navigation;
  private final MessageSender messageSender;
  private final MarineTankUnitOrder marineTankUnitOrder;
  private final MarineUnitOrder marineUnitOrder;

  public FightingArchonBehavior(
      RobotController rc,
      Radar radar,
      NavigationSystem navigation,
      MessageSender messageSender,
      MarineTankUnitOrder marineTankUnitOrder,
      MarineUnitOrder marineUnitOrder) {
    this.rc = rc;
    this.radar = radar;
    this.navigation = navigation;
    this.messageSender = messageSender;
    this.marineTankUnitOrder = marineTankUnitOrder;
    this.marineUnitOrder = marineUnitOrder;
  }

  @Override
  public void run() throws GameActionException {
    RobotInfo[] hostiles = radar.getNearbyHostiles();
    int numEnemiesAttackingUs = 0;
    for (int i = hostiles.length; --i >= 0;) {
      RobotInfo robot = hostiles[i];
      int dist = rc.getLocation().distanceSquaredTo(robot.location);
      if (robot.type.canAttack() && robot.type.attackRadiusSquared >= dist) {
        ++numEnemiesAttackingUs;
      }
    }
    MapLocation myLoc = rc.getLocation();
    RobotInfo closestHostile = RadarUtils.getClosestRobot(radar.getNearbyHostiles(), myLoc);
    if (closestHostile == null) {
      return;
    }

    UnitOrder unitOrder = radar.getNearbyZombies().length > 0 ? marineUnitOrder
        : marineTankUnitOrder;
    Direction retreatDir = myLoc.directionTo(closestHostile.location).opposite();
    if (numEnemiesAttackingUs == 0 && closestHostile.location.distanceSquaredTo(myLoc) > 13) { // Could
                                                                                               // maybe
                                                                                               // spawn
      if (!UnitSpawner.spawnInDirection(rc, unitOrder.getNextUnit(), retreatDir)) {
        if (!UnitSpawner.spawnInDirection(rc, unitOrder.getNextUnit(), retreatDir.rotateLeft())) {
          if (UnitSpawner.spawnInDirection(rc, unitOrder.getNextUnit(), retreatDir.rotateRight())) {
            unitOrder.computeNextUnit();
          }
        } else {
          unitOrder.computeNextUnit();
        }
      } else {
        unitOrder.computeNextUnit();
      }
    }

    if (closestHostile.type != RobotType.SCOUT && rc.getRoundNum() % 10 == 0) {
      messageSender.sendArmyRallyLocation(
          closestHostile.location, RALLY_MESSAGE_DISTANCE_MULTIPLIER);
    }

    MapLocation target = myLoc.add(retreatDir, 10);
    if (!navigation.directTo(target, true /* avoidAttackers */, false /* clearRubble */)) {
      navigation.directTo(target, false /* avoidAttackers */, false /* clearRubble */);
    }
    rc.setIndicatorString(0, "I'm retreating " + retreatDir + "!");
  }
}
