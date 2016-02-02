package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class UnpackedTurretBehavior implements Behavior {

  private static final int MIN_IDLE_ROUNDS = 10;
  private static final int MAX_IDLE_ROUNDS = 30;

  private final RobotController rc;
  private final Radar radar;
  private final AttackSystem attackSystem;

  private int idleRounds;

  public UnpackedTurretBehavior(RobotController rc, Radar radar, AttackSystem attackSystem) {
    this.rc = rc;
    this.radar = radar;
    this.attackSystem = attackSystem;
    idleRounds = MIN_IDLE_ROUNDS;
  }

  @Override
  public void run() throws GameActionException {
    rc.setIndicatorString(0, "I'm sieged, will unsiege in " + idleRounds + " rounds.");
    attack();
    maybeUnsiege(rc);
  }

  private void maybeUnsiege(RobotController rc) throws GameActionException {
    RobotInfo closest = RadarUtils.getClosestRobot(radar.getNearbyHostiles(), rc.getLocation());
    if (closest != null && closest.location.distanceSquaredTo(
        rc.getLocation()) <= rc.getType().attackRadiusSquared) {
      idleRounds = computeIdleRounds();
    } else {
      if (idleRounds-- <= 0 && rc.isCoreReady()) {
        rc.pack();
        idleRounds = computeIdleRounds();
      }
    }
  }

  private void attack() throws GameActionException {
    if (rc.isWeaponReady()) {
      RobotInfo[] enemies = radar.getNearbyEnemies();
      RobotInfo[] allies = radar.getNearbyAllies();
      MapLocation target = attackSystem.getBestEnemyToShoot(rc, enemies, enemies.length, allies,
          allies.length);
      if (target != null) {
        rc.attackLocation(target);
        return;
      }
      RobotInfo[] zombies = radar.getNearbyZombies();
      target = attackSystem.getBestEnemyToShoot(rc, zombies, zombies.length, allies,
          allies.length);
      if (target != null) {
        rc.attackLocation(target);
        return;
      }
    }
  }

  private int computeIdleRounds() {
    return MIN_IDLE_ROUNDS + (int) (Math.random() * (MAX_IDLE_ROUNDS - MIN_IDLE_ROUNDS));
  }
}
