package net.ltgt.promises;

import static org.fest.assertions.api.Assertions.*;

import javax.annotation.Nullable;

import net.ltgt.promises.Promise.Callback;
import net.ltgt.promises.Promise.DoneCallback;
import net.ltgt.promises.Promise.ImmediateCallback;

import org.junit.Test;


public abstract class PromiseTestBase<P extends Promise<Object>> {

  static class TestCallback extends Callback<Object, Object> {
    private final Promise<Object> onFulfilled;
    private final Promise<Object> onRejected;

    private boolean actualValueSet;
    private Object actualValue;
    private Throwable actualReason;
  
    public TestCallback(Promise<Object> onFulfilled) {
      this(onFulfilled, null);
    }

    public TestCallback(Promise<Object> onFulfilled, @Nullable Promise<Object> onRejected) {
      this.onFulfilled = onFulfilled;
      this.onRejected = onRejected;
    }

    @Override
    public Promise<Object> onFulfilled(@Nullable Object value) {
      assertThat(actualValueSet).as("onFulfilled must not be called more than once").isFalse();
      actualValueSet = true;
      actualValue = value;
      return onFulfilled;
    }
  
    @Override
    public Promise<Object> onRejected(Throwable reason) {
      assertThat(actualReason).as("onRejected must not be called more than once").isNull();
      actualReason = reason;
      return (onRejected == null) ? super.onRejected(reason) : onRejected;
    }
  
    void assertPending() {
      assertThat(actualValueSet).as("Promise unexpectedly fulfilled").isFalse();
      assertThat(actualReason).as("Promise unexpectedly rejected").isNull();
    }
  
    void assertFulfilled(@Nullable Object expectedValue) {
      assertThat(actualReason).as("Promise unexpectedly rejected").isNull();
      assertThat(actualValueSet).as("onFulfilled called").isTrue();
      assertThat(actualValue).as("fulfilled value").isSameAs(expectedValue);
    }
  
    void assertRejected(Throwable expectedReason) {
      assertThat(actualValueSet).as("Promise unexpectedly fulfilled").isFalse();
      assertThat(actualReason).as("rejection reason").isNotNull().isSameAs(expectedReason);
    }
  }

  static class TestImmediateCallback extends ImmediateCallback<Object, Object> {
    private boolean actualValueSet;
    private Object actualValue;
    private Throwable actualReason;

    @Override
    public final Object onFulfilled(@Nullable Object value) throws Throwable {
      assertThat(actualValueSet).as("onFulfilled must not be called more than once").isFalse();
      actualValueSet = true;
      actualValue = value;
      return doOnFulfilled(value);
    }

    protected Object doOnFulfilled(Object value) throws Throwable {
      fail("Promise unexpectedly fulfilled");
      return null;
    }

    @Override
    public final Object onRejected(Throwable reason) throws Throwable {
      assertThat(actualReason).as("onRejected must not be called more than once").isNull();
      actualReason = reason;
      return doOnRejected(reason);
    }

    protected Object doOnRejected(Throwable reason) throws Throwable {
      return super.onRejected(reason);
    }

    void assertPending() {
      assertThat(actualValueSet).as("Promise unexpectedly fulfilled").isFalse();
      assertThat(actualReason).as("Promise unexpectedly rejected").isNull();
    }

    void assertFulfilled(@Nullable Object expectedValue) {
      assertThat(actualReason).as("Promise unexpectedly rejected").isNull();
      assertThat(actualValueSet).as("onFulfilled called").isTrue();
      assertThat(actualValue).as("fulfilled value").isSameAs(expectedValue);
    }

    void assertRejected(Throwable expectedReason) {
      assertThat(actualValueSet).as("Promise unexpectedly fulfilled").isFalse();
      assertThat(actualReason).as("rejection reason").isNotNull().isSameAs(expectedReason);
    }
  }

  static class TestDoneCallback extends DoneCallback<Object> {
    private boolean actualValueSet;
    private Object actualValue;
    private Throwable actualReason;
  
    @Override
    public void onFulfilled(@Nullable Object value) {
      assertThat(actualValueSet).as("onFulfilled must not be called more than once").isFalse();
      actualValueSet = true;
      actualValue = value;
    }
  
