Promises for Java [![Build Status](https://travis-ci.org/tbroyer/promises.png?branch=master)](https://travis-ci.org/tbroyer/promises)
=================

Promises for Java is a simple implementation of promises, based on the
[Promises/A+ proposal](http://promises-aplus.github.com/promises-spec/) for JavaScript.

From that proposal:
> A promise represents a value that may not be available yet. The primary method for interacting with a promise is its `then` method.

The goal is to simplify asynchronous flows, avoiding the so-called _Callback Pyramid of Doom_.

The major difference, besides strong type-checking brought/enforced by Java, is that the `then` method is not asynchronous: if the promise is already fulfilled or rejected, the callbacks will be called synchronously, before the method returns.

The implementation is inspired by [Guava](http://code.google.com/p/guava-libraries/)'s
`Optional` and `SettableFuture`, and includes a `FuturePromise` as a two-way bridge with Guava's `ListenableFuture`. The dependency on Guava is optional though (but then you won't bridge with futures).

License
-------

    Copyright 2013 Thomas Broyer

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
