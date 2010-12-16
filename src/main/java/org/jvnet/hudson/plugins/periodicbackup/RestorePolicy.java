package org.jvnet.hudson.plugins.periodicbackup;

import java.io.File;

public interface RestorePolicy {

    void restore(Iterable<File> files);

}
