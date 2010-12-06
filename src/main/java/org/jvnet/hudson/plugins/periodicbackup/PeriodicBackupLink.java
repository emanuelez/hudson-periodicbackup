/*
 * The MIT License
 *
 * Copyright (c) 2010 Tomasz Blaszczynski, Emanuele Zattin
 *
 * This plugin is based on and inspired by the "backup" plugin developed by:
 * Vincent Sellier, Manufacture Française des Pneumatiques Michelin, Romain Seguy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package org.jvnet.hudson.plugins.periodicbackup;

import hudson.BulkChange;
import hudson.Extension;
import hudson.XmlFile;
import hudson.model.*;
import hudson.util.DescribableList;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static org.jvnet.hudson.plugins.periodicbackup.Storage.StorageDescriptor;

@Extension
public class PeriodicBackupLink extends ManagementLink implements Saveable, Describable<PeriodicBackupLink> {

    private final DescribableList<PeriodicBackupPlugin, PeriodicBackupPluginDescriptor> periodicBackupPlugins = new DescribableList<PeriodicBackupPlugin, PeriodicBackupPluginDescriptor>(this);
    private String targetDirectory;
    private FileManager.FileManagerDescriptor fileManagerDescriptor;
    private FileManager fileManager;
    private StorageDescriptor storageDescriptor;
    private Storage storage;
    private FileManagerFactory fileManagerFactory = new FileManagerFactory();
    private StorageFactory storageFactory = new StorageFactory();

    public StorageDescriptor getStorageDescriptor() {
        return storageDescriptor;
    }

    public Storage getStorage() {
        return storage;
    }


    public String getTargetDirectory() {
        return targetDirectory;
    }

    public FileManager.FileManagerDescriptor getFileManagerDescriptor() {
        return fileManagerDescriptor;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    /**
     * Sets this.storageDescriptor to descriptor whose value returned by getDisplayName()
     * is the same as the value from config page, calls setStorage to set this.storage afterwards
     *
     * @param storageDescriptorString String obtained from config page
     * @throws IOException
     */
    public void setStorageDescriptor(String storageDescriptorString) throws IOException {
        StorageDescriptor storageDescriptor = null;
        for (PeriodicBackupPluginDescriptor d : getDescriptors()) {
            if (d != null) {
                if (d.getDisplayName().equals(storageDescriptorString)) {
                    storageDescriptor = (Storage.StorageDescriptor) d;
                }
            }
        }
        this.storageDescriptor = storageDescriptor;
        save();
        setStorage(storageDescriptor);
    }

    public void setStorage(StorageDescriptor storageDescriptor) throws IOException {
        this.storage = storageFactory.create(storageDescriptor);
        save();
    }

    /**
     * Sets this.fileManagerDescriptor to descriptor whose value returned by getDisplayName()
     * is the same as the value from config page, calls setFileManager to set this.fileManager afterwards
     *
     * @param fileManagerDescriptorString String obtained from config page
     * @throws IOException
     */
    public void setFileManagerDescriptor(String fileManagerDescriptorString) throws IOException {

        FileManager.FileManagerDescriptor fileManagerDescriptor = null;
        for (PeriodicBackupPluginDescriptor d : getDescriptors()) {
            if (d != null) {
                if (d.getDisplayName().equals(fileManagerDescriptorString)) {
                    fileManagerDescriptor = (FileManager.FileManagerDescriptor) d;
                }
            }
        }
        this.fileManagerDescriptor = fileManagerDescriptor;
        save();
        setFileManager(fileManagerDescriptor);
    }


    public void setFileManager(FileManager.FileManagerDescriptor fileManagerDescriptor) throws IOException {
        this.fileManager = fileManagerFactory.create(fileManagerDescriptor);
        save();
    }

    public void setTargetDirectory(String targetDirectory) throws IOException {
        this.targetDirectory = targetDirectory;
        save();
    }

    public String getDisplayName() {
        return Messages.displayName();
    }

    @Override
    public String getUrlName() {
        return "periodicbackup";
    }

    @Override
    public String getIconFileName() {
        return "/plugin/periodicbackup/images/periodicbackup.png";
    }

    @Override
    public String getDescription() {
        return Messages.description();
    }

    protected void load() throws IOException {
        XmlFile xml = getConfigXml();
        if (xml.exists())
            xml.unmarshal(this);  //Loads the contents of this file into an existing object.
    }

    public void save() throws IOException {
        if (BulkChange.contains(this)) return;
        getConfigXml().write(this);
    }

    protected XmlFile getConfigXml() {
        return new XmlFile(Hudson.XSTREAM,
                new File(Hudson.getInstance().getRootDir(), "periodicBackup.xml"));
    }

    /**
     * All registered descriptors exposed for UI
     */
    public Collection<PeriodicBackupPluginDescriptor> getDescriptors() {
        return PeriodicBackupPlugin.all();
    }

    public PeriodicBackupLink() throws IOException {
        load();           //loading configuration
    }

    public Collection<Storage.StorageDescriptor> getStorageDescriptors() {
        Collection<PeriodicBackupPluginDescriptor> all = getDescriptors();
        ArrayList<StorageDescriptor> storages = new ArrayList<StorageDescriptor>();
        for (PeriodicBackupPluginDescriptor descriptor : all) {
            if (descriptor instanceof StorageDescriptor) {
                storages.add((StorageDescriptor) descriptor);
            }
        }
        return storages;
    }

    public Collection<FileManager.FileManagerDescriptor> getFileManagerDescriptors() {
        Collection<PeriodicBackupPluginDescriptor> all = getDescriptors();
        ArrayList<FileManager.FileManagerDescriptor> managers = new ArrayList<FileManager.FileManagerDescriptor>();
        for (PeriodicBackupPluginDescriptor descriptor : all) {
            if (descriptor instanceof FileManager.FileManagerDescriptor) {
                managers.add((FileManager.FileManagerDescriptor) descriptor);
            }
        }
        return managers;
    }

    public DescribableList<PeriodicBackupPlugin, PeriodicBackupPluginDescriptor> getPeriodicBackupPlugins() {
        return periodicBackupPlugins;
    }

    public Descriptor<PeriodicBackupLink> getDescriptor() {
        return Hudson.getInstance().getDescriptorByType(DescriptorImpl.class);
    }

    /**
     * Descriptor is only used for UI form bindings
     */
    @Extension
    public static final class DescriptorImpl extends Descriptor<PeriodicBackupLink> {
        public String getDisplayName() {
            return null; // unused
        }
    }

    public String getRootDirectory() {
        return Hudson.getInstance().getRootDir().getAbsolutePath();
    }

    public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException, InterruptedException {
        JSONObject form = req.getSubmittedForm();

        // persist the setting
        BulkChange bc = new BulkChange(this);
        try {
            setTargetDirectory(form.getString("targetDirectory"));
            setFileManagerDescriptor(form.getString("fileManagerDescriptor"));
            setStorageDescriptor(form.getString("storage"));

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            bc.commit();
        }
        rsp.sendRedirect(".");
    }
}
