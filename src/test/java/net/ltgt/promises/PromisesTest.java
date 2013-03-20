package net.ltgt.promises;

import static org.fest.assertions.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import net.ltgt.promises.Promise.DoneCallback;

import org.junit.Test;

// Tests borrowed from https://code.google.com/p/dart/source/browse/branches/bleeding_edge/dart/tests/lib/async/futures_test.dart?r=19164
public class PromisesTest {

  static class TestCallback extends DoneCallback<List<Object>> {
    @Override
    public void onFulfilled(List<Object> value) {
      fail("Promise unexpectedly fulfilled");
    }
    @Override
    public void onRejected(Throwable reason) {
      fail("Promise unexpectedly rejected");
    }
  }

  @Test
  public void testWaitEmpty() {
    Promises.wait(new ArrayList<Promise<Object>>()).done(new TestCallback() {
      @Override
      public void onFulfilled(List<Object> value) {
        assertThat(value).isEmpty();
      }
    });
  }

  @Test
  public void testFulfillAfterWait() {
    final Object expected = new Object();
    FulfillablePromise<Object> promise = FulfillablePromise.create();
    Promises.wait(promise).done(new TestCallback() {
      @Override
      public void onFulfilled(List<Object> value) {
        assertThat(value).hasSize(1);
        assertThat(value.get(0)).isSameAs(expected);
      }
    });
    promise.fulfill(expected);
  }

  @Test
  public void testFulfillBeforeWait() {
    final Object expected = new Object();
    Promise<Object> promise = Promises.fulfilled(expected);
    Promises.wait(promise).done(new TestCallback() {
      @Override
      public void onFulfilled(List<Object> value) {
        assertThat(value).hasSize(1);
        assertThat(value.get(0)).isSameAs(expected);
      }
    });
  }

  @Test
  public void testWaitWithMultipleValues() {
    final Object expected1 = new Object();
    final Object expected2 = new Object();
    Promises.wait(
          Promises.fulfilled(expected1),
          Promises.fulfilled(expected2)
        ).done(new TestCallback() {
          @Override
          public void onFulfilled(List<Object> value) {
            assertThat(value).hasSize(2);
            assertThat(value.get(0)).isSameAs(expected1);
            assertThat(value.get(1)).isSameAs(expected2);
          }
        });
  }

  @Test
  public void testWaitWithSingleError() {
    final Throwable expected = new ClassCastException("foo");
    Promises.wait(
          Promises.fulfilled(new Object()),
          Promises.rejected(expected)
        ).done(new TestCallback() {
          @Override
          public void onRejected(Throwable reason) {
            assertThat(reason).isSameAs(expected);
          }
        });
  }

  @Test
  public void testWaitWithMulpleErrors() {
    final Throwable expected1 = new ClassCastException("foo");
    Promises.wait(
          Promises.rejected(expected1),
          Promises.rejected(new IllegalArgumentException("bar"))
        ).done(new TestCallback() {
          @Override
          public void onRejected(Throwable reason) {
            assertThat(reason).isSameAs(expected1);
          }
        });
  }
}
