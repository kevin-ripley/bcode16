package team389;

import battlecode.common.RobotController;

public class BehaviorFactory {

  public static class BehaviorInfo {

    public final PreBehavior preBehavior;
    public final Behavior behavior;

    public BehaviorInfo(PreBehavior preBehavior, Behavior behavior) {
      this.preBehavior = preBehavior;
      this.behavior = behavior;
    }
  }

  public static BehaviorInfo createForRobotController(RobotController rc) {
    switch (rc.getType()) {
      case ARCHON:
        return archon(rc);
      case SCOUT:
        return scout(rc);
      case SOLDIER:
        return soldier(rc);
      case GUARD:
        return guard(rc);
      case VIPER:
        return viper(rc);
      case TURRET:
        return turret(rc);
      default:
        NoOpBehavior b = new NoOpBehavior(rc);
        return new BehaviorInfo(b /* preBehavior */, b /* behavior */);
    }
  }

  private static BehaviorInfo archon(RobotController rc) {
    AlliedArchonTracker alliedArchonTracker = new PersistentAlliedArchonTracker(rc);
    Radar radar = new SenseRadar(rc);
    ZombieDenReporter zombieDenReporter = new DefaultZombieDenReporter(rc, radar);
    ArmyRallyReporter armyRallyReporter = new DefaultArmyRally(rc);
    RepairSystem repairSystem = new DefaultRepairSystem(rc);
    EnemyTurretCache enemyTurretCache = new DefaultEnemyTurretCache(rc);
    EeHanTimingManager eeHanTimingManager = new EeHanTimingManager(rc, radar, enemyTurretCache);
    EnemyMessageProcessor enemyMessageProcessor = new MatchObservationEnemyMessageProcessor(rc);
    PickupLocationReporter pickupLocationReporter = new DefaultPickupLocationReporter(rc);
    DefaultMessageSenderReceiver messageSenderReceiver = new DefaultMessageSenderReceiver(
        rc,
        alliedArchonTracker,
        new NoOpDistantHostileReporter(),
        zombieDenReporter,
        new NoOpBasicMessages(),
        armyRallyReporter,
        enemyTurretCache,
        eeHanTimingManager /* eeHanTimingReporter */,
        pickupLocationReporter,
        enemyMessageProcessor);
    NavigationSystem navigation = new DuckNavigationSystem(
        rc, radar, alliedArchonTracker, enemyTurretCache);

    PreBehavior preBehavior = new DefaultPreBehavior(
        rc,
        radar,
        messageSenderReceiver /* messageReceiver */,
        messageSenderReceiver /* messageSender */,
        alliedArchonTracker,
        zombieDenReporter,
        enemyTurretCache,
        eeHanTimingManager /* eeHanTimingCalculator */);
    Behavior behavior = new ArchonBehavior(
        rc,
        radar,
        navigation,
        messageSenderReceiver /* messageSender */,
        alliedArchonTracker,
        repairSystem,
        zombieDenReporter,
        pickupLocationReporter);
    return new BehaviorInfo(preBehavior, behavior);
  }

  private static BehaviorInfo scout(RobotController rc) {
    AlliedArchonTracker alliedArchonTracker = new PersistentAlliedArchonTracker(rc);
    EnemyArchonTracker enemyArchonTracker = new DefaultEnemyArchonTracker(rc);
    Radar radar = new SenseRadar(rc);
    ZombieDenReporter zombieDenReporter = new DefaultZombieDenReporter(rc, radar);
    BasicMessages basicMessages = new DefaultBasicMessages();
    ArmyRallyReporter armyRallyReporter = new DefaultArmyRally(rc);
    EnemyTurretCache enemyTurretCache = new DefaultEnemyTurretCache(rc);
    EeHanTimingManager eeHanTimingManager = new EeHanTimingManager(rc, radar, enemyTurretCache);
    PickupLocationReporter pickupLocationReporter = new DefaultPickupLocationReporter(rc);
    DefaultMessageSenderReceiver messageSenderReceiver = new DefaultMessageSenderReceiver(
        rc,
        alliedArchonTracker,
        new NoOpDistantHostileReporter(),
        zombieDenReporter,
        basicMessages,
        armyRallyReporter,
        enemyTurretCache,
        eeHanTimingManager /* eeHanTimingReporter */,
        pickupLocationReporter,
        new NoOpEnemyMessageProcessor());

    PreBehavior preBehavior = new DefaultPreBehavior(
        rc,
        radar,
        messageSenderReceiver /* messageReceiver */,
        messageSenderReceiver /* messageSender */,
        alliedArchonTracker,
        zombieDenReporter,
        enemyTurretCache,
        eeHanTimingManager /* eeHanTimingCalculator */);
    NavigationSystem navigation = new DuckNavigationSystem(
        rc, radar, alliedArchonTracker, enemyTurretCache);
    Behavior behavior = new ScoutBehavior(
        rc,
        navigation,
        radar,
        basicMessages,
        enemyArchonTracker,
        alliedArchonTracker,
        enemyTurretCache,
        messageSenderReceiver,
        pickupLocationReporter);
    return new BehaviorInfo(preBehavior, behavior);
  }

