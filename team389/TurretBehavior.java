package team389;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class TurretBehavior implements PreBehavior, Behavior {

  private final RobotController rc;
  private final MessageReceiver messageReceiver;
  private final MessageSender messageSender;
  private final Radar radar;
  private final ArmyRally armyRally;
  private final EeHanTimingReporter eeHanTimingReporter;
  private final ZombieDenReporter zombieDenReporter;

  private final Behavior unpackedBehavior;
  private final SwarmingBehavior swarmingBehavior;
  private final RallyBehavior rallyBehavior;
  private final TimingAttackBehavior timingAttackBehavior;
  private final HuntingBehavior huntingBehavior;

  public TurretBehavior(
      RobotController rc,
      MessageReceiver messageReceiver,
      MessageSender messageSender,
      Radar radar,
      NavigationSystem navigation,
      AlliedArchonTracker alliedArchonTracker,
      AttackSystem attackSystem,
      ArmyRally armyRally,
      EeHanTimingReporter eeHanTimingReporter,
      ZombieDenReporter zombieDenReporter) {
    this.rc = rc;
    this.messageReceiver = messageReceiver;
    this.messageSender = messageSender;
    this.radar = radar;
    this.armyRally = armyRally;
    this.eeHanTimingReporter = eeHanTimingReporter;
    this.zombieDenReporter = zombieDenReporter;

    swarmingBehavior = new SwarmingBehavior(rc, navigation, alliedArchonTracker);
    unpackedBehavior = new UnpackedTurretBehavior(rc, radar, attackSystem);
    rallyBehavior = new RallyBehavior(rc, armyRally, navigation);
    timingAttackBehavior = new TimingAttackBehavior(rc, eeHanTimingReporter, navigation);
    huntingBehavior = new HuntingBehavior(rc, navigation, zombieDenReporter);
  }

  @Override
  public void preRun() throws GameActionException {
    messageReceiver.receiveMessages();

    RobotInfo[] adjacentRobots = rc.senseNearbyRobots(2);
    boolean adjacentScout = false;
    for (RobotInfo robot : adjacentRobots) {
      if (robot.type.equals(RobotType.SCOUT)) {
        adjacentScout = true;
        break;
      }
    }
    if (!adjacentScout) {
      messageSender.sendNeedTurretBuddy();
    }
  }

  @Override
  public void run() throws GameActionException {
    if (rc.getType() == RobotType.TURRET) {
      unpackedBehavior.run();
    } else {
      RobotInfo closest = RadarUtils.getClosestRobot(radar.getNearbyHostiles(), rc.getLocation());
      if (closest != null && closest.location.distanceSquaredTo(
          rc.getLocation()) <= RobotType.TURRET.attackRadiusSquared) {
        rc.unpack();
      }
      getCurrentBehavior().run();
    }
  }

  private Behavior getCurrentBehavior() {
    if (armyRally.getRally() != null) {
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
