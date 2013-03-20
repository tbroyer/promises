package net.ltgt.promises;

public abstract class ForwardingPromise<V> implements Promise<V> {

  protected abstract Promise<V> getDelegate();

  @Override
  public <R> Promise<R> then(Callback<? super V, R> callback) {
    return getDelegate().then(callback);
  }

  @Override
  public <R> Promise<R> then(ImmediateCallback<? super V, R> callback) {
    return getDelegate().then(callback);
  }

  @Override
  public void done(DoneCallback<? super V> callback) {
    getDelegate().done(callback);
  }

  @Override
  public void done() {
    getDelegate().done();
  }
}
