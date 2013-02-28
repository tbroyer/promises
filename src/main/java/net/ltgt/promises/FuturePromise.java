package net.ltgt.promises;

import java.util.concurrent.Future;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.ForwardingListenableFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * A {@link Promise} around a {@link Future}, that is also a {@link Future}.
 * 
 * @param <V> {@inheritDoc}
 */
public class FuturePromise<V> extends ForwardingListenableFuture<V> implements Promise<V> {

  public interface VoidFunction<V> {

    void apply(@Nullable V input);
  }

  public static <V> FuturePromise<V> create(final ListenableFuture <V> future) {
    return new FuturePromise<>(future);
  }

  public static <V> ListenableFuture<V> adapt(Promise<V> promise) {
    if (promise instanceof FuturePromise) {
      return (FuturePromise<V>) promise;
    }
    final SettableFuture<V> future = SettableFuture.create();
    promise.then(new LeafCallback<V>() {
      @Override
      public void onFulfilled(V value) {
        future.set(value);
      }
      @Override
      public void onRejected(Throwable reason) {
        future.setException(reason);
      }
    });
    return future;
  }

  private final ListenableFuture<V> future;

  private FuturePromise(ListenableFuture<V> wrappedFuture) {
    this.future = wrappedFuture;
  }

  @Override
  public <R> FuturePromise<R> then(final ChainingCallback<? super V, R> callback) {
    final SettableFuture<R> ret = SettableFuture.create();
    then(new FutureCallback<V>() {
      @Override
      public void onSuccess(V result) {
        try {
          chain(callback.onFulfilled(result), ret);
        } catch (Throwable t) {
          ret.setException(t);
        }
      }

      @Override
      public void onFailure(Throwable t) {
        try {
          chain(callback.onRejected(t), ret);
        } catch (Throwable t2) {
          ret.setException(t2);
        }
      }

      private void chain(Promise<R> promise, final SettableFuture<R> into) {
        if (into == null) {
          return;
        }
        promise.then(new LeafCallback<R>() {
          @Override
          public void onFulfilled(R value) {
            into.set(value);
          }

          @Override
          public void onRejected(Throwable reason) {
            into.setException(reason);
          }
        });
      }
    });
    return new FuturePromise<>(ret);
  }

  @Override
  public <R> FuturePromise<R> then(final ChainingImmediateCallback<? super V, R> callback) {
    final SettableFuture<R> ret = SettableFuture.create();
    then(new FutureCallback<V>() {
      @Override
      public void onSuccess(V result) {
        try {
          ret.set(callback.onFulfilled(result));
        } catch (Throwable t) {
          ret.setException(t);
        }
      }

      @Override
      public void onFailure(Throwable t) {
        try {
          ret.set(callback.onRejected(t));
        } catch (Throwable t2) {
          ret.setException(t2);
        }
      }
    });
    return new FuturePromise<>(ret);
  }

  @Override
  public void then(final LeafCallback<? super V> callback) {
    then(new FutureCallback<V>() {
      @Override
      public void onSuccess(V result) {
        callback.onFulfilled(result);
      }

      @Override
      public void onFailure(Throwable t) {
        callback.onRejected(t);
      }
    });
  }

  public void then(FutureCallback<? super V> callback) {
    Futures.addCallback(future, callback);
  }

  public <R> FuturePromise<R> then(final Function<? super V, Promise<R>> fulfilled) {
    return then(new ChainingCallback<V, R>() {
      @Override
      public Promise<R> onFulfilled(V value) {
        return fulfilled.apply(value);
      }
    });
  }

  public <R> FuturePromise<R> then(AsyncFunction<? super V, R> fulfilled) {
    return new FuturePromise<>(Futures.transform(future, fulfilled));
  }

  public <R> FuturePromise<R> then(final AsyncFunction<? super V, R> fulfilled,
      final AsyncFunction<Throwable, R> rejected) {
    return then(new ChainingCallback<V, R>() {
      @Override
      public Promise<R> onFulfilled(V value) {
        try {
          return new FuturePromise<>(fulfilled.apply(value));
        } catch (Throwable t) {
          return Promises.rejected(t);
        }
      }
      @Override
      public Promise<R> onRejected(Throwable reason) {
        try {
          return new FuturePromise<>(rejected.apply(reason));
        } catch (Throwable t) {
          return Promises.rejected(t);
        }
      }
    });
  }

  public <R> FuturePromise<R> then(final Function<? super V, Promise<R>> fulfilled,
      final Function<Throwable, Promise<R>> rejected) {
    return then(new ChainingCallback<V, R>() {
      @Override
      public Promise<R> onFulfilled(V value) {
        return fulfilled.apply(value);
      }
      @Override
      public Promise<R> onRejected(Throwable reason) {
        return rejected.apply(reason);
      }
    });
  }

  public void then(final VoidFunction<? super V> fulfilled, final VoidFunction<Throwable> rejected) {
    then(new LeafCallback<V>() {
      @Override
      public void onFulfilled(V value) {
        fulfilled.apply(value);
      }
      @Override
      public void onRejected(Throwable reason) {
        rejected.apply(reason);
      }
    });
  }

  @Override
  protected ListenableFuture<V> delegate() {
    return future;
  }
}
