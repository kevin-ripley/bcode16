package team389;

public class BuddySystem {

  private int buddy;

  public BuddySystem() {
    this.buddy = -1;
  }

  public int getBuddy() {
    return buddy;
  }

  public void removeBuddy() {
    this.buddy = -1;
  }

  public boolean hasBuddy() {
    return this.buddy != -1;
  }

  public void setBuddy(int buddy) {
    this.buddy = buddy;
  }
}