  private static BehaviorInfo soldier(RobotController rc) {
    AlliedArchonTracker alliedArchonTracker = new PersistentAlliedArchonTracker(rc);
    Radar radar = new SenseRadar(rc);
    ZombieDenReporter zombieDenReporter = new DefaultZombieDenReporter(rc, radar);
    DefaultArmyRally defaultArmyRally = new DefaultArmyRally(rc);
    EnemyTurretCache enemyTurretCache = new DefaultEnemyTurretCache(rc);
    EeHanTimingManager eeHanTimingManager = new EeHanTimingManager(rc, radar, enemyTurretCache);
    DefaultMessageSenderReceiver messageSenderReceiver = new DefaultMessageSenderReceiver(
        rc,
        alliedArchonTracker,
        new NoOpDistantHostileReporter(),
        zombieDenReporter,
        new NoOpBasicMessages(),
        defaultArmyRally /* armyRallyReporter */,
        enemyTurretCache,
        eeHanTimingManager /* eeHanTimingReporter */,
        new NoOpPickupLocationReporter(),
        new NoOpEnemyMessageProcessor());
    NavigationSystem navigation = new DuckNavigationSystem(
        rc, radar, alliedArchonTracker, enemyTurretCache);
    AttackSystem attackSystem = new DefaultAttackSystem();
    ZombieSpawnScheduleInfo zombieSpawnScheduleInfo = new ZombieSpawnScheduleInfo(rc, rc
        .getZombieSpawnSchedule());
    RangedArmyBehavior soldierBehavior = new RangedArmyBehavior(
        rc,
        messageSenderReceiver /* messageReceiver */,
        alliedArchonTracker,
        radar,
        navigation,
        attackSystem,
        zombieSpawnScheduleInfo,
        defaultArmyRally /* armyRally */,
        eeHanTimingManager /* eeHanTimingReporter */,
        zombieDenReporter);
    return new BehaviorInfo(soldierBehavior /* preBehavior */, soldierBehavior /* behavior */);
  }

  private static BehaviorInfo guard(RobotController rc) {
    AlliedArchonTracker alliedArchonTracker = new NonPersistentAlliedArchonTracker(rc);
    Radar radar = new SenseRadar(rc);
    ZombieDenReporter zombieDenReporter = new DefaultZombieDenReporter(rc, radar);
    DefaultArmyRally defaultArmyRally = new DefaultArmyRally(rc);
    EnemyTurretCache enemyTurretCache = new DefaultEnemyTurretCache(rc);
    EeHanTimingManager eeHanTimingManager = new EeHanTimingManager(rc, radar, enemyTurretCache);
    DefaultMessageSenderReceiver messageSenderReceiver = new DefaultMessageSenderReceiver(
        rc,
        alliedArchonTracker,
        new NoOpDistantHostileReporter(),
        zombieDenReporter,
        new NoOpBasicMessages(),
        defaultArmyRally /* armyRallyReporter */,
        enemyTurretCache,
        eeHanTimingManager /* eeHanTimingReporter */,
        new NoOpPickupLocationReporter(),
        new NoOpEnemyMessageProcessor());
    NavigationSystem navigation = new DuckNavigationSystem(
        rc, radar, alliedArchonTracker, enemyTurretCache);
    AttackSystem attackSystem = new DefaultAttackSystem();
    ZombieSpawnScheduleInfo zombieSpawnScheduleInfo = new ZombieSpawnScheduleInfo(rc, rc
        .getZombieSpawnSchedule());
    RangedArmyBehavior rangedArmyBehavior = new RangedArmyBehavior(
        rc,
        messageSenderReceiver /* messageReceiver */,
        alliedArchonTracker,
        radar,
        navigation,
        attackSystem,
        zombieSpawnScheduleInfo,
        defaultArmyRally /* armyRally */,
        eeHanTimingManager /* eeHanTimingReporter */,
        zombieDenReporter);
    return new BehaviorInfo(
        rangedArmyBehavior /* preBehavior */, rangedArmyBehavior /* behavior */);
  }

