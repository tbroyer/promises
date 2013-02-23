package net.ltgt.promises;

import javax.annotation.Nullable;

import net.ltgt.promises.FulfillablePromise;

public class FulfillablePromiseTest extends PromiseTestBase<FulfillablePromise<Object>> {

  @Override
  protected FulfillablePromise<Object> createPromise() {
    return FulfillablePromise.create();
  }

  @Override
  protected void fulfill(FulfillablePromise<Object> promise, @Nullable Object value) {
    promise.fulfill(value);
  }

  @Override
  protected void reject(FulfillablePromise<Object> promise, Throwable reason) {
    promise.reject(reason);
  }
}
