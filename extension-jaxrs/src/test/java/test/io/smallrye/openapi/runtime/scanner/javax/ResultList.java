package test.io.smallrye.openapi.runtime.scanner.javax;

import java.util.List;

public class ResultList<T extends BaseModel> {

    private List<T> result;
    private Message error;
    private Integer status;

    public List<T> getResult() {
        return result;
    }

    public Message getError() {
        return error;
    }

    public Integer getStatus() {
        return status;
    }

    public static class ResultBuilder<T extends BaseModel> {

        private Integer status;
        private Message error = new Message();
        private List<T> result;

        public ResultList.ResultBuilder<T> status(Integer status) {
            this.status = status;
            return this;
        }

        public ResultList.ResultBuilder<T> error(String message) {
            this.error = new Message(message);
            return this;
        }

        public ResultList.ResultBuilder<T> error(String message, String description) {
            this.error = new Message(message, description);
            return this;
        }

        public ResultList.ResultBuilder<T> result(List<T> result) {
            this.result = result;
            return this;
        }

        public ResultList<T> build() {
            ResultList<T> response = new ResultList<T>();
            response.status = this.status;
            response.error = this.error;
            response.result = this.result;
            return response;
        }
    }

}
