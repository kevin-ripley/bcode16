package team389;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import team389.AlliedArchonTracker.AlliedArchonInfo;

public class FightingRangedBehavior implements Behavior, PreBehavior {

  private static final int MAX_ROBOTS = 100;

  private final RobotController rc;
  private final Radar radar;
  private final AttackSystem attackSystem;
  private final ZombieSpawnScheduleInfo zombieSchedule;

  private AlliedArchonInfo closestAlliedArchon;

  private boolean isAdjacentToEnemy;
  private int numEnemiesAttackingUs;

  private RobotInfo[] visibleEnemies;
  private int numVisibleEnemies;
  private RobotInfo[] attackableEnemies;
  private int numAttackableEnemies;
  private RobotInfo[] attackableZombies;
  private int numAttackableZombies;
  private RobotInfo[] allies;
  private RobotInfo[] hostiles;
  private int numZombies;
  private RobotInfo closestEnemy;
  private int closestEnemyDist;
  private RobotInfo closestAttackingZombie;
  private int closestAttackingZombieDist;
  private RobotInfo closestZombieDen;
  private int closestZombieDenDist;
  private double leastHealthDenHealth;
  private RobotInfo leastHealthDen;

  private int enemyStrength;
  private int zombieStrength;
  private boolean canOneHitEnemy;
  private boolean enemyCanShootAtUs;
  private final NavigationSystem navigation;
  private boolean commited;
  private boolean inHealingState;

  public FightingRangedBehavior(RobotController rc, Radar radar, NavigationSystem navigation,
      ZombieSpawnScheduleInfo zombieSchedule,
      AttackSystem attackSystem) {
    this.rc = rc;
    this.radar = radar;
    this.visibleEnemies = new RobotInfo[MAX_ROBOTS];
    this.numVisibleEnemies = 0;
    this.attackableEnemies = new RobotInfo[MAX_ROBOTS];
    this.numAttackableEnemies = 0;
    this.attackableZombies = new RobotInfo[MAX_ROBOTS];
    this.numAttackableZombies = 0;
    this.navigation = navigation;
    this.attackSystem = attackSystem;
    this.zombieSchedule = zombieSchedule;
    this.commited = true;
  }

  @Override
  public void preRun() throws GameActionException {}

  @Override
  public void run() throws GameActionException {
    RobotPlayer.profiler.split("before clear cache");
    clearCache();
    RobotPlayer.profiler.split("after clear cache");
    cacheNeededVariables();
    RobotPlayer.profiler.split("after cache needed variables");
    if (numVisibleEnemies == 0 && numZombies == 0) {
      navigation.moveRandomly();
      RobotPlayer.profiler.split("after move randomly, fighting");
    } else {
      fight();
      RobotPlayer.profiler.split("after fight");
    }
  }

  private void clearCache() {
    isAdjacentToEnemy = false;
    numEnemiesAttackingUs = 0;
    numVisibleEnemies = 0;
    numAttackableEnemies = 0;
    numAttackableZombies = 0;
    enemyStrength = 0;
    zombieStrength = 0;
    numZombies = 0;
    closestEnemy = null;
    closestEnemyDist = 99999;
    canOneHitEnemy = false;
    enemyCanShootAtUs = false;
    closestAttackingZombie = null;
    closestAttackingZombieDist = 99999;
    closestZombieDen = null;
    closestZombieDenDist = 99999;
    leastHealthDen = null;
    leastHealthDenHealth = 999999;
  }