  private static BehaviorInfo viper(RobotController rc) {
    AlliedArchonTracker alliedArchonTracker = new PersistentAlliedArchonTracker(rc);
    Radar radar = new SenseRadar(rc);
    ZombieDenReporter zombieDenReporter = new DefaultZombieDenReporter(rc, radar);
    DefaultArmyRally defaultArmyRally = new DefaultArmyRally(rc);
    EnemyTurretCache enemyTurretCache = new DefaultEnemyTurretCache(rc);
    EeHanTimingManager eeHanTimingManager = new EeHanTimingManager(rc, radar, enemyTurretCache);
    DefaultMessageSenderReceiver messageSenderReceiver = new DefaultMessageSenderReceiver(
        rc,
        alliedArchonTracker,
        new NoOpDistantHostileReporter(),
        zombieDenReporter,
        new NoOpBasicMessages(),
        defaultArmyRally /* armyRallyReporter */,
        enemyTurretCache,
        eeHanTimingManager /* eeHanTimingReporter */,
        new NoOpPickupLocationReporter(),
        new NoOpEnemyMessageProcessor());
    NavigationSystem navigation = new DuckNavigationSystem(
        rc, radar, alliedArchonTracker, enemyTurretCache);
    AttackSystem attackSystem = new ViperAttackSystem();
    ZombieSpawnScheduleInfo zombieSpawnScheduleInfo = new ZombieSpawnScheduleInfo(
        rc, rc.getZombieSpawnSchedule());
    RangedArmyBehavior soldierBehavior = new RangedArmyBehavior(
        rc,
        messageSenderReceiver /* messageReceiver */,
        alliedArchonTracker,
        radar,
        navigation,
        attackSystem,
        zombieSpawnScheduleInfo,
        defaultArmyRally /* armyRally */,
        eeHanTimingManager /* eeHanTimingReporter */,
        zombieDenReporter);
    return new BehaviorInfo(soldierBehavior /* preBehavior */, soldierBehavior /* behavior */);
  }

  private static BehaviorInfo turret(RobotController rc) {
    AlliedArchonTracker alliedArchonTracker = new PersistentAlliedArchonTracker(rc);
    Radar radar = new SenseRadar(rc);
    ZombieDenReporter zombieDenReporter = new DefaultZombieDenReporter(rc, radar);
    ExtendedRadar extendedRadar = new ExtendedRadar(rc, radar);
    DefaultArmyRally defaultArmyRally = new DefaultArmyRally(rc);
    EnemyTurretCache enemyTurretCache = new DefaultEnemyTurretCache(rc);
    EeHanTimingManager eeHanTimingManager = new EeHanTimingManager(
        rc, extendedRadar /* radar */, enemyTurretCache);
    DefaultMessageSenderReceiver messageSenderReceiver = new DefaultMessageSenderReceiver(
        rc,
        alliedArchonTracker,
        extendedRadar /* distantHostileReporter */,
        zombieDenReporter,
        new NoOpBasicMessages(),
        defaultArmyRally /* armyRallyReporter */,
        enemyTurretCache,
        eeHanTimingManager /* eeHanTimingReporter */,
        new NoOpPickupLocationReporter(),
        new NoOpEnemyMessageProcessor());
    NavigationSystem navigation = new DuckNavigationSystem(
        rc, radar, alliedArchonTracker, enemyTurretCache);
    AttackSystem attackSystem = new TurretAttackSystem();
    TurretBehavior turretBehavior = new TurretBehavior(
        rc,
        messageSenderReceiver /* messageReceiver */,
        messageSenderReceiver /* messageSender */,
        extendedRadar /* radar */,
        navigation,
        alliedArchonTracker,
        attackSystem,
        defaultArmyRally,
        eeHanTimingManager /* eeHanTimingReporter */,
        zombieDenReporter);
    return new BehaviorInfo(turretBehavior /* preBehavior */, turretBehavior /* behavior */);
  }
}
