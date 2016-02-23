/*
 * FactoryMethod.java
 *
 * created at 21.02.2016 by m.nikolov <m.nikolov@seeburger.de>
 *
 * Copyright (c) SEEBURGER AG, Germany. All Rights Reserved.
 */
package net.sf.webdav.methods;

import net.sf.webdav.IMimeTyper;
import net.sf.webdav.IWebdavStore;
import net.sf.webdav.locking.IResourceLocks;
import net.sf.webdav.locking.ResourceLocks;


public interface FactoryMethod
{

    public DoGet doGet(IWebdavStore store, String dftIndexFile, String insteadOf404,
                              ResourceLocks resourceLocks, IMimeTyper mimeTyper,
                              int contentLengthHeader);

    public DoHead doHead(IWebdavStore store, String dftIndexFile, String insteadOf404,
                                ResourceLocks resourceLocks, IMimeTyper mimeTyper,
                                int contentLengthHeader);

    public DoDelete doDelete(IWebdavStore store, ResourceLocks resourceLocks,
                                    boolean readOnly);

    public DoCopy doCopy(IWebdavStore store, ResourceLocks resourceLocks,
                         DoDelete doDelete, boolean readOnly);

    public DoLock doLock(IWebdavStore store, IResourceLocks resourceLocks,
                         boolean readOnly);

    public DoUnlock doUnlock(IWebdavStore store, IResourceLocks resourceLocks,
                             boolean readOnly);

    public DoMove doMove(ResourceLocks resourceLocks, DoDelete doDelete,
                         DoCopy doCopy, boolean readOnly);

    public DoMkcol doMkcol(IWebdavStore store, IResourceLocks resourceLocks,
                           boolean readOnly);

    public DoOptions doOptions(IWebdavStore store, ResourceLocks resLocks);

    public DoPut doPut(IWebdavStore store, IResourceLocks resLocks, boolean readOnly,
                       boolean lazyFolderCreationOnPut);

    public DoPropfind doPropfind(IWebdavStore store, ResourceLocks resLocks,
                                 IMimeTyper mimeTyper);

    public DoProppatch doProppatch(IWebdavStore store, ResourceLocks resLocks,
                                    boolean readOnly);

    public DoNotImplemented doNotImplemented(boolean readOnly);

}