  private void cacheNeededVariables() {
    hostiles = radar.getNearbyHostiles();
    int attackRadiusSquared = rc.getType().attackRadiusSquared;
    for (int i = hostiles.length; --i >= 0;) {
      RobotInfo robot = hostiles[i];
      if (robot.location.distanceSquaredTo(rc.getLocation()) <= 2) {
        isAdjacentToEnemy = true;
      }
      if (robot.team == rc.getTeam().opponent()) { // Enemy
        visibleEnemies[numVisibleEnemies] = robot;
        ++numVisibleEnemies;

        int dist = rc.getLocation().distanceSquaredTo(robot.location);
        if (robot.type.canAttack() && robot.type.attackRadiusSquared >= dist) {
          ++numEnemiesAttackingUs;
        }

        if (attackRadiusSquared >= dist) {
          attackableEnemies[numAttackableEnemies] = robot;
          ++numAttackableEnemies;
        }

        if (dist < closestEnemyDist) {
          closestEnemyDist = dist;
          closestEnemy = robot;
        }

        if (robot.health <= RobotType.SOLDIER.attackPower) {
          canOneHitEnemy = true;
        }

        if (robot.weaponDelay < 2) {
          enemyCanShootAtUs = true;
        }

        enemyStrength += getStrengthOfRobot(robot);
      } else { // Zombie
        int dist = rc.getLocation().distanceSquaredTo(robot.location);
        if (RobotType.SOLDIER.attackRadiusSquared >= dist) {
          attackableZombies[numAttackableZombies] = robot;
          ++numAttackableZombies;
        }

        zombieStrength += getStrengthOfRobot(robot);
        if (robot.type == RobotType.ZOMBIEDEN) {
          if (robot.health < leastHealthDenHealth) {
            leastHealthDen = robot;
            leastHealthDenHealth = robot.health;
          }
          if (dist < closestZombieDenDist) {
            closestZombieDenDist = dist;
            closestZombieDen = robot;
          }
        } else {
          if (dist < closestAttackingZombieDist) {
            closestAttackingZombieDist = dist;
            closestAttackingZombie = robot;
          }
        }
        ++numZombies;
      }
    }
    allies = radar.getNearbyAllies();
    if (numEnemiesAttackingUs == 0) {
      commited = false;
    }

    inHealingState = false; // TODO healing?
  }

