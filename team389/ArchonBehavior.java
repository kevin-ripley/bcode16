package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class ArchonBehavior implements Behavior {

  private final RobotController rc;
  private final Radar radar;
  private final MessageSender messageSender;
  private final RepairSystem repairSystem;
  private final ZombieDenReporter zombieDenReporter;

  private final ScoutUnitOrder scoutUnitOrder;
  private final MarineUnitOrder marineUnitOrder;
  private final MarineTankUnitOrder marineTankUnitOrder;

  private final OpeningArchonBehavior openingBehavior;
  private final FightingArchonBehavior fightingBehavior;
  private final PickerUpperArchonBehavior pickerUpperBehavior;
  private final LeaderArchonBehavior leaderBehavior;
  private final ExploringBehavior exploringBehavior;

  private int justSpawnedTimer;

  public final static int POS_BROADCAST_WAIT = 10;

  public ArchonBehavior(
      RobotController rc,
      Radar radar,
      NavigationSystem navigation,
      MessageSender messageSender,
      AlliedArchonTracker alliedArchonTracker,
      RepairSystem repairSystem,
      ZombieDenReporter zombieDenReporter,
      PickupLocationReporter pickupLocationReporter) {
    this.rc = rc;
    this.radar = radar;
    this.messageSender = messageSender;
    this.repairSystem = repairSystem;
    this.zombieDenReporter = zombieDenReporter;

    scoutUnitOrder = new ScoutUnitOrder();
    marineUnitOrder = new MarineUnitOrder();
    marineTankUnitOrder = new MarineTankUnitOrder(rc);
    openingBehavior = new OpeningArchonBehavior(rc, scoutUnitOrder);
    fightingBehavior = new FightingArchonBehavior(rc, radar, navigation,
        messageSender, marineTankUnitOrder, marineUnitOrder);
    pickerUpperBehavior = new PickerUpperArchonBehavior(rc, navigation);
    leaderBehavior = new LeaderArchonBehavior(rc, navigation, pickupLocationReporter);
    PatrolWaypointCalculator patrolWaypointCalculator = new LawnMowerPatrolWaypointCalculator(
        5 /* laneHalfWidth */, 4 /* mapBoundaryMargin */);
    exploringBehavior = new ExploringBehavior(rc, navigation, patrolWaypointCalculator);

    justSpawnedTimer = 0;
  }

  @Override
  public void run() throws GameActionException {

    if (rc.getRoundNum() % POS_BROADCAST_WAIT == 0) {
      messageSender.sendSelfArchonLocation();
    }

    rc.setIndicatorString(1, " " + justSpawnedTimer);
    if (rc.isCoreReady() && justSpawnedTimer == 0) {
      zombieDenReporter.shareAllDens(messageSender);
      zombieDenReporter.showDebugInfo();
      justSpawnedTimer = -1;
    }
    if (openingBehavior.isOpeningOver()
        && radar.getNearbyHostiles().length == 0
        && UnitSpawner.spawn(rc, marineTankUnitOrder.getNextUnit())) {
      justSpawnedTimer = marineTankUnitOrder.getNextUnit().buildTurns;
      marineTankUnitOrder.computeNextUnit();
    }
    getCurrentBehavior().run();
    MapLocation target = repairSystem.getBestAllyToHeal(radar.getNearbyAllies());
    if (target != null) {
      rc.setIndicatorString(2, "Healing target " + target + " " + rc.getRoundNum());
      rc.repair(target);
    }
    if (justSpawnedTimer > 0) {
      justSpawnedTimer--;
    }
  }

  private Behavior getCurrentBehavior() throws GameActionException {
    if (!openingBehavior.isOpeningOver()) {
      return openingBehavior;
    }
    if (radar.getNearbyHostiles().length != 0) {
      return fightingBehavior;
    }
    if (pickerUpperBehavior.getTarget() != null) {
      return pickerUpperBehavior;
    }
    if (leaderBehavior.getTarget() != null) {
      return leaderBehavior;
    }
    return exploringBehavior;
  }
}
