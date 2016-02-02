package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class EeHanTimingManager implements EeHanTimingCalculator, EeHanTimingReporter {

  private final int MIN_ROUNDS_UNTIL_TIMING = 500;
  private final int TIMING_ATTACK_RALLY_TIME = 100;
  private final int TIMING_ATTACK_DURATION = 200;
  private final int MIN_ALLIED_ROBOTS = 70;

  private final RobotController rc;
  private final Radar radar;
  private final EnemyTurretCache enemyTurretCache;

  private TimingAttack timingAttack;
  private int roundsUntilTiming;

  public EeHanTimingManager(RobotController rc, Radar radar, EnemyTurretCache enemyTurretCache) {
    this.rc = rc;
    this.radar = radar;
    this.enemyTurretCache = enemyTurretCache;
    timingAttack = null;
    roundsUntilTiming = MIN_ROUNDS_UNTIL_TIMING;
  }

  @Override
  public void reportTimingAttack(MapLocation location, int startRound, int endRound) {
    if (timingAttack == null
        || startRound < timingAttack.startRound
        || rc.getRoundNum() > timingAttack.endRound && startRound > timingAttack.endRound) {
      timingAttack = new TimingAttack(location, startRound, endRound);
    }
  }

  @Override
  public TimingAttack getTimingAttack() {
    return timingAttack != null && rc.getRoundNum() < timingAttack.endRound
        ? timingAttack
        : null;
  }

  @Override
  public void computeAndShareTimingAttack(MessageSender messageSender) throws GameActionException {
    int roundNum = rc.getRoundNum();
    if (timingAttack != null && roundNum < timingAttack.endRound) {
      return;
    }

    if (radar.getNearbyZombies().length > 0
        || rc.getRobotCount() < MIN_ALLIED_ROBOTS
        || enemyTurretCache.getNewestTurret() == null) {
      roundsUntilTiming = MIN_ROUNDS_UNTIL_TIMING;
    } else {
      roundsUntilTiming--;
    }

    if (roundsUntilTiming <= 0) {
      timingAttack = new TimingAttack(
          enemyTurretCache.getNewestTurret(),
          roundNum + TIMING_ATTACK_RALLY_TIME,
          roundNum + TIMING_ATTACK_RALLY_TIME + TIMING_ATTACK_DURATION);
      messageSender.sendTimingAttackEverywhere(
          timingAttack.location, timingAttack.startRound, timingAttack.endRound);
    }
  }
}
