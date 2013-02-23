package net.ltgt.promises;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

public class FulfillablePromise<V> implements Promise<V> {

  public static <V> FulfillablePromise<V> create() {
    return new FulfillablePromise<>();
  }

  private enum State {
    PENDING,
    FULFILLED,
    REJECTED,
  }

  private State state = State.PENDING;
  private V value;
  private Throwable reason;

  private ChainingCallback<? super V,Object> callback;
  private FulfillablePromise<Object> next;

  public synchronized void fulfill(@Nullable V value) {
    assert state == State.PENDING;

    state = State.FULFILLED;
    this.value = value;

    if (callback != null) {
      if (next == null) {
        callback.onFulfilled(value);
      } else {
        try {
          chain(callback.onFulfilled(value), next);
        } catch (Throwable t) {
          next.reject(t);
        }
      }
    }
  }

  public synchronized void reject(Throwable reason) {
    assert state == State.PENDING;

    state = State.REJECTED;
    this.reason = reason;

    if (callback != null) {
      if (next == null) {
        callback.onRejected(reason);
      } else {
        try {
          chain(callback.onRejected(reason), next);
        } catch (Throwable t) {
          next.reject(t);
        }
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public synchronized <R> Promise<R> then(final ChainingCallback<? super V, R> callback) {
    switch (state) {
    case FULFILLED:
      try {
        return callback.onFulfilled(value);
      } catch (Throwable t) {
        return Promises.rejected(t);
      }
    case REJECTED:
      try {
        return callback.onRejected(reason);
      } catch (Throwable t) {
        return Promises.rejected(t);
      }
    case PENDING:
    default:
      this.callback = (ChainingCallback<? super V, Object>) requireNonNull(callback);
      next = new FulfillablePromise<>();
      return (Promise<R>) next;
    }
  }

  @Override
  public synchronized void then(final LeafCallback<? super V> callback) {
    switch (state) {
    case FULFILLED:
      callback.onFulfilled(value);
      break;
    case REJECTED:
        callback.onRejected(reason);
        break;
    case PENDING:
    default:
      this.callback = (ChainingCallback<? super V, Object>) wrap(callback);
    }
  }

  private static <V> void chain(Promise<V> promise, @Nullable final FulfillablePromise<V> into) {
    if (into == null) {
      return;
    }
    promise.then(new LeafCallback<V>() {
      @Override
      public void onFulfilled(V value) {
        into.fulfill(value);
      }

      @Override
      public void onRejected(Throwable reason) {
        into.reject(reason);
      }
    });
  }

  private ChainingCallback<V, Object> wrap(final LeafCallback<? super V> callback) {
    return new ChainingCallback<V, Object>() {
      @Override
      public Promise<Object> onFulfilled(V value) {
        callback.onFulfilled(value);
        return null;
      }
      @Override
      public Promise<Object> onRejected(Throwable reason) {
        callback.onRejected(reason);
        return null;
      }
    };
  }
}
