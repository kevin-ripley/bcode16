package team389;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class ScoutBehavior implements Behavior {

  private final RobotController rc;
  private final Radar radar;
  private final MessageSender messageSender;
  private final EnemyArchonTracker enemyArchonTracker;
  private final EnemyTurretCache enemyTurretCache;
  private final PickupLocationReporter pickupLocationReporter;

  private final BuddyScoutBehavior buddyBehavior;
  private final ExploringBehavior exploringBehavior;
  private final ZombieDraggingScoutBehavior zombieDraggingBehavior;

  public ScoutBehavior(
      RobotController rc,
      NavigationSystem navigation,
      Radar radar,
      BasicMessages basicMessages,
      EnemyArchonTracker enemyArchonTracker,
      AlliedArchonTracker alliedArchonTracker,
      EnemyTurretCache enemyTurretCache,
      MessageSender messageSender,
      PickupLocationReporter pickupLocationReporter) {
    this.rc = rc;
    this.radar = radar;
    this.messageSender = messageSender;
    this.enemyArchonTracker = enemyArchonTracker;
    this.enemyTurretCache = enemyTurretCache;
    this.pickupLocationReporter = pickupLocationReporter;

    buddyBehavior = new BuddyScoutBehavior(
        rc,
        navigation,
        new BuddySystem(),
        basicMessages,
        radar,
        messageSender);
    PatrolWaypointCalculator patrolWaypointCalculator = new ReflectingPatrolWaypointCalculator(
        new LawnMowerPatrolWaypointCalculator(7 /* laneHalfWidth */, 5 /* mapBoundaryMargin */),
        (rc.getID() & 2) == 0 /* flipX */,
        (rc.getID() & 4) == 0 /* flipY */);
    exploringBehavior = new ExploringBehavior(rc, navigation, patrolWaypointCalculator);
    zombieDraggingBehavior = new ZombieDraggingScoutBehavior(rc, navigation, radar,
        enemyArchonTracker,
        alliedArchonTracker);
  }

  @Override
  public void run() throws GameActionException {
    RobotPlayer.profiler.split("start of run");
    updateEnemyArchonTrackerAndTurretCache();
    RobotPlayer.profiler.split("after update enemy archon tracker and turret cache");
    updatePickupLocationReporter();
    RobotPlayer.profiler.split("after update pickup location reporter");

    getBehavior().run();
  }

  private Behavior getBehavior() throws GameActionException {
    if (buddyBehavior.shouldBuddy()) {
      return buddyBehavior;
    } else if (zombieDraggingBehavior.shouldDrag()) {
      return zombieDraggingBehavior;
    } else {
      return exploringBehavior;
    }
  }

  private void updateEnemyArchonTrackerAndTurretCache() {
    RobotInfo[] enemies = radar.getNearbyEnemies();
    for (int i = enemies.length; --i >= 0;) {
      RobotInfo enemy = enemies[i];
      int timestamp = rc.getRoundNum();
      if (enemy.type == RobotType.ARCHON) {
        enemyArchonTracker.reportEnemyArchon(enemy.ID, enemy.location, timestamp);
      } else if (enemy.type == RobotType.TURRET) {
        enemyTurretCache.reportEnemyTurretPresent(enemy.location, timestamp);
      }
    }
  }

  private void updatePickupLocationReporter() throws GameActionException {
    pickupLocationReporter.findNearbyPickups();
    RobotPlayer.profiler.split("after find nearby pickups");
    pickupLocationReporter.shareNewPickups(messageSender);
    RobotPlayer.profiler.split("after share new pickups");
  }
}
