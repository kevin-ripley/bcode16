package team389;

import battlecode.common.MapLocation;
import battlecode.common.Signal;

public class MessageData {

  private static final int MAX_BITS = 32;
  private static final int COORDINATE_BITS = 16;
  private static final int COORDINATE_OFFSET = 16000;
  private static final int NULL_LOCATION_DATA = -1;

  public static class Builder {

    private int bitsUsed;
    private int data;

    public Builder() {
      bitsUsed = 0;
    }

    public Builder addBits(int numBits, int payload) {
      int maxPayload = maxPayloadForBits(numBits);
      if (payload < 0) {
        payload = 0;
      } else if (payload > maxPayload) {
        payload = maxPayload;
      }

      if (bitsUsed + numBits > MAX_BITS) {
        return this;
      }

      bitsUsed += numBits;
      data += (payload << (MAX_BITS - bitsUsed));
      return this;
    }

    public MessageData build() {
      return new MessageData(this);
    }

    private int maxPayloadForBits(int numBits) {
      return (1 << numBits) - 1;
    }
  }

  private final int data;

  private MessageData(Builder builder) {
    this(builder.data);
  }

  private MessageData(int data) {
    this.data = data;
  }

  public static MessageData empty() {
    return new MessageData(0);
  }

  public static MessageData fromMapLocation(MapLocation loc) {
    if (loc == null) {
      return new MessageData(NULL_LOCATION_DATA);
    }
    Builder builder = new Builder();
    builder.addBits(COORDINATE_BITS, loc.x + COORDINATE_OFFSET);
    builder.addBits(COORDINATE_BITS, loc.y + COORDINATE_OFFSET);
    return builder.build();
  }

  public static MessageData fromSignal(Signal s, boolean firstData) {
    int index = firstData ? 0 : 1;
    int[] message = s.getMessage();
    return message.length == 2
        ? new MessageData(message[index])
        : empty();
  }

  public int getData() {
    return data;
  }

  public int getPayload(int startBit, int inclusiveEndBit) {
    int length = inclusiveEndBit - startBit + 1;
    return (data >> (MAX_BITS - inclusiveEndBit - 1)) & ((1 << length) - 1);
  }

  public int[] getAllPayloads(int[] payloadBitLengths) {
    int totalLength = 0;
    int[] payloads = new int[payloadBitLengths.length];
    for (int i = 0; i < payloadBitLengths.length; i++) {
      int length = payloadBitLengths[i];
      totalLength += length;
      if (totalLength > MAX_BITS) {
        return new int[payloadBitLengths.length];
      }

      payloads[i] = (data >> (MAX_BITS - totalLength)) & ((1 << length) - 1);
    }
    return payloads;
  }

  public MapLocation toMapLocation() {
    if (data == NULL_LOCATION_DATA) {
      return null;
    }

    int[] payloads = getAllPayloads(new int[] {
      COORDINATE_BITS, COORDINATE_BITS
    });

    return new MapLocation(payloads[0] - COORDINATE_OFFSET, payloads[1] - COORDINATE_OFFSET);
  }
}