  private void fight() throws GameActionException {

    MapLocation myLoc = rc.getLocation();
    boolean isInfected = rc.getInfectedTurns() > 1;
    boolean willDieFromViper = rc.getViperInfectedTurns() * 2 >= rc.getHealth();
    boolean willDieFromEnemy = numEnemiesAttackingUs * 3 > rc.getHealth();
    boolean closeToArchon = closestAlliedArchon != null && myLoc.distanceSquaredTo(
        closestAlliedArchon.loc) > 24;
    boolean willDie = (willDieFromEnemy || willDieFromViper) && !closeToArchon;

    if (rc.isCoreReady() && isInfected && willDie && numVisibleEnemies > 0 && rc.getHealth() < 15) {
      Direction[] dirsToEnemy = NavigationUtil.getAllDirections(myLoc, closestEnemy.location);
      int minAlly = 9;
      Direction bestDir = null;
      for (Direction d : dirsToEnemy) {
        if (rc.canMove(d)) {
          int numNearbyAlly = rc.senseNearbyRobots(myLoc.add(d), 2, rc.getTeam()).length;
          if (numNearbyAlly == 0) {
            rc.move(d);
            return;
          } else if (numNearbyAlly < minAlly) {
            minAlly = numNearbyAlly;
            bestDir = d;
          }
        }
      }
      if (bestDir != null) {
        rc.move(bestDir);
      }
    }

    if (isAdjacentToEnemy && (zombieStrength != 0 || enemyStrength != 0)) {
      MapLocation target;
      if (numAttackableEnemies > 0) {
        target = attackSystem.getBestEnemyToShoot(rc, attackableEnemies,
            numAttackableEnemies, allies,
            allies.length);
      } else {
        target = attackSystem.getBestEnemyToShoot(rc, attackableZombies,
            numAttackableZombies, allies,
            allies.length);
      }
      if (rc.getType() == RobotType.GUARD && commited) {
        attack(rc, target);
        retreat(rc);
      } else {
        retreat(rc);
        attack(rc, target);
      }
      return;
    }

    // Zombies
    if (numZombies > 0) {
      rc.setIndicatorString(0, "0");
      if (inHealingState) {
        rc.setIndicatorString(1, "healing state");
        // heal();
        return;
      }

      MapLocation target;
      // Prefer shooting enemies when closer to enemy than zombie, or if there's
      // not many allies nearby
      boolean manyAllies = allies.length > 5;
      if (numAttackableEnemies > 0 && (!manyAllies
          || closestEnemyDist < closestAttackingZombieDist)) {
        target = attackSystem.getBestEnemyToShoot(rc, attackableEnemies,
            numAttackableEnemies, allies,
            allies.length);
      } else {
        target = attackSystem.getBestEnemyToShoot(rc, attackableZombies,
            numAttackableZombies, allies,
            allies.length);
      }

      attack(rc, target);
      if (zombieStrength == 0 && enemyStrength == 0) { // Just dens
        rc.setIndicatorString(2, "zombie round " + zombieSchedule.getNextZombieRound());
        if (zombieSchedule.getNextZombieRound() - rc.getRoundNum() > 5) {
          // Should just be a zombie den, move closer
          if (rc.getLocation().distanceSquaredTo(leastHealthDen.location) <= 2) {
            return;
          } else {
            rc.setIndicatorString(1, "Moving to " + leastHealthDenHealth + " " + rc.getRoundNum());
            navigation.directToOnlyForwardAndSides(leastHealthDen.location,
                false /* avoidAttackers */,
                true /* clearRubble */);
          }
        } else { // Dens about to spawn, backup
          Direction backupDir = leastHealthDen.location.directionTo(rc
              .getLocation());
          rc.setIndicatorString(2, "Schedule about to start " + backupDir);
          if (closestZombieDenDist < 13) {
            rc.setIndicatorString(1, "Backing up cause too close " + rc.getRoundNum());
            navigation.directToOnlyForwardAndSides(rc.getLocation().add(backupDir),
                false /* avoidAttackers */,
                true /* clearRubble */);
          } else { // We might be blocking people from backing up
            rc.setIndicatorString(1, "Backing up to clear the path" + rc.getRoundNum());
            if (rc.senseNearbyRobots(leastHealthDen.location, 12, rc.getTeam()).length > 0) {
              navigation.directToOnlyForwardAndSides(rc.getLocation().add(backupDir),
                  false /* avoidAttackers */,
                  true /* clearRubble */);
            }
          }
        }
        return;
      } else if (zombieStrength > 0) {
        zombieMicro();
        return;
      }
    }

    // Just a measly scout, try and chase it I suppose?
    if (enemyStrength == 0 && numVisibleEnemies > 0 && !inHealingState) {
      rc.setIndicatorString(0, "1");
      MapLocation target = attackSystem.getBestEnemyToShoot(rc, attackableEnemies,
          numAttackableEnemies, allies, allies.length);
      attack(rc, target);
      navigation.directTo(hostiles[0].location, false /* avoidAttackers */,
          true /* clearRubble */);
      return;
    }

    if (numEnemiesAttackingUs >= 1) { // In combat
      rc.setIndicatorString(0, "2");
      if (numAttackableEnemies == 0) { // Ruh roh, getting outranged by
                                       // something (pretty sure this is only
                                       // viper or turret vs soldier and most
                                       // things vs guard)
        if (guessIfFightIsWinning() || commited) {
          navigation.directTo(closestEnemy.location, false /* avoidAttacks */,
              false /* clearRubble */);
          commited = true;
        } else {
          retreat(rc);
        }
        return;
      }
      RobotPlayer.profiler.split("before max allies");
      int maxAlliesWhoCanAttackEnemy = getMaxAlliesWhoCanAttackEnemy(rc) + 1;
      RobotPlayer.profiler.split("after max allies");
      // A lone enemy!
      if (numEnemiesAttackingUs == 1) {
        RobotInfo singleEnemy = attackableEnemies[0]; // TODO Will get messed up
                                                      // with someone with
                                                      // longer range

        if (singleEnemy != null && maxAlliesWhoCanAttackEnemy == 1) {
          // 1v1 obs on final destination, fight if we are winning, retreat
          // otherwise
          boolean weAreWinning1v1 = rc.getHealth() >= singleEnemy.health;
          if (weAreWinning1v1) {
            MapLocation target = attackSystem.getBestEnemyToShoot(rc, attackableEnemies,
                numAttackableEnemies, allies, allies.length);
            attack(rc, target);
            return;
          } else {
            retreatOrFight(rc);
            MapLocation target = attackSystem.getBestEnemyToShoot(rc, attackableEnemies,
                numAttackableEnemies, allies, allies.length);
            attack(rc, target);
            return;
          }
        } else {
          // We outnumber the lone enemy don't retreat
          if (singleEnemy != null && inHealingState && rc.getHealth() < singleEnemy.health) {
            retreatOrFight(rc);
          } else {
            MapLocation target = attackSystem.getBestEnemyToShoot(rc, attackableEnemies,
                numAttackableEnemies, allies, allies.length);
            attack(rc, target);
          }
          return;
        }
      } else if (inHealingState || numEnemiesAttackingUs > maxAlliesWhoCanAttackEnemy
          || !guessIfFightIsWinning()) {
        rc.setIndicatorString(0, "3");
        RobotPlayer.profiler.split("before retreat or fight ");
        retreatOrFight(rc);
        RobotPlayer.profiler.split("after retreat or fight");
        return;
      } else {
        // Good enough double team
        MapLocation target = attackSystem.getBestEnemyToShoot(rc, attackableEnemies,
            numAttackableEnemies, allies, allies.length);
        attack(rc, target);
        return;
      }
    } else if (!inHealingState) { // Not directly in combat
      rc.setIndicatorString(0, "4");
      MapLocation target = attackSystem.getBestEnemyToShoot(rc, attackableEnemies,
          numAttackableEnemies, allies, allies.length);
      if (target != null) { // We can hit but aren't getting hit? Must be a
                            // guard, retreat
        attack(rc, target);
        retreatOrFight(rc);
        return;
      }
      if (closestEnemy != null) {
        int numAlliesFighting = getNumAlliesWhoCanAttackLocation(rc, closestEnemy.location);
        if (numAlliesFighting > 0) {
          int maxEnemyExposure = Math.min(numAlliesFighting + 1, 3);
          if (navigation.directToWithMaximumEnemyExposure(closestEnemy.location,
              maxEnemyExposure)) {
            return;
          }
        }
      }
    } else {
      rc.setIndicatorString(0, "5");
      // heal();
      return;
    }
    // Didn't have to do anything walk to enemy!
  }

