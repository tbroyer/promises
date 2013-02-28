package net.ltgt.promises.jaxrs;

import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.GenericType;

import net.ltgt.promises.ForwardingPromise;
import net.ltgt.promises.FulfillablePromise;
import net.ltgt.promises.Promise;

/**
 * An {@link InvocationCallback} that is also a {@link Promise}.
 * <p>
 * Because of the way JAX-RS works, you have to create a subclass (generally anonymous) of
 * {@link InvocationCallbackPromise} in order to <i>record</i> the generic type argument into
 * the type, so that JAX-RS can <i>read</i> using reflection; similar to {@link GenericType}.
 * <p>
 * For example:
 * <pre><code>
 * InvocationCallbackPromise&lt;JsonObject> promise = new InvocationCallbackPromise&ltJsonObject>() {};
 * asyncInvoker.get(promise);
 * promise.then(…).then(…);
 * </code></pre>
 */
public abstract class InvocationCallbackPromise<V> extends ForwardingPromise<V> implements InvocationCallback<V> {

  private final FulfillablePromise<V> promise = FulfillablePromise.create();

  @Override
  public void completed(V response) {
    promise.fulfill(response);
  }

  @Override
  public void failed(Throwable throwable) {
    promise.reject(throwable);
  }

  @Override
  protected Promise<V> getDelegate() {
    return promise;
  }
}
