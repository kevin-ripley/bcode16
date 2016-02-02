package team389;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import team389.BehaviorFactory.BehaviorInfo;

public class RobotPlayer {

  static BytecodeProfiler profiler;

  public static void run(RobotController rc) {
    profiler = new DefaultBytecodeProfiler(rc);
    // profiler = new NoOpBytecodeProfiler();

    BehaviorInfo b = BehaviorFactory.createForRobotController(rc);
    PreBehavior preBehavior = b.preBehavior;
    Behavior behavior = b.behavior;

    // Empty all messages received during build time.
    rc.emptySignalQueue();

    while (true) {
      profiler.start();
      try {
        int currentRound = rc.getRoundNum();
        preBehavior.preRun();
        profiler.split("after pre behave");
        behavior.run();
        profiler.end();
        if (rc.getRoundNum() != currentRound) {
          int bytecodesUsed = (rc.getRoundNum() - currentRound) *
              rc.getType().bytecodeLimit + Clock.getBytecodeNum();
          System.out.println("Over bytecode limit: " + bytecodesUsed);
          profiler.printToConsole();
        }
      } catch (GameActionException e) {
        e.printStackTrace();
      }
      // In case of bytecode overages, clear all messages before ending the
      // turn.
      rc.emptySignalQueue();
      Clock.yield();
    }
  }
}
