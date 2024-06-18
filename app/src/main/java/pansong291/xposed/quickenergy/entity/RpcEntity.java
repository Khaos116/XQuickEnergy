package pansong291.xposed.quickenergy.entity;

import lombok.Getter;

@Getter
public class RpcEntity {

    private final Thread requestThread;

    private final String requestMethod;

    private final String requestData;

    private volatile Boolean hasResult = false;

    private volatile Boolean hasError = false;

    private volatile Object responseObject;

    private volatile String responseString;

    public RpcEntity(String requestMethod, String requestData) {
        this.requestThread = Thread.currentThread();
        this.requestMethod = requestMethod;
        this.requestData = requestData;
    }

    public RpcEntity() {
        this(null, null);
    }

    public void setResponseObject(Object result, String resultStr) {
        this.hasResult = true;
        this.responseObject = result;
        this.responseString = resultStr;
    }

    public void setError() {
        this.hasError = true;
    }

    public Thread getRequestThread() {
        return requestThread;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestData() {
        return requestData;
    }

    public Boolean getHasResult() {
        return hasResult;
    }

    public void setHasResult(Boolean hasResult) {
        this.hasResult = hasResult;
    }

    public Boolean getHasError() {
        return hasError;
    }

    public void setHasError(Boolean hasError) {
        this.hasError = hasError;
    }

    public Object getResponseObject() {
        return responseObject;
    }

    public void setResponseObject(Object responseObject) {
        this.responseObject = responseObject;
    }

    public String getResponseString() {
        return responseString;
    }

    public void setResponseString(String responseString) {
        this.responseString = responseString;
    }
}
