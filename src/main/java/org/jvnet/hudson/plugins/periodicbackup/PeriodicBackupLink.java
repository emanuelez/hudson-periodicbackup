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
import hudson.model.Hudson;
import hudson.model.ManagementLink;
import hudson.model.Saveable;
import hudson.util.DescribableList;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

@Extension
public class PeriodicBackupLink extends ManagementLink implements Saveable {

    private final DescribableList<FileManager, FileManagerDescriptor> fileManagerPlugins = new DescribableList<FileManager, FileManagerDescriptor>(this);
    private final DescribableList<Location, LocationDescriptor> locationPlugins = new DescribableList<Location, LocationDescriptor>(this);
    private final DescribableList<Storage, StorageDescriptor> storagePlugins = new DescribableList<Storage, StorageDescriptor>(this);

    private String targetDirectory;

    public String getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(String targetDirectory) {
        this.targetDirectory = targetDirectory;
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
        return "/plugin/periodicbackup/images/48x48/periodicbackup.png";
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

    public String getRootDirectory() {
        return Hudson.getInstance().getRootDir().getAbsolutePath();
    }

    public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException, InterruptedException {
        JSONObject form = req.getSubmittedForm();

        // persist the setting
        BulkChange bc = new BulkChange(this);
        try {
            //TODO: for each element of configuration file we have to have setter here
            setTargetDirectory(form.getString("targetDirectory"));
            //TODO: if we will use ID's we need assign ID here (look into PXE doConfigSubmit
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bc.commit();
        }
        rsp.sendRedirect(".");
    }

    public Collection<StorageDescriptor> getStorageDescriptors() {
        return Storage.all();
    }

    public Collection<FileManagerDescriptor> getFileManagerDescriptors() {
        return FileManager.all();
    }

    public Collection<FileManagerDescriptor> getLocationDescriptors() {
        return FileManager.all();
    }

}

