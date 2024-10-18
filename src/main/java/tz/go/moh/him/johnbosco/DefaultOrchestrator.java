package tz.go.moh.him.johnbosco;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import java.util.HashMap;
import java.util.Map;
public class DefaultOrchestrator extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final MediatorConfig config;

    public DefaultOrchestrator(MediatorConfig config) {
        this.config = config;
    }
    private final Map<String, String> endpointMap = new HashMap<String, String>() {{
        put("1", "http://endpoint1.example.com");
        put("2", "http://endpoint2.example.com");
        put("3", "http://endpoint3.example.com");
    }};

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof MediatorHTTPRequest) {
            String body = ((MediatorHTTPRequest) msg).getBody();
            JSONObject payload = new JSONObject(body);
            String HfrCode = payload.getString("HfrCode");

            if (endpointMap.containsKey(HfrCode)) {
                String endpoint = endpointMap.get(HfrCode);
                sendRequest(endpoint, (MediatorHTTPRequest) msg);
            } else {
                FinishRequest fr = new FinishRequest("No endpoint found for HfrCode: " + HfrCode, "text/plain", HttpStatus.SC_NOT_FOUND);
                ((MediatorHTTPRequest) msg).getRequestHandler().tell(fr, getSelf());
            }
        } else {
            unhandled(msg);
        }
    }

    private void sendRequest(String endpoint, MediatorHTTPRequest originalRequest) {
        log.info("Sending request to endpoint: " + endpoint);
        MediatorHTTPRequest request = new MediatorHTTPRequest(
                originalRequest.getRequestHandler(),
                getSelf(),
                "Endpoint Service",
                "POST",
                endpoint,
                originalRequest.getBody(),
                originalRequest.getHeaders(),
                null
        );
        ActorSelection httpConnector = getContext().actorSelection(config.userPathFor("http-connector"));
        httpConnector.tell(request, getSelf());
    }

    private void handleRequest(MediatorHTTPRequest request) throws Exception {
        log.info("Received request: " + request.getPath());
        String body = request.getBody();
        JSONObject payload = new JSONObject(body);
        String HfrCode = payload.getString("HfrCode");

        if (endpointMap.containsKey(HfrCode)) {
            String endpoint = endpointMap.get(HfrCode);
            sendRequest(endpoint, request);
        } else {
            request.getRequestHandler().tell(new FinishRequest("No endpoint found for HfrCode: " + HfrCode, "text/plain", HttpStatus.SC_NOT_FOUND), getSelf());
        }
    }
}
