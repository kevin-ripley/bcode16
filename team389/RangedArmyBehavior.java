package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class RangedArmyBehavior implements PreBehavior, Behavior {

  private final RobotController rc;
  private final MessageReceiver messageReceiver;
  private final Radar radar;
  private final ArmyRally armyRally;
  private final EeHanTimingReporter eeHanTimingReporter;
  private final ZombieDenReporter zombieDenReporter;

  private final FightingRangedBehavior fightingBehavior;
  private final SwarmingBehavior swarmingBehavior;
  private final RallyBehavior rallyBehavior;
  private final TimingAttackBehavior timingAttackBehavior;
  private final HuntingBehavior huntingBehavior;

  public RangedArmyBehavior(
      RobotController rc,
      MessageReceiver messageReceiver,
      AlliedArchonTracker alliedArchonTracker,
      Radar radar,
      NavigationSystem navigation,
      AttackSystem attackSystem,
      ZombieSpawnScheduleInfo zombieSchedule,
      ArmyRally armyRally,
      EeHanTimingReporter eeHanTimingReporter,
      ZombieDenReporter zombieDenReporter) {
    this.radar = radar;
    this.rc = rc;
    this.messageReceiver = messageReceiver;
    this.armyRally = armyRally;
    this.eeHanTimingReporter = eeHanTimingReporter;
    this.zombieDenReporter = zombieDenReporter;
    fightingBehavior = new FightingRangedBehavior(rc, radar, navigation, zombieSchedule,
        attackSystem);
    swarmingBehavior = new SwarmingBehavior(rc, navigation, alliedArchonTracker);
    rallyBehavior = new RallyBehavior(rc, armyRally, navigation);
    timingAttackBehavior = new TimingAttackBehavior(rc, eeHanTimingReporter, navigation);
    huntingBehavior = new HuntingBehavior(rc, navigation, zombieDenReporter);
  }

  @Override
  public void preRun() throws GameActionException {
    // Don't receive messages in battle.
    if (radar.getNearbyHostiles().length != 0) {
      return;
    }

    messageReceiver.receiveMessages();
    zombieDenReporter.invalidateNearbyDestroyedDens();
    RobotPlayer.profiler.split("after receiving messages");
  }

  @Override
  public void run() throws GameActionException {
    getCurrentBehavior().run();
  }

  private Behavior getCurrentBehavior() {
    if (radar.getNearbyHostiles().length != 0) {
      return fightingBehavior;
    }
    MapLocation huntingTarget = huntingBehavior.getTarget();
    MapLocation rallyLoc = armyRally.getRally();
    MapLocation myLoc = rc.getLocation();
    if (rallyLoc != null && (huntingTarget == null || (huntingTarget.distanceSquaredTo(
        myLoc) >= rallyLoc.distanceSquaredTo(myLoc)))) {
      return rallyBehavior;
    }
    if (eeHanTimingReporter.getTimingAttack() != null) {
      return timingAttackBehavior;
    }
    if (huntingBehavior.getTarget() != null) {
      return huntingBehavior;
    }
    return swarmingBehavior;
  }
}
