package io.entgra.application.mgt.common;

import java.util.List;

public class SPApplicationListResponse {
    private int totalResults;
    private int startIndex;
    private int count;
    private List<SPApplication> applications;

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<SPApplication> getApplications() {
        return applications;
    }

    public void setApplications(List<SPApplication> applications) {
        this.applications = applications;
    }
}
