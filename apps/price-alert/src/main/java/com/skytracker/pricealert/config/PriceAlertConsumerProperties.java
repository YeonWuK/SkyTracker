package com.skytracker.pricealert.config;

public class PriceAlertConsumerProperties {
    public static final int REPLICATION_FACTOR = 1;
    public static final short REPLICA_COUNT = 3;
    public static final int MAX_ATTEMPT_COUNT = 2;
    public static final long BACKOFF_PERIOD = 2000L;
}
