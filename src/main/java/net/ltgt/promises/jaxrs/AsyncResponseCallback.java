package net.ltgt.promises.jaxrs;

import java.util.Objects;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;

import net.ltgt.promises.Promise.LeafCallback;

public final class AsyncResponseCallback extends LeafCallback<Object> {

  public static AsyncResponseCallback create(AsyncResponse response) {
    return new AsyncResponseCallback(response);
  }

  private final AsyncResponse response;

  private AsyncResponseCallback(AsyncResponse response) {
    this.response = Objects.requireNonNull(response);
  }

  @Override
  public void onFulfilled(Object value) {
    response.resume(value);
  }

  @Override
  public void onRejected(Throwable reason) {
    // FIXME: workarounds http://java.net/jira/browse/JERSEY-1753
    if (reason instanceof WebApplicationException) {
      response.resume(((WebApplicationException) reason).getResponse());
    } else if (reason instanceof RuntimeException) {
      response.resume(reason);
    }
  }
}
