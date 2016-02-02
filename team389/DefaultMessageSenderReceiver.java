package team389;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Signal;
import battlecode.common.Team;

public class DefaultMessageSenderReceiver implements MessageSender, MessageReceiver {

  private static enum MessageType {
    SELF_ARCHON_LOCATION(0),
    DISTANT_HOSTILE_INFO(1),
    ARMY_RALLY_LOCATION(2),
    DEN_LOCATION(3),
    DEN_LOCATION_REMOVED(4),
    ENEMY_TURRET_LOCATION(5),
    ENEMY_TURRET_LOCATION_REMOVED(6),
    TIMING_ATTACK_INFO(7),
    PICKUP_LOCATION(8);

    public final int opCode;

    private MessageType(int opCode) {
      this.opCode = opCode;
    }
  }

  private final RobotController rc;
  private final AlliedArchonTracker alliedArchonTracker;
  private final DistantHostileReporter distantHostileReporter;
  private final ZombieDenReporter zombieDenReporter;
  private final BasicMessages basicMessages;
  private final ArmyRallyReporter armyRallyReporter;
  private final EnemyTurretCache enemyTurretCache;
  private final EeHanTimingReporter eeHanTimingReporter;
  private final PickupLocationReporter pickupLocationReporter;
  private final EnemyMessageProcessor enemyMessageProcessor;
  private final boolean canSendMessages;

  public DefaultMessageSenderReceiver(
      RobotController rc,
      AlliedArchonTracker alliedArchonTracker,
      DistantHostileReporter distantHostileReporter,
      ZombieDenReporter zombieDenReporter,
      BasicMessages basicMessages,
      ArmyRallyReporter armyRallyReporter,
      EnemyTurretCache enemyTurretCache,
      EeHanTimingReporter eeHanTimingReporter,
      PickupLocationReporter pickupLocationReporter,
      EnemyMessageProcessor enemyMessageProcessor) {
    this.rc = rc;
    this.alliedArchonTracker = alliedArchonTracker;
    this.distantHostileReporter = distantHostileReporter;
    this.zombieDenReporter = zombieDenReporter;
    this.basicMessages = basicMessages;
    this.armyRallyReporter = armyRallyReporter;
    this.enemyTurretCache = enemyTurretCache;
    this.eeHanTimingReporter = eeHanTimingReporter;
    this.pickupLocationReporter = pickupLocationReporter;
    this.enemyMessageProcessor = enemyMessageProcessor;

    canSendMessages = rc.getType() == RobotType.ARCHON || rc.getType() == RobotType.SCOUT;
  }

