package team389;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Signal;
import battlecode.common.Team;

public class BuddyScoutBehavior implements Behavior {

  private final RobotController rc;
  private final NavigationSystem navigation;
  private final BuddySystem buddySystem;
  private final Radar radar;
  private final BasicMessages basicMessages;
  private final MessageSender messageSender;

  public BuddyScoutBehavior(
      RobotController rc,
      NavigationSystem navigation,
      BuddySystem buddySystem,
      BasicMessages basicMessages,
      Radar radar,
      MessageSender messageSender) {
    this.rc = rc;
    this.navigation = navigation;
    this.radar = radar;
    this.buddySystem = buddySystem;
    this.basicMessages = basicMessages;
    this.messageSender = messageSender;
  }

  @Override
  public void run() throws GameActionException {
    if (!buddySystem.hasBuddy()) {
      return;
    }
    RobotInfo turret = rc.senseRobot(buddySystem.getBuddy());
    rc.setIndicatorString(0, "I'm sitting by turret buddy " + buddySystem.getBuddy());
    RobotInfo closestHostile = RadarUtils.getClosestRobot(radar
        .getNearbyHostiles(), rc.getLocation());
    if (closestHostile != null) {
      Direction awayFromEnemies = rc.getLocation().directionTo(closestHostile.location).opposite();
      MapLocation desiredLocation = turret.location.add(awayFromEnemies);
      if (desiredLocation.distanceSquaredTo(turret.location) == 2) {
        desiredLocation = Math.random() < .5 ? turret.location.add(awayFromEnemies.rotateLeft())
            : turret.location.add(awayFromEnemies.rotateRight());
      }
      rc.setIndicatorString(2, "Desired location " + desiredLocation);
      if (!rc.getLocation().equals(desiredLocation)) {
        if (!navigation.directTo(
            desiredLocation,
            true /* avoidAttackers */,
            false /* clearRubble */)) {
          navigation.directTo(desiredLocation, false /* avoidAttackers */, false /* clearRubble */);
        }
      }
    } else {
      if (rc.getLocation().distanceSquaredTo(turret.location) > 2) {
        if (!navigation.directTo(
            turret.location,
            true /* avoidAttackers */,
            false /* clearRubble */)) {
          navigation.directTo(turret.location, false /* avoidAttackers */, false /* clearRubble */);
        }
      }
    }
    shareExtendedBroadcast();
    return;
  }

  public boolean shouldBuddy() throws GameActionException {
    if (buddySystem.hasBuddy() &&
        rc.canSenseRobot(buddySystem.getBuddy()) &&
        amLowestIdAdjacent()) { // Turret's
                                // still
                                // there
      return true;
    }
    buddySystem.removeBuddy();
    // Check messages for buddy in need
    Signal[] messages = basicMessages.getAllyBasicMessages();
    int closestDist = 99999;
    RobotInfo closestTurretNeedingBuddy = null;
    for (int i = messages.length; --i >= 0;) {
      int robotId = messages[i].getID();
      if (rc.canSenseRobot(robotId)) {
        RobotInfo robotInfo = rc.senseRobot(robotId);
        if (robotInfo == null) {
          continue;
        }
        int dist = robotInfo.location.distanceSquaredTo(rc.getLocation());
        if ((robotInfo.type == RobotType.TURRET || robotInfo.type == RobotType.TTM) &&
            (closestTurretNeedingBuddy == null || dist < closestDist)) {
          closestTurretNeedingBuddy = robotInfo;
          closestDist = dist;
          break;
        }
      }
    }

    // Check for a turret who thinks we are their buddy
    Direction d = Direction.NORTH;
    for (int i = 8; --i >= 0;) {
      RobotInfo r = rc.senseRobotAtLocation(rc.getLocation().add(d));
      if (r != null && r.team == rc.getTeam() && (r.type == RobotType.TURRET
          || r.type == RobotType.TTM)) {
        RobotInfo[] adjacentRobots = rc.senseNearbyRobots(r.location, 2, rc.getTeam());
        boolean foundOtherScout = false;
        for (int j = adjacentRobots.length; --j >= 0;) {
          RobotInfo adjacentRobot = adjacentRobots[j];
          if (adjacentRobot.type == RobotType.SCOUT &&
              adjacentRobot.ID < rc.getID()) {
            foundOtherScout = true;
            break;
          }
        }
        if (!foundOtherScout) {
          closestTurretNeedingBuddy = r;
          break;
        }
      }
      d = d.rotateLeft();
    }
    if (closestTurretNeedingBuddy != null) {
      buddySystem.setBuddy(closestTurretNeedingBuddy.ID);
    }
    return buddySystem.hasBuddy();
  }

