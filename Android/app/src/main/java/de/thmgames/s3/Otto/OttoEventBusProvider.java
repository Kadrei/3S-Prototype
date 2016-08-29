package de.thmgames.s3.Otto;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by Benedikt on 23.10.2014.
 */
public class OttoEventBusProvider {
    // Provided by Square under the Apache License
    private static final Bus BUS_UI = new Bus(ThreadEnforcer.MAIN);
    private static final Bus BUS_ANY = new Bus(ThreadEnforcer.ANY);

    public static Bus getInstanceForUIThread() {
        return BUS_UI;
    }

    public static Bus getInstance() {
        return BUS_ANY;
    }

    private OttoEventBusProvider() {
        // No instances.
    }
}