    @Override
    public void onRejected(Throwable reason) {
      assertThat(actualReason).as("onRejected must not be called more than once").isNull();
      actualReason = reason;
    }
  
    void assertPending() {
      assertThat(actualValueSet).as("Promise unexpectedly fulfilled").isFalse();
      assertThat(actualReason).as("Promise unexpectedly rejected").isNull();
    }
  
    void assertFulfilled(@Nullable Object expectedValue) {
      assertThat(actualReason).as("Promise unexpectedly rejected").isNull();
      assertThat(actualValueSet).as("onFulfilled called").isTrue();
      assertThat(actualValue).as("fulfilled value").isSameAs(expectedValue);
    }
  
    void assertRejected(Throwable expectedReason) {
      assertThat(actualValueSet).as("Promise unexpectedly fulfilled").isFalse();
      assertThat(actualReason).as("rejection reason").isNotNull().isSameAs(expectedReason);
    }
  }

  /** This is a hook for PrefilledPromiseTest. */
  P createFulfilledPromise(@Nullable Object value) {
    return createPromise();
  }
  
  protected abstract P createPromise();

  /** This is a hook for PrefilledPromiseTest. */
  P createRejectedPromise(Throwable reason) {
    return createPromise();
  }
  
  protected abstract void fulfill(P promise, @Nullable Object value);
  
  protected abstract void reject(P promise, Throwable reason);

  /** This is a hook for PrefilledPromiseTest. */
  void assertPending(TestCallback callback) {
    callback.assertPending();
  }

  /** This is a hook for PrefilledPromiseTest. */
  void assertPending(TestImmediateCallback callback) {
    callback.assertPending();
  }

  /** This is a hook for PrefilledPromiseTest. */
  void assertPending(TestDoneCallback callback) {
    callback.assertPending();
  }

  @Test
  public void testOnFulfilled() {
    Object expected = new Object();
    TestDoneCallback callback = new TestDoneCallback();
    P promise = createFulfilledPromise(expected);
  
    promise.done(callback);
    assertPending(callback);

    // we expect this to be a no-op
    promise.done();
  
    fulfill(promise, expected);
    callback.assertFulfilled(expected);
  }

  @Test
  public void testOnFulfilledWithNull() {
    TestDoneCallback callback = new TestDoneCallback();
    P promise = createFulfilledPromise(null);
  
    promise.done(callback);
    assertPending(callback);

    // we expect this to be a no-op
    promise.done();
  
    fulfill(promise, null);
    callback.assertFulfilled(null);
  }

  @Test
  public void testOnRejected() {
    Throwable expected = new ClassCastException("foo");
    TestDoneCallback callback = new TestDoneCallback();
    P promise = createRejectedPromise(expected);
  
    promise.done(callback);
    assertPending(callback);
  
    reject(promise, expected);
    callback.assertRejected(expected);
  }

  @Test
  public void testOnAlreadyFulfilled() {
    Object expected = new Object();
    TestDoneCallback callback = new TestDoneCallback();
    P promise = createFulfilledPromise(expected);
    fulfill(promise, expected);
  
    promise.done(callback);
  
    callback.assertFulfilled(expected);

    // we expect this to be a no-op
    promise.done();
  }

  @Test
  public void testOnAlreadyFulfilledWithNull() {
    TestDoneCallback callback = new TestDoneCallback();
    P promise = createFulfilledPromise(null);
    fulfill(promise, null);
  
    promise.done(callback);
  
    callback.assertFulfilled(null);

    // we expect this to be a no-op
    promise.done();
  }

  @Test
  public void testOnAlreadyRejected() {
    Exception expected = new ClassCastException("foo");
    TestDoneCallback callback = new TestDoneCallback();
    P promise = createRejectedPromise(expected);
    reject(promise, expected);
  
    promise.done(callback);
  
    callback.assertRejected(expected);

    try {
      promise.done();
      failBecauseExceptionWasNotThrown(expected.getClass());
    } catch (Throwable t) {
      // Should be the same, as it's a RuntimeException.
      assertThat(t).isSameAs(expected);
    }
  }

