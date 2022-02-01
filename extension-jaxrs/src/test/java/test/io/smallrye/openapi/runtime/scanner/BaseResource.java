package test.io.smallrye.openapi.runtime.scanner;

import jakarta.ws.rs.core.Response;

public abstract class BaseResource<T extends BaseModel> {

    protected ResultList<T> getAll1() {
        return new ResultList.ResultBuilder<T>().status(200).build();
    }

    protected Result<T> post1(T t) {
        return new Result.ResultBuilder<T>().status(200).build();
    }

    protected Result<T> put1(T e) {
        return new Result.ResultBuilder<T>().status(200).build();
    }

    protected Response delete1(T t) {
        return Response.status(Response.Status.NO_CONTENT).build();
    }

}
