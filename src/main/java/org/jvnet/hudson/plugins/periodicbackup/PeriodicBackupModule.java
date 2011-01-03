package org.jvnet.hudson.plugins.periodicbackup;

import com.google.inject.AbstractModule;

public class PeriodicBackupModule extends AbstractModule {
    @Override
    protected void configure() {

        bind(RestorePolicy.class)
                .annotatedWith(Overwrite.class)
                .to(OverwriteRestorePolicy.class);
        bind(RestorePolicy.class)
                .annotatedWith(Replace.class)
                .to(ReplaceRestorePolicy.class);
    }
}
