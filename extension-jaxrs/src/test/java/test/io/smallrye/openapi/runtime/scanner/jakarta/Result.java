package test.io.smallrye.openapi.runtime.scanner.jakarta;

public class Result<T> {

    private T result;
    private Message error;
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public Message getError() {
        return error;
    }

    public T getResult() {
        return result;
    }

    public static class ResultBuilder<T> {

        private Integer status;
        private Message error = new Message();
        private T result;

        public ResultBuilder<T> status(Integer status) {
            this.status = status;
            return this;
        }

        public ResultBuilder<T> error(String message) {
            this.error = new Message(message);
            return this;
        }

        public ResultBuilder<T> error(String message, String description) {
            this.error = new Message(message, description);
            return this;
        }

        public ResultBuilder<T> result(T result) {
            this.result = result;
            return this;
        }

        public Result<T> build() {
            Result<T> response = new Result<T>();
            response.status = this.status;
            response.error = this.error;
            response.result = this.result;
            return response;
        }
    }

}