  @Test
  public void testChainingFulfilled() {
    Object expected1 = new Object();
    Object expected2 = new Object();
    P promise1 = createFulfilledPromise(expected1);
    P promise2 = createFulfilledPromise(expected2);
    TestCallback callback1 = new TestCallback(promise2);
    TestDoneCallback callback2 = new TestDoneCallback();

    promise1.then(callback1).done(callback2);
    assertPending(callback1);
    assertPending(callback2);

    fulfill(promise1, expected1);
    callback1.assertFulfilled(expected1);
    assertPending(callback2);

    fulfill(promise2, expected2);
    callback2.assertFulfilled(expected2);
  }

  @Test
  public void testChainingRejected() {
    Throwable expected1 = new ClassCastException("foo");
    Throwable expected2 = new IllegalArgumentException("bar");
    P promise1 = createRejectedPromise(expected1);
    P promise2 = createRejectedPromise(expected2);
    TestCallback callback1 =
        new TestCallback(createFulfilledPromise(new Object()), promise2);
    TestDoneCallback callback2 = new TestDoneCallback();

    promise1.then(callback1).done(callback2);
    assertPending(callback1);
    assertPending(callback2);

    reject(promise1, expected1);
    callback1.assertRejected(expected1);
    assertPending(callback2);

    reject(promise2, expected2);
    callback2.assertRejected(expected2);
  }

  @Test
  public void testChainingRejectedFulfilled() {
    Throwable expected1 = new ClassCastException("foo");
    Object expected2 = new Object();
    P promise1 = createRejectedPromise(expected1);
    P promise2 = createFulfilledPromise(expected2);
    TestCallback callback1 = new TestCallback(
        createRejectedPromise(new IllegalArgumentException("bar")), promise2);
    TestDoneCallback callback2 = new TestDoneCallback();

    promise1.then(callback1).done(callback2);
    assertPending(callback1);
    assertPending(callback2);

    reject(promise1, expected1);
    callback1.assertRejected(expected1);
    assertPending(callback2);

    fulfill(promise2, expected2);
    callback2.assertFulfilled(expected2);
  }

  @Test
  public void testChainingFulfilledRejected() {
    Object expected1 = new Object();
    Throwable expected2 = new ClassCastException("foo");
    P promise1 = createFulfilledPromise(expected1);
    P promise2 = createRejectedPromise(expected2);
    TestCallback callback1 = new TestCallback(promise2);
    TestDoneCallback callback2 = new TestDoneCallback();

    promise1.then(callback1).done(callback2);
    assertPending(callback1);
    assertPending(callback2);

    fulfill(promise1, expected1);
    callback1.assertFulfilled(expected1);
    assertPending(callback2);

    reject(promise2, expected2);
    callback2.assertRejected(expected2);
  }

  @Test
  public void testChainingAlreadyFulfilled() {
    Object expected1 = new Object();
    Object expected2 = new Object();
    P promise1 = createFulfilledPromise(expected1);
    P promise2 = createFulfilledPromise(expected2);
    TestCallback callback1 = new TestCallback(promise2);
    TestDoneCallback callback2 = new TestDoneCallback();

    fulfill(promise1, expected1);
    fulfill(promise2, expected2);
    
    promise1.then(callback1).done(callback2);

    callback1.assertFulfilled(expected1);
    callback2.assertFulfilled(expected2);
  }

  @Test
  public void testChainingAlreadyRejected() {
    Throwable expected1 = new ClassCastException("foo");
    Throwable expected2 = new IllegalArgumentException("bar");
    P promise1 = createRejectedPromise(expected1);
    P promise2 = createRejectedPromise(expected2);
    TestCallback callback1 =
        new TestCallback(createFulfilledPromise(new Object()), promise2);
    TestDoneCallback callback2 = new TestDoneCallback();

    reject(promise1, expected1);
    reject(promise2, expected2);
    
    promise1.then(callback1).done(callback2);

    callback1.assertRejected(expected1);
    callback2.assertRejected(expected2);
  }