  private void shareExtendedBroadcast()
      throws GameActionException {
    RobotInfo broadcastedRobot = null;
    RobotInfo buddy = null;
    if (buddySystem.hasBuddy() && rc.canSenseRobot(buddySystem.getBuddy())) {
      buddy = rc.senseRobot(buddySystem.getBuddy());
    } else {
      buddy = getClosestFriendlyTurret();
    }
    if (buddy != null) {
      broadcastedRobot = getBuddyTarget(buddy);
    }
    if (broadcastedRobot != null) {
      messageSender.sendDistantHostileInfo(broadcastedRobot.location, broadcastedRobot.coreDelay,
          broadcastedRobot.team == Team.ZOMBIE, buddy.location.distanceSquaredTo(rc.getLocation()));
    }
  }

  private RobotInfo getClosestFriendlyTurret() {
    RobotInfo[] allies = radar.getNearbyAllies();
    int closestDist = 999999;
    RobotInfo closestTurret = null;
    for (int i = allies.length; --i >= 0;) {
      RobotInfo robot = allies[i];
      if (robot.type != RobotType.TURRET) {
        continue;
      }
      int dist = rc.getLocation().distanceSquaredTo(robot.location);
      if (dist < closestDist) {
        closestTurret = robot;
      }
    }
    return closestTurret;
  }

  private RobotInfo getBuddyTarget(RobotInfo buddy) {
    RobotInfo furthestEnemy = null;
    RobotInfo[] enemies = radar.getNearbyEnemies();
    int furthestDist = 0;
    for (int i = enemies.length; --i >= 0;) {
      RobotInfo enemy = enemies[i];
      int dist = enemy.location.distanceSquaredTo(buddy.location);
      if (dist > RobotType.TURRET.attackRadiusSquared) {
        continue;
      }
      if (canHit(enemy)) {
        return enemy;
      }
      if (furthestEnemy == null || dist > furthestDist) {
        furthestEnemy = enemy;
        furthestDist = dist;
      }
    }
    if (furthestEnemy != null) {
      return furthestEnemy;
    }

    RobotInfo[] zombies = radar.getNearbyZombies();
    for (int i = zombies.length; --i >= 0;) {
      RobotInfo zombie = zombies[i];
      int dist = zombie.location.distanceSquaredTo(buddy.location);
      if (dist > RobotType.TURRET.attackRadiusSquared) {
        continue;
      }
      if (canHit(zombie)) {
        return zombie;
      }
      if (furthestEnemy == null || dist > furthestDist) {
        furthestEnemy = zombie;
        furthestDist = dist;
      }
    }
    return furthestEnemy;

  }

  private boolean canHit(RobotInfo robot) {
    return robot.coreDelay >= 2;
  }

  private boolean amLowestIdAdjacent() throws GameActionException {
    RobotInfo buddy = rc.senseRobot(buddySystem.getBuddy());
    RobotInfo[] nearbyRobots = rc.senseNearbyRobots(buddy.location, 2, rc.getTeam());
    for (int i = nearbyRobots.length; --i >= 0;) {
      if (nearbyRobots[i].type == RobotType.SCOUT && nearbyRobots[i].ID < rc.getID()) {
        return false;
      }
    }
    return true;
  }
}
