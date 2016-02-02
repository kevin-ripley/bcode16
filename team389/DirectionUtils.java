package team389;

import java.util.Random;

import battlecode.common.Direction;

public class DirectionUtils {

  public static final Direction[] movableDirections = {
    Direction.NORTH,
    Direction.NORTH_EAST,
    Direction.EAST,
    Direction.SOUTH_EAST,
    Direction.SOUTH,
    Direction.SOUTH_WEST,
    Direction.WEST,
    Direction.NORTH_WEST
  };

  public static Direction getRandomMovableDirection() {
    return movableDirections[(int) (Math.random() * movableDirections.length)];
  };

  public static Direction getRandomMovableDirection(int seed) {
    Random random = new Random(seed);
    return movableDirections[random.nextInt(movableDirections.length)];
  };

  public static Direction[] getFrontDirections(Direction d) {
    Direction[] frontDirections = {
      d,
      d.rotateLeft(),
      d.rotateRight()
    };
    return frontDirections;
  }

  public static Direction[] getFrontAndSide(Direction d) {
    Direction[] frontDirections = {
      d,
      d.rotateLeft(),
      d.rotateRight(),
      d.rotateLeft().rotateLeft(),
      d.rotateRight().rotateRight()
    };
    return frontDirections;
  }

  public static Direction[] orderDirectionsFromFront(Direction d) {
    Direction[] frontDirections = {
      d,
      d.rotateLeft(),
      d.rotateRight(),
      d.rotateLeft().rotateLeft(),
      d.rotateRight().rotateRight(),
      d.rotateLeft().rotateLeft().rotateLeft(),
      d.rotateRight().rotateRight().rotateRight(),
      d.opposite()
    };
    return frontDirections;
  }
}