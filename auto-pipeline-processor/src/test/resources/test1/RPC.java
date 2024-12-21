package test1;

import com.foldright.auto.pipeline.AutoPipeline;
import com.foldright.auto.pipeline.PipelineDirection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.foldright.auto.pipeline.PipelineDirection.Direction.REVERSE;

@AutoPipeline
public interface RPC {


    Response request(Request request);

    @PipelineDirection(REVERSE)
    void onResponse(Response response);


    class Request {
        private final List<String> infos = new ArrayList<>();

        public void append(String info) {
            infos.add(info);
        }

        public List<String> infos() {
            return Collections.unmodifiableList(infos);
        }
    }

    class Response {
        private final List<String> infos = new ArrayList<>();

        public void append(String info) {
            infos.add(info);
        }

        public List<String> infos() {
            return Collections.unmodifiableList(infos);
        }
    }
}
