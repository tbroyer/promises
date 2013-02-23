package net.ltgt.promises;

import static org.fest.assertions.api.Assertions.assertThat;

import javax.annotation.Nullable;

import net.ltgt.promises.Promise;
import net.ltgt.promises.Promise.ChainingCallback;
import net.ltgt.promises.Promise.LeafCallback;

import org.junit.Test;


public abstract class PromiseTestBase<P extends Promise<Object>> {

  static class TestChainingCallback extends ChainingCallback<Object, Object> {
    private final Promise<Object> onFulfilled;
    private final Promise<Object> onRejected;

    private boolean actualValueSet;
    private Object actualValue;
    private Throwable actualReason;
  
    public TestChainingCallback(Promise<Object> onFulfilled) {
      this(onFulfilled, null);
    }

    public TestChainingCallback(Promise<Object> onFulfilled, @Nullable Promise<Object> onRejected) {
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

  static class TestLeafCallback extends LeafCallback<Object> {
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
  void assertPending(TestLeafCallback callback) {
    callback.assertPending();
  }

  /** This is a hook for PrefilledPromiseTest. */
  void assertPending(TestChainingCallback callback) {
    callback.assertPending();
  }

  @Test
  public void testOnFulfilled() {
    Object expected = new Object();
    TestLeafCallback callback = new TestLeafCallback();
    P promise = createFulfilledPromise(expected);
  
    promise.then(callback);
    assertPending(callback);
  
    fulfill(promise, expected);
    callback.assertFulfilled(expected);
  }

  @Test
  public void testOnFulfilledWithNull() {
    TestLeafCallback callback = new TestLeafCallback();
    P promise = createFulfilledPromise(null);
  
    promise.then(callback);
    assertPending(callback);
  
    fulfill(promise, null);
    callback.assertFulfilled(null);
  }

  @Test
  public void testOnRejected() {
    Throwable expected = new ClassCastException("foo");
    TestLeafCallback callback = new TestLeafCallback();
    P promise = createRejectedPromise(expected);
  
    promise.then(callback);
    assertPending(callback);
  
    reject(promise, expected);
    callback.assertRejected(expected);
  }

  @Test
  public void testOnAlreadyFulfilled() {
    Object expected = new Object();
    TestLeafCallback callback = new TestLeafCallback();
    P promise = createFulfilledPromise(expected);
    fulfill(promise, expected);
  
    promise.then(callback);
  
    callback.assertFulfilled(expected);
  }

  @Test
  public void testOnAlreadyFulfilledWithNull() {
    TestLeafCallback callback = new TestLeafCallback();
    P promise = createFulfilledPromise(null);
    fulfill(promise, null);
  
    promise.then(callback);
  
    callback.assertFulfilled(null);
  }

  @Test
  public void testOnAlreadyRejected() {
    Throwable expected = new ClassCastException("foo");
    TestLeafCallback callback = new TestLeafCallback();
    P promise = createRejectedPromise(expected);
    reject(promise, expected);
  
    promise.then(callback);
  
    callback.assertRejected(expected);
  }

  @Test
  public void testChainingFulfilled() {
    Object expected1 = new Object();
    Object expected2 = new Object();
    P promise1 = createFulfilledPromise(expected1);
    P promise2 = createFulfilledPromise(expected2);
    TestChainingCallback callback1 = new TestChainingCallback(promise2);
    TestLeafCallback callback2 = new TestLeafCallback();

    promise1.then(callback1).then(callback2);
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
    TestChainingCallback callback1 =
        new TestChainingCallback(createFulfilledPromise(new Object()), promise2);
    TestLeafCallback callback2 = new TestLeafCallback();

    promise1.then(callback1).then(callback2);
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
    TestChainingCallback callback1 = new TestChainingCallback(
        createRejectedPromise(new IllegalArgumentException("bar")), promise2);
    TestLeafCallback callback2 = new TestLeafCallback();

    promise1.then(callback1).then(callback2);
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
    TestChainingCallback callback1 = new TestChainingCallback(promise2);
    TestLeafCallback callback2 = new TestLeafCallback();

    promise1.then(callback1).then(callback2);
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
    TestChainingCallback callback1 = new TestChainingCallback(promise2);
    TestLeafCallback callback2 = new TestLeafCallback();

    fulfill(promise1, expected1);
    fulfill(promise2, expected2);
    
    promise1.then(callback1).then(callback2);

    callback1.assertFulfilled(expected1);
    callback2.assertFulfilled(expected2);
  }

  @Test
  public void testChainingAlreadyRejected() {
    Throwable expected1 = new ClassCastException("foo");
    Throwable expected2 = new IllegalArgumentException("bar");
    P promise1 = createRejectedPromise(expected1);
    P promise2 = createRejectedPromise(expected2);
    TestChainingCallback callback1 =
        new TestChainingCallback(createFulfilledPromise(new Object()), promise2);
    TestLeafCallback callback2 = new TestLeafCallback();

    reject(promise1, expected1);
    reject(promise2, expected2);
    
    promise1.then(callback1).then(callback2);

    callback1.assertRejected(expected1);
    callback2.assertRejected(expected2);
  }

  @Test
  public void testChainingAlreadyRejectedAlreadyFulfilled() {
    Throwable expected1 = new ClassCastException("foo");
    Object expected2 = new Object();
    P promise1 = createRejectedPromise(expected1);
    P promise2 = createFulfilledPromise(expected2);
    TestChainingCallback callback1 = new TestChainingCallback(
        createRejectedPromise(new IllegalArgumentException("bar")), promise2);
    TestLeafCallback callback2 = new TestLeafCallback();

    reject(promise1, expected1);
    fulfill(promise2, expected2);
    
    promise1.then(callback1).then(callback2);

    callback1.assertRejected(expected1);
    callback2.assertFulfilled(expected2);
  }

  @Test
  public void testChainingAlreadyFulfilledAlreadyRejected() {
    Object expected1 = new Object();
    Throwable expected2 = new ClassCastException("foo");
    P promise1 = createFulfilledPromise(expected1);
    P promise2 = createRejectedPromise(expected2);
    TestChainingCallback callback1 = new TestChainingCallback(promise2);
    TestLeafCallback callback2 = new TestLeafCallback();

    fulfill(promise1, expected1);
    reject(promise2, expected2);
    
    promise1.then(callback1).then(callback2);

    callback1.assertFulfilled(expected1);
    callback2.assertRejected(expected2);
  }
}
