package org.fourthline.cling.support.contentdirectory.callback;

import java.util.logging.Logger;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SearchResult;
import org.fourthline.cling.support.model.SortCriterion;
/* loaded from: classes.dex */
public abstract class Search extends ActionCallback {
    public static final String CAPS_WILDCARD = "*";
    private static Logger log = Logger.getLogger(Search.class.getName());

    public abstract void received(ActionInvocation actionInvocation, DIDLContent dIDLContent);

    public abstract void updateStatus(Status status);

    /* loaded from: classes.dex */
    public enum Status {
        NO_CONTENT("No Content"),
        LOADING("Loading..."),
        OK("OK");
        
        private String defaultMessage;

        Status(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        public String getDefaultMessage() {
            return this.defaultMessage;
        }
    }

    public Search(Service service, String containerId, String searchCriteria) {
        this(service, containerId, searchCriteria, "*", 0L, null, new SortCriterion[0]);
    }

    public Search(Service service, String containerId, String searchCriteria, String filter, long firstResult, Long maxResults, SortCriterion... orderBy) {
        super(new ActionInvocation(service.getAction("Search")));
        Logger logger = log;
        logger.fine("Creating browse action for container ID: " + containerId);
        getActionInvocation().setInput("ContainerID", containerId);
        getActionInvocation().setInput("SearchCriteria", searchCriteria);
        getActionInvocation().setInput("Filter", filter);
        getActionInvocation().setInput("StartingIndex", new UnsignedIntegerFourBytes(firstResult));
        getActionInvocation().setInput("RequestedCount", new UnsignedIntegerFourBytes((maxResults == null ? getDefaultMaxResults() : maxResults).longValue()));
        getActionInvocation().setInput("SortCriteria", SortCriterion.toString(orderBy));
    }

    @Override // org.fourthline.cling.controlpoint.ActionCallback, java.lang.Runnable
    public void run() {
        updateStatus(Status.LOADING);
        super.run();
    }

    @Override // org.fourthline.cling.controlpoint.ActionCallback
    public void success(ActionInvocation actionInvocation) {
        log.fine("Successful search action, reading output argument values");
        SearchResult result = new SearchResult(actionInvocation.getOutput("Result").getValue().toString(), (UnsignedIntegerFourBytes) actionInvocation.getOutput("NumberReturned").getValue(), (UnsignedIntegerFourBytes) actionInvocation.getOutput("TotalMatches").getValue(), (UnsignedIntegerFourBytes) actionInvocation.getOutput("UpdateID").getValue());
        boolean proceed = receivedRaw(actionInvocation, result);
        if (proceed && result.getCountLong() > 0 && result.getResult().length() > 0) {
            try {
                DIDLParser didlParser = new DIDLParser();
                DIDLContent didl = didlParser.parse(result.getResult());
                received(actionInvocation, didl);
                updateStatus(Status.OK);
                return;
            } catch (Exception ex) {
                ErrorCode errorCode = ErrorCode.ACTION_FAILED;
                actionInvocation.setFailure(new ActionException(errorCode, "Can't parse DIDL XML response: " + ex, ex));
                failure(actionInvocation, null);
                return;
            }
        }
        received(actionInvocation, new DIDLContent());
        updateStatus(Status.NO_CONTENT);
    }

    public Long getDefaultMaxResults() {
        return 999L;
    }

    public boolean receivedRaw(ActionInvocation actionInvocation, SearchResult searchResult) {
        return true;
    }
}