  @Test
  public void testChainingAlreadyRejectedAlreadyFulfilled() {
    Throwable expected1 = new ClassCastException("foo");
    Object expected2 = new Object();
    P promise1 = createRejectedPromise(expected1);
    P promise2 = createFulfilledPromise(expected2);
    TestCallback callback1 = new TestCallback(
        createRejectedPromise(new IllegalArgumentException("bar")), promise2);
    TestDoneCallback callback2 = new TestDoneCallback();

    reject(promise1, expected1);
    fulfill(promise2, expected2);
    
    promise1.then(callback1).done(callback2);

    callback1.assertRejected(expected1);
    callback2.assertFulfilled(expected2);
  }

  @Test
  public void testChainingAlreadyFulfilledAlreadyRejected() {
    Object expected1 = new Object();
    Throwable expected2 = new ClassCastException("foo");
    P promise1 = createFulfilledPromise(expected1);
    P promise2 = createRejectedPromise(expected2);
    TestCallback callback1 = new TestCallback(promise2);
    TestDoneCallback callback2 = new TestDoneCallback();

    fulfill(promise1, expected1);
    reject(promise2, expected2);
    
    promise1.then(callback1).done(callback2);

    callback1.assertFulfilled(expected1);
    callback2.assertRejected(expected2);
  }

  @Test
  public void testChainingImmediateFulfilled() {
    Object expected1 = new Object();
    final Object expected2 = new Object();
    P promise1 = createFulfilledPromise(expected1);
    TestImmediateCallback callback1 = new TestImmediateCallback() {
      protected Object doOnFulfilled(Object value) throws Throwable {
        return expected2;
      }
    };
    TestDoneCallback callback2 = new TestDoneCallback();

    promise1.then(callback1).done(callback2);
    assertPending(callback1);
    assertPending(callback2);

    fulfill(promise1, expected1);
    callback1.assertFulfilled(expected1);
    callback2.assertFulfilled(expected2);
  }

  @Test
  public void testChainingImmediateRejected() {
    Throwable expected1 = new ClassCastException("foo");
    final Throwable expected2 = new IllegalArgumentException("bar");
    P promise1 = createRejectedPromise(expected1);
    TestImmediateCallback callback1 = new TestImmediateCallback() {
      @Override
      protected Object doOnRejected(Throwable reason) throws Throwable {
        throw expected2;
      }
    };
    TestDoneCallback callback2 = new TestDoneCallback();

    promise1.then(callback1).done(callback2);
    assertPending(callback1);
    assertPending(callback2);

    reject(promise1, expected1);
    callback1.assertRejected(expected1);
    callback2.assertRejected(expected2);
  }

  @Test
  public void testChainingImmediateRejectedChained() {
    Throwable expected1 = new ClassCastException("foo");
    P promise1 = createRejectedPromise(expected1);
    TestImmediateCallback callback1 = new TestImmediateCallback();
    TestDoneCallback callback2 = new TestDoneCallback();

    promise1.then(callback1).done(callback2);
    assertPending(callback1);
    assertPending(callback2);

    reject(promise1, expected1);
    callback1.assertRejected(expected1);
    callback2.assertRejected(expected1);
  }

  @Test
  public void testChainingImmediateRejectedFulfilled() {
    Throwable expected1 = new ClassCastException("foo");
    final Object expected2 = new Object();
    P promise1 = createRejectedPromise(expected1);
    TestImmediateCallback callback1 = new TestImmediateCallback() {
      protected Object doOnRejected(Throwable reason) throws Throwable {
        return expected2;
      }
    };
    TestDoneCallback callback2 = new TestDoneCallback();

    promise1.then(callback1).done(callback2);
    assertPending(callback1);
    assertPending(callback2);

    reject(promise1, expected1);
    callback1.assertRejected(expected1);
    callback2.assertFulfilled(expected2);
  }

  @Test
  public void testChainingImmediateFulfilledRejected() {
    Object expected1 = new Object();
    final Throwable expected2 = new ClassCastException("foo");
    P promise1 = createFulfilledPromise(expected1);
    TestImmediateCallback callback1 = new TestImmediateCallback() {
      @Override
      protected Object doOnFulfilled(Object value) throws Throwable {
        throw expected2;
      }
    };
    TestDoneCallback callback2 = new TestDoneCallback();

    promise1.then(callback1).done(callback2);
    assertPending(callback1);
    assertPending(callback2);

    fulfill(promise1, expected1);
    callback1.assertFulfilled(expected1);
    callback2.assertRejected(expected2);
  }

