package team389;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import team389.AlliedArchonTracker.AlliedArchonInfo;
import team389.EnemyArchonTracker.EnemyArchonInfo;

public class ZombieDraggingScoutBehavior implements Behavior {
  private static final int DIVEBOMB_DISTANCE = 24;

  private final RobotController rc;
  private final NavigationSystem navigation;
  private final Radar radar;
  private final EnemyArchonTracker enemyArchonTracker;
  private final AlliedArchonTracker alliedArchonTracker;

  private EnemyArchonInfo bestEnemyToDragTo;

  public ZombieDraggingScoutBehavior(RobotController rc, NavigationSystem navigation, Radar radar,
      EnemyArchonTracker enemyArchonTracker, AlliedArchonTracker alliedArchonTracker) {
    this.rc = rc;
    this.navigation = navigation;
    this.radar = radar;
    this.enemyArchonTracker = enemyArchonTracker;
    this.alliedArchonTracker = alliedArchonTracker;
  }

  @Override
  public void run() throws GameActionException {
    rc.setIndicatorString(0, "DRAGGING");

    MapLocation loc = bestEnemyToDragTo.loc;
    rc.setIndicatorString(1, "Dragging to enemy " + loc + " " + rc.getRoundNum());
    if (!isAttackableByZombieNextTwoTurns()) {
      return;
    }
    if (rc.getLocation().distanceSquaredTo(loc) <= DIVEBOMB_DISTANCE) {

      if (rc.getLocation().distanceSquaredTo(loc) <= 13 && rc.getInfectedTurns() > 0) {
        rc.disintegrate();
      }
      if (rc.getInfectedTurns() > 0) { // Divebomb
        if (navigation.directToOnlyForward(loc,
            false /* avoidEnemies */,
            false /* clearRubble */)) {
          rc.setIndicatorString(1, "Dragging " + loc + " " + rc
              .getRoundNum() + " with divebomb " + true);
          return;
        }
      } else { // Wait to be infected
        return;
      }
    } else {
      if (!navigation.directToOnlyForward(loc,
          true /* avoidEnemies */,
          false /* clearRubble */)) {
        navigation.directToOnlyForward(loc,
            false /* avoidEnemies */,
            false /* clearRubble */);
        return;
      }
    }
  }

  private boolean isAttackableByZombieNextTwoTurns() {
    RobotInfo[] zombies = radar.getNearbyZombies();
    for (int i = zombies.length; --i >= 0;) {
      RobotInfo zombie = zombies[i];
      int dist = zombie.location.distanceSquaredTo(rc.getLocation());
      if (dist >= 10) {
        if (zombie.type == RobotType.RANGEDZOMBIE) {
          if (dist <= 13) {
            return true;
          } else if (dist <= 24 && zombie.coreDelay < 2) {
            return true;
          }
        }
        continue;
      } else if (dist <= 2) {
        if (zombie.weaponDelay < 3) {
          return true;
        }
      } else {
        if (zombie.coreDelay < 2 && zombie.weaponDelay < 2) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean shouldDrag() {
    RobotInfo[] zombies = radar.getNearbyZombies();
    int outbreakLevel = rc.getRoundNum() / 300;
    double multiplier;
    switch (outbreakLevel) {
      case 0:
        multiplier = 1;
        break;
      case 1:
        multiplier = 1.1;
        break;
      case 2:
        multiplier = 1.2;
        break;
      case 3:
        multiplier = 1.3;
        break;
      case 4:
        multiplier = 1.5;
        break;
      case 5:
        multiplier = 1.7;
        break;
      case 6:
        multiplier = 2.0;
        break;
      case 7:
        multiplier = 2.3;
        break;
      case 8:
        multiplier = 2.6;
        break;
      case 9:
        multiplier = 3.0;
        break;
      default:
        outbreakLevel -= 9;
        multiplier = 3.0 + outbreakLevel;
        break;
    }
    int score = 0;
    for (int i = zombies.length; --i >= 0;) {
      RobotInfo zombie = zombies[i];
      switch (zombie.type) {
        case STANDARDZOMBIE:
          score += 2;
          break;
        case RANGEDZOMBIE:
          score += 10;
          break;
        case BIGZOMBIE:
          score += 10;
          break;
        case FASTZOMBIE:
          score += 10;
          break;
        default:
          break;
      }
    }
    rc.setIndicatorString(2, " " + (score * multiplier));
    if (score * multiplier >= 20) {
      EnemyArchonInfo[] enemyArchonInfos = enemyArchonTracker.getEnemyArchons();
      AlliedArchonInfo[] alliedArchonInfos = alliedArchonTracker.getAlliedArchons();
      bestEnemyToDragTo = getBestEnemyArchonToGoTo(enemyArchonInfos, alliedArchonInfos);
      return bestEnemyToDragTo != null;
    }
    return score * multiplier >= 20;
  }

  private EnemyArchonInfo getBestEnemyArchonToGoTo(EnemyArchonInfo[] enemyArchons,
      AlliedArchonInfo[] alliedArchons) {
    RobotInfo[] zombies = radar.getNearbyZombies();
    // Get the direction of the zombie
    int length = Math.min(8, zombies.length);
    int[] zombiesInDirection = new int[10];
    for (int i = length; --i >= 0;) {
      Direction zombieDirection = rc.getLocation().directionTo(zombies[i].location);
      ++zombiesInDirection[zombieDirection.ordinal()];
    }
    length = Math.min(8, alliedArchons.length);
    int[] alliesInDirection = new int[10];
    for (int i = length; --i >= 0;) {
      if (rc.getRoundNum() - alliedArchons[i].timestamp >= 250) {
        continue;
      }
      Direction allyDirection = rc.getLocation().directionTo(alliedArchons[i].loc);
      ++alliesInDirection[allyDirection.ordinal()];
    }

    int closestDist = 99999;
    EnemyArchonInfo bestEnemyToDragTo = null;
    for (int i = enemyArchons.length; --i >= 0;) {
      if (rc.getRoundNum() - enemyArchons[i].timestamp >= 500) {
        continue;
      }
      Direction d = rc.getLocation().directionTo(enemyArchons[i].loc);
      Direction dLeft = d.rotateLeft();
      Direction dRight = d.rotateRight();

      int dOrdinal = d.ordinal();
      int dLeftOrdinal = dLeft.ordinal();
      int dRightOrdinal = dRight.ordinal();
      if (zombiesInDirection[dOrdinal] > 0 ||
          zombiesInDirection[dLeftOrdinal] > 0 ||
          zombiesInDirection[dRightOrdinal] > 0 ||
          alliesInDirection[dOrdinal] > 0 ||
          alliesInDirection[dLeftOrdinal] > 0 ||
          alliesInDirection[dRightOrdinal] > 0) {
        continue;
      }

      int dist = rc.getLocation().distanceSquaredTo(enemyArchons[i].loc);
      if (dist < closestDist) {
        bestEnemyToDragTo = enemyArchons[i];
        closestDist = dist;
      }
    }
    return bestEnemyToDragTo;
  }

}
