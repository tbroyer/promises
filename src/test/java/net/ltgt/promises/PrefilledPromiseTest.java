package net.ltgt.promises;

import javax.annotation.Nullable;

import net.ltgt.promises.Promise;
import net.ltgt.promises.Promises;

public class PrefilledPromiseTest extends PromiseTestBase<Promise<Object>> {

  @Override
  void assertPending(TestCallback callback) {
    // no-op
  }

  @Override
  void assertPending(TestImmediateCallback callback) {
    // no-op
  }

  @Override
  void assertPending(TestDoneCallback callback) {
    // no-op
  }

  @Override
  protected Promise<Object> createPromise() {
    throw new AssertionError();
  }

  @Override
  Promise<Object> createFulfilledPromise(@Nullable Object value) {
    return Promises.fulfilled(value);
  }

  @Override
  Promise<Object> createRejectedPromise(Throwable reason) {
    return Promises.rejected(reason);
  }

  @Override
  protected void fulfill(Promise<Object> promise, @Nullable Object value) {
    // no-op
  }

  @Override
  protected void reject(Promise<Object> promise, Throwable reason) {
    // no-op
  }
}