  @Test
  public void testChainingImmediateAlreadyFulfilled() {
    Object expected1 = new Object();
    final Object expected2 = new Object();
    P promise1 = createFulfilledPromise(expected1);
    TestImmediateCallback callback1 = new TestImmediateCallback() {
      @Override
      protected Object doOnFulfilled(Object value) throws Throwable {
        return expected2;
      }
    };
    TestDoneCallback callback2 = new TestDoneCallback();

    fulfill(promise1, expected1);

    promise1.then(callback1).done(callback2);

    callback1.assertFulfilled(expected1);
    callback2.assertFulfilled(expected2);
  }

  @Test
  public void testChainingImmediateAlreadyRejected() {
    Throwable expected1 = new ClassCastException("foo");
    final Throwable expected2 = new IllegalArgumentException("bar");
    P promise1 = createRejectedPromise(expected1);
    TestImmediateCallback callback1 = new TestImmediateCallback() {
      @Override
      protected Object doOnRejected(Throwable reason) throws Throwable {
        throw expected2;
      }
    };
    TestDoneCallback callback2 = new TestDoneCallback();

    reject(promise1, expected1);

    promise1.then(callback1).done(callback2);

    callback1.assertRejected(expected1);
    callback2.assertRejected(expected2);
  }

  @Test
  public void testChainingImmediateAlreadyRejectedChained() {
    Throwable expected1 = new ClassCastException("foo");
    P promise1 = createRejectedPromise(expected1);
    TestImmediateCallback callback1 = new TestImmediateCallback();
    TestDoneCallback callback2 = new TestDoneCallback();

    reject(promise1, expected1);

    promise1.then(callback1).done(callback2);

    callback1.assertRejected(expected1);
    callback2.assertRejected(expected1);
  }

  @Test
  public void testChainingImmediateAlreadyRejectedFulfilled() {
    Throwable expected1 = new ClassCastException("foo");
    final Object expected2 = new Object();
    P promise1 = createRejectedPromise(expected1);
    TestImmediateCallback callback1 = new TestImmediateCallback() {
      @Override
      protected Object doOnRejected(Throwable reason) throws Throwable {
        return expected2;
      }
    };
    TestDoneCallback callback2 = new TestDoneCallback();

    reject(promise1, expected1);

    promise1.then(callback1).done(callback2);

    callback1.assertRejected(expected1);
    callback2.assertFulfilled(expected2);
  }

  @Test
  public void testChainingImmediateAlreadyFulfilledRejected() {
    Object expected1 = new Object();
    final Throwable expected2 = new ClassCastException("foo");
    P promise1 = createFulfilledPromise(expected1);
    TestImmediateCallback callback1 = new TestImmediateCallback() {
      @Override
      protected Object doOnFulfilled(Object value) throws Throwable {
        throw expected2;
      }
    };
    TestDoneCallback callback2 = new TestDoneCallback();

    fulfill(promise1, expected1);

    promise1.then(callback1).done(callback2);

    callback1.assertFulfilled(expected1);
    callback2.assertRejected(expected2);
  }

  @Test
  public void testManyCallbacksFulfilled() {
    Object expected = new Object();
    P promise = createFulfilledPromise(expected);
    TestDoneCallback before = new TestDoneCallback();
    TestDoneCallback after1 = new TestDoneCallback();
    TestDoneCallback after2 = new TestDoneCallback();

    promise.done(before);
    fulfill(promise, expected);
    promise.done(after1);
    promise.done(after2);

    before.assertFulfilled(expected);
    after1.assertFulfilled(expected);
    after2.assertFulfilled(expected);
  }

  @Test
  public void testManyCallbacksRejected() {
    Throwable expected = new ClassCastException("foo");
    P promise = createRejectedPromise(expected);
    TestDoneCallback before = new TestDoneCallback();
    TestDoneCallback after1 = new TestDoneCallback();
    TestDoneCallback after2 = new TestDoneCallback();

    promise.done(before);
    reject(promise, expected);
    promise.done(after1);
    promise.done(after2);

    before.assertRejected(expected);
    after1.assertRejected(expected);
    after2.assertRejected(expected);
  }
}