  private void heal() throws GameActionException {
    rc.setIndicatorString(0, "HEAL");

    // If there's no archons with known positions, should never happen
    if (closestAlliedArchon == null) {
      return;
    }

    MapLocation target;
    if (numAttackableEnemies > 0) {
      target = attackSystem.getBestEnemyToShoot(rc, attackableEnemies,
          numAttackableEnemies, allies,
          allies.length);
    } else {
      target = attackSystem.getBestEnemyToShoot(rc, attackableZombies,
          numAttackableZombies, allies,
          allies.length);
    }
    if (target != null) {
      attack(rc, target);
    }
    boolean nearArchon = rc.getLocation()
        .distanceSquaredTo(closestAlliedArchon.loc) < 13;
    if (nearArchon) {
      return;
    } else {
      if (!navigation.directTo(closestAlliedArchon.loc, true /* avoidAttackers */,
          true /* clearRubble */)) {
        navigation.directTo(closestAlliedArchon.loc, false /* avoidAttackers */,
            true /* clearRubble */);
      }
    }
  }

  private void zombieMicro() throws GameActionException {
    MapLocation myLoc = rc.getLocation();
    Direction zombieDir = myLoc.directionTo(closestAttackingZombie.location);
    Direction retreatDir = zombieDir.opposite();
    MapLocation retreatLoc = myLoc.add(retreatDir);
    rc.setIndicatorString(2, "Zombie microing " + rc.getRoundNum());
    switch (closestAttackingZombie.type) {
      case BIGZOMBIE:
        if (closestAttackingZombieDist > 8) {
          if (closestAttackingZombieDist <= 18) {
            retreatIfBlocking(myLoc, closestAttackingZombie.location);
          }
          navigation.directToWithoutBlockingAllyRetreat(zombieDir, closestAttackingZombie.location);
        } else if (closestAttackingZombieDist <= 2) {
          retreatTryNonDiagonal(retreatLoc, retreatDir);
        } else {
          retreatIfBlocking(myLoc, closestAttackingZombie.location);
        }
        break;
      case STANDARDZOMBIE:
        if (closestAttackingZombieDist >= 16) {
          navigation.directToWithoutBlockingAllyRetreat(zombieDir, closestAttackingZombie.location);
        } else if (closestAttackingZombieDist <= 2) {
          retreatTryNonDiagonal(retreatLoc, retreatDir);
        } else {
          retreatIfBlocking(myLoc, closestAttackingZombie.location);
        }
        break;
      case RANGEDZOMBIE:
        retreatTryNonDiagonal(retreatLoc, retreatDir);
        break;
      case FASTZOMBIE:
        if (closestAttackingZombieDist == 8) {
          retreatTryNonDiagonal(retreatLoc, retreatDir);
        } else if (closestAttackingZombieDist < 8) {
          retreatTryNonDiagonal(retreatLoc, retreatDir);
        } else {
          retreatIfBlocking(myLoc, closestAttackingZombie.location);
        }
        break;
      default:
        break;
    }
  }

