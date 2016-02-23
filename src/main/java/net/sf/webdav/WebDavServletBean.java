package net.sf.webdav;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.webdav.exceptions.UnauthenticatedException;
import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.fromcatalina.MD5Encoder;
import net.sf.webdav.locking.ResourceLocks;
import net.sf.webdav.methods.DoCopy;
import net.sf.webdav.methods.DoDelete;
import net.sf.webdav.methods.FactoryMethod;
import net.sf.webdav.methods.FactoryMethodImpl;

public class WebDavServletBean extends HttpServlet {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory
            .getLogger(WebDavServletBean.class);

    /**
     * MD5 message digest provider.
     */
    protected static MessageDigest MD5_HELPER;

    /**
     * The MD5 helper object for this class.
     */
    protected static final MD5Encoder MD5_ENCODER = new MD5Encoder();

    private static final boolean READ_ONLY = false;
    private ResourceLocks _resLocks;
    private IWebdavStore _store;
    private HashMap<String, IMethodExecutor> _methodMap = new HashMap<String, IMethodExecutor>();

    public WebDavServletBean() {
        _resLocks = new ResourceLocks();

        try {
            MD5_HELPER = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException();
        }
    }

    public void init(IWebdavStore store, String dftIndexFile,
                     String insteadOf404, int nocontentLenghHeaders,
                     boolean lazyFolderCreationOnPut) throws ServletException {
        init(new FactoryMethodImpl(), store, dftIndexFile,
             insteadOf404, nocontentLenghHeaders,
             lazyFolderCreationOnPut);
    }

    public void init(FactoryMethod factory, IWebdavStore store, String dftIndexFile,
            String insteadOf404, int nocontentLenghHeaders,
            boolean lazyFolderCreationOnPut) throws ServletException {

        _store = store;

        IMimeTyper mimeTyper = new IMimeTyper() {
            public String getMimeType(String path) {
                return getServletContext().getMimeType(path);
            }
        };

        register("GET", factory.doGet(store, dftIndexFile, insteadOf404, _resLocks,
                mimeTyper, nocontentLenghHeaders));
        register("HEAD", factory.doHead(store, dftIndexFile, insteadOf404,
                _resLocks, mimeTyper, nocontentLenghHeaders));
        DoDelete doDelete = (DoDelete) register("DELETE", factory.doDelete(store,
                _resLocks, READ_ONLY));
        DoCopy doCopy = (DoCopy) register("COPY", factory.doCopy(store, _resLocks,
                doDelete, READ_ONLY));
        register("LOCK", factory.doLock(store, _resLocks, READ_ONLY));
        register("UNLOCK", factory.doUnlock(store, _resLocks, READ_ONLY));
        register("MOVE", factory.doMove(_resLocks, doDelete, doCopy, READ_ONLY));
        register("MKCOL", factory.doMkcol(store, _resLocks, READ_ONLY));
        register("OPTIONS", factory.doOptions(store, _resLocks));
        register("PUT", factory.doPut(store, _resLocks, READ_ONLY,
                lazyFolderCreationOnPut));
        register("PROPFIND", factory.doPropfind(store, _resLocks, mimeTyper));
        register("PROPPATCH", factory.doProppatch(store, _resLocks, READ_ONLY));
        register("*NO*IMPL*", factory.doNotImplemented(READ_ONLY));
    }


    protected IMethodExecutor register(String methodName, IMethodExecutor method) {
        _methodMap.put(methodName, method);
        return method;
    }


    protected boolean isReadOnly()
    {
        return WebDavServletBean.READ_ONLY;
    }


    public ResourceLocks getResLocks()
    {
        return _resLocks;
    }


    /**
     * Handles the special WebDAV methods.
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String methodName = req.getMethod();
        ITransaction transaction = null;
        boolean needRollback = false;

        if (LOG.isTraceEnabled())
            debugRequest(methodName, req);

        try {
            Principal userPrincipal = req.getUserPrincipal();
            transaction = _store.begin(userPrincipal);
            needRollback = true;
            _store.checkAuthentication(transaction);
            resp.setStatus(WebdavStatus.SC_OK);

            try {
                IMethodExecutor methodExecutor = (IMethodExecutor) _methodMap
                        .get(methodName);
                if (methodExecutor == null) {
                    methodExecutor = (IMethodExecutor) _methodMap
                            .get("*NO*IMPL*");
                }

                methodExecutor.execute(transaction, req, resp);

                _store.commit(transaction);
                /** Clear not consumed data
                 *
                 * Clear input stream if available otherwise later access
                 * include current input.  These cases occur if the client
                 * sends a request with body to an not existing resource.
                 */
                if (req.getContentLength() != 0 && req.getInputStream().available() > 0) {
                    if (LOG.isTraceEnabled()) { LOG.trace("Clear not consumed data!"); }
                    while (req.getInputStream().available() > 0) {
                        req.getInputStream().read();
                    }
                }
                needRollback = false;
            } catch (IOException e) {
                java.io.StringWriter sw = new java.io.StringWriter();
                java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                e.printStackTrace(pw);
                LOG.error("IOException: " + sw.toString());
                resp.sendError(WebdavStatus.SC_INTERNAL_SERVER_ERROR);
                _store.rollback(transaction);
                throw new ServletException(e);
            }

        } catch (UnauthenticatedException e) {
            resp.sendError(WebdavStatus.SC_FORBIDDEN);
        } catch (WebdavException e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            LOG.error("WebdavException: " + sw.toString());
            throw new ServletException(e);
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            LOG.error("Exception: " + sw.toString());
        } finally {
            if (needRollback)
                _store.rollback(transaction);
        }

    }

    private void debugRequest(String methodName, HttpServletRequest req) {
        LOG.trace("-----------");
        LOG.trace("WebdavServlet\n request: methodName = " + methodName);
        LOG.trace("time: " + System.currentTimeMillis());
        LOG.trace("path: " + req.getRequestURI());
        LOG.trace("-----------");
        Enumeration<?> e = req.getHeaderNames();
        while (e.hasMoreElements()) {
            String s = (String) e.nextElement();
            LOG.trace("header: " + s + " " + req.getHeader(s));
        }
        e = req.getAttributeNames();
        while (e.hasMoreElements()) {
            String s = (String) e.nextElement();
            LOG.trace("attribute: " + s + " " + req.getAttribute(s));
        }
        e = req.getParameterNames();
        while (e.hasMoreElements()) {
            String s = (String) e.nextElement();
            LOG.trace("parameter: " + s + " " + req.getParameter(s));
        }
    }

}
