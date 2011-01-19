package org.jvnet.hudson.plugins.periodicbackup;

import java.io.File;

public class ReplaceRestorePolicy implements RestorePolicy {
    public void restore(Iterable<File> files) {
        // TODO: implement
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }
}