  private void retreatTryNonDiagonal(
      MapLocation retreatLoc, Direction retreatDir) throws GameActionException {
    if (!navigation.directToOnlyNonDiagonal(retreatDir)) {
      navigation.directToOnlyForward(retreatLoc, false, false);
    }
  }

  private void retreatIfBlocking(
      MapLocation myLoc, MapLocation retreatFromLoc) throws GameActionException {
    Direction dir = myLoc.directionTo(retreatFromLoc);
    rc.setIndicatorString(2, "Checking retreat if blocking " + rc.getRoundNum());
    if (isAllyInDir(dir) || isAllyInDir(dir.rotateLeft()) || isAllyInDir(dir.rotateRight())) {
      Direction retreatDir = dir.opposite();
      MapLocation retreatLoc = myLoc.add(retreatDir);
      retreatTryNonDiagonal(retreatLoc, retreatDir);
    }
  }

  private boolean isAllyInDir(Direction dir) throws GameActionException {
    MapLocation allyLoc = rc.getLocation().add(dir);
    RobotInfo robot = rc.canSense(allyLoc) ? rc.senseRobotAtLocation(allyLoc) : null;
    return robot != null && robot.team == rc.getTeam();
  }

  private boolean guessIfFightIsWinning() {
    int allyStrength = allies.length * 10;
    if (enemyStrength == 0) {
      return true;
    } else if (enemyStrength <= 10) {
      return allyStrength >= 20;
    } else if (enemyStrength <= 20) {
      return allyStrength >= 40;
    } else if (enemyStrength <= 30) {
      return allyStrength >= 50;
    } else {
      return allyStrength >= 1.5 * enemyStrength;
    }
  }

  private void retreatOrFight(RobotController rc) throws GameActionException {
    // If all our opponents have really high action delay, we can fire a last
    // shot
    // and still be able to move before they can return fire. This would most
    // probably
    // happen if an enemy engaged us after several diagonal moves. This could
    // turn
    // a losing 1v1 into a winning one! Also, if we can one-hit an enemy we
    // should
    // do so instead of retreating even if we take hits to do so

    if (!inHealingState && (canOneHitEnemy || !enemyCanShootAtUs)) {
      rc.setIndicatorString(0, "Round " + rc.getRoundNum()
          + " Can one shot or not be hit, staying");
      RobotPlayer.profiler.split("staying");
      MapLocation target = attackSystem.getBestEnemyToShoot(rc, attackableEnemies,
          numAttackableEnemies, allies, allies.length);
      attack(rc, target);
      return;
    }
    RobotPlayer.profiler.split("retreating");
    retreat(rc);
    RobotPlayer.profiler.split("after retreating");
    MapLocation target = attackSystem.getBestEnemyToShoot(rc, attackableEnemies,
        numAttackableEnemies, allies, allies.length);
    attack(rc, target);
  }

  private int getStrengthOfRobot(RobotInfo robotInfo) {
    switch (robotInfo.type) {
      case SOLDIER:
        return 10;
      case GUARD:
        return 2;
      case VIPER:
        return 4;
      case TTM:
        return 1;
      case TURRET:
        return 30;
      case SCOUT:
        return 0;
      case ARCHON:
        return 0;
      case STANDARDZOMBIE:
        return 2;
      case RANGEDZOMBIE:
        return 10;
      case FASTZOMBIE:
        return 10;
      case BIGZOMBIE:
        return 5;
      case ZOMBIEDEN:
      default:
        return 0;
    }
  }

