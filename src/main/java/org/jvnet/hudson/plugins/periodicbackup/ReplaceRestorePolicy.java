package org.jvnet.hudson.plugins.periodicbackup;

import java.io.File;

public class ReplaceRestorePolicy implements RestorePolicy {
    public void restore(Iterable<File> files) {
        // TODO: implement
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ReplaceRestorePolicy;
    }

    @Override
    public int hashCode() {
        return 83;
    }
}
