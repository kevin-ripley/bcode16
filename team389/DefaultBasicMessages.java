package team389;

import battlecode.common.RobotController;
import battlecode.common.Signal;

public class DefaultBasicMessages implements BasicMessages {

  private static final int MAX_MESSAGES = 50;

  private int numAllyMessages;
  private int numEnemyMessages;
  private Signal[] allyMessages;
  private Signal[] enemyMessages;

  public DefaultBasicMessages() {
    this.numEnemyMessages = 0;
    this.numAllyMessages = 0;
    this.allyMessages = new Signal[MAX_MESSAGES];
    this.enemyMessages = new Signal[MAX_MESSAGES];
  }

  @Override
  public Signal[] getAllyBasicMessages() {
    Signal[] copiedMessages = new Signal[numAllyMessages];
    for (int i = 0; i < numAllyMessages; i++) {
      copiedMessages[i] = allyMessages[i];
    }
    return copiedMessages;
  }

  @Override
  public Signal[] getEnemyBasicMessages() {
    Signal[] copiedMessages = new Signal[numEnemyMessages];
    for (int i = 0; i < numEnemyMessages; i++) {
      copiedMessages[i] = enemyMessages[i];
    }
    return copiedMessages;
  }

  @Override
  public void clearBasicMessages() {
    numAllyMessages = 0;
    numEnemyMessages = 0;
  }

  @Override
  public void addMessage(Signal s, RobotController rc) {
    if (!s.getTeam().equals(rc.getTeam())) {
      enemyMessages[numEnemyMessages++] = s;
    } else {
      allyMessages[numAllyMessages++] = s;
    }
  }

}