  public Direction getBestRetreatDirection(RobotController rc) {
    int repelX = 0;
    int repelY = 0;
    int length = Math.min(5, hostiles.length);
    for (int i = length; --i >= 0;) {
      Direction repelDir = hostiles[i].location.directionTo(rc.getLocation());
      repelX += repelDir.dx;
      repelY += repelDir.dy;
    }
    int absRepelX = Math.abs(repelX);
    int absRepelY = Math.abs(repelY);
    Direction retreatDir;
    if (absRepelX >= 1.5 * absRepelY) {
      retreatDir = repelX > 0 ? Direction.EAST : Direction.WEST;
    } else if (absRepelY >= 1.5 * absRepelX) {
      retreatDir = repelY > 0 ? Direction.SOUTH : Direction.NORTH;
    } else if (repelX > 0) {
      retreatDir = repelY > 0 ? Direction.SOUTH_EAST : Direction.NORTH_EAST;
    } else {
      retreatDir = repelY > 0 ? Direction.SOUTH_WEST : Direction.NORTH_WEST;
    }

    int bestMinEnemyDistSq = 999999;
    for (int j = length; j-- > 0;) {
      int enemyDistSq = rc.getLocation().distanceSquaredTo(hostiles[j].location);
      if (enemyDistSq < bestMinEnemyDistSq)
        bestMinEnemyDistSq = enemyDistSq;
    }
    Direction bestDir = null;
    int[] tryDirs = new int[] {
      0, 1, -1, 2, -2, 3, -3, 4
    };
    for (int i = 0; i < tryDirs.length; i++) {
      Direction tryDir = Direction.values()[(retreatDir.ordinal() + tryDirs[i] + 8) % 8];
      if (!rc.canMove(tryDir))
        continue;
      MapLocation tryLoc = rc.getLocation().add(tryDir);

      int minEnemyDistSq = 999999;
      for (int j = length; --j >= 0;) {
        int enemyDistSq = tryLoc.distanceSquaredTo(hostiles[j].location);
        if (enemyDistSq < minEnemyDistSq)
          minEnemyDistSq = enemyDistSq;
      }
      if (minEnemyDistSq > RobotType.SOLDIER.attackRadiusSquared) {
        return tryDir; // we can escape!!
      }
      if (minEnemyDistSq > bestMinEnemyDistSq) {
        bestMinEnemyDistSq = minEnemyDistSq;
        bestDir = tryDir;
      }
    }

    return bestDir;
  }

  private void attack(RobotController rc, MapLocation loc) throws GameActionException {
    if (rc.isWeaponReady() && loc != null) {
      rc.attackLocation(loc);
    }
  }

  private void retreat(RobotController rc) throws GameActionException {
    if (commited) {
      return;
    }
    Direction d = getBestRetreatDirection(rc);
    rc.setIndicatorString(1, "Retreat dir " + d + " health " + rc.getHealth());
    if (d != null) {
      if (navigation.directToOnlyForwardAndSides(rc.getLocation().add(d), false /* avoidAttacker */,
          false /* clearRubble */)) {
        return;
      }
    }
    // A failsafe for at least running somewhere
    navigation.directTo(rc.getLocation().add(rc.getLocation().directionTo(
        hostiles[0].location).opposite()), false /* avoidAttacker */, false /* clearRubble */);

  }

  public int getMaxAlliesWhoCanAttackEnemy(RobotController rc) {
    int maxAlliesAttackingEnemy = 0;
    for (int i = numAttackableEnemies; --i >= 0;) {
      // TODO Can definitely optimize this by just calling
      // rc.senseNearbyRobots(enemies[i].location,
      // rc.getType().attackRadiusSquared, rc.getTeam())
      // Only want soldiers :(
      int allyWhoCanHitCount = rc.senseNearbyRobots(attackableEnemies[i].location, rc
          .getType().attackRadiusSquared, rc.getTeam()).length;
      maxAlliesAttackingEnemy = Math.max(maxAlliesAttackingEnemy, allyWhoCanHitCount);
    }
    return maxAlliesAttackingEnemy;
  }

  public int getNumAlliesWhoCanAttackLocation(RobotController rc, MapLocation location) {
    int numAlliesWhoCanAttackLocation = 0;
    for (int i = allies.length; --i >= 0;) {
      if (allies[i].location.distanceSquaredTo(location) <= allies[i].type.attackRadiusSquared) {
        ++numAlliesWhoCanAttackLocation;
      }
    }
    return numAlliesWhoCanAttackLocation;
  }

  public void setClosestAlliedArchon(AlliedArchonInfo alliedArchonInfo) {
    closestAlliedArchon = alliedArchonInfo;
  }
}
