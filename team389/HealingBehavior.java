package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import team389.AlliedArchonTracker.AlliedArchonInfo;

public class HealingBehavior implements Behavior {

  private final RobotController rc;
  private final NavigationSystem navigation;
  private final AlliedArchonTracker alliedArchonTracker;
  private final AttackSystem attackSystem;
  private final Radar radar;

  public HealingBehavior(
      RobotController rc,
      NavigationSystem navigation,
      AlliedArchonTracker alliedArchonTracker,
      Radar radar,
      AttackSystem attackSystem) {
    this.rc = rc;
    this.navigation = navigation;
    this.alliedArchonTracker = alliedArchonTracker;
    this.attackSystem = attackSystem;
    this.radar = radar;
  }

  @Override
  public void run() throws GameActionException {
    rc.setIndicatorString(0, "HEALING");

    // Try and retreat first
    if (rc.isCoreReady()) {
      // Check to see if there are any archons in sensor range
      RobotInfo[] allies = radar.getNearbyAllies();
      int closestDist = 99999;
      RobotInfo closestArchon = null;
      for (int i = allies.length; --i >= 0;) {
        RobotInfo ally = allies[i];
        if (ally.type == RobotType.ARCHON) {
          int dist = ally.location.distanceSquaredTo(ally.location);
          if (dist > closestDist) {
            closestArchon = ally;
            closestDist = dist;
          }
        }
      }

      MapLocation target = null;
      if (closestArchon != null) {
        target = closestArchon.location;
      } else {
        AlliedArchonInfo alliedArchonInfo = alliedArchonTracker.getClosestAlliedArchon(rc
            .getLocation());
        if (alliedArchonInfo != null) {
          target = alliedArchonInfo.loc;
        }
      }
      if (target != null) {
        if (rc.getLocation().distanceSquaredTo(target) <= 9) {
          navigation.directToAvoidingAlliedArchons(target, 2 /* avoidDistance */,
              true /* clearRubble */);
        } else {
          if (navigation.directTo(target, true /* avoidAttackers */,
              false /* clearRubble */)) {
            navigation.directTo(target, false /* avoidAttacker */, false /* clearRubble */);
          }
        }

      }
    }
    if (rc.isWeaponReady()) {
      RobotInfo[] enemies = radar.getNearbyEnemies();
      RobotInfo[] allies = radar.getNearbyAllies();
      MapLocation target = attackSystem.getBestEnemyToShoot(rc, enemies,
          enemies.length, allies,
          allies.length);
      if (target != null) {
        rc.attackLocation(target);
        return;
      }
      RobotInfo[] zombies = radar.getNearbyZombies();
      target = attackSystem.getBestEnemyToShoot(rc, zombies,
          zombies.length, allies, allies.length);
      if (target != null) {
        rc.attackLocation(target);
        return;
      }
    }
  }
}