  @Override
  public void receiveMessages() throws GameActionException {
    basicMessages.clearBasicMessages();
    Signal[] signals = rc.emptySignalQueue();
    Team myTeam = rc.getTeam();
    for (int i = signals.length; --i >= 0;) {
      Signal s = signals[i];
      if (s.getTeam() != myTeam) {
        enemyMessageProcessor.processEnemyMessage(s);
        continue;
      }
      int[] data = s.getMessage();

      if (data == null) { // Basic message
        basicMessages.addMessage(s, rc);
        continue;
      }
      if (data.length != 2) {
        continue;
      }
      MessageData data1 = MessageData.fromSignal(s, true /* firstData */);
      MessageData data2 = MessageData.fromSignal(s, false /* firstData */);
      int opCode = data1.getPayload(0, 3);
      if (opCode == MessageType.SELF_ARCHON_LOCATION.opCode) {
        RobotPlayer.profiler.split("received SELF_ARCHON_LOCATION, id = " + s.getID());
        alliedArchonTracker.reportAlliedArchon(s.getID(), s.getLocation());
      } else if (opCode == MessageType.DEN_LOCATION.opCode) {
        RobotPlayer.profiler.split("received DEN_LOCATION");
        MapLocation denLoc = data2.toMapLocation();
        zombieDenReporter.reportDen(denLoc);
      } else if (opCode == MessageType.DEN_LOCATION_REMOVED.opCode) {
        RobotPlayer.profiler.split("received DEN_LOCATION_REMOVED");
        MapLocation denLoc = data2.toMapLocation();
        zombieDenReporter.reportDenDestroyed(denLoc);
      } else if (opCode == MessageType.DISTANT_HOSTILE_INFO.opCode) {
        RobotPlayer.profiler.split("received DISTANT_HOSTILE_INFO");
        MapLocation hostileLoc = data2.toMapLocation();
        int[] payloads = data1.getAllPayloads(new int[] {
          4, 8, 1
        });
        distantHostileReporter.reportDistantHostile(
            hostileLoc, payloads[1] /* coreDelayTenths */, payloads[2] == 1 /* isZombie */);
      } else if (opCode == MessageType.ARMY_RALLY_LOCATION.opCode) {
        RobotPlayer.profiler.split("received ARMY_RALLY_LOCATION");
        MapLocation rallyLoc = data2.toMapLocation();
        armyRallyReporter.reportRally(rallyLoc);
      } else if (opCode == MessageType.ENEMY_TURRET_LOCATION.opCode) {
        RobotPlayer.profiler.split("received ENEMY_TURRET_LOCATION");
        MapLocation turretLoc = data2.toMapLocation();
        int[] payloads = data1.getAllPayloads(new int[] {
          4, 20
        });
        enemyTurretCache.reportEnemyTurretPresent(turretLoc, payloads[1] /* timestamp */);
      } else if (opCode == MessageType.ENEMY_TURRET_LOCATION_REMOVED.opCode) {
        RobotPlayer.profiler.split("received ENEMY_TURRET_LOCATION_REMOVED");
        MapLocation turretLoc = data2.toMapLocation();
        int[] payloads = data1.getAllPayloads(new int[] {
          4, 20
        });
        enemyTurretCache.reportEnemyTurretAbsent(turretLoc, payloads[1] /* timestamp */);
      } else if (opCode == MessageType.TIMING_ATTACK_INFO.opCode) {
        RobotPlayer.profiler.split("received TIMING_ATTACK_INFO");
        MapLocation timingAttackLoc = data2.toMapLocation();
        int[] payloads = data1.getAllPayloads(new int[] {
          4, 14, 14
        });
        eeHanTimingReporter.reportTimingAttack(timingAttackLoc, payloads[1], payloads[2]);
      } else if (opCode == MessageType.PICKUP_LOCATION.opCode) {
        RobotPlayer.profiler.split("received PICKUP_LOCATION");
        MapLocation pickupLoc = data2.toMapLocation();
        pickupLocationReporter.reportPickup(pickupLoc);
      }
    }
  }

  @Override
  public void sendSelfArchonLocation() throws GameActionException {
    MessageData data1 = new MessageData.Builder()
        .addBits(4, MessageType.SELF_ARCHON_LOCATION.opCode)
        .build();
    MessageData data2 = MessageData.empty();
    sendMessageWithMultiplier(data1, data2, 9 /* distanceMultipler */);
  }

  @Override
  public void sendDenLocationNearby(MapLocation loc) throws GameActionException {
    MessageData data1 = new MessageData.Builder()
        .addBits(4, MessageType.DEN_LOCATION.opCode)
        .build();
    MessageData data2 = MessageData.fromMapLocation(loc);
    sendMessage(data1, data2);
  }

  @Override
  public void sendDenLocationEverywhere(MapLocation loc) throws GameActionException {
    MessageData data1 = new MessageData.Builder()
        .addBits(4, MessageType.DEN_LOCATION.opCode)
        .build();
    MessageData data2 = MessageData.fromMapLocation(loc);
    sendMessageEverywhere(data1, data2);
  }

  @Override
  public void sendDenLocationRemovedEverywhere(MapLocation loc) throws GameActionException {
    MessageData data1 = new MessageData.Builder()
        .addBits(4, MessageType.DEN_LOCATION_REMOVED.opCode)
        .build();
    MessageData data2 = MessageData.fromMapLocation(loc);
    sendMessageEverywhere(data1, data2);
  }

  @Override
  public void sendPickupLocationEverywhere(MapLocation loc) throws GameActionException {
    MessageData data1 = new MessageData.Builder()
        .addBits(4, MessageType.PICKUP_LOCATION.opCode)
        .build();
    MessageData data2 = MessageData.fromMapLocation(loc);
    sendMessageEverywhere(data1, data2);
  }

