/*
 * FactoryMethodImpl.java
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


public class FactoryMethodImpl implements FactoryMethod
{

    @Override
    public DoGet doGet(IWebdavStore store, String dftIndexFile, String insteadOf404, ResourceLocks resourceLocks, IMimeTyper mimeTyper, int contentLengthHeader)
    {
        return new DoGet(store, dftIndexFile, insteadOf404, resourceLocks, mimeTyper, contentLengthHeader);
    }


    @Override
    public DoHead doHead(IWebdavStore store, String dftIndexFile, String insteadOf404, ResourceLocks resourceLocks, IMimeTyper mimeTyper, int contentLengthHeader)
    {
        return new DoHead(store, dftIndexFile, insteadOf404, resourceLocks, mimeTyper, contentLengthHeader);
    }


    @Override
    public DoDelete doDelete(IWebdavStore store, ResourceLocks resourceLocks, boolean readOnly)
    {
        return new DoDelete(store, resourceLocks, readOnly);
    }


    @Override
    public DoCopy doCopy(IWebdavStore store, ResourceLocks resourceLocks, DoDelete doDelete, boolean readOnly)
    {
        return new DoCopy(store, resourceLocks, doDelete, readOnly);
    }


    @Override
    public DoLock doLock(IWebdavStore store, IResourceLocks resourceLocks, boolean readOnly)
    {
        return new DoLock(store, resourceLocks, readOnly);
    }


    @Override
    public DoUnlock doUnlock(IWebdavStore store, IResourceLocks resourceLocks, boolean readOnly)
    {
        return new DoUnlock(store, resourceLocks, readOnly);
    }


    @Override
    public DoMove doMove(ResourceLocks resourceLocks, DoDelete doDelete, DoCopy doCopy, boolean readOnly)
    {
        return new DoMove(resourceLocks, doDelete, doCopy, readOnly);
    }


    @Override
    public DoMkcol doMkcol(IWebdavStore store, IResourceLocks resourceLocks, boolean readOnly)
    {
        return new DoMkcol(store, resourceLocks, readOnly);
    }


    @Override
    public DoOptions doOptions(IWebdavStore store, ResourceLocks resLocks)
    {
        return new DoOptions(store, resLocks);
    }


    @Override
    public DoPut doPut(IWebdavStore store, IResourceLocks resLocks, boolean readOnly, boolean lazyFolderCreationOnPut)
    {
        return new DoPut(store, resLocks, readOnly, lazyFolderCreationOnPut);
    }


    @Override
    public DoPropfind doPropfind(IWebdavStore store, ResourceLocks resLocks, IMimeTyper mimeTyper)
    {
        return new DoPropfind(store, resLocks, mimeTyper);
    }


    @Override
    public DoProppatch doProppatch(IWebdavStore store, ResourceLocks resLocks, boolean readOnly)
    {
        return new DoProppatch(store, resLocks, readOnly);
    }


    @Override
    public DoNotImplemented doNotImplemented(boolean readOnly)
    {
        return new DoNotImplemented(readOnly);
    }

}



