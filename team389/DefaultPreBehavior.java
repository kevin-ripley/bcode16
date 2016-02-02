package team389;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class DefaultPreBehavior implements PreBehavior {

  private final RobotController rc;
  private final MessageReceiver messageReceiver;
  private final MessageSender messageSender;
  private final ZombieDenReporter zombieDenReporter;
  private final EnemyTurretCache enemyTurretCache;
  private final EeHanTimingCalculator eeHanTimingCalculator;

  public DefaultPreBehavior(
      RobotController rc,
      Radar radar,
      MessageReceiver messageReceiver,
      MessageSender messageSender,
      AlliedArchonTracker alliedArchonTracker,
      ZombieDenReporter zombieDenReporter,
      EnemyTurretCache enemyTurretCache,
      EeHanTimingCalculator eeHanTimingCalculator) {
    this.rc = rc;
    this.messageReceiver = messageReceiver;
    this.messageSender = messageSender;
    this.zombieDenReporter = zombieDenReporter;
    this.enemyTurretCache = enemyTurretCache;
    this.eeHanTimingCalculator = eeHanTimingCalculator;
  }

  @Override
  public void preRun() throws GameActionException {
    messageReceiver.receiveMessages();
    updateAndShareZombieDens();
    updateAndShareEnemyTurrets();
    computeAndShareTimingAttack();
  }

  private void updateAndShareZombieDens() throws GameActionException {
    zombieDenReporter.invalidateNearbyDestroyedDens();
    zombieDenReporter.searchForNewDens();
    zombieDenReporter.shareNewlyAddedAndRemovedDens(messageSender);
  }

  private void updateAndShareEnemyTurrets() throws GameActionException {
    enemyTurretCache.invalidateNearbyEnemyTurrets();
    int frequency = getSharingFrequency();
    int random = (rc.getRoundNum() + frequency / 3 + rc.getID());
    if (random % frequency == 0) {
      enemyTurretCache.shareRandomEnemyTurret(messageSender);
    }
  }

  private void computeAndShareTimingAttack() throws GameActionException {
    eeHanTimingCalculator.computeAndShareTimingAttack(messageSender);
  }

  private int getSharingFrequency() throws GameActionException {
    return Math.max(
        (rc.getRobotCount() / 10) + 1,
        (rc.getRoundNum() / 200) + 1);
  }
}