  @Override
  public void sendDistantHostileInfo(
      MapLocation loc, double coreDelay, boolean isZombie, int broadcastDistanceSquared)
          throws GameActionException {
    int coreDelayTenths = (10 * coreDelay) >= 250 ? 250 : (int) (10 * coreDelay);
    MessageData data1 = new MessageData.Builder()
        .addBits(4, MessageType.DISTANT_HOSTILE_INFO.opCode)
        .addBits(8, coreDelayTenths)
        .addBits(1, isZombie ? 1 : 0)
        .build();
    MessageData data2 = MessageData.fromMapLocation(loc);
    sendMessageWithDistanceSquared(data1, data2, broadcastDistanceSquared);
  }

  @Override
  public void sendArmyRallyLocation(
      MapLocation loc, int distanceMultiplier) throws GameActionException {
    MessageData data1 = new MessageData.Builder()
        .addBits(4, MessageType.ARMY_RALLY_LOCATION.opCode)
        .build();
    MessageData data2 = MessageData.fromMapLocation(loc);
    sendMessageWithMultiplier(data1, data2, distanceMultiplier);
  }

  @Override
  public void sendEnemyTurretLocation(
      MapLocation loc, int timestamp) throws GameActionException {
    MessageData data1 = new MessageData.Builder()
        .addBits(4, MessageType.ENEMY_TURRET_LOCATION.opCode)
        .addBits(20, timestamp)
        .build();
    MessageData data2 = MessageData.fromMapLocation(loc);
    sendMessage(data1, data2);
  }

  @Override
  public void sendEnemyTurretLocationRemoved(
      MapLocation loc, int timestamp) throws GameActionException {
    MessageData data1 = new MessageData.Builder()
        .addBits(4, MessageType.ENEMY_TURRET_LOCATION_REMOVED.opCode)
        .addBits(20, timestamp)
        .build();
    MessageData data2 = MessageData.fromMapLocation(loc);
    sendMessage(data1, data2);
  }

  @Override
  public void sendTimingAttackEverywhere(
      MapLocation loc, int startRound, int endRound) throws GameActionException {
    MessageData data1 = new MessageData.Builder()
        .addBits(4, MessageType.TIMING_ATTACK_INFO.opCode)
        .addBits(14, startRound)
        .addBits(14, endRound)
        .build();
    MessageData data2 = MessageData.fromMapLocation(loc);
    sendMessageEverywhere(data1, data2);
  }

  @Override
  public void sendNeedTurretBuddy() throws GameActionException {
    if (rc.getBasicSignalCount() < GameConstants.BASIC_SIGNALS_PER_TURN) {
      sendBasicSignal(2 /* distanceMultiplier */);
    }
  }

  private void sendBasicSignal(int distanceMultiplier) throws GameActionException {
    if (rc.getBasicSignalCount() < GameConstants.BASIC_SIGNALS_PER_TURN) {
      rc.broadcastSignal(distanceMultiplier * rc.getType().sensorRadiusSquared);
    }
  }

  private void sendMessage(MessageData data1, MessageData data2)
      throws GameActionException {
    sendMessageWithMultiplier(data1, data2, 2 /* distanceMultiplier */);
  }

  private void sendMessageEverywhere(
      MessageData data1, MessageData data2) throws GameActionException {
    sendMessageWithMultiplier(data1, data2, getEverywhereDistanceMultipler());
  }

  private void sendMessageWithMultiplier(
      MessageData data1, MessageData data2, int distanceMultiplier) throws GameActionException {
    sendMessageWithDistanceSquared(
        data1, data2, distanceMultiplier * rc.getType().sensorRadiusSquared);
  }

  private void sendMessageWithDistanceSquared(
      MessageData data1, MessageData data2, int distanceSquared) throws GameActionException {
    if (canSendMessages
        && rc.getMessageSignalCount() < GameConstants.MESSAGE_SIGNALS_PER_TURN) {
      rc.broadcastMessageSignal(
          data1.getData(),
          data2.getData(),
          distanceSquared);
    }
  }

  private int getEverywhereDistanceMultipler() {
    return rc.getType() == RobotType.SCOUT ? 241 : 376;
  }
}
