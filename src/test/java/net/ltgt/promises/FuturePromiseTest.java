package net.ltgt.promises;

import static org.fest.assertions.api.Assertions.*;

import java.util.concurrent.ExecutionException;

import org.junit.Test;

import net.ltgt.promises.FuturePromise;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

public class FuturePromiseTest extends PromiseTestBase<FuturePromise<Object>> {

  @Override
  protected FuturePromise<Object> createPromise() {
    return FuturePromise.create(SettableFuture.create());
  }

  @Override
  protected void fulfill(FuturePromise<Object> promise, Object value) {
    ((SettableFuture<Object>) promise.delegate()).set(value);
  }

  @Override
  protected void reject(FuturePromise<Object> promise, Throwable reason) {
    ((SettableFuture<Object>) promise.delegate()).setException(reason);
  }

  @Test
  public void testAdapt() throws Throwable {
    Object expected = new Object();
    FulfillablePromise<Object> promise = FulfillablePromise.create();
    ListenableFuture<Object> future = FuturePromise.adapt(promise);

    promise.fulfill(expected);

    assertThat(future.get()).isSameAs(expected);
  }

  @Test
  public void testAdaptRejected() {
    Throwable expected = new ClassCastException("foo");
    FulfillablePromise<Object> promise = FulfillablePromise.create();
    ListenableFuture<Object> future = FuturePromise.adapt(promise);

    promise.reject(expected);

    try {
      future.get();
      failBecauseExceptionWasNotThrown(ExecutionException.class);
    } catch (Throwable t) {
      assertThat(t).isInstanceOf(ExecutionException.class);
      assertThat(t.getCause()).isSameAs(expected);
    }
  }

  @Test
  public void testAdaptAlreadyFulfilled() throws Throwable {
    Object expected = new Object();
    Promise<Object> promise = Promises.fulfilled(expected);
    ListenableFuture<Object> future = FuturePromise.adapt(promise);

    assertThat(future.get()).isSameAs(expected);
  }

  @Test
  public void testAdaptAlreadyRejected() {
    Throwable expected = new ClassCastException("foo");
    Promise<Object> promise = Promises.rejected(expected);
    ListenableFuture<Object> future = FuturePromise.adapt(promise);

    try {
      future.get();
      failBecauseExceptionWasNotThrown(ExecutionException.class);
    } catch (Throwable t) {
      assertThat(t).isInstanceOf(ExecutionException.class);
      assertThat(t.getCause()).isSameAs(expected);
    }
  }
}
