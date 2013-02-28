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

  private static abstract class Handler<V> {
    Handler<V> next;

    abstract void fulfill(V value);

    abstract void reject(Throwable reason);
  }

  private State state = State.PENDING;
  private V value;
  private Throwable reason;

  private Handler<V> first;
  private Handler<V> last;

  public synchronized void fulfill(@Nullable V value) {
    if (state != State.PENDING) {
      throw new IllegalStateException();
    }

    state = State.FULFILLED;
    this.value = value;

    for (Handler<V> handler = first; handler != null; handler = handler.next) {
      handler.fulfill(value);
    }
    first = last = null;
  }

  public synchronized void reject(Throwable reason) {
    if (state != State.PENDING) {
      throw new IllegalStateException();
    }

    state = State.REJECTED;
    this.reason = reason;

    for (Handler<V> handler = first; handler != null; handler = handler.next) {
      handler.reject(reason);
    }
    first = last = null;
  }

  @Override
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
      requireNonNull(callback);
      final FulfillablePromise<R> promise = new FulfillablePromise<>();
      addHandler(new Handler<V>() {
        @Override
        void fulfill(V value) {
          try {
            chain(callback.onFulfilled(value), promise);
          } catch (Throwable t) {
            promise.reject(t);
          }
        }

        @Override
        void reject(Throwable reason) {
          try {
            chain(callback.onRejected(reason), promise);
          } catch (Throwable t) {
            promise.reject(t);
          }
        }

        private void chain(Promise<R> promise, final FulfillablePromise<R> into) {
          promise.then(new LeafCallback<R>() {
            @Override
            public void onFulfilled(R value) {
              into.fulfill(value);
            }

            @Override
            public void onRejected(Throwable reason) {
              into.reject(reason);
            }
          });
        }
      });
      return promise;
    }
  }

  @Override
  public <R> Promise<R> then(final ChainingImmediateCallback<? super V, R> callback) {
    switch (state) {
    case FULFILLED:
      try {
        return Promises.fulfilled(callback.onFulfilled(value));
      } catch (Throwable t) {
        return Promises.rejected(t);
      }
    case REJECTED:
      try {
        return Promises.fulfilled(callback.onRejected(reason));
      } catch (Throwable t) {
        return Promises.rejected(t);
      }
    case PENDING:
    default:
      requireNonNull(callback);
      final FulfillablePromise<R> promise = new FulfillablePromise<>();
      addHandler(new Handler<V>() {
        @Override
        void fulfill(V value) {
          try {
            promise.fulfill(callback.onFulfilled(value));
          } catch (Throwable t) {
            promise.reject(t);
          }
        }

        @Override
        void reject(Throwable reason) {
          try {
            promise.fulfill(callback.onRejected(reason));
          } catch (Throwable t) {
            promise.reject(t);
          }
        }
      });
      return promise;
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
      requireNonNull(callback);
      addHandler(new Handler<V>() {
        @Override
        void fulfill(V value) {
          callback.onFulfilled(value);
        }

        @Override
        void reject(Throwable reason) {
          callback.onRejected(reason);
        }
      });
    }
  }

  private void addHandler(Handler<V> handler) {
    if (last == null) {
      first = last = handler;
    } else {
      last = last.next = handler;
    }
  }
}
