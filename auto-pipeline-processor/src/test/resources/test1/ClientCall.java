package test1;

public class ClientCall<ReqT, RespT> {

    private final CallOptions callOptions;

    public ClientCall(CallOptions callOptions) {
        this.callOptions = callOptions;
    }

    public CallOptions getCallOptions() {
        return callOptions;
    }
}
