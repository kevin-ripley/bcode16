package team389;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class DuckNavigationSystem implements NavigationSystem {

  private static final float CLEAR_RUBBLE_THRESHOLD = 40000;

  private final RobotController rc;
  private final Radar radar;
  private final AlliedArchonTracker alliedArchonTracker;
  private final EnemyTurretCache enemyTurretCache;

  private MapLocation bugDestination;
  private BugState bugState;
  private WallSide bugWallSide;
  private int bugStartDistSq;
  private Direction bugLastMoveDir;
  private Direction bugLookStartDir;
  private int bugRotationCount;
  private int bugMovesSinceSeenObstacle;

  private enum BugState {
    DIRECT,
    BUG
  }

  private enum WallSide {
    LEFT,
    RIGHT
  }

  public DuckNavigationSystem(
      RobotController rc,
      Radar radar,
      AlliedArchonTracker alliedArchonTracker,
      EnemyTurretCache enemyTurretCache) {
    this.rc = rc;
    this.radar = radar;
    this.alliedArchonTracker = alliedArchonTracker;
    this.enemyTurretCache = enemyTurretCache;
    bugState = BugState.DIRECT;
    bugMovesSinceSeenObstacle = 0;
    bugWallSide = Math.random() < .5 ? WallSide.LEFT : WallSide.RIGHT;
  }

  @Override
  public boolean directTo(
      MapLocation loc,
      boolean avoidAttackers,
      boolean clearRubble) throws GameActionException {
    if (!rc.isCoreReady()) {
      return false;
    }
    NavigationSafetyPolicy policy = getPolicy(avoidAttackers);
    if (!checkForwardDirectTowards(loc, policy, clearRubble)) {
      return directTowards(loc, policy, clearRubble, NavigationUtil.getBackwardsDirections(
          rc.getLocation(),
          loc));
    }
    return true;
  }

  @Override
  public boolean directToOnlyForward(
      MapLocation loc,
      boolean avoidAttackers,
      boolean clearRubble) throws GameActionException {
    if (!rc.isCoreReady()) {
      return false;
    }
    NavigationSafetyPolicy policy = getPolicy(avoidAttackers);
    if (!checkForwardDirectTowards(loc, policy, clearRubble)) {
      return false;
    }
    return true;
  }

  @Override
  public boolean directToOnlyForwardAndSides(
      MapLocation loc,
      boolean avoidAttackers,
      boolean clearRubble) throws GameActionException {
    return rc.isCoreReady()
        ? directTowards(loc, getPolicy(avoidAttackers), clearRubble,
            NavigationUtil.getForwardAndSideDirections(
                rc.getLocation(),
                loc))
        : false;
  }

  @Override
  public boolean directToWithoutBlockingAllyRetreat(
      Direction dir, MapLocation retreatFromLoc) throws GameActionException {
    return rc.isCoreReady()
        ? directTowards(
            rc.getLocation().add(dir),
            new AvoidBlockingAllyRetreatPolicy(retreatFromLoc),
            false /* clearRubble */,
            NavigationUtil.getNonDiagonalDirections(dir))
        : false;
  }

  @Override
  public boolean directToOnlyNonDiagonal(Direction dir) throws GameActionException {
    return rc.isCoreReady()
        ? directTowards(
            rc.getLocation().add(dir),
            getPolicy(false /* avoidAttackers */),
            false /* clearRubble */,
            NavigationUtil.getNonDiagonalDirections(dir))
        : false;
  }

  @Override
  public boolean directToAvoidingAlliedArchons(
      MapLocation loc,
      int avoidDist,
      boolean clearRubble) throws GameActionException {
    return rc.isCoreReady()
        ? directTowards(
            loc,
            new AvoidAlliedArchonsPolicy(alliedArchonTracker, enemyTurretCache, avoidDist),
            clearRubble,
            NavigationUtil.getAllDirections(rc.getLocation(), loc))
        : false;
  };

  @Override
  public boolean directToWithMaximumEnemyExposure(
      MapLocation loc,
      int maximumEnemyExposure) throws GameActionException {
    if (!rc.isCoreReady()) {
      return false;
    }
    int[] numEnemiesAttackingDirs = getNumEnemiesAttackingMoveDirs(radar);

    Direction toEnemy = rc.getLocation().directionTo(loc);
    Direction[] tryDirs = new Direction[] {
      toEnemy, toEnemy.rotateLeft(), toEnemy.rotateRight()
    };
    for (int i = tryDirs.length; --i >= 0;) {
      Direction tryDir = tryDirs[i];
      if (!rc.canMove(tryDir)) {
        continue;
      }
      if (numEnemiesAttackingDirs[tryDir.ordinal()] > maximumEnemyExposure) {
        continue;
      }
      rc.move(tryDir);
      return true;
    }
    return false;
  }

  // Built with
  // public static void tmp() {
  // MapLocation center = new MapLocation(0, 0);
  // for (int ex = -5; ex <= +5; ex++) {
  // System.out.print("{");
  // for (int ey = -5; ey <= +5; ey++) {
  // MapLocation enemyLoc = new MapLocation(ex, ey);
  // ArrayList<Integer> attacked = new ArrayList<Integer>();
  // for (int dir = 0; dir < 8; dir++) {
  // MapLocation moveLoc = center.add(Direction.values()[dir]);
  // if (moveLoc.distanceSquaredTo(enemyLoc) <=
  // RobotType.SOLDIER.attackRadiusSquared)
  // attacked.add(dir);
  // }
  // System.out.print("{");
  // for (int i = 0; i < attacked.size(); i++) {
  // System.out.print(attacked.get(i));
  // if (i < attacked.size() - 1)
  // System.out.print(",");
  // }
  // System.out.print("}");
  // if (ey < +5) {
  // System.out.print(",");
  // int spaces = Math.min(16, 17 - 2 * attacked.size());
  // for (int i = 0; i < spaces; i++)
  // System.out.print(" ");
  // }
  // }
  // System.out.println("}");
  // }
  // }

  private static int[][][] attackNotes = {
    {
      {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
      },
    {
      {}, {}, {
        7
      }, {
        6, 7
      }, {
        5, 6, 7
      }, {
        5, 6, 7
      }, {
        5, 6, 7
      }, {
        5, 6
      }, {
        5
      }, {}, {}
      },
    {
      {}, {
        7
      }, {
        0, 6, 7
      }, {
        0, 5, 6, 7
      }, {
        0, 4, 5, 6, 7
      }, {
        0, 4, 5, 6, 7
      }, {
        0, 4, 5, 6, 7
      }, {
        4, 5, 6, 7
      }, {
        4, 5, 6
      }, {
        5
      }, {}
      },
    {
      {}, {
        0, 7
      }, {
        0, 1, 6, 7
      }, {
        0, 1, 2, 4, 5, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6, 7
      }, {
        0, 2, 3, 4, 5, 6, 7
      }, {
        3, 4, 5, 6
      }, {
        4, 5
      }, {}
      },
    {
      {}, {
        0, 1, 7
      }, {
        0, 1, 2, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6, 7
      }, {
        2, 3, 4, 5, 6
      }, {
        3, 4, 5
      }, {}
      },
    {
      {}, {
        0, 1, 7
      }, {
        0, 1, 2, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6, 7
      }, {
        2, 3, 4, 5, 6
      }, {
        3, 4, 5
      }, {}
      },
    {
      {}, {
        0, 1, 7
      }, {
        0, 1, 2, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6, 7
      }, {
        2, 3, 4, 5, 6
      }, {
        3, 4, 5
      }, {}
      },
    {
      {}, {
        0, 1
      }, {
        0, 1, 2, 7
      }, {
        0, 1, 2, 3, 4, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6, 7
      }, {
        0, 1, 2, 3, 4, 5, 6
      }, {
        2, 3, 4, 5
      }, {
        3, 4
      }, {}
      },
    {
      {}, {
        1
      }, {
        0, 1, 2
      }, {
        0, 1, 2, 3
      }, {
        0, 1, 2, 3, 4
      }, {
        0, 1, 2, 3, 4
      }, {
        0, 1, 2, 3, 4
      }, {
        1, 2, 3, 4
      }, {
        2, 3, 4
      }, {
        3
      }, {}
      },
    {
      {}, {}, {
        1
      }, {
        1, 2
      }, {
        1, 2, 3
      }, {
        1, 2, 3
      }, {
        1, 2, 3
      }, {
        2, 3
      }, {
        3
      }, {}, {}
      },
    {
      {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
      }
  };

  private int[] getNumEnemiesAttackingMoveDirs(Radar radar) {
    int[] numEnemiesAttackingMoveDirs = new int[8];
    RobotInfo[] nearbyEnemies = radar.getNearbyEnemies();
    for (int i = nearbyEnemies.length; i-- > 0;) {
      RobotInfo info = nearbyEnemies[i];
      if (info.type.canAttack()) {
        MapLocation enemyLoc = info.location;
        if (Math.abs(enemyLoc.x - rc.getLocation().x) <= 5 &&
            Math.abs(enemyLoc.y - rc.getLocation().y) <= 5) {
          int[] attackedDirs = attackNotes[5 + enemyLoc.x - rc.getLocation().x][5 + enemyLoc.y
              - rc.getLocation().y];
          for (int j = attackedDirs.length; j-- > 0;) {
            numEnemiesAttackingMoveDirs[attackedDirs[j]]++;
          }
        }
      }
    }
    return numEnemiesAttackingMoveDirs;
  }

  @Override
  public boolean bugTo(
      MapLocation loc,
      boolean avoidAttackers,
      boolean clearRubble) throws GameActionException {
    if (!rc.isCoreReady()) {
      return false;
    }
    return bugTowards(loc, getPolicy(avoidAttackers), clearRubble);
  }

  @Override
  public boolean moveRandomly() throws GameActionException {
    if (!rc.isCoreReady()) {
      return false;
    }
    Direction d = DirectionUtils.getRandomMovableDirection();
    for (int i = 8; --i >= 0;) {
      if (rc.canMove(d)) {
        rc.move(d);
        return true;
      }
      d = d.rotateLeft();
    }

    return false;
  }

  private boolean checkForwardDirectTowards(MapLocation destination,
      NavigationSafetyPolicy policy,
      boolean clearRubble) throws GameActionException {
    Direction[] dirs = NavigationUtil.getForwardDirections(rc.getLocation(), destination);
    int length = dirs.length;
    for (int i = 0; i < length; i++) {
      Direction dir = dirs[i];
      if (safeToMove(dir, policy, false)) {
        move(dir, false);
        return true;
      }
    }
    if (clearRubble) {
      for (int i = 0; i < length; i++) {
        Direction dir = dirs[i];
        if (safeToMove(dir, policy, true)) {
          move(dir, true);
          return true;
        }
      }
    }
    return false;
  }

  private boolean directTowards(
      MapLocation destination,
      NavigationSafetyPolicy policy,
      boolean clearRubble,
      Direction[] dirs) throws GameActionException {
    int length = dirs.length;
    for (int i = 0; i < length; i++) {
      Direction dir = dirs[i];
      if (safeToMove(dir, policy, clearRubble)) {
        move(dir, clearRubble);
        return true;
      }
    }

    return false;
  }

  private boolean safeToMove(
      Direction dir,
      NavigationSafetyPolicy policy,
      boolean clearRubble) throws GameActionException {

    MapLocation testLocation = rc.getLocation().add(dir);
    boolean policySafe = policy.isSafeToMoveTo(rc, testLocation);
    if (!policySafe) {
      return false;
    }
    if (rc.canMove(dir)) {
      return true;
    }

    if (clearRubble &&
        rc.onTheMap(testLocation) &&
        rc.senseRobotAtLocation(testLocation) == null) {
      return rc.senseRubble(testLocation) < CLEAR_RUBBLE_THRESHOLD;
    }
    return false;
  }

  private void move(Direction dir, boolean clearRubble) throws GameActionException {
    if (!rc.isCoreReady()) {
      return;
    }
    if (clearRubble && rc.senseRubble(rc.getLocation().add(
        dir)) >= GameConstants.RUBBLE_SLOW_THRESH) {
      rc.clearRubble(dir);
    } else if (rc.canMove(dir)) {
      rc.move(dir);
    }
  }

  private NavigationSafetyPolicy getPolicy(boolean avoidAttackers) {
    return avoidAttackers
        ? new AvoidAttackingUnitsPolicy(radar, enemyTurretCache)
        : new NoSafetyPolicy();
  }

  private boolean bugTowards(
      MapLocation dest,
      NavigationSafetyPolicy policy,
      boolean clearRubble) throws GameActionException {
    if (!dest.equals(bugDestination)) {
      bugDestination = dest;
      bugState = BugState.DIRECT;
    }

    if (rc.getLocation().equals(dest)) {
      return false;
    }

    return bugMove(policy, clearRubble);
  }

  private void startBug(
      NavigationSafetyPolicy policy, boolean clearRubble) throws GameActionException {
    bugStartDistSq = rc.getLocation().distanceSquaredTo(bugDestination);
    bugLastMoveDir = rc.getLocation().directionTo(bugDestination);
    bugLookStartDir = rc.getLocation().directionTo(bugDestination);
    bugRotationCount = 0;
    bugMovesSinceSeenObstacle = 0;

    if (bugWallSide == null) {
      // try to intelligently choose on which side we will keep the wall
      Direction leftTryDir = bugLastMoveDir.rotateLeft();
      for (int i = 0; i < 3; i++) {
        if (!safeToMove(leftTryDir, policy, clearRubble))
          leftTryDir = leftTryDir.rotateLeft();
        else
          break;
      }
      Direction rightTryDir = bugLastMoveDir.rotateRight();
      for (int i = 0; i < 3; i++) {
        if (!safeToMove(rightTryDir, policy, clearRubble))
          rightTryDir = rightTryDir.rotateRight();
        else
          break;
      }
      if (bugDestination.distanceSquaredTo(rc.getLocation().add(leftTryDir)) < bugDestination
          .distanceSquaredTo(rc.getLocation().add(rightTryDir))) {
        bugWallSide = WallSide.RIGHT;
      } else {
        bugWallSide = WallSide.LEFT;
      }
    }
  }

  private Direction findBugMoveDir(
      NavigationSafetyPolicy policy, boolean clearRubble) throws GameActionException {
    bugMovesSinceSeenObstacle++;
    Direction dir = bugLookStartDir;
    for (int i = 8; i-- > 0;) {
      if (safeToMove(dir, policy, clearRubble))
        return dir;
      dir = (bugWallSide == WallSide.LEFT ? dir.rotateRight() : dir.rotateLeft());
      bugMovesSinceSeenObstacle = 0;
    }
    return null;
  }

  private int numRightRotations(Direction start, Direction end) {
    return (end.ordinal() - start.ordinal() + 8) % 8;
  }

  private int numLeftRotations(Direction start, Direction end) {
    return (-end.ordinal() + start.ordinal() + 8) % 8;
  }

  private int calculateBugRotation(Direction moveDir) {
    if (bugWallSide == WallSide.LEFT) {
      return numRightRotations(bugLookStartDir, moveDir) - numRightRotations(bugLookStartDir,
          bugLastMoveDir);
    } else {
      return numLeftRotations(bugLookStartDir, moveDir) - numLeftRotations(bugLookStartDir,
          bugLastMoveDir);
    }
  }

  private boolean bugMove(Direction dir) throws GameActionException {
    move(dir, false /* clearRubble */);
    bugRotationCount += calculateBugRotation(dir);
    bugLastMoveDir = dir;
    if (bugWallSide == WallSide.LEFT) {
      bugLookStartDir = dir.rotateLeft().rotateLeft();
    } else {
      bugLookStartDir = dir.rotateRight().rotateRight();
    }
    return true;
  }

  private boolean detectBugIntoEdge(Direction proposedMoveDir) throws GameActionException {
    if (proposedMoveDir == null) {
      return false;
    }
    if (bugWallSide == WallSide.LEFT) {
      return !rc.onTheMap(rc.getLocation().add(proposedMoveDir.rotateLeft()));
    } else {
      return !rc.onTheMap(rc.getLocation().add(proposedMoveDir.rotateRight()));
    }
  }

  private void reverseBugWallFollowDir(RobotController rc, NavigationSafetyPolicy policy,
      boolean clearRubble)
          throws GameActionException {
    bugWallSide = (bugWallSide == WallSide.LEFT ? WallSide.RIGHT : WallSide.LEFT);
    startBug(policy, clearRubble);
  }

  private boolean bugTurn(
      NavigationSafetyPolicy policy, boolean clearRubble) throws GameActionException {
    Direction dir = findBugMoveDir(policy, clearRubble);
    if (detectBugIntoEdge(dir)) {
      reverseBugWallFollowDir(rc, policy, clearRubble);
      dir = findBugMoveDir(policy, clearRubble);
    }
    if (dir != null) {
      return bugMove(dir);
    }

    return false;
  }

  private boolean canEndBug(RobotController rc) {
    if (bugMovesSinceSeenObstacle >= 4)
      return true;
    return (bugRotationCount <= 0 || bugRotationCount >= 8) && rc.getLocation().distanceSquaredTo(
        bugDestination) <= bugStartDistSq;
  }

  private boolean bugMove(
      NavigationSafetyPolicy policy,
      boolean clearRubble) throws GameActionException {
    // Check if we can stop bugging at the *beginning* of the turn
    if (bugState == BugState.BUG) {
      if (canEndBug(rc)) {
        bugState = BugState.DIRECT;
      }
    }

    // If DIRECT mode, try to go directly to target
    if (bugState == BugState.DIRECT) {
      if (!directTowards(bugDestination, policy, clearRubble, NavigationUtil.getAllDirections(rc
          .getLocation(), bugDestination))) {
        bugState = BugState.BUG;
        startBug(policy, clearRubble);
      } else {
        return true;
      }
    }

    // If that failed, or if bugging, bug
    if (bugState == BugState.BUG) {
      return bugTurn(policy, clearRubble);
    }

    return false;
  }

